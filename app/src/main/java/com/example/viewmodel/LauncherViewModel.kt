package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.PreferencesManager
import com.example.model.AppCategory
import com.example.model.AppInfo
import com.example.model.ClockStyle
import com.example.model.DockStyle
import com.example.model.Folder
import com.example.model.GestureAction
import com.example.model.IconShape
import com.example.model.IconThemeStyle
import com.example.model.LauncherSettings
import com.example.model.WallpaperType
import com.example.service.LiveWeatherData
import com.example.service.StepTelemetry
import com.example.service.StepTrackerManager
import com.example.service.WeatherManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class LauncherUiState(
    val allApps: List<AppInfo> = emptyList(),
    val homeApps: List<AppInfo> = emptyList(),
    val dockApps: List<AppInfo> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val settings: LauncherSettings = LauncherSettings(),
    val isLoading: Boolean = true
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)
    private val prefs = PreferencesManager(application)

    val stepTracker = StepTrackerManager(application)
    val stepTelemetry: StateFlow<StepTelemetry> = stepTracker.telemetry

    val weatherManager = WeatherManager(application, viewModelScope)
    val weatherData: StateFlow<LiveWeatherData> = weatherManager.weather

    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val allApps: StateFlow<List<AppInfo>> = _allApps.asStateFlow()

    private val _isLoading = MutableStateFlow(true)

    // Wake word triggered event
    private val _wakeWordTriggered = MutableStateFlow<Long?>(null)
    val wakeWordTriggered: StateFlow<Long?> = _wakeWordTriggered.asStateFlow()

    fun onWakeWordHandled() {
        _wakeWordTriggered.value = null
    }

    fun triggerWakeWord() {
        _wakeWordTriggered.value = System.currentTimeMillis()
    }

    val uiState: StateFlow<LauncherUiState> = combine(
        _allApps,
        prefs.launcherSettingsFlow,
        prefs.homeAppIdsFlow,
        prefs.foldersFlow,
        _isLoading
    ) { apps, settings, homeIds, folders, loading ->
        val resolvedDockApps = if (settings.dockAppIds.isNotEmpty()) {
            settings.dockAppIds.mapNotNull { id -> apps.find { it.id == id } }
        } else {
            pickDefaultDockApps(apps)
        }

        val resolvedHomeApps = if (homeIds.isNotEmpty()) {
            homeIds.mapNotNull { id -> apps.find { it.id == id } }
        } else {
            pickDefaultHomeApps(apps, resolvedDockApps.map { it.id }.toSet())
        }

        // Keep step tracker goal in sync
        stepTracker.startListening(settings.dailyStepGoal)

        LauncherUiState(
            allApps = apps,
            homeApps = resolvedHomeApps,
            dockApps = resolvedDockApps,
            folders = folders,
            settings = settings,
            isLoading = loading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LauncherUiState()
    )

    val musicApps: StateFlow<List<AppInfo>> = _allApps.map { apps ->
        apps.filter { app ->
            app.category == AppCategory.MEDIA ||
            app.packageName.contains("music") ||
            app.packageName.contains("spotify") ||
            app.packageName.contains("audio") ||
            app.label.lowercase().contains("music")
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            _isLoading.value = true
            val apps = repository.getInstalledApps()
            _allApps.value = apps
            _isLoading.value = false
        }
    }

    fun launchApp(app: AppInfo): Boolean {
        return repository.launchApp(app)
    }

    fun openAppDetails(app: AppInfo) {
        repository.openAppDetails(app)
    }

    fun uninstallApp(app: AppInfo) {
        repository.uninstallApp(app)
    }

    fun toggleDockApp(appId: String) {
        viewModelScope.launch {
            val currentDock = uiState.value.dockApps.map { it.id }.toMutableList()
            if (currentDock.contains(appId)) {
                currentDock.remove(appId)
            } else {
                if (currentDock.size < 5) {
                    currentDock.add(appId)
                }
            }
            prefs.updateDockApps(currentDock)
        }
    }

    fun toggleHomeApp(appId: String) {
        viewModelScope.launch {
            val currentHome = uiState.value.homeApps.map { it.id }.toMutableList()
            if (currentHome.contains(appId)) {
                currentHome.remove(appId)
            } else {
                currentHome.add(appId)
            }
            prefs.updateHomeApps(currentHome)
        }
    }

    fun createFolder(name: String, appIds: List<String>) {
        viewModelScope.launch {
            val currentFolders = uiState.value.folders.toMutableList()
            val newFolder = Folder(
                id = UUID.randomUUID().toString(),
                name = name,
                appIds = appIds
            )
            currentFolders.add(newFolder)
            prefs.saveFolders(currentFolders)
        }
    }

    fun renameFolder(folderId: String, newName: String) {
        viewModelScope.launch {
            val currentFolders = uiState.value.folders.map {
                if (it.id == folderId) it.copy(name = newName) else it
            }
            prefs.saveFolders(currentFolders)
        }
    }

    fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            val currentFolders = uiState.value.folders.filter { it.id != folderId }
            prefs.saveFolders(currentFolders)
        }
    }

    fun addAppToFolder(folderId: String, appId: String) {
        viewModelScope.launch {
            val currentFolders = uiState.value.folders.map { folder ->
                if (folder.id == folderId && !folder.appIds.contains(appId)) {
                    folder.copy(appIds = folder.appIds + appId)
                } else folder
            }
            prefs.saveFolders(currentFolders)
        }
    }

    fun removeAppFromFolder(folderId: String, appId: String) {
        viewModelScope.launch {
            val currentFolders = uiState.value.folders.map { folder ->
                if (folder.id == folderId) {
                    folder.copy(appIds = folder.appIds - appId)
                } else folder
            }
            prefs.saveFolders(currentFolders)
        }
    }

    fun updateWallpaper(type: WallpaperType, customUri: String? = null) {
        viewModelScope.launch {
            prefs.updateWallpaper(type, customUri)
        }
    }

    fun updateAccent(index: Int) {
        viewModelScope.launch {
            prefs.updateAccent(index)
        }
    }

    fun updateGlassOpacity(opacity: Float) {
        viewModelScope.launch {
            prefs.updateGlassOpacity(opacity)
        }
    }

    fun updateIconShape(shape: IconShape) {
        viewModelScope.launch {
            prefs.updateIconShape(shape)
        }
    }

    fun updateIconTheme(theme: IconThemeStyle) {
        viewModelScope.launch {
            prefs.updateIconTheme(theme)
        }
    }

    fun updateGridColumns(cols: Int) {
        viewModelScope.launch {
            prefs.updateGridColumns(cols)
        }
    }

    fun toggleTempUnit() {
        viewModelScope.launch {
            prefs.toggleCelsius(!uiState.value.settings.isCelsius)
        }
    }

    fun updateGlassCornerRadius(radiusDp: Int) {
        viewModelScope.launch {
            prefs.updateGlassCornerRadius(radiusDp)
        }
    }

    fun updateOpticalGlass(blurIntensity: Int? = null, bendCurvature: Float? = null, specularity: Float? = null) {
        viewModelScope.launch {
            prefs.updateOpticalGlass(blurIntensity, bendCurvature, specularity)
        }
    }

    fun updateIconSize(sizeDp: Int) {
        viewModelScope.launch {
            prefs.updateIconSize(sizeDp)
        }
    }

    fun updateLabelSettings(showHome: Boolean, showDrawer: Boolean, fontSizeSp: Int) {
        viewModelScope.launch {
            prefs.updateLabelVisibility(showHome, showDrawer)
            prefs.updateLabelFontSize(fontSizeSp)
        }
    }

    fun updateWidgetToggles(
        clock: Boolean? = null,
        weather: Boolean? = null,
        battery: Boolean? = null,
        music: Boolean? = null,
        search: Boolean? = null,
        dockAi: Boolean? = null
    ) {
        viewModelScope.launch {
            prefs.updateWidgetToggles(clock, weather, battery, music, search, dockAi)
        }
    }

    fun toggleWidget(widgetId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            prefs.toggleWidget(widgetId, isEnabled)
        }
    }

    fun moveWidget(widgetId: String, moveUp: Boolean) {
        viewModelScope.launch {
            prefs.moveWidget(widgetId, moveUp)
        }
    }

    fun updateWidgetOrder(order: List<String>) {
        viewModelScope.launch {
            prefs.updateWidgetOrder(order)
        }
    }

    fun updateStyles(dockStyle: DockStyle? = null, clockStyle: ClockStyle? = null) {
        viewModelScope.launch {
            prefs.updateStyles(dockStyle, clockStyle)
        }
    }

    fun updateDailyStepGoal(goal: Int) {
        viewModelScope.launch {
            stepTracker.setGoal(goal)
            prefs.updateDailyStepGoal(goal)
        }
    }

    fun addManualSteps(count: Int) {
        stepTracker.addManualSteps(count)
    }

    fun updateWeatherLocation(city: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            weatherManager.setLocation(city, lat, lon)
            prefs.updateWeatherLocation(city, lat, lon)
        }
    }

    fun refreshWeather() {
        val s = uiState.value.settings
        weatherManager.refresh(s.weatherCity, s.weatherLat, s.weatherLon)
    }

    fun updateQuickNote(text: String) {
        viewModelScope.launch {
            prefs.updateQuickNote(text)
        }
    }

    fun updateWakeWord(enabled: Boolean) {
        viewModelScope.launch {
            prefs.updateWakeWord(enabled)
        }
    }

    fun completeSetup(newSettings: LauncherSettings) {
        viewModelScope.launch {
            prefs.updateAccent(newSettings.accentColorIndex)
            prefs.updateGlassOpacity(newSettings.glassOpacity)
            prefs.updateOpticalGlass(newSettings.glassBlurIntensity, newSettings.glassBendCurvature, newSettings.glassSpecularity)
            prefs.updateIconTheme(newSettings.iconTheme)
            prefs.updateIconShape(newSettings.iconShape)
            prefs.updateGridColumns(newSettings.gridColumns)
            prefs.updateWakeWord(newSettings.enableWakeWord)
            prefs.setSetupCompleted(true)
        }
    }

    fun reopenSetupWizard() {
        viewModelScope.launch {
            prefs.setSetupCompleted(false)
        }
    }

    fun updateGestures(
        swipeUp: GestureAction? = null,
        swipeDown: GestureAction? = null,
        doubleTap: GestureAction? = null,
        pinch: GestureAction? = null,
        twoFingerDown: GestureAction? = null
    ) {
        viewModelScope.launch {
            prefs.updateGestures(swipeUp, swipeDown, doubleTap, pinch, twoFingerDown)
        }
    }

    fun updateDefaultMusicApp(pkgName: String?) {
        viewModelScope.launch {
            prefs.updateDefaultMusicApp(pkgName)
        }
    }

    fun executeGesture(
        context: Context,
        action: GestureAction,
        onOpenDrawer: () -> Unit,
        onOpenSearch: () -> Unit,
        onOpenSettings: () -> Unit,
        onAskComet: () -> Unit = {}
    ) {
        when (action) {
            GestureAction.OPEN_DRAWER -> onOpenDrawer()
            GestureAction.QUICK_SEARCH -> onOpenSearch()
            GestureAction.ASK_COMET -> onAskComet()
            GestureAction.LAUNCHER_SETTINGS -> onOpenSettings()
            GestureAction.VOICE_SEARCH -> {
                try {
                    val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    onOpenSearch()
                }
            }
            GestureAction.EXPAND_NOTIFICATIONS -> {
                try {
                    val statusBarService = context.getSystemService("statusbar")
                    val statusBarManager = Class.forName("android.app.StatusBarManager")
                    val expand = statusBarManager.getMethod("expandNotificationsPanel")
                    expand.invoke(statusBarService)
                } catch (e: Exception) {
                    onOpenSearch()
                }
            }
            GestureAction.NONE -> {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        stepTracker.stopListening()
    }

    private fun pickDefaultDockApps(apps: List<AppInfo>): List<AppInfo> {
        val candidates = listOf("dialer", "phone", "message", "chrome", "browser", "camera", "gallery")
        val dockList = mutableListOf<AppInfo>()
        for (pattern in candidates) {
            val match = apps.find { it.packageName.lowercase().contains(pattern) || it.label.lowercase().contains(pattern) }
            if (match != null && !dockList.contains(match) && dockList.size < 4) {
                dockList.add(match)
            }
        }
        if (dockList.isEmpty() && apps.isNotEmpty()) {
            dockList.addAll(apps.take(4))
        }
        return dockList
    }

    private fun pickDefaultHomeApps(apps: List<AppInfo>, dockIds: Set<String>): List<AppInfo> {
        return apps.filter { !dockIds.contains(it.id) }.take(8)
    }
}
