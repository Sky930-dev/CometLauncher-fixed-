package com.example.ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class WakeWordManager(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onWakeWordDetected: () -> Unit
) {
    private var recognizer: SpeechRecognizer? = null
    private var isListening = false
    private var isEnabled = true
    private var restartJob: Job? = null

    private val _isWakeWordActive = MutableStateFlow(false)
    val isWakeWordActive: StateFlow<Boolean> = _isWakeWordActive.asStateFlow()

    init {
        createRecognizer()
    }

    private fun createRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return

        try {
            recognizer?.destroy()
            recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isWakeWordActive.value = true
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}

                    override fun onError(error: Int) {
                        _isWakeWordActive.value = false
                        scheduleRestart()
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        checkWakeWord(matches)
                        scheduleRestart()
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        checkWakeWord(matches)
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        } catch (e: Exception) {
            Log.e("WakeWord", "Error creating SpeechRecognizer", e)
        }
    }

    private fun checkWakeWord(matches: List<String>?) {
        if (matches.isNullOrEmpty()) return
        for (phrase in matches) {
            val lower = phrase.lowercase()
            if (lower.contains("hey comet") || lower.contains("hay comet") || lower.contains("ok comet") || lower.contains("comet ai") || lower == "comet") {
                stop()
                onWakeWordDetected()
                break
            }
        }
    }

    fun start(enabled: Boolean) {
        isEnabled = enabled
        if (!enabled) {
            stop()
            return
        }
        if (isListening) return

        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            recognizer?.startListening(intent)
            isListening = true
            _isWakeWordActive.value = true
        } catch (e: Exception) {
            Log.e("WakeWord", "Failed to start wake word listener", e)
            scheduleRestart()
        }
    }

    fun stop() {
        isListening = false
        _isWakeWordActive.value = false
        restartJob?.cancel()
        try {
            recognizer?.stopListening()
        } catch (ignored: Exception) {}
    }

    private fun scheduleRestart() {
        if (!isEnabled || !isListening) return
        restartJob?.cancel()
        restartJob = scope.launch {
            delay(1200)
            if (isEnabled && isListening) {
                try {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    }
                    recognizer?.startListening(intent)
                    _isWakeWordActive.value = true
                } catch (e: Exception) {
                    delay(2500)
                    createRecognizer()
                }
            }
        }
    }

    fun destroy() {
        stop()
        try {
            recognizer?.destroy()
        } catch (ignored: Exception) {}
    }
}
