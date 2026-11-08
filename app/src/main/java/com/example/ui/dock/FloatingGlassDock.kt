package com.example.ui.dock

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.model.AppInfo
import com.example.model.DockStyle
import com.example.model.IconShape
import com.example.ui.components.AppIconView
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan

@Composable
fun FloatingGlassDock(
    dockApps: List<AppInfo>,
    iconShape: IconShape,
    accentColor: Color = NeonCyan,
    dockStyle: DockStyle = DockStyle.GLASS,
    showAiButton: Boolean = true,
    glassCornerRadiusDp: Int = 32,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenComet: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dock_float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    val dockShape = RoundedCornerShape(glassCornerRadiusDp.dp)

    val (dockBgColor, dockBorderColor) = when (dockStyle) {
        DockStyle.GLASS -> Color(0x3812163E) to Color.White.copy(alpha = 0.5f)
        DockStyle.TRANSLUCENT -> Color(0x1AFFFFFF) to Color.White.copy(alpha = 0.25f)
        DockStyle.OUTLINE_ONLY -> Color.Transparent to accentColor.copy(alpha = 0.7f)
        DockStyle.MINIMAL_FLOAT -> Color(0x550B1028) to accentColor.copy(alpha = 0.35f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassCard(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .offset(y = if (dockStyle == DockStyle.MINIMAL_FLOAT) 0.dp else floatOffset.dp)
                .shadow(
                    elevation = if (dockStyle == DockStyle.OUTLINE_ONLY) 6.dp else 16.dp,
                    shape = dockShape,
                    spotColor = accentColor.copy(alpha = 0.45f)
                ),
            shape = dockShape,
            backgroundColor = dockBgColor,
            borderColor = dockBorderColor,
            accentGlow = if (dockStyle == DockStyle.OUTLINE_ONLY) Color.Transparent else accentColor
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pinned Dock Apps (up to 4)
                dockApps.take(4).forEach { app ->
                    AppIconView(
                        app = app,
                        iconSize = 48.dp,
                        showLabel = false,
                        iconShape = iconShape,
                        accentColor = accentColor,
                        onClick = { onAppClick(app) },
                        onLongClick = { onAppLongClick(app) }
                    )
                }

                // Comet Siri AI Orb Trigger Button
                if (showAiButton) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.radialGradient(
                                    listOf(accentColor.copy(alpha = 0.6f), ElectricViolet.copy(alpha = 0.45f), Color(0x22FFFFFF))
                                )
                            )
                            .clickable(onClick = onOpenComet),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "Ask Comet AI",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // App Drawer trigger button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.radialGradient(
                                listOf(accentColor.copy(alpha = 0.35f), Color(0x22FFFFFF))
                            )
                        )
                        .clickable(onClick = onOpenDrawer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Apps,
                        contentDescription = "All Applications",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
