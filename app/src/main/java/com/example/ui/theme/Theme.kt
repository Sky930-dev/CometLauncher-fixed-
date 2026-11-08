package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LiquidGlassColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = CosmicMidnight,
    primaryContainer = AuroraPurple,
    onPrimaryContainer = Color.White,
    secondary = ElectricViolet,
    onSecondary = Color.White,
    secondaryContainer = CosmicSurface,
    onSecondaryContainer = StarlightBlue,
    tertiary = CyberPink,
    onTertiary = Color.White,
    background = CosmicMidnight,
    onBackground = TextPrimary,
    surface = CosmicDark,
    onSurface = TextPrimary,
    surfaceVariant = CosmicSurface,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorderHighlight,
    outlineVariant = GlassBorderSubtle
)

@Composable
fun CometLauncherTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            dynamicDarkColorScheme(context)
        }
        else -> LiquidGlassColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
