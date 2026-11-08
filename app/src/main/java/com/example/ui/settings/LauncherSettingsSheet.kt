package com.example.ui.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.SwipeDown
import androidx.compose.material.icons.rounded.SwipeUp
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppInfo
import com.example.model.ClockStyle
import com.example.model.DockStyle
import com.example.model.GestureAction
import com.example.model.IconShape
import com.example.model.IconThemeStyle
import com.example.model.LauncherSettings
import com.example.model.WallpaperType
import com.example.model.WidgetIds
import com.example.service.ActiveMediaManager
import com.example.ui.components.AccentColors
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class SettingsTab(val label: String, val icon: ImageVector) {
    VISUAL("Visual & Optics", Icons.Rounded.DashboardCustomize),
    LAYOUT("Layout & Icons", Icons.Rounded.GridView),
    WIDGETS("Widgets Manager", Icons.Rounded.Widgets),
    GESTURES("Gestures", Icons.Rounded.Gesture),
    WALLPAPERS("Wallpapers", Icons.Rounded.Wallpaper),
    COMET_AI("Comet AI & Voice", Icons.Rounded.SmartToy)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherSettingsSheet(
    visible: Boolean,
    settings: LauncherSettings,
    installedMusicApps: List<AppInfo> = emptyList(),
    accentColor: Color = NeonCyan,
    onDismiss: () -> Unit,
    onUpdateWallpaper: (WallpaperType, String?) -> Unit,
    onUpdateAccent: (Int) -> Unit,
    onUpdateOpacity: (Float) -> Unit,
    onUpdateGlassCornerRadius: (Int) -> Unit = {},
    onUpdateOpticalGlass: (blur: Int?, bend: Float?, specularity: Float?) -> Unit = { _, _, _ -> },
    onUpdateIconShape: (IconShape) -> Unit,
    onUpdateIconTheme: (IconThemeStyle) -> Unit = {},
    onUpdateGridColumns: (Int) -> Unit,
    onUpdateIconSize: (Int) -> Unit = {},
    onUpdateLabelSettings: (showHome: Boolean, showDrawer: Boolean, fontSizeSp: Int) -> Unit = { _, _, _ -> },
    onUpdateWidgetToggles: (clock: Boolean?, weather: Boolean?, battery: Boolean?, music: Boolean?, search: Boolean?, dockAi: Boolean?) -> Unit = { _, _, _, _, _, _ -> },
    onToggleWidget: (String, Boolean) -> Unit = { _, _ -> },
    onMoveWidget: (String, Boolean) -> Unit = { _, _ -> },
    onOpenWidgetEditMode: () -> Unit = {},
    onUpdateStyles: (dockStyle: DockStyle?, clockStyle: ClockStyle?) -> Unit = { _, _ -> },
    onUpdateGestures: (swipeUp: GestureAction?, swipeDown: GestureAction?, doubleTap: GestureAction?, pinch: GestureAction?, twoFingerDown: GestureAction?) -> Unit = { _, _, _, _, _ -> },
    onUpdateDefaultMusicApp: (String?) -> Unit = {},
    onUpdateDailyStepGoal: (Int) -> Unit = {},
    onUpdateWakeWord: (Boolean) -> Unit = {},
    onReopenSetup: () -> Unit = {}
) {
    if (!visible) return

    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(SettingsTab.VISUAL) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onUpdateWallpaper(WallpaperType.MINIMAL_FROST, uri.toString())
        }
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        LiquidGlassCard(
            shape = RoundedCornerShape(settings.glassCornerRadiusDp.dp),
            backgroundColor = Color(0xF2090D2A),
            borderColor = accentColor.copy(alpha = 0.6f),
            accentGlow = accentColor,
            blurIntensity = settings.glassBlurIntensity,
            bendCurvature = settings.glassBendCurvature,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(accentColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = "Settings",
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Comet Preferences",
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Liquid Glass Customization Suite",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Default Launcher Banner
                LiquidGlassCard(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = Color(0x351F2A66),
                    borderColor = accentColor.copy(alpha = 0.4f),
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (ignored: Exception) {}
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = "Default Launcher",
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Set Comet as Default Home",
                                color = TextPrimary,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Make Comet your permanent Android launcher",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Settings Tabs Navigation Bar
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SettingsTab.values()) { tab ->
                        val isSelected = tab == selectedTab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) accentColor.copy(alpha = 0.35f) else Color(0x1EFFFFFF))
                                .clickable { selectedTab = tab }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) TextPrimary else TextSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tab.label,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(14.dp))

                when (selectedTab) {
                    // TAB 1: VISUAL & OPTICS
                    SettingsTab.VISUAL -> {
                        // Refraction Glow Accent
                        Text(
                            text = "Refraction Glow Accent",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(AccentColors) { index, color ->
                                val isSelected = settings.accentColorIndex == index
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(2.dp, if (isSelected) Color.White else Color.Transparent, CircleShape)
                                        .clickable { onUpdateAccent(index) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Liquid Glass Optical Bend Curvature
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Glass Refraction Meniscus Bend", color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "${(settings.glassBendCurvature * 100).toInt()}%", color = accentColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = settings.glassBendCurvature,
                            onValueChange = { onUpdateOpticalGlass(null, it, null) },
                            valueRange = 0.10f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = accentColor,
                                activeTrackColor = accentColor,
                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Glass Frosted Blur Depth
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Glass Frosted Blur Depth", color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Level ${settings.glassBlurIntensity}", color = accentColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = settings.glassBlurIntensity.toFloat(),
                            onValueChange = { onUpdateOpticalGlass(it.toInt(), null, null) },
                            valueRange = 1f..5f,
                            steps = 3,
                            colors = SliderDefaults.colors(
                                thumbColor = accentColor,
                                activeTrackColor = accentColor,
                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Glass Translucency Opacity
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Glass Translucency Opacity", color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "${(settings.glassOpacity * 100).toInt()}%", color = accentColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = settings.glassOpacity,
                            onValueChange = onUpdateOpacity,
                            valueRange = 0.15f..0.60f,
                            colors = SliderDefaults.colors(
                                thumbColor = accentColor,
                                activeTrackColor = accentColor,
                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Glass Corner Radius
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Glass Corner Curvature", color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "${settings.glassCornerRadiusDp} dp", color = accentColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = settings.glassCornerRadiusDp.toFloat(),
                            onValueChange = { onUpdateGlassCornerRadius(it.toInt()) },
                            valueRange = 14f..36f,
                            steps = 11,
                            colors = SliderDefaults.colors(
                                thumbColor = accentColor,
                                activeTrackColor = accentColor,
                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Dock Style Selector
                        Text(text = "Floating Dock Aesthetic", color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            DockStyle.values().forEach { style ->
                                val isSelected = settings.dockStyle == style
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) accentColor.copy(alpha = 0.35f) else Color(0x18FFFFFF))
                                        .clickable { onUpdateStyles(style, null) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = style.displayName.replace(" ", "\n"),
                                        color = if (isSelected) TextPrimary else TextSecondary,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        lineHeight = 13.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Clock Style Selector
                        Text(text = "Clock Typography Style", color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ClockStyle.values().forEach { style ->
                                val isSelected = settings.clockStyle == style
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) accentColor.copy(alpha = 0.35f) else Color(0x18FFFFFF))
                                        .clickable { onUpdateStyles(null, style) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = style.displayName.replace(" ", "\n"),
                                        color = if (isSelected) TextPrimary else TextSecondary,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        lineHeight = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // TAB 2: LAYOUT & ICONS
                    SettingsTab.LAYOUT -> {
                        // Icon Theme Style
                        Text(text = "App Icon Design Theme", color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Choose Liquid Glass tiles or standard icons", color = TextSecondary, fontSize = 11.5.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        IconThemeStyle.values().forEach { style ->
                            val isSelected = settings.iconTheme == style
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) accentColor.copy(alpha = 0.30f) else Color(0x18FFFFFF))
                                    .border(1.dp, if (isSelected) accentColor else Color.Transparent, RoundedCornerShape(12.dp))
                                    .clickable { onUpdateIconTheme(style) }
                                    .padding(horizontal = 12.dp, vertical = 9.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = style.displayName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = style.description, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                    if (isSelected) {
                                        Icon(Icons.Rounded.Check, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Home Grid Density
                        Text(text = "Home Screen Grid Columns", color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(3 to "3 Cols", 4 to "4 (Standard)", 5 to "5 (Dense)", 6 to "6 (Max)").forEach { (cols, label) ->
                                val isSelected = settings.gridColumns == cols
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) accentColor.copy(alpha = 0.35f) else Color(0x18FFFFFF))
                                        .clickable { onUpdateGridColumns(cols) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) TextPrimary else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Icon Size Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Icon Size", color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "${settings.iconSizeDp} dp", color = accentColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = settings.iconSizeDp.toFloat(),
                            onValueChange = { onUpdateIconSize(it.toInt()) },
                            valueRange = 40f..72f,
                            steps = 16,
                            colors = SliderDefaults.colors(
                                thumbColor = accentColor,
                                activeTrackColor = accentColor,
                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Icon Shape
                        Text(text = "App Icon Shape", color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconShape.values().forEach { shape ->
                                val isSelected = settings.iconShape == shape
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) accentColor.copy(alpha = 0.35f) else Color(0x18FFFFFF))
                                        .clickable { onUpdateIconShape(shape) }
                                        .padding(vertical = 9.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = shape.displayName,
                                        color = if (isSelected) TextPrimary else TextSecondary,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // App Label Toggles
                        SettingsToggleRow(
                            title = "Show Labels on Home Screen",
                            subtitle = "Display app name below icons on the desktop",
                            checked = settings.showLabelsOnHome,
                            accentColor = accentColor,
                            onCheckedChange = {
                                onUpdateLabelSettings(it, settings.showLabelsInDrawer, settings.labelFontSizeSp)
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SettingsToggleRow(
                            title = "Show Labels in App Drawer",
                            subtitle = "Display app name in the vertical app drawer",
                            checked = settings.showLabelsInDrawer,
                            accentColor = accentColor,
                            onCheckedChange = {
                                onUpdateLabelSettings(settings.showLabelsOnHome, it, settings.labelFontSizeSp)
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Label Font Size Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Label Font Size", color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "${settings.labelFontSizeSp} sp", color = accentColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = settings.labelFontSizeSp.toFloat(),
                            onValueChange = {
                                onUpdateLabelSettings(settings.showLabelsOnHome, settings.showLabelsInDrawer, it.toInt())
                            },
                            valueRange = 10f..16f,
                            steps = 6,
                            colors = SliderDefaults.colors(
                                thumbColor = accentColor,
                                activeTrackColor = accentColor,
                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                            )
                        )
                    }

                    // TAB 3: WIDGETS MANAGER
                    SettingsTab.WIDGETS -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Reorder & Move Widgets",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Use arrows to move up/down on your desktop",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(accentColor.copy(alpha = 0.25f))
                                    .clickable {
                                        onDismiss()
                                        onOpenWidgetEditMode()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(text = "Edit on Home", color = accentColor, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Reorderable list of widgets
                        val widgetLabels = mapOf(
                            WidgetIds.CLOCK to "Futuristic Clock & Date",
                            WidgetIds.WEATHER to "Real-time Weather & Forecast",
                            WidgetIds.STEPS to "Step Counter & Fitness Goal",
                            WidgetIds.BATTERY to "Battery & System Telemetry",
                            WidgetIds.MUSIC to "Active Media Music Player",
                            WidgetIds.QUICK_TOGGLES to "Control Island Quick Toggles",
                            WidgetIds.QUICK_NOTES to "Liquid Scratchpad Quick Notes"
                        )

                        settings.widgetOrder.forEachIndexed { index, id ->
                            val label = widgetLabels[id] ?: id
                            val isEnabled = settings.enabledWidgets.contains(id)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isEnabled) Color(0x22FFFFFF) else Color(0x10FFFFFF))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = label, color = if (isEnabled) TextPrimary else TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Text(text = if (isEnabled) "Active on home" else "Hidden", color = if (isEnabled) accentColor else TextSecondary, fontSize = 10.5.sp)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Move Up button
                                        IconButton(
                                            onClick = { onMoveWidget(id, true) },
                                            enabled = index > 0,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Rounded.ArrowUpward,
                                                contentDescription = "Move Up",
                                                tint = if (index > 0) TextPrimary else Color.White.copy(alpha = 0.2f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        // Move Down button
                                        IconButton(
                                            onClick = { onMoveWidget(id, false) },
                                            enabled = index < settings.widgetOrder.size - 1,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Rounded.ArrowDownward,
                                                contentDescription = "Move Down",
                                                tint = if (index < settings.widgetOrder.size - 1) TextPrimary else Color.White.copy(alpha = 0.2f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        // Enable/Disable switch
                                        Switch(
                                            checked = isEnabled,
                                            onCheckedChange = { onToggleWidget(id, it) },
                                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(12.dp))

                        SettingsToggleRow(
                            title = "Top Quick Search Bar",
                            subtitle = "Fluid search pill with voice icon",
                            checked = settings.showSearchBar,
                            accentColor = accentColor,
                            onCheckedChange = { onUpdateWidgetToggles(null, null, null, null, it, null) }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SettingsToggleRow(
                            title = "Comet Siri AI Orb in Dock",
                            subtitle = "Voice/chat orb in the floating glass dock",
                            checked = settings.showDockAiButton,
                            accentColor = accentColor,
                            onCheckedChange = { onUpdateWidgetToggles(null, null, null, null, null, it) }
                        )
                    }

                    // TAB 4: GESTURES
                    SettingsTab.GESTURES -> {
                        Text(
                            text = "Fluid Gesture Mapping",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Configure instant gestures from anywhere on the home screen",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        GestureSelectorRow(
                            icon = Icons.Rounded.SwipeDown,
                            label = "Swipe Down",
                            currentAction = settings.swipeDownAction,
                            accentColor = accentColor,
                            onSelect = { onUpdateGestures(null, it, null, null, null) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        GestureSelectorRow(
                            icon = Icons.Rounded.SwipeUp,
                            label = "Swipe Up",
                            currentAction = settings.swipeUpAction,
                            accentColor = accentColor,
                            onSelect = { onUpdateGestures(it, null, null, null, null) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        GestureSelectorRow(
                            icon = Icons.Rounded.TouchApp,
                            label = "Double Tap",
                            currentAction = settings.doubleTapAction,
                            accentColor = accentColor,
                            onSelect = { onUpdateGestures(null, null, it, null, null) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        GestureSelectorRow(
                            icon = Icons.Rounded.Gesture,
                            label = "Pinch Screen",
                            currentAction = settings.pinchAction,
                            accentColor = accentColor,
                            onSelect = { onUpdateGestures(null, null, null, it, null) }
                        )
                    }

                    // TAB 5: WALLPAPERS
                    SettingsTab.WALLPAPERS -> {
                        Text(
                            text = "Clean Minimal Wallpapers",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Non-spacy, elegant frosted liquid glass wallpapers",
                            color = TextSecondary,
                            fontSize = 11.5.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                WallpaperThumb(
                                    title = "Clean Frost",
                                    resId = R.drawable.wallpaper_minimal_frost_1793997930230,
                                    isSelected = settings.wallpaperType == WallpaperType.MINIMAL_FROST && settings.customWallpaperUri == null,
                                    accentColor = accentColor,
                                    onClick = { onUpdateWallpaper(WallpaperType.MINIMAL_FROST, null) },
                                    modifier = Modifier.width(105.dp)
                                )
                            }

                            item {
                                WallpaperThumb(
                                    title = "Liquid Indigo",
                                    resId = R.drawable.wallpaper_minimal_indigo_1793997945300,
                                    isSelected = settings.wallpaperType == WallpaperType.MINIMAL_INDIGO && settings.customWallpaperUri == null,
                                    accentColor = accentColor,
                                    onClick = { onUpdateWallpaper(WallpaperType.MINIMAL_INDIGO, null) },
                                    modifier = Modifier.width(105.dp)
                                )
                            }

                            item {
                                ProceduralWallpaperThumb(
                                    title = "Pure Slate",
                                    gradient = listOf(Color(0xFF1B202E), Color(0xFF111420), Color(0xFF0C0F17)),
                                    isSelected = settings.wallpaperType == WallpaperType.CLEAN_SLATE && settings.customWallpaperUri == null,
                                    accentColor = accentColor,
                                    onClick = { onUpdateWallpaper(WallpaperType.CLEAN_SLATE, null) },
                                    modifier = Modifier.width(105.dp)
                                )
                            }

                            item {
                                ProceduralWallpaperThumb(
                                    title = "Silk Minimal",
                                    gradient = listOf(Color(0xFF282C44), Color(0xFF171A2E), Color(0xFF0F1122)),
                                    isSelected = settings.wallpaperType == WallpaperType.SILK_AURA && settings.customWallpaperUri == null,
                                    accentColor = accentColor,
                                    onClick = { onUpdateWallpaper(WallpaperType.SILK_AURA, null) },
                                    modifier = Modifier.width(105.dp)
                                )
                            }

                            item {
                                Box(
                                    modifier = Modifier
                                        .width(105.dp)
                                        .height(90.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0x30FFFFFF))
                                        .border(
                                            1.5.dp,
                                            if (settings.customWallpaperUri != null) accentColor else Color.Transparent,
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Rounded.AddPhotoAlternate,
                                            contentDescription = "Pick Photo",
                                            tint = if (settings.customWallpaperUri != null) accentColor else TextSecondary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (settings.customWallpaperUri != null) "Active Custom" else "Pick Photo",
                                            color = if (settings.customWallpaperUri != null) accentColor else TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // TAB 6: COMET AI & VOICE
                    SettingsTab.COMET_AI -> {
                        Text(
                            text = "Comet AI & Voice Assistant",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Intelligent launcher assistant with 'Hey Comet' wake word",
                            color = TextSecondary,
                            fontSize = 11.5.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Wake word toggle
                        SettingsToggleRow(
                            title = "\"Hey Comet\" Wake Word",
                            subtitle = "Ambient voice trigger to wake up Comet AI instantly",
                            checked = settings.enableWakeWord,
                            accentColor = accentColor,
                            onCheckedChange = { onUpdateWakeWord(it) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Reopen Setup Wizard button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(accentColor.copy(alpha = 0.20f))
                                .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .clickable {
                                    onDismiss()
                                    onReopenSetup()
                                }
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.RocketLaunch, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = "Relaunch Setup Wizard", color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "Reconfigure initial launcher preferences step-by-step", color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Daily Step Goal",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Target steps", color = TextSecondary, fontSize = 12.sp)
                            Text(text = "${settings.dailyStepGoal} steps", color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = settings.dailyStepGoal.toFloat(),
                            onValueChange = { onUpdateDailyStepGoal(it.toInt()) },
                            valueRange = 4000f..20000f,
                            steps = 15,
                            colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Default Music App selector
                        Text(
                            text = "Default Music Player for Widget",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (installedMusicApps.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(installedMusicApps) { app ->
                                    val isSelected = settings.defaultMusicAppPackage == app.packageName
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) accentColor.copy(alpha = 0.35f) else Color(0x20FFFFFF))
                                            .clickable { onUpdateDefaultMusicApp(app.packageName) }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = app.label,
                                            color = if (isSelected) TextPrimary else TextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable { ActiveMediaManager.openNotificationAccessSettings(context) }
                                .padding(vertical = 11.dp, horizontal = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.MusicNote,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Open Media Session Notification Access",
                                        color = TextPrimary,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Allows Comet to display currently playing songs in real time",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    accentColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x18FFFFFF))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(text = title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = TextSecondary, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accentColor,
                uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
            )
        )
    }
}

@Composable
private fun GestureSelectorRow(
    icon: ImageVector,
    label: String,
    currentAction: GestureAction,
    accentColor: Color,
    onSelect: (GestureAction) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(GestureAction.values()) { action ->
                val isSelected = action == currentAction
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) accentColor.copy(alpha = 0.35f) else Color(0x18FFFFFF))
                        .clickable { onSelect(action) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = action.displayName,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun WallpaperThumb(
    title: String,
    resId: Int,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(90.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.5.dp,
                if (isSelected) accentColor else Color.Transparent,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.25f))
        )
        Text(
            text = title,
            color = Color.White,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
        )
    }
}

@Composable
private fun ProceduralWallpaperThumb(
    title: String,
    gradient: List<Color>,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(90.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(gradient))
            .border(
                1.5.dp,
                if (isSelected) accentColor else Color.Transparent,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
    }
}
