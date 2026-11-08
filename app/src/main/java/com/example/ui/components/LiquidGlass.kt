package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassBorderHighlight
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.NeonCyan

val AccentColors = listOf(
    NeonCyan,
    Color(0xFF9D4EDD), // Electric Violet
    Color(0xFF00F5D4), // Mint Cyan
    Color(0xFFFF007A), // Cyber Pink
    Color(0xFFFFB703), // Solar Amber
    Color(0xFF4CC9F0), // Starlight Blue
    Color(0xFFFF5E57)  // Coral Sun
)

val AccentNames = listOf(
    "Aurora Cyan",
    "Nebula Violet",
    "Cyber Mint",
    "Neon Fuchsia",
    "Solar Flare",
    "Starlight Blue",
    "Coral Sun"
)

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = Color(0x2414193F),
    borderColor: Color = GlassBorderHighlight,
    accentGlow: Color? = null,
    borderWidth: Dp = 1.dp,
    blurIntensity: Int = 3,         // 1 to 5
    bendCurvature: Float = 0.65f,   // 0.0 to 1.0 (refraction bend)
    specularity: Float = 0.70f,     // 0.0 to 1.0 (specular gloss)
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.965f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 420f),
        label = "press_scale"
    )

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else Modifier

    val elevation = (blurIntensity * 3.5f + 4f).dp

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = shape,
                spotColor = (accentGlow ?: borderColor).copy(alpha = 0.25f + blurIntensity * 0.05f),
                ambientColor = Color.Black.copy(alpha = 0.45f)
            )
            .clip(shape)
            .drawBehind {
                val w = size.width
                val h = size.height

                // 1. Layered frosted blur simulation: multi-stage radial diffuse light
                if (accentGlow != null) {
                    val glowRadius = w * (0.5f + blurIntensity * 0.10f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentGlow.copy(alpha = 0.15f + blurIntensity * 0.04f),
                                accentGlow.copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.85f, h * 0.15f),
                            radius = glowRadius
                        )
                    )
                }

                // 2. Optical Liquid Glass "Bend" Curvature (Refraction Arc)
                // A smooth refractive caustic arc representing the curved glass meniscus lens
                if (bendCurvature > 0.05f) {
                    val bendPath = Path().apply {
                        moveTo(0f, h * 0.35f * bendCurvature)
                        cubicTo(
                            w * 0.35f, h * 0.05f * bendCurvature,
                            w * 0.65f, h * 0.50f * bendCurvature,
                            w, h * 0.20f * bendCurvature
                        )
                    }

                    drawPath(
                        path = bendPath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.40f * specularity),
                                (accentGlow ?: Color.White).copy(alpha = 0.25f * bendCurvature),
                                Color.Transparent
                            )
                        ),
                        style = Stroke(width = 2.5f + bendCurvature * 2f)
                    )
                }

                // 3. Top-left Specular Glass Glare / Gloss Refraction Line
                val specAlpha = 0.55f * specularity
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = specAlpha),
                            Color.White.copy(alpha = specAlpha * 0.4f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(w * (0.6f + bendCurvature * 0.3f), 0f),
                    strokeWidth = 2.8f
                )

                // 4. Subtle inner depth rim for realistic glass thickness
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.12f * specularity),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.18f)
                        )
                    ),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
                )
            }
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = (backgroundColor.alpha * 1.15f).coerceAtMost(0.70f)),
                        backgroundColor.copy(alpha = (backgroundColor.alpha * 0.75f).coerceAtLeast(0.10f))
                    )
                )
            )
            .border(
                BorderStroke(
                    borderWidth,
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = (0.50f * specularity).coerceIn(0.2f, 0.8f)),
                            borderColor.copy(alpha = 0.65f),
                            accentGlow?.copy(alpha = 0.45f) ?: GlassBorderSubtle,
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.04f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                ),
                shape = shape
            )
            .then(clickableModifier),
        content = content
    )
}

@Composable
fun LiquidGlassPill(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0x3018204A),
    borderColor: Color = GlassBorderHighlight,
    accentGlow: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    LiquidGlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(percent = 50),
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        accentGlow = accentGlow,
        onClick = onClick,
        content = content
    )
}
