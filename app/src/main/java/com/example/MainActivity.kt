package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.service.ActiveMediaManager
import com.example.ui.home.HomeScreen
import com.example.ui.theme.CometLauncherTheme
import com.example.ui.theme.CosmicMidnight
import com.example.viewmodel.LauncherViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.loadApps()
        }
    }

    private val mediaBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val track = intent?.getStringExtra("track") ?: intent?.getStringExtra("title")
            val artist = intent?.getStringExtra("artist")
            val isPlaying = intent?.getBooleanExtra("playing", false)
            val pkg = intent?.getStringExtra("package")
            ActiveMediaManager.updateFromBroadcast(track, artist, isPlaying, pkg)
            ActiveMediaManager.checkActiveSessions()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure App Check with launch intent before any UI or Firebase operations
        com.example.util.configureAppCheck(this, intent)

        enableEdgeToEdge()

        ActiveMediaManager.init(this)

        val packageFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        registerReceiver(packageReceiver, packageFilter)

        val mediaFilter = IntentFilter().apply {
            addAction("com.android.music.metachanged")
            addAction("com.android.music.playstatechanged")
            addAction("com.android.music.playbackcomplete")
            addAction("com.spotify.music.metadatachanged")
            addAction("com.spotify.music.playbackstatechanged")
            addAction("com.htc.music.metachanged")
            addAction("fm.last.android.metachanged")
            addAction("com.sec.android.app.music.metachanged")
            addAction("com.nullsoft.winamp.metachanged")
            addAction("com.amazon.mp3.metachanged")
            addAction("com.miui.player.metachanged")
            addAction("com.real.RealPlayer.metachanged")
        }
        try {
            registerReceiver(mediaBroadcastReceiver, mediaFilter)
        } catch (ignored: Exception) {}

        setContent {
            CometLauncherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CosmicMidnight
                ) {
                    val uiState by viewModel.uiState.collectAsState()
                    HomeScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadApps()
        ActiveMediaManager.checkActiveSessions()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(packageReceiver)
        } catch (ignored: Exception) {}
        try {
            unregisterReceiver(mediaBroadcastReceiver)
        } catch (ignored: Exception) {}
    }
}
