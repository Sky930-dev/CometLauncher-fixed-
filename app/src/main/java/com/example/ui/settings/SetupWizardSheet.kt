package com.example.ui.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.IconShape
import com.example.model.IconThemeStyle
import com.example.model.LauncherSettings
import com.example.model.WidgetIds
import com.example.ui.components.AccentColors
import com.example.ui.components.AccentNames
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SetupWizardSheet(
    visible: Boolean,
    currentSettings: LauncherSettings,
    accentColor: Color = NeonCyan,
    onCompleteSetup: (LauncherSettings) -> Unit
) {
    if (!visible) return

    var currentStep by remember { mutableIntStateOf(0) }

    var selectedAccentIndex by remember { mutableIntStateOf(currentSettings.accentColorIndex) }
    var glassOpacity by remember { mutableFloatStateOf(currentSettings.glassOpacity) }
    var blurIntensity by remember { mutableIntStateOf(currentSettings.glassBlurIntensity) }
    var bendCurvature by remember { mutableFloatStateOf(currentSettings.glassBendCurvature) }
    var iconTheme by remember { mutableStateOf(currentSettings.iconTheme) }
    var iconShape by remember { mutableStateOf(currentSettings.iconShape) }
    var gridColumns by remember { mutableIntStateOf(currentSettings.gridColumns) }
    var enableWakeWord by remember { mutableStateOf(currentSettings.enableWakeWord) }
    var enabledWidgets by remember { mutableStateOf(currentSettings.enabledWidgets) }

    val activeAccent = AccentColors.getOrElse(selectedAccentIndex) { NeonCyan }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xF5080C26),
                        Color(0xFA0E1538),
                        Color(0xFF090D24)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Wizard Header
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(activeAccent.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = activeAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Welcome to Comet",
                                color = TextPrimary,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Personalize your Liquid Glass experience",
                                color = TextSecondary,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    // Step Indicator
                    Text(
                        text = "Step ${currentStep + 1} of 4",
                        color = activeAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 0..3) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (i <= currentStep) activeAccent else Color.White.copy(alpha = 0.15f))
                        )
                    }
                }
            }

            // Step Content Animated
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                    label = "wizard_step"
                ) { step ->
                    when (step) {
                        0 -> StepVisualsAndOptics(
                            selectedAccentIndex = selectedAccentIndex,
                            glassOpacity = glassOpacity,
                            blurIntensity = blurIntensity,
                            bendCurvature = bendCurvature,
                            activeAccent = activeAccent,
                            onAccentChange = { selectedAccentIndex = it },
                            onOpacityChange = { glassOpacity = it },
                            onBlurChange = { blurIntensity = it },
                            onBendChange = { bendCurvature = it }
                        )

                        1 -> StepIconAesthetics(
                            iconTheme = iconTheme,
                            iconShape = iconShape,
                            activeAccent = activeAccent,
                            onThemeChange = { iconTheme = it },
                            onShapeChange = { iconShape = it }
                        )

                        2 -> StepWidgetsAndGrid(
                            enabledWidgets = enabledWidgets,
                            gridColumns = gridColumns,
                            activeAccent = activeAccent,
                            onToggleWidget = { id, enabled ->
                                enabledWidgets = if (enabled) enabledWidgets + id else enabledWidgets - id
                            },
                            onGridChange = { gridColumns = it }
                        )

                        3 -> StepCometAiWakeWord(
                            enableWakeWord = enableWakeWord,
                            activeAccent = activeAccent,
                            onWakeWordToggle = { enableWakeWord = it }
                        )
                    }
                }
            }

            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.10f))
                            .clickable { currentStep-- }
                            .padding(horizontal = 18.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Back", color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(10.dp))
                }

                if (currentStep < 3) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(activeAccent)
                            .clickable { currentStep++ }
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Next Step", color = Color.Black, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Rounded.ArrowForward, contentDescription = "Next", tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(activeAccent, Color(0xFF00F5D4))
                                )
                            )
                            .clickable {
                                onCompleteSetup(
                                    currentSettings.copy(
                                        accentColorIndex = selectedAccentIndex,
                                        glassOpacity = glassOpacity,
                                        glassBlurIntensity = blurIntensity,
                                        glassBendCurvature = bendCurvature,
                                        iconTheme = iconTheme,
                                        iconShape = iconShape,
                                        gridColumns = gridColumns,
                                        enableWakeWord = enableWakeWord,
                                        enabledWidgets = enabledWidgets,
                                        isSetupCompleted = true
                                    )
                                )
                            }
                            .padding(horizontal = 26.dp, vertical = 13.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.RocketLaunch, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Dive into Comet Experience", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepVisualsAndOptics(
    selectedAccentIndex: Int,
    glassOpacity: Float,
    blurIntensity: Int,
    bendCurvature: Float,
    activeAccent: Color,
    onAccentChange: (Int) -> Unit,
    onOpacityChange: (Float) -> Unit,
    onBlurChange: (Int) -> Unit,
    onBendChange: (Float) -> Unit
) {
    Column {
        Text(text = "Optical Glass & Refraction", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Customize the lens curvature, specular bend, and glow tint", color = TextSecondary, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(14.dp))

        // Live preview of custom liquid glass tile
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            accentGlow = activeAccent,
            blurIntensity = blurIntensity,
            bendCurvature = bendCurvature
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Live Liquid Glass Preview", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Optical curvature bend: ${(bendCurvature * 100).toInt()}% • Blur: $blurIntensity", color = activeAccent, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Accent Colors
        Text(text = "Glow Accent Tint", color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            itemsIndexed(AccentColors) { idx, col ->
                val isSelected = idx == selectedAccentIndex
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(col)
                        .border(2.dp, if (isSelected) Color.White else Color.Transparent, CircleShape)
                        .clickable { onAccentChange(idx) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bend Curvature Slider
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Glass Meniscus Bend Curvature", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(text = "${(bendCurvature * 100).toInt()}%", color = activeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = bendCurvature,
            onValueChange = onBendChange,
            valueRange = 0.1f..1.0f,
            colors = SliderDefaults.colors(thumbColor = activeAccent, activeTrackColor = activeAccent)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Blur Intensity Slider
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Backdrop Frosted Blur Depth", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(text = "Level $blurIntensity", color = activeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = blurIntensity.toFloat(),
            onValueChange = { onBlurChange(it.toInt()) },
            valueRange = 1f..5f,
            steps = 3,
            colors = SliderDefaults.colors(thumbColor = activeAccent, activeTrackColor = activeAccent)
        )
    }
}

@Composable
private fun StepIconAesthetics(
    iconTheme: IconThemeStyle,
    iconShape: IconShape,
    activeAccent: Color,
    onThemeChange: (IconThemeStyle) -> Unit,
    onShapeChange: (IconShape) -> Unit
) {
    Column {
        Text(text = "App Icon Style", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Make app icons look like liquid glass tiles or original icons", color = TextSecondary, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(14.dp))

        IconThemeStyle.values().forEach { style ->
            val isSelected = style == iconTheme
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) activeAccent.copy(alpha = 0.25f) else Color(0x18FFFFFF))
                    .border(1.dp, if (isSelected) activeAccent else Color.Transparent, RoundedCornerShape(14.dp))
                    .clickable { onThemeChange(style) }
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) activeAccent else Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = style.displayName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = style.description, color = TextSecondary, fontSize = 11.5.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Tile Geometric Cutout", color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            IconShape.values().forEach { shape ->
                val isSelected = shape == iconShape
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) activeAccent.copy(alpha = 0.35f) else Color(0x18FFFFFF))
                        .clickable { onShapeChange(shape) }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = shape.displayName, color = if (isSelected) TextPrimary else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun StepWidgetsAndGrid(
    enabledWidgets: Set<String>,
    gridColumns: Int,
    activeAccent: Color,
    onToggleWidget: (String, Boolean) -> Unit,
    onGridChange: (Int) -> Unit
) {
    Column {
        Text(text = "Active Widgets & Grid", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Choose which interactive widgets are placed on your home screen", color = TextSecondary, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(14.dp))

        val availableWidgets = listOf(
            WidgetIds.CLOCK to "Futuristic Clock & Alarms",
            WidgetIds.WEATHER to "Real-time Weather & Forecast",
            WidgetIds.STEPS to "Step Counter & Fitness Goal",
            WidgetIds.BATTERY to "Battery & System Health",
            WidgetIds.MUSIC to "Active Now-Playing Media Player",
            WidgetIds.QUICK_TOGGLES to "Control Island Quick Toggles",
            WidgetIds.QUICK_NOTES to "Liquid Scratchpad Quick Notes"
        )

        availableWidgets.forEach { (id, label) ->
            val isEnabled = enabledWidgets.contains(id)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x18FFFFFF))
                    .clickable { onToggleWidget(id, !isEnabled) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = label, color = TextPrimary, fontSize = 13.sp)
                Checkbox(
                    checked = isEnabled,
                    onCheckedChange = { onToggleWidget(id, it) },
                    colors = CheckboxDefaults.colors(checkedColor = activeAccent, checkmarkColor = Color.Black)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(text = "Home Grid Columns", color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(3, 4, 5, 6).forEach { cols ->
                val isSelected = cols == gridColumns
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) activeAccent.copy(alpha = 0.35f) else Color(0x18FFFFFF))
                        .clickable { onGridChange(cols) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "$cols Cols", color = if (isSelected) TextPrimary else TextSecondary, fontSize = 11.5.sp)
                }
            }
        }
    }
}

@Composable
private fun StepCometAiWakeWord(
    enableWakeWord: Boolean,
    activeAccent: Color,
    onWakeWordToggle: (Boolean) -> Unit
) {
    Column {
        Text(text = "Comet AI & Voice Control", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Hands-free launcher control with 'Hey Comet'", color = TextSecondary, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(16.dp))

        LiquidGlassCard(
            shape = RoundedCornerShape(20.dp),
            backgroundColor = Color(0x35121844),
            borderColor = activeAccent.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(activeAccent.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Mic, contentDescription = null, tint = activeAccent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "\"Hey Comet\" Wake Word", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Say 'Hey Comet' anytime to summon AI", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Switch(
                        checked = enableWakeWord,
                        onCheckedChange = onWakeWordToggle,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = activeAccent)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Commands you can say:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                listOf(
                    "\"Hey Comet, open Spotify\"",
                    "\"Hey Comet, how many steps did I take?\"",
                    "\"Hey Comet, what's my morning briefing?\"",
                    "\"Hey Comet, turn on flashlight\""
                ).forEach { example ->
                    Text(text = "• $example", color = activeAccent, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        }
    }
}
