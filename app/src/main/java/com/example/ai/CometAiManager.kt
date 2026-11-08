package com.example.ai

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.model.AppInfo
import com.example.service.ActiveMediaManager
import com.example.service.StepTelemetry
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.Locale

private const val TAG = "CometAi"

enum class AssistantStatus {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

enum class CometActionType {
    OPEN_APP,
    MUSIC_PLAY,
    MUSIC_PAUSE,
    MUSIC_NEXT,
    MUSIC_PREV,
    OPEN_WEATHER,
    OPEN_SETTINGS,
    FLASHLIGHT_TOGGLE,
    DEVICE_SETTINGS
}

data class CometMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "comet"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionApp: AppInfo? = null,
    val actionType: CometActionType? = null
)

class CometAiManager(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onLaunchApp: (AppInfo) -> Unit = {},
    private val onOpenWeather: () -> Unit = {},
    private val onOpenSettings: () -> Unit = {},
    private val getStepTelemetry: () -> StepTelemetry = { StepTelemetry() }
) {
    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    "You are Comet, an intelligent, conversational, Siri-like AI assistant and chatbot directly integrated into Comet Launcher for Android. " +
                    "You can answer questions, chat normally, summarize topics, assist with tasks, and help manage device apps. " +
                    "Your personality is witty, smart, polite, and helpful. " +
                    "Keep normal conversational answers clear, punchy, and conversational (1-3 sentences) suitable for reading and speech synthesis, " +
                    "unless the user asks for a detailed explanation, list, or tutorial."
                )
            }
        )
    }

    private var chatSession: Chat? = null

    private val _status = MutableStateFlow(AssistantStatus.IDLE)
    val status: StateFlow<AssistantStatus> = _status.asStateFlow()

    private val _messages = MutableStateFlow<List<CometMessage>>(
        listOf(
            CometMessage(
                sender = "comet",
                text = "Hey! I'm Comet, your launcher assistant. Try saying 'Hey Comet', ask me questions, or tell me to open any app, check your steps, or play music."
            )
        )
    )
    val messages: StateFlow<List<CometMessage>> = _messages.asStateFlow()

    private val _currentStreamingText = MutableStateFlow("")
    val currentStreamingText: StateFlow<String> = _currentStreamingText.asStateFlow()

    private val _isTtsMuted = MutableStateFlow(false)
    val isTtsMuted: StateFlow<Boolean> = _isTtsMuted.asStateFlow()

    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private var speechRecognizer: SpeechRecognizer? = null
    private var activeJob: Job? = null

    init {
        initTts()
        initSpeechRecognizer()
    }

    private fun initTts() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.getDefault()
                textToSpeech?.setPitch(1.05f)
                textToSpeech?.setSpeechRate(1.0f)
                isTtsReady = true

                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _status.value = AssistantStatus.SPEAKING
                    }

                    override fun onDone(utteranceId: String?) {
                        if (_status.value == AssistantStatus.SPEAKING) {
                            _status.value = AssistantStatus.IDLE
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        _status.value = AssistantStatus.IDLE
                    }
                })
            }
        }
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _status.value = AssistantStatus.LISTENING
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _status.value = AssistantStatus.THINKING
                    }

                    override fun onError(error: Int) {
                        _status.value = AssistantStatus.IDLE
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val userVoicePrompt = matches[0]
                            askComet(userVoicePrompt)
                        } else {
                            _status.value = AssistantStatus.IDLE
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    fun startListening() {
        stopSpeaking()
        val recognizer = speechRecognizer ?: return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        try {
            recognizer.startListening(intent)
            _status.value = AssistantStatus.LISTENING
        } catch (e: Exception) {
            Log.e(TAG, "SpeechRecognizer start failed", e)
            _status.value = AssistantStatus.IDLE
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        if (_status.value == AssistantStatus.LISTENING) {
            _status.value = AssistantStatus.IDLE
        }
    }

    fun toggleMuteTts() {
        _isTtsMuted.value = !_isTtsMuted.value
        if (_isTtsMuted.value) {
            stopSpeaking()
        }
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
        if (_status.value == AssistantStatus.SPEAKING) {
            _status.value = AssistantStatus.IDLE
        }
    }

    fun clearHistory() {
        stopSpeaking()
        stopListening()
        chatSession = null
        _messages.value = listOf(
            CometMessage(
                sender = "comet",
                text = "Chat cleared! How can I assist you?"
            )
        )
        _status.value = AssistantStatus.IDLE
    }

    fun askComet(prompt: String, allInstalledApps: List<AppInfo> = emptyList()) {
        if (prompt.isBlank()) return

        stopSpeaking()
        stopListening()

        val trimmed = prompt.trim()
        val userMsg = CometMessage(sender = "user", text = trimmed)
        _messages.value = _messages.value + userMsg

        // Check for direct device/launcher intent actions
        val handled = handleDirectCommands(trimmed, allInstalledApps)
        if (handled) return

        // Otherwise stream conversational response with Gemini 3.8 Flash via Firebase AI Logic
        activeJob?.cancel()
        activeJob = scope.launch {
            _status.value = AssistantStatus.THINKING
            _currentStreamingText.value = ""

            try {
                if (chatSession == null) {
                    chatSession = model.startChat()
                }

                val accumulated = StringBuilder()
                withTimeout(60_000L) {
                    val stream = chatSession!!.sendMessageStream(trimmed)
                    stream.collect { chunk ->
                        val chunkText = chunk.text.orEmpty()
                        accumulated.append(chunkText)
                        _currentStreamingText.value = accumulated.toString()
                    }
                }

                val finalReply = accumulated.toString().trim()
                if (finalReply.isNotBlank()) {
                    val matchedApp = findMentionedApp(finalReply, allInstalledApps)
                    val cometMsg = CometMessage(
                        sender = "comet",
                        text = finalReply,
                        actionApp = matchedApp,
                        actionType = if (matchedApp != null) CometActionType.OPEN_APP else null
                    )
                    _messages.value = _messages.value + cometMsg
                    _currentStreamingText.value = ""
                    _status.value = AssistantStatus.IDLE

                    if (!_isTtsMuted.value) {
                        speakOutLoud(finalReply)
                    }
                } else {
                    _status.value = AssistantStatus.IDLE
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Comet response timed out", e)
                val errorMsg = "Sorry, my connection timed out. Please try again."
                _messages.value = _messages.value + CometMessage(sender = "comet", text = errorMsg)
                _status.value = AssistantStatus.ERROR
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Firebase AI Logic inference error", e)
                val errorMsg = "I couldn't process that: ${e.localizedMessage ?: "Unknown error"}"
                _messages.value = _messages.value + CometMessage(sender = "comet", text = errorMsg)
                _status.value = AssistantStatus.ERROR
            }
        }
    }

    private fun handleDirectCommands(prompt: String, allInstalledApps: List<AppInfo>): Boolean {
        val lower = prompt.lowercase()

        // 1. Step & Activity query
        if (lower.contains("how many steps") || lower.contains("my steps") || lower.contains("step count") || lower == "steps") {
            val telemetry = getStepTelemetry()
            val reply = "You've walked ${String.format("%,d", telemetry.steps)} steps today (${telemetry.distanceKm} km, burned ${telemetry.caloriesKcal} kcal) — ${(telemetry.progress * 100).toInt()}% of your ${telemetry.goal} goal!"
            replyWithAction(reply, CometActionType.OPEN_SETTINGS)
            return true
        }

        // 2. Morning Briefing / Day Summary
        if (lower.contains("morning briefing") || lower.contains("daily overview") || lower.contains("my day") || lower.contains("daily summary")) {
            val telemetry = getStepTelemetry()
            val reply = "Here's your Comet Overview: You've taken ${String.format("%,d", telemetry.steps)} steps so far today. Your launcher is running smooth liquid glass with quick access to your favorite apps."
            replyWithAction(reply, CometActionType.OPEN_WEATHER)
            return true
        }

        // 3. Flashlight toggle
        if (lower.contains("flashlight") || lower.contains("torch")) {
            val turnOn = lower.contains("on") || !lower.contains("off")
            try {
                val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                val cameraId = cameraManager?.cameraIdList?.firstOrNull()
                if (cameraId != null) {
                    cameraManager.setTorchMode(cameraId, turnOn)
                }
            } catch (ignored: Exception) {}
            replyWithAction(if (turnOn) "Turned on your flashlight." else "Flashlight turned off.", CometActionType.FLASHLIGHT_TOGGLE)
            return true
        }

        // 4. Music playback controls
        if (lower.contains("play music") || lower == "play" || lower == "resume music" || lower.contains("start music")) {
            ActiveMediaManager.play()
            replyWithAction("Playing your music now.", CometActionType.MUSIC_PLAY)
            return true
        }
        if (lower.contains("pause music") || lower == "pause" || lower == "stop music") {
            ActiveMediaManager.pause()
            replyWithAction("Paused music playback.", CometActionType.MUSIC_PAUSE)
            return true
        }
        if (lower.contains("next song") || lower.contains("skip song") || lower == "next" || lower.contains("skip track")) {
            ActiveMediaManager.skipToNext()
            replyWithAction("Skipping to the next track.", CometActionType.MUSIC_NEXT)
            return true
        }
        if (lower.contains("previous song") || lower == "previous" || lower.contains("back track")) {
            ActiveMediaManager.skipToPrevious()
            replyWithAction("Playing the previous track.", CometActionType.MUSIC_PREV)
            return true
        }

        // 5. Weather
        if (lower.contains("weather") && (lower.contains("open") || lower.contains("show") || lower.contains("check") || lower.contains("forecast"))) {
            onOpenWeather()
            replyWithAction("Opening atmospheric weather forecast.", CometActionType.OPEN_WEATHER)
            return true
        }

        // 6. Settings / Customization
        if (lower.contains("open settings") || lower.contains("launcher settings") || lower.contains("preferences") || lower.contains("customize")) {
            onOpenSettings()
            replyWithAction("Opening Comet customization center.", CometActionType.OPEN_SETTINGS)
            return true
        }

        // 7. App Launching Intent: "open X", "launch X", "start X", "can you open X", "please open X", "run X"
        val openPatterns = listOf("open ", "launch ", "start ", "run ", "can you open ", "please open ", "go to ", "take me to ")
        for (pattern in openPatterns) {
            if (lower.contains(pattern)) {
                val appQuery = lower.substringAfter(pattern).trim().removeSuffix(".").removeSuffix("!")
                if (appQuery.isNotBlank()) {
                    val match = allInstalledApps.find {
                        it.label.lowercase() == appQuery ||
                        it.label.lowercase().contains(appQuery) ||
                        it.packageName.lowercase().contains(appQuery)
                    }
                    if (match != null) {
                        onLaunchApp(match)
                        replyWithAction("Opening ${match.label} for you.", CometActionType.OPEN_APP, match)
                        return true
                    }
                }
            }
        }

        return false
    }

    private fun findMentionedApp(text: String, allInstalledApps: List<AppInfo>): AppInfo? {
        val lower = text.lowercase()
        return allInstalledApps.find { app ->
            val labelLower = app.label.lowercase()
            labelLower.length >= 4 && lower.contains(labelLower)
        }
    }

    private fun replyWithAction(text: String, actionType: CometActionType, app: AppInfo? = null) {
        val cometMsg = CometMessage(
            sender = "comet",
            text = text,
            actionType = actionType,
            actionApp = app
        )
        _messages.value = _messages.value + cometMsg
        if (!_isTtsMuted.value) {
            speakOutLoud(text)
        }
    }

    fun speakOutLoud(text: String) {
        if (!isTtsReady || _isTtsMuted.value) return
        val cleanText = text.replace(Regex("[*#_`~]"), "")
        textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "comet_voice")
    }

    fun cleanUp() {
        stopSpeaking()
        stopListening()
        speechRecognizer?.destroy()
        textToSpeech?.shutdown()
    }
}
