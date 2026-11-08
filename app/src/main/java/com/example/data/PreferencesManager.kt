package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.ClockStyle
import com.example.model.DockStyle
import com.example.model.Folder
import com.example.model.GestureAction
import com.example.model.IconShape
import com.example.model.IconThemeStyle
import com.example.model.LauncherSettings
import com.example.model.WallpaperType
import com.example.model.WidgetIds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "comet_launcher_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        private val KEY_WALLPAPER = stringPreferencesKey("wallpaper_type")
        private val KEY_CUSTOM_WALLPAPER = stringPreferencesKey("custom_wallpaper_uri")
        private val KEY_ACCENT_INDEX = intPreferencesKey("accent_color_index")
        private val KEY_GLASS_OPACITY = floatPreferencesKey("glass_opacity")
        private val KEY_GLASS_RADIUS = intPreferencesKey("glass_radius")
        private val KEY_GLASS_BLUR = intPreferencesKey("glass_blur")
        private val KEY_GLASS_BEND = floatPreferencesKey("glass_bend")
        private val KEY_GLASS_SPECULARITY = floatPreferencesKey("glass_specularity")
        private val KEY_ICON_SHAPE = stringPreferencesKey("icon_shape")
        private val KEY_ICON_THEME = stringPreferencesKey("icon_theme")
        private val KEY_GRID_COLUMNS = intPreferencesKey("grid_columns")
        private val KEY_ICON_SIZE = intPreferencesKey("icon_size")
        private val KEY_SHOW_LABELS_HOME = booleanPreferencesKey("show_labels_home")
        private val KEY_SHOW_LABELS_DRAWER = booleanPreferencesKey("show_labels_drawer")
        private val KEY_LABEL_FONT_SIZE = intPreferencesKey("label_font_size")
        private val KEY_DOCK_APPS = stringPreferencesKey("dock_apps")
        private val KEY_HOME_APPS = stringPreferencesKey("home_apps")
        private val KEY_FOLDERS = stringPreferencesKey("home_folders")
        private val KEY_IS_CELSIUS = stringPreferencesKey("is_celsius")

        // Widget Toggles & Order
        private val KEY_SHOW_CLOCK = booleanPreferencesKey("show_clock")
        private val KEY_SHOW_WEATHER = booleanPreferencesKey("show_weather")
        private val KEY_SHOW_BATTERY = booleanPreferencesKey("show_battery")
        private val KEY_SHOW_MUSIC = booleanPreferencesKey("show_music")
        private val KEY_SHOW_SEARCH = booleanPreferencesKey("show_search")
        private val KEY_SHOW_DOCK_AI = booleanPreferencesKey("show_dock_ai")
        private val KEY_DOCK_STYLE = stringPreferencesKey("dock_style")
        private val KEY_CLOCK_STYLE = stringPreferencesKey("clock_style")
        private val KEY_WIDGET_ORDER = stringPreferencesKey("widget_order")
        private val KEY_ENABLED_WIDGETS = stringPreferencesKey("enabled_widgets")

        // Gestures
        private val KEY_SWIPE_UP = stringPreferencesKey("gesture_swipe_up")
        private val KEY_SWIPE_DOWN = stringPreferencesKey("gesture_swipe_down")
        private val KEY_DOUBLE_TAP = stringPreferencesKey("gesture_double_tap")
        private val KEY_PINCH = stringPreferencesKey("gesture_pinch")
        private val KEY_TWO_FINGER_DOWN = stringPreferencesKey("gesture_two_finger_down")
        private val KEY_DEFAULT_MUSIC_APP = stringPreferencesKey("default_music_app")

        // Steps, Weather location, Quick note, Wake word, Setup status
        private val KEY_DAILY_STEP_GOAL = intPreferencesKey("daily_step_goal")
        private val KEY_WEATHER_CITY = stringPreferencesKey("weather_city")
        private val KEY_WEATHER_LAT = doublePreferencesKey("weather_lat")
        private val KEY_WEATHER_LON = doublePreferencesKey("weather_lon")
        private val KEY_QUICK_NOTE = stringPreferencesKey("quick_note")
        private val KEY_WAKE_WORD = booleanPreferencesKey("wake_word_enabled")
        private val KEY_SETUP_COMPLETED = booleanPreferencesKey("setup_wizard_completed")
    }

    val launcherSettingsFlow: Flow<LauncherSettings> = context.dataStore.data.map { prefs ->
        val wallpaperStr = prefs[KEY_WALLPAPER] ?: WallpaperType.MINIMAL_FROST.name
        val wallpaperType = try {
            WallpaperType.valueOf(wallpaperStr)
        } catch (e: Exception) {
            WallpaperType.MINIMAL_FROST
        }

        val iconShapeStr = prefs[KEY_ICON_SHAPE] ?: IconShape.SQUIRCLE.name
        val iconShape = try {
            IconShape.valueOf(iconShapeStr)
        } catch (e: Exception) {
            IconShape.SQUIRCLE
        }

        val iconThemeStr = prefs[KEY_ICON_THEME] ?: IconThemeStyle.LIQUID_GLASS.name
        val iconTheme = try {
            IconThemeStyle.valueOf(iconThemeStr)
        } catch (e: Exception) {
            IconThemeStyle.LIQUID_GLASS
        }

        val dockStyleStr = prefs[KEY_DOCK_STYLE] ?: DockStyle.GLASS.name
        val dockStyle = try {
            DockStyle.valueOf(dockStyleStr)
        } catch (e: Exception) {
            DockStyle.GLASS
        }

        val clockStyleStr = prefs[KEY_CLOCK_STYLE] ?: ClockStyle.FUTURISTIC_DIGITAL.name
        val clockStyle = try {
            ClockStyle.valueOf(clockStyleStr)
        } catch (e: Exception) {
            ClockStyle.FUTURISTIC_DIGITAL
        }

        val swipeUp = try {
            GestureAction.valueOf(prefs[KEY_SWIPE_UP] ?: GestureAction.OPEN_DRAWER.name)
        } catch (e: Exception) {
            GestureAction.OPEN_DRAWER
        }

        val swipeDown = try {
            GestureAction.valueOf(prefs[KEY_SWIPE_DOWN] ?: GestureAction.QUICK_SEARCH.name)
        } catch (e: Exception) {
            GestureAction.QUICK_SEARCH
        }

        val doubleTap = try {
            GestureAction.valueOf(prefs[KEY_DOUBLE_TAP] ?: GestureAction.LAUNCHER_SETTINGS.name)
        } catch (e: Exception) {
            GestureAction.LAUNCHER_SETTINGS
        }

        val pinch = try {
            GestureAction.valueOf(prefs[KEY_PINCH] ?: GestureAction.LAUNCHER_SETTINGS.name)
        } catch (e: Exception) {
            GestureAction.LAUNCHER_SETTINGS
        }

        val twoFingerDown = try {
            GestureAction.valueOf(prefs[KEY_TWO_FINGER_DOWN] ?: GestureAction.EXPAND_NOTIFICATIONS.name)
        } catch (e: Exception) {
            GestureAction.EXPAND_NOTIFICATIONS
        }

        val dockAppsRaw = prefs[KEY_DOCK_APPS] ?: ""
        val dockApps = if (dockAppsRaw.isNotBlank()) dockAppsRaw.split("|||") else emptyList()

        val rawOrder = prefs[KEY_WIDGET_ORDER] ?: ""
        val widgetOrder = if (rawOrder.isNotBlank()) {
            val list = rawOrder.split(",")
            // Make sure all default widgets exist in the list
            val set = list.toMutableSet()
            WidgetIds.DEFAULT_ORDER.forEach { if (!set.contains(it)) list.plus(it) }
            list
        } else {
            WidgetIds.DEFAULT_ORDER
        }

        val rawEnabled = prefs[KEY_ENABLED_WIDGETS] ?: ""
        val enabledWidgets = if (rawEnabled.isNotBlank()) {
            rawEnabled.split(",").toSet()
        } else {
            WidgetIds.DEFAULT_ENABLED
        }

        LauncherSettings(
            wallpaperType = wallpaperType,
            customWallpaperUri = prefs[KEY_CUSTOM_WALLPAPER],
            accentColorIndex = prefs[KEY_ACCENT_INDEX] ?: 0,
            glassOpacity = prefs[KEY_GLASS_OPACITY] ?: 0.25f,
            glassCornerRadiusDp = prefs[KEY_GLASS_RADIUS] ?: 26,
            glassBlurIntensity = prefs[KEY_GLASS_BLUR] ?: 3,
            glassBendCurvature = prefs[KEY_GLASS_BEND] ?: 0.65f,
            glassSpecularity = prefs[KEY_GLASS_SPECULARITY] ?: 0.70f,
            iconShape = iconShape,
            iconTheme = iconTheme,
            gridColumns = prefs[KEY_GRID_COLUMNS] ?: 4,
            iconSizeDp = prefs[KEY_ICON_SIZE] ?: 54,
            showLabelsOnHome = prefs[KEY_SHOW_LABELS_HOME] ?: true,
            showLabelsInDrawer = prefs[KEY_SHOW_LABELS_DRAWER] ?: true,
            labelFontSizeSp = prefs[KEY_LABEL_FONT_SIZE] ?: 12,
            isCelsius = (prefs[KEY_IS_CELSIUS] ?: "true") == "true",
            dockAppIds = dockApps,
            showClockWidget = prefs[KEY_SHOW_CLOCK] ?: true,
            showWeatherWidget = prefs[KEY_SHOW_WEATHER] ?: true,
            showBatteryWidget = prefs[KEY_SHOW_BATTERY] ?: true,
            showMusicWidget = prefs[KEY_SHOW_MUSIC] ?: true,
            showSearchBar = prefs[KEY_SHOW_SEARCH] ?: true,
            showDockAiButton = prefs[KEY_SHOW_DOCK_AI] ?: true,
            dockStyle = dockStyle,
            clockStyle = clockStyle,
            swipeUpAction = swipeUp,
            swipeDownAction = swipeDown,
            doubleTapAction = doubleTap,
            pinchAction = pinch,
            twoFingerSwipeDownAction = twoFingerDown,
            defaultMusicAppPackage = prefs[KEY_DEFAULT_MUSIC_APP],
            dailyStepGoal = prefs[KEY_DAILY_STEP_GOAL] ?: 8000,
            weatherCity = prefs[KEY_WEATHER_CITY] ?: "Aurora City",
            weatherLat = prefs[KEY_WEATHER_LAT] ?: 40.7128,
            weatherLon = prefs[KEY_WEATHER_LON] ?: -74.0060,
            quickNoteText = prefs[KEY_QUICK_NOTE] ?: "Welcome to Comet! Tap to jot ideas.",
            enableWakeWord = prefs[KEY_WAKE_WORD] ?: true,
            isSetupCompleted = prefs[KEY_SETUP_COMPLETED] ?: false,
            widgetOrder = widgetOrder,
            enabledWidgets = enabledWidgets
        )
    }

    val homeAppIdsFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_HOME_APPS] ?: ""
        if (raw.isNotBlank()) raw.split("|||") else emptyList()
    }

    val foldersFlow: Flow<List<Folder>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_FOLDERS] ?: ""
        parseFolders(raw)
    }

    suspend fun updateWallpaper(type: WallpaperType, customUri: String? = null) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WALLPAPER] = type.name
            if (customUri != null) {
                prefs[KEY_CUSTOM_WALLPAPER] = customUri
            } else {
                prefs.remove(KEY_CUSTOM_WALLPAPER)
            }
        }
    }

    suspend fun updateAccent(index: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCENT_INDEX] = index
        }
    }

    suspend fun updateGlassOpacity(opacity: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_GLASS_OPACITY] = opacity
        }
    }

    suspend fun updateGlassCornerRadius(radiusDp: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_GLASS_RADIUS] = radiusDp
        }
    }

    suspend fun updateOpticalGlass(blurIntensity: Int? = null, bendCurvature: Float? = null, specularity: Float? = null) {
        context.dataStore.edit { prefs ->
            blurIntensity?.let { prefs[KEY_GLASS_BLUR] = it }
            bendCurvature?.let { prefs[KEY_GLASS_BEND] = it }
            specularity?.let { prefs[KEY_GLASS_SPECULARITY] = it }
        }
    }

    suspend fun updateIconShape(shape: IconShape) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ICON_SHAPE] = shape.name
        }
    }

    suspend fun updateIconTheme(theme: IconThemeStyle) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ICON_THEME] = theme.name
        }
    }

    suspend fun updateGridColumns(columns: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_GRID_COLUMNS] = columns
        }
    }

    suspend fun updateIconSize(sizeDp: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ICON_SIZE] = sizeDp
        }
    }

    suspend fun updateLabelVisibility(showHome: Boolean, showDrawer: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_LABELS_HOME] = showHome
            prefs[KEY_SHOW_LABELS_DRAWER] = showDrawer
        }
    }

    suspend fun updateLabelFontSize(sizeSp: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LABEL_FONT_SIZE] = sizeSp
        }
    }

    suspend fun updateWidgetToggles(
        clock: Boolean? = null,
        weather: Boolean? = null,
        battery: Boolean? = null,
        music: Boolean? = null,
        search: Boolean? = null,
        dockAi: Boolean? = null
    ) {
        context.dataStore.edit { prefs ->
            clock?.let { prefs[KEY_SHOW_CLOCK] = it }
            weather?.let { prefs[KEY_SHOW_WEATHER] = it }
            battery?.let { prefs[KEY_SHOW_BATTERY] = it }
            music?.let { prefs[KEY_SHOW_MUSIC] = it }
            search?.let { prefs[KEY_SHOW_SEARCH] = it }
            dockAi?.let { prefs[KEY_SHOW_DOCK_AI] = it }
        }
    }

    suspend fun updateWidgetOrder(order: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WIDGET_ORDER] = order.joinToString(",")
        }
    }

    suspend fun toggleWidget(widgetId: String, isEnabled: Boolean) {
        context.dataStore.edit { prefs ->
            val raw = prefs[KEY_ENABLED_WIDGETS] ?: WidgetIds.DEFAULT_ENABLED.joinToString(",")
            val set = raw.split(",").filter { it.isNotBlank() }.toMutableSet()
            if (isEnabled) {
                set.add(widgetId)
            } else {
                set.remove(widgetId)
            }
            prefs[KEY_ENABLED_WIDGETS] = set.joinToString(",")
        }
    }

    suspend fun moveWidget(widgetId: String, moveUp: Boolean) {
        context.dataStore.edit { prefs ->
            val raw = prefs[KEY_WIDGET_ORDER] ?: WidgetIds.DEFAULT_ORDER.joinToString(",")
            val currentOrder = raw.split(",").filter { it.isNotBlank() }.toMutableList()
            val index = currentOrder.indexOf(widgetId)
            if (index != -1) {
                if (moveUp && index > 0) {
                    val temp = currentOrder[index - 1]
                    currentOrder[index - 1] = currentOrder[index]
                    currentOrder[index] = temp
                } else if (!moveUp && index < currentOrder.size - 1) {
                    val temp = currentOrder[index + 1]
                    currentOrder[index + 1] = currentOrder[index]
                    currentOrder[index] = temp
                }
                prefs[KEY_WIDGET_ORDER] = currentOrder.joinToString(",")
            }
        }
    }

    suspend fun updateStyles(
        dockStyle: DockStyle? = null,
        clockStyle: ClockStyle? = null
    ) {
        context.dataStore.edit { prefs ->
            dockStyle?.let { prefs[KEY_DOCK_STYLE] = it.name }
            clockStyle?.let { prefs[KEY_CLOCK_STYLE] = it.name }
        }
    }

    suspend fun updateDailyStepGoal(goal: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DAILY_STEP_GOAL] = goal
        }
    }

    suspend fun updateWeatherLocation(city: String, lat: Double, lon: Double) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WEATHER_CITY] = city
            prefs[KEY_WEATHER_LAT] = lat
            prefs[KEY_WEATHER_LON] = lon
        }
    }

    suspend fun updateQuickNote(text: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_QUICK_NOTE] = text
        }
    }

    suspend fun updateWakeWord(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WAKE_WORD] = enabled
        }
    }

    suspend fun setSetupCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SETUP_COMPLETED] = completed
        }
    }

    suspend fun updateDockApps(dockAppIds: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DOCK_APPS] = dockAppIds.joinToString("|||")
        }
    }

    suspend fun updateHomeApps(homeAppIds: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HOME_APPS] = homeAppIds.joinToString("|||")
        }
    }

    suspend fun saveFolders(folders: List<Folder>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FOLDERS] = serializeFolders(folders)
        }
    }

    suspend fun toggleCelsius(isCelsius: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_CELSIUS] = if (isCelsius) "true" else "false"
        }
    }

    suspend fun updateGestures(
        swipeUp: GestureAction? = null,
        swipeDown: GestureAction? = null,
        doubleTap: GestureAction? = null,
        pinch: GestureAction? = null,
        twoFingerDown: GestureAction? = null
    ) {
        context.dataStore.edit { prefs ->
            swipeUp?.let { prefs[KEY_SWIPE_UP] = it.name }
            swipeDown?.let { prefs[KEY_SWIPE_DOWN] = it.name }
            doubleTap?.let { prefs[KEY_DOUBLE_TAP] = it.name }
            pinch?.let { prefs[KEY_PINCH] = it.name }
            twoFingerDown?.let { prefs[KEY_TWO_FINGER_DOWN] = it.name }
        }
    }

    suspend fun updateDefaultMusicApp(packageName: String?) {
        context.dataStore.edit { prefs ->
            if (packageName != null) {
                prefs[KEY_DEFAULT_MUSIC_APP] = packageName
            } else {
                prefs.remove(KEY_DEFAULT_MUSIC_APP)
            }
        }
    }

    private fun serializeFolders(folders: List<Folder>): String {
        val array = JSONArray()
        folders.forEach { folder ->
            val obj = JSONObject()
            obj.put("id", folder.id)
            obj.put("name", folder.name)
            val appsArray = JSONArray()
            folder.appIds.forEach { appsArray.put(it) }
            obj.put("apps", appsArray)
            array.put(obj)
        }
        return array.toString()
    }

    private fun parseFolders(raw: String): List<Folder> {
        if (raw.isBlank()) return emptyList()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<Folder>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.getString("name")
                val appsArray = obj.getJSONArray("apps")
                val apps = mutableListOf<String>()
                for (j in 0 until appsArray.length()) {
                    apps.add(appsArray.getString(j))
                }
                list.add(Folder(id = id, name = name, appIds = apps))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}
