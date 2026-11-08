package com.example.ui.home

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ai.CometAiManager
import com.example.ai.WakeWordManager
import com.example.model.AppInfo
import com.example.model.Folder
import com.example.model.GestureAction
import com.example.model.WallpaperType
import com.example.model.WidgetIds
import com.example.ui.ai.CometSiriSheet
import com.example.ui.components.AccentColors
import com.example.ui.components.AppIconView
import com.example.ui.components.BatteryWidget
import com.example.ui.components.ClockWidget
import com.example.ui.components.ContextMenuDialog
import com.example.ui.components.FolderDialog
import com.example.ui.components.FolderIconView
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.MusicWidget
import com.example.ui.components.QuickNotesWidget
import com.example.ui.components.QuickTogglesWidget
import com.example.ui.components.SearchWidget
import com.example.ui.components.StepsWidget
import com.example.ui.components.WeatherWidget
import com.example.ui.dock.FloatingGlassDock
import com.example.ui.drawer.AppDrawerSheet
import com.example.ui.settings.LauncherSettingsSheet
import com.example.ui.settings.SetupWizardSheet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.LauncherUiState
import com.example.viewmodel.LauncherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    uiState: LauncherUiState
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val accentColor = remember(uiState.settings.accentColorIndex) {
        AccentColors.getOrElse(uiState.settings.accentColorIndex) { NeonCyan }
    }

    val musicApps by viewModel.musicApps.collectAsState()
    val stepTelemetry by viewModel.stepTelemetry.collectAsState()
    val weatherData by viewModel.weatherData.collectAsState()
    val wakeWordEvent by viewModel.wakeWordTriggered.collectAsState()

    var isDrawerOpen by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var isCometOpen by remember { mutableStateOf(false) }
    var isWidgetEditMode by remember { mutableStateOf(false) }
    var showAddWidgetDialog by remember { mutableStateOf(false) }
    var selectedAppContextMenu by remember { mutableStateOf<AppInfo?>(null) }
    var openedFolder by remember { mutableStateOf<Folder?>(null) }

    var accumulatedDragY by remember { mutableFloatStateOf(0f) }

    // Initialize Comet AI Assistant
    val cometAiManager = remember {
        CometAiManager(
            context = context,
            scope = coroutineScope,
            onLaunchApp = { viewModel.launchApp(it) },
            onOpenWeather = { /* handled */ },
            onOpenSettings = { isSettingsOpen = true },
            getStepTelemetry = { viewModel.stepTelemetry.value }
        )
    }

    // Wake Word Manager for "Hey Comet"
    val wakeWordManager = remember {
        WakeWordManager(
            context = context,
            scope = coroutineScope,
            onWakeWordDetected = {
                isCometOpen = true
                cometAiManager.startListening()
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            cometAiManager.cleanUp()
            wakeWordManager.destroy()
        }
    }

    DisposableEffect(uiState.settings.enableWakeWord) {
        wakeWordManager.start(uiState.settings.enableWakeWord)
        onDispose {
            wakeWordManager.stop()
        }
    }

    LaunchedEffect(wakeWordEvent) {
        if (wakeWordEvent != null) {
            isCometOpen = true
            cometAiManager.startListening()
            viewModel.onWakeWordHandled()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Gesture navigation handler: Swipe Up / Swipe Down / Double Tap / Long Press
            .pointerInput(uiState.settings.swipeUpAction, uiState.settings.swipeDownAction) {
                detectVerticalDragGestures(
                    onDragStart = { accumulatedDragY = 0f },
                    onDragEnd = {
                        if (accumulatedDragY < -45f) {
                            viewModel.executeGesture(
                                context = context,
                                action = uiState.settings.swipeUpAction,
                                onOpenDrawer = { isDrawerOpen = true },
                                onOpenSearch = { isDrawerOpen = true },
                                onOpenSettings = { isSettingsOpen = true },
                                onAskComet = {
                                    isCometOpen = true
                                    cometAiManager.startListening()
                                }
                            )
                        } else if (accumulatedDragY > 45f) {
                            viewModel.executeGesture(
                                context = context,
                                action = uiState.settings.swipeDownAction,
                                onOpenDrawer = { isDrawerOpen = true },
                                onOpenSearch = { isDrawerOpen = true },
                                onOpenSettings = { isSettingsOpen = true },
                                onAskComet = {
                                    isCometOpen = true
                                    cometAiManager.startListening()
                                }
                            )
                        }
                        accumulatedDragY = 0f
                    },
                    onDragCancel = { accumulatedDragY = 0f },
                    onVerticalDrag = { _, dragAmount ->
                        accumulatedDragY += dragAmount
                    }
                )
            }
            .pointerInput(uiState.settings.doubleTapAction) {
                detectTapGestures(
                    onDoubleTap = {
                        viewModel.executeGesture(
                            context = context,
                            action = uiState.settings.doubleTapAction,
                            onOpenDrawer = { isDrawerOpen = true },
                            onOpenSearch = { isDrawerOpen = true },
                            onOpenSettings = { isSettingsOpen = true },
                            onAskComet = {
                                isCometOpen = true
                                cometAiManager.startListening()
                            }
                        )
                    },
                    onLongPress = {
                        isSettingsOpen = true
                    }
                )
            }
    ) {
        // Wallpaper Layer (Clean, non-spacy by default with full user customizability)
        if (uiState.settings.customWallpaperUri != null) {
            AsyncImage(
                model = Uri.parse(uiState.settings.customWallpaperUri),
                contentDescription = "Custom Wallpaper",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            when (uiState.settings.wallpaperType) {
                WallpaperType.MINIMAL_FROST -> {
                    Image(
                        painter = painterResource(id = R.drawable.wallpaper_minimal_frost_1793997930230),
                        contentDescription = "Clean Minimal Frost Wallpaper",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                WallpaperType.MINIMAL_INDIGO -> {
                    Image(
                        painter = painterResource(id = R.drawable.wallpaper_minimal_indigo_1793997945300),
                        contentDescription = "Liquid Indigo Wallpaper",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                WallpaperType.CLEAN_SLATE -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1B202E), Color(0xFF111420), Color(0xFF0C0F17))
                                )
                            )
                    )
                }
                WallpaperType.SILK_AURA -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF282C44), Color(0xFF171A2E), Color(0xFF0F1122))
                                )
                            )
                    )
                }
                WallpaperType.LIQUID_NEBULA -> {
                    Image(
                        painter = painterResource(id = R.drawable.wallpaper_liquid_nebula_1793996489118),
                        contentDescription = "Liquid Nebula Wallpaper",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                WallpaperType.CRYSTAL_AURORA -> {
                    Image(
                        painter = painterResource(id = R.drawable.wallpaper_crystal_aurora_1793996513336),
                        contentDescription = "Crystal Flow Wallpaper",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Frosted Liquid Glass Vignette and Refractive Backdrop Tint
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x280A0E22),
                            Color(0x180E132D),
                            Color(0x38090C1E)
                        )
                    )
                )
                .background(Color.Black.copy(alpha = uiState.settings.glassOpacity * 0.35f))
        )

        // Main Home Screen Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar: Quick Search Pill & Comet Settings Icon
            if (uiState.settings.showSearchBar) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchWidget(
                        accentColor = accentColor,
                        onSearchClick = { isDrawerOpen = true },
                        onVoiceClick = {
                            isCometOpen = true
                            cometAiManager.startListening()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0x35121840))
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = { isSettingsOpen = true })
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Launcher Settings",
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0x35121840))
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = { isSettingsOpen = true })
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Launcher Settings",
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Scrollable widgets and home apps section
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Widget Edit Mode Banner (when user activates move/add/remove mode)
                if (isWidgetEditMode) {
                    item {
                        LiquidGlassCard(
                            shape = RoundedCornerShape(18.dp),
                            backgroundColor = Color(0x40182455),
                            borderColor = accentColor,
                            accentGlow = accentColor
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.DashboardCustomize, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Widget Customizer", color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                        Text("Move or remove widgets on home", color = TextSecondary, fontSize = 11.sp)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0x30FFFFFF))
                                            .clickable { showAddWidgetDialog = true }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("+ Add", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(accentColor)
                                            .clickable { isWidgetEditMode = false }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Done", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // DYNAMIC ORDERED WIDGETS
                for (widgetId in uiState.settings.widgetOrder) {
                    if (uiState.settings.enabledWidgets.contains(widgetId)) {
                        item(key = widgetId) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Edit controls bar above widget in Edit Mode
                                if (isWidgetEditMode) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.DragIndicator, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = widgetTitle(widgetId),
                                                color = accentColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { viewModel.moveWidget(widgetId, true) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Rounded.ArrowUpward, contentDescription = "Move Up", tint = TextPrimary, modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(
                                                onClick = { viewModel.moveWidget(widgetId, false) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Rounded.ArrowDownward, contentDescription = "Move Down", tint = TextPrimary, modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(
                                                onClick = { viewModel.toggleWidget(widgetId, false) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Rounded.Close, contentDescription = "Remove Widget", tint = Color(0xFFFF5E57), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }

                                when (widgetId) {
                                    WidgetIds.CLOCK -> ClockWidget(
                                        accentColor = accentColor,
                                        clockStyle = uiState.settings.clockStyle,
                                        glassCornerRadiusDp = uiState.settings.glassCornerRadiusDp
                                    )

                                    WidgetIds.WEATHER -> WeatherWidget(
                                        weather = weatherData,
                                        accentColor = accentColor,
                                        glassCornerRadiusDp = uiState.settings.glassCornerRadiusDp,
                                        isCelsius = uiState.settings.isCelsius,
                                        onToggleUnit = { viewModel.toggleTempUnit() },
                                        onRefresh = { viewModel.refreshWeather() },
                                        onSelectCity = { name, lat, lon -> viewModel.updateWeatherLocation(name, lat, lon) }
                                    )

                                    WidgetIds.STEPS -> StepsWidget(
                                        telemetry = stepTelemetry,
                                        accentColor = accentColor,
                                        glassCornerRadiusDp = uiState.settings.glassCornerRadiusDp,
                                        onUpdateGoal = { viewModel.updateDailyStepGoal(it) },
                                        onAddSimulatedSteps = { viewModel.addManualSteps(it) }
                                    )

                                    WidgetIds.BATTERY -> BatteryWidget(accentColor = accentColor)

                                    WidgetIds.MUSIC -> MusicWidget(
                                        accentColor = accentColor,
                                        defaultMusicAppPackage = uiState.settings.defaultMusicAppPackage,
                                        installedMusicApps = musicApps,
                                        onSelectDefaultMusicApp = { viewModel.updateDefaultMusicApp(it) }
                                    )

                                    WidgetIds.QUICK_TOGGLES -> QuickTogglesWidget(
                                        accentColor = accentColor,
                                        glassCornerRadiusDp = uiState.settings.glassCornerRadiusDp
                                    )

                                    WidgetIds.QUICK_NOTES -> QuickNotesWidget(
                                        noteContent = uiState.settings.quickNoteText,
                                        accentColor = accentColor,
                                        glassCornerRadiusDp = uiState.settings.glassCornerRadiusDp,
                                        onSaveNote = { viewModel.updateQuickNote(it) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Widget Customization & Add Button pill at bottom of widgets
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x20FFFFFF))
                                .clickable {
                                    if (!isWidgetEditMode) {
                                        isWidgetEditMode = true
                                    } else {
                                        showAddWidgetDialog = true
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (!isWidgetEditMode) Icons.Rounded.Tune else Icons.Rounded.Add,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (!isWidgetEditMode) "Move / Edit Widgets" else "Add More Widgets",
                                    color = TextPrimary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Home Apps Section Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Favorites & Folders",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.createFolder(
                                    name = "Liquid Pod",
                                    appIds = uiState.allApps.take(3).map { it.id }
                                )
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CreateNewFolder,
                                contentDescription = "New Folder",
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Grid of Folders and Home Apps
                item {
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = accentColor)
                        }
                    } else {
                        val gridCols = uiState.settings.gridColumns
                        val allItemsCount = uiState.folders.size + uiState.homeApps.size
                        val rows = (allItemsCount + gridCols - 1) / gridCols

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (r in 0 until rows) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    for (c in 0 until gridCols) {
                                        val index = r * gridCols + c
                                        if (index < uiState.folders.size) {
                                            val folder = uiState.folders[index]
                                            val appsInFolder = folder.appIds.mapNotNull { id ->
                                                uiState.allApps.find { it.id == id }
                                            }
                                            FolderIconView(
                                                folder = folder,
                                                appsInFolder = appsInFolder,
                                                accentColor = accentColor,
                                                onClick = { openedFolder = folder },
                                                onLongClick = { openedFolder = folder },
                                                modifier = Modifier.weight(1f)
                                            )
                                        } else if (index < allItemsCount) {
                                            val appIndex = index - uiState.folders.size
                                            val app = uiState.homeApps[appIndex]
                                            AppIconView(
                                                app = app,
                                                iconSize = uiState.settings.iconSizeDp.dp,
                                                showLabel = uiState.settings.showLabelsOnHome,
                                                labelFontSize = uiState.settings.labelFontSizeSp.sp,
                                                iconShape = uiState.settings.iconShape,
                                                iconTheme = uiState.settings.iconTheme,
                                                accentColor = accentColor,
                                                onClick = { viewModel.launchApp(app) },
                                                onLongClick = { selectedAppContextMenu = app },
                                                modifier = Modifier.weight(1f)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom spacer to prevent dock overlap
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }

            // Floating Liquid Glass Dock with Comet Siri AI trigger button
            FloatingGlassDock(
                dockApps = uiState.dockApps,
                iconShape = uiState.settings.iconShape,
                accentColor = accentColor,
                dockStyle = uiState.settings.dockStyle,
                showAiButton = uiState.settings.showDockAiButton,
                glassCornerRadiusDp = uiState.settings.glassCornerRadiusDp,
                onAppClick = { viewModel.launchApp(it) },
                onAppLongClick = { selectedAppContextMenu = it },
                onOpenDrawer = { isDrawerOpen = true },
                onOpenComet = {
                    isCometOpen = true
                    cometAiManager.startListening()
                },
                modifier = Modifier.navigationBarsPadding()
            )
        }

        // App Drawer Full Overlay
        AppDrawerSheet(
            visible = isDrawerOpen,
            apps = uiState.allApps,
            iconShape = uiState.settings.iconShape,
            iconTheme = uiState.settings.iconTheme,
            accentColor = accentColor,
            showLabels = uiState.settings.showLabelsInDrawer,
            labelFontSizeSp = uiState.settings.labelFontSizeSp,
            iconSizeDp = uiState.settings.iconSizeDp,
            onDismiss = { isDrawerOpen = false },
            onAppClick = { app ->
                isDrawerOpen = false
                viewModel.launchApp(app)
            },
            onAppLongClick = { app ->
                selectedAppContextMenu = app
            }
        )

        // Long Press Context Menu Dialog
        selectedAppContextMenu?.let { app ->
            val isPinnedToDock = uiState.dockApps.any { it.id == app.id }
            val isPinnedToHome = uiState.homeApps.any { it.id == app.id }

            ContextMenuDialog(
                app = app,
                isPinnedToDock = isPinnedToDock,
                isPinnedToHome = isPinnedToHome,
                accentColor = accentColor,
                onDismiss = { selectedAppContextMenu = null },
                onLaunch = { viewModel.launchApp(app) },
                onToggleDock = { viewModel.toggleDockApp(app.id) },
                onToggleHome = { viewModel.toggleHomeApp(app.id) },
                onAddToFolder = {
                    if (uiState.folders.isNotEmpty()) {
                        viewModel.addAppToFolder(uiState.folders.first().id, app.id)
                    } else {
                        viewModel.createFolder("Liquid Pod", listOf(app.id))
                    }
                },
                onAppInfo = { viewModel.openAppDetails(app) },
                onUninstall = { viewModel.uninstallApp(app) }
            )
        }

        // Folder Expansion Dialog
        openedFolder?.let { folder ->
            val appsInFolder = folder.appIds.mapNotNull { id ->
                uiState.allApps.find { it.id == id }
            }
            FolderDialog(
                folder = folder,
                appsInFolder = appsInFolder,
                allApps = uiState.allApps,
                iconShape = uiState.settings.iconShape,
                accentColor = accentColor,
                onDismiss = { openedFolder = null },
                onLaunchApp = { app ->
                    openedFolder = null
                    viewModel.launchApp(app)
                },
                onRenameFolder = { newName ->
                    viewModel.renameFolder(folder.id, newName)
                    openedFolder = openedFolder?.copy(name = newName)
                },
                onDeleteFolder = {
                    viewModel.deleteFolder(folder.id)
                    openedFolder = null
                },
                onAddAppToFolder = { appId ->
                    viewModel.addAppToFolder(folder.id, appId)
                    openedFolder = openedFolder?.copy(appIds = openedFolder!!.appIds + appId)
                },
                onRemoveAppFromFolder = { appId ->
                    viewModel.removeAppFromFolder(folder.id, appId)
                    openedFolder = openedFolder?.copy(appIds = openedFolder!!.appIds - appId)
                }
            )
        }

        // Add Widget Dialog
        if (showAddWidgetDialog) {
            BasicAlertDialog(onDismissRequest = { showAddWidgetDialog = false }) {
                LiquidGlassCard(
                    shape = RoundedCornerShape(26.dp),
                    backgroundColor = Color(0xF20B112D),
                    borderColor = accentColor.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Add Widget to Home", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showAddWidgetDialog = false }, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val allWidgets = listOf(
                            WidgetIds.CLOCK to "Futuristic Clock & Date",
                            WidgetIds.WEATHER to "Real-time Weather & Forecast",
                            WidgetIds.STEPS to "Step Counter & Fitness Goal",
                            WidgetIds.BATTERY to "Battery & System Telemetry",
                            WidgetIds.MUSIC to "Active Media Music Player",
                            WidgetIds.QUICK_TOGGLES to "Control Island Quick Toggles",
                            WidgetIds.QUICK_NOTES to "Liquid Scratchpad Quick Notes"
                        )

                        allWidgets.forEach { (id, title) ->
                            val isCurrentlyActive = uiState.settings.enabledWidgets.contains(id)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isCurrentlyActive) accentColor.copy(alpha = 0.2f) else Color(0x18FFFFFF))
                                    .clickable {
                                        viewModel.toggleWidget(id, !isCurrentlyActive)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = title, color = TextPrimary, fontSize = 13.sp)
                                    if (isCurrentlyActive) {
                                        Text("Active", color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text("+ Add", color = TextSecondary, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Launcher Settings Sheet
        LauncherSettingsSheet(
            visible = isSettingsOpen,
            settings = uiState.settings,
            installedMusicApps = musicApps,
            accentColor = accentColor,
            onDismiss = { isSettingsOpen = false },
            onUpdateWallpaper = { type, uri -> viewModel.updateWallpaper(type, uri) },
            onUpdateAccent = { viewModel.updateAccent(it) },
            onUpdateOpacity = { viewModel.updateGlassOpacity(it) },
            onUpdateGlassCornerRadius = { viewModel.updateGlassCornerRadius(it) },
            onUpdateOpticalGlass = { blur, bend, spec -> viewModel.updateOpticalGlass(blur, bend, spec) },
            onUpdateIconShape = { viewModel.updateIconShape(it) },
            onUpdateIconTheme = { viewModel.updateIconTheme(it) },
            onUpdateGridColumns = { viewModel.updateGridColumns(it) },
            onUpdateIconSize = { viewModel.updateIconSize(it) },
            onUpdateLabelSettings = { home, drawer, font -> viewModel.updateLabelSettings(home, drawer, font) },
            onUpdateWidgetToggles = { clock, weather, battery, music, search, dockAi ->
                viewModel.updateWidgetToggles(clock, weather, battery, music, search, dockAi)
            },
            onToggleWidget = { id, enabled -> viewModel.toggleWidget(id, enabled) },
            onMoveWidget = { id, moveUp -> viewModel.moveWidget(id, moveUp) },
            onOpenWidgetEditMode = { isWidgetEditMode = true },
            onUpdateStyles = { dock, clock -> viewModel.updateStyles(dock, clock) },
            onUpdateGestures = { up, down, dbl, pinch, twoFinger ->
                viewModel.updateGestures(up, down, dbl, pinch, twoFinger)
            },
            onUpdateDefaultMusicApp = { viewModel.updateDefaultMusicApp(it) },
            onUpdateDailyStepGoal = { viewModel.updateDailyStepGoal(it) },
            onUpdateWakeWord = { viewModel.updateWakeWord(it) },
            onReopenSetup = { viewModel.reopenSetupWizard() }
        )

        // Siri-like Comet AI Voice Assistant Sheet
        CometSiriSheet(
            visible = isCometOpen,
            aiManager = cometAiManager,
            allInstalledApps = uiState.allApps,
            accentColor = accentColor,
            onLaunchApp = { viewModel.launchApp(it) },
            onDismiss = {
                isCometOpen = false
                cometAiManager.stopSpeaking()
                cometAiManager.stopListening()
            }
        )

        // First-launch or requested Setup Wizard Onboarding Menu
        if (!uiState.settings.isSetupCompleted) {
            SetupWizardSheet(
                visible = true,
                currentSettings = uiState.settings,
                accentColor = accentColor,
                onCompleteSetup = { newSettings ->
                    viewModel.completeSetup(newSettings)
                }
            )
        }
    }
}

private fun widgetTitle(id: String): String {
    return when (id) {
        WidgetIds.CLOCK -> "Clock & Date"
        WidgetIds.WEATHER -> "Live Weather"
        WidgetIds.STEPS -> "Activity & Steps"
        WidgetIds.BATTERY -> "Battery Health"
        WidgetIds.MUSIC -> "Now Playing"
        WidgetIds.QUICK_TOGGLES -> "Control Island"
        WidgetIds.QUICK_NOTES -> "Liquid Scratchpad"
        else -> id.replaceFirstChar { it.uppercase() }
    }
}
