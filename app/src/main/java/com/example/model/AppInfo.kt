package com.example.model

import android.graphics.Bitmap

enum class AppCategory(val displayName: String) {
    ALL("All"),
    ESSENTIALS("Essentials"),
    SOCIAL("Social"),
    MEDIA("Media"),
    TOOLS("Tools"),
    GAMES("Games")
}

data class AppInfo(
    val id: String,
    val packageName: String,
    val activityName: String,
    val label: String,
    val iconBitmap: Bitmap?,
    val category: AppCategory = AppCategory.TOOLS,
    val isSystemApp: Boolean = false,
    val installTime: Long = 0L
)

data class Folder(
    val id: String,
    val name: String,
    val appIds: List<String>
)

enum class IconShape(val displayName: String, val cornerPercent: Int) {
    SQUIRCLE("Squircle", 28),
    CIRCLE("Circle", 50),
    ROUNDED_SQUARE("Rounded", 18),
    PEBBLE("Pebble", 36)
}

enum class IconThemeStyle(val displayName: String, val description: String) {
    ORIGINAL("Original", "Original Android system icons"),
    LIQUID_GLASS("Liquid Glass", "Translucent frosted glass capsule with specular glow"),
    FROSTED_PEBBLE("Frosted Pebble", "Subtle diffused frost tile"),
    TINTED_NEON("Neon Glow", "Glowing refractive outline accent")
}

enum class WallpaperType(val displayName: String, val drawableRes: String) {
    MINIMAL_FROST("Clean Frost", "wallpaper_minimal_frost_1793997930230"),
    MINIMAL_INDIGO("Liquid Indigo", "wallpaper_minimal_indigo_1793997945300"),
    CLEAN_SLATE("Pure Slate Glass", ""),
    SILK_AURA("Silk Minimal Glass", ""),
    LIQUID_NEBULA("Nebula Ribbon", "wallpaper_liquid_nebula_1793996489118"),
    CRYSTAL_AURORA("Crystal Flow", "wallpaper_crystal_aurora_1793996513336")
}

enum class GestureAction(val displayName: String) {
    OPEN_DRAWER("Open App Drawer"),
    QUICK_SEARCH("Quick Search"),
    ASK_COMET("Ask Comet AI"),
    EXPAND_NOTIFICATIONS("Notification Shade"),
    LAUNCHER_SETTINGS("Launcher Settings"),
    VOICE_SEARCH("Voice Search"),
    NONE("None")
}

enum class DockStyle(val displayName: String) {
    GLASS("Liquid Glass"),
    TRANSLUCENT("Subtle Frost"),
    OUTLINE_ONLY("Refractive Outline"),
    MINIMAL_FLOAT("Floating Island")
}

enum class ClockStyle(val displayName: String) {
    FUTURISTIC_DIGITAL("Futuristic Digital"),
    MINIMAL_CLEAN("Minimal Clean"),
    GLOW_HORIZON("Glow Horizon"),
    COMPACT_PILL("Compact Pill")
}

object WidgetIds {
    const val CLOCK = "clock"
    const val WEATHER = "weather"
    const val STEPS = "steps"
    const val BATTERY = "battery"
    const val MUSIC = "music"
    const val QUICK_TOGGLES = "quick_toggles"
    const val QUICK_NOTES = "quick_notes"

    val DEFAULT_ORDER = listOf(
        CLOCK,
        WEATHER,
        STEPS,
        BATTERY,
        MUSIC,
        QUICK_TOGGLES,
        QUICK_NOTES
    )

    val DEFAULT_ENABLED = setOf(
        CLOCK,
        WEATHER,
        STEPS,
        BATTERY,
        MUSIC,
        QUICK_TOGGLES
    )
}

data class LauncherSettings(
    val wallpaperType: WallpaperType = WallpaperType.MINIMAL_FROST,
    val customWallpaperUri: String? = null,
    val accentColorIndex: Int = 0,
    val glassOpacity: Float = 0.25f,
    val glassCornerRadiusDp: Int = 26,
    val glassBlurIntensity: Int = 3,       // 1 to 5
    val glassBendCurvature: Float = 0.65f, // 0.0 to 1.0 (refraction curvature & lens distortion)
    val glassSpecularity: Float = 0.70f,   // 0.0 to 1.0 (specular gloss reflection)
    val iconShape: IconShape = IconShape.SQUIRCLE,
    val iconTheme: IconThemeStyle = IconThemeStyle.LIQUID_GLASS,
    val gridColumns: Int = 4,
    val iconSizeDp: Int = 54,
    val showLabelsOnHome: Boolean = true,
    val showLabelsInDrawer: Boolean = true,
    val labelFontSizeSp: Int = 12,
    val isCelsius: Boolean = true,
    val dockAppIds: List<String> = emptyList(),
    val showClockWidget: Boolean = true,
    val showWeatherWidget: Boolean = true,
    val showBatteryWidget: Boolean = true,
    val showMusicWidget: Boolean = true,
    val showSearchBar: Boolean = true,
    val showDockAiButton: Boolean = true,
    val dockStyle: DockStyle = DockStyle.GLASS,
    val clockStyle: ClockStyle = ClockStyle.FUTURISTIC_DIGITAL,
    val swipeUpAction: GestureAction = GestureAction.OPEN_DRAWER,
    val swipeDownAction: GestureAction = GestureAction.QUICK_SEARCH,
    val doubleTapAction: GestureAction = GestureAction.LAUNCHER_SETTINGS,
    val pinchAction: GestureAction = GestureAction.LAUNCHER_SETTINGS,
    val twoFingerSwipeDownAction: GestureAction = GestureAction.EXPAND_NOTIFICATIONS,
    val defaultMusicAppPackage: String? = null,
    // Step counter & fitness
    val dailyStepGoal: Int = 8000,
    // Weather location
    val weatherCity: String = "Aurora City",
    val weatherLat: Double = 40.7128,
    val weatherLon: Double = -74.0060,
    // Quick Note
    val quickNoteText: String = "Liquid glass note...",
    // Comet wake word ("Hey Comet")
    val enableWakeWord: Boolean = true,
    // Setup Wizard status
    val isSetupCompleted: Boolean = false,
    // Dynamic widget order and enabled state
    val widgetOrder: List<String> = WidgetIds.DEFAULT_ORDER,
    val enabledWidgets: Set<String> = WidgetIds.DEFAULT_ENABLED
)
