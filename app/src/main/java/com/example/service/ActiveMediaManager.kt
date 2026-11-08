package com.example.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ActiveMediaState(
    val title: String = "No Track Playing",
    val artist: String = "Select your music app",
    val album: String = "",
    val albumArt: Bitmap? = null,
    val isPlaying: Boolean = false,
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val appPackageName: String? = null,
    val appName: String = "Music",
    val hasSession: Boolean = false,
    val isPermissionGranted: Boolean = false
)

object ActiveMediaManager {

    private val _mediaState = MutableStateFlow(ActiveMediaState())
    val mediaState: StateFlow<ActiveMediaState> = _mediaState.asStateFlow()

    private var activeController: MediaController? = null
    private var appContext: Context? = null
    private var mediaSessionManager: MediaSessionManager? = null
    private var listenerService: CometMediaListenerService? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updateFromController(activeController)
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateFromController(activeController)
        }

        override fun onSessionDestroyed() {
            activeController = null
            checkActiveSessions()
        }
    }

    private val sessionsChangedListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        findAndAttachActiveController(controllers)
    }

    fun init(context: Context) {
        appContext = context.applicationContext
        mediaSessionManager = appContext?.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
        checkActiveSessions()
    }

    fun notifyListenerConnected(service: CometMediaListenerService) {
        listenerService = service
        registerSessionsListener()
        checkActiveSessions()
    }

    fun notifyListenerDisconnected() {
        listenerService = null
        checkActiveSessions()
    }

    fun isNotificationAccessGranted(context: Context): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(context)
            .contains(context.packageName)
    }

    fun openNotificationAccessSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun registerSessionsListener() {
        val context = appContext ?: return
        val manager = mediaSessionManager ?: return
        if (isNotificationAccessGranted(context)) {
            try {
                val component = ComponentName(context, CometMediaListenerService::class.java)
                manager.removeOnActiveSessionsChangedListener(sessionsChangedListener)
                manager.addOnActiveSessionsChangedListener(sessionsChangedListener, component)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun checkActiveSessions() {
        val context = appContext ?: return
        val manager = mediaSessionManager ?: return
        val isGranted = isNotificationAccessGranted(context)

        if (!isGranted) {
            _mediaState.value = _mediaState.value.copy(
                isPermissionGranted = false,
                hasSession = false
            )
            return
        }

        try {
            val component = ComponentName(context, CometMediaListenerService::class.java)
            val controllers = manager.getActiveSessions(component)
            findAndAttachActiveController(controllers)
        } catch (e: Exception) {
            // Permission or session error
            _mediaState.value = _mediaState.value.copy(
                isPermissionGranted = isGranted,
                hasSession = false
            )
        }
    }

    private fun findAndAttachActiveController(controllers: List<MediaController>?) {
        mainHandler.post {
            val context = appContext ?: return@post
            val isGranted = isNotificationAccessGranted(context)

            if (controllers.isNullOrEmpty()) {
                activeController?.unregisterCallback(controllerCallback)
                activeController = null
                _mediaState.value = _mediaState.value.copy(
                    hasSession = false,
                    isPermissionGranted = isGranted,
                    isPlaying = false,
                    title = "No Track Playing",
                    artist = "Tap to open music app"
                )
                return@post
            }

            // Prefer playing session, otherwise first
            val best = controllers.find {
                it.playbackState?.state == PlaybackState.STATE_PLAYING
            } ?: controllers.first()

            if (activeController != best) {
                activeController?.unregisterCallback(controllerCallback)
                activeController = best
                best.registerCallback(controllerCallback)
            }

            updateFromController(best)
        }
    }

    private fun updateFromController(controller: MediaController?) {
        val context = appContext ?: return
        val isGranted = isNotificationAccessGranted(context)

        if (controller == null) {
            _mediaState.value = _mediaState.value.copy(
                hasSession = false,
                isPermissionGranted = isGranted,
                isPlaying = false
            )
            return
        }

        val metadata = controller.metadata
        val pbState = controller.playbackState

        val isPlaying = pbState?.state == PlaybackState.STATE_PLAYING
        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?: "Media Track"
        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_AUTHOR)
            ?: "Unknown Artist"
        val album = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: ""

        val albumArt = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)

        val duration = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
        val position = pbState?.position ?: 0L

        val pkgName = controller.packageName
        var appLabel = "Music"
        try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(pkgName, 0)
            appLabel = pm.getApplicationLabel(appInfo).toString()
        } catch (ignored: Exception) {}

        _mediaState.value = ActiveMediaState(
            title = title,
            artist = artist,
            album = album,
            albumArt = albumArt,
            isPlaying = isPlaying,
            durationMs = duration,
            positionMs = position,
            appPackageName = pkgName,
            appName = appLabel,
            hasSession = true,
            isPermissionGranted = isGranted
        )
    }

    fun play() {
        try {
            activeController?.transportControls?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pause() {
        try {
            activeController?.transportControls?.pause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun togglePlayPause() {
        if (_mediaState.value.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun skipToNext() {
        try {
            activeController?.transportControls?.skipToNext()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun skipToPrevious() {
        try {
            activeController?.transportControls?.skipToPrevious()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun seekTo(positionMs: Long) {
        try {
            activeController?.transportControls?.seekTo(positionMs)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun launchActiveOrSetMusicApp(context: Context, defaultPackage: String? = null) {
        val targetPkg = _mediaState.value.appPackageName ?: defaultPackage
        if (targetPkg != null) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(targetPkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return
            }
        }

        // Generic launch music player
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_MUSIC)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Fallback broadcast updater (for Spotify, generic Android music broadcasts)
    fun updateFromBroadcast(title: String?, artist: String?, isPlaying: Boolean?, pkg: String?) {
        if (title != null || artist != null) {
            _mediaState.value = _mediaState.value.copy(
                title = title ?: _mediaState.value.title,
                artist = artist ?: _mediaState.value.artist,
                isPlaying = isPlaying ?: _mediaState.value.isPlaying,
                appPackageName = pkg ?: _mediaState.value.appPackageName,
                hasSession = true
            )
        }
    }
}
