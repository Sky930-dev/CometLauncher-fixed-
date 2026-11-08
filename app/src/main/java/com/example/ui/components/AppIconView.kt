package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppInfo
import com.example.model.IconShape
import com.example.model.IconThemeStyle
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIconView(
    app: AppInfo,
    modifier: Modifier = Modifier,
    iconSize: Dp = 54.dp,
    showLabel: Boolean = true,
    labelFontSize: TextUnit = 11.5.sp,
    iconShape: IconShape = IconShape.SQUIRCLE,
    iconTheme: IconThemeStyle = IconThemeStyle.LIQUID_GLASS,
    accentColor: Color = NeonCyan,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
        label = "icon_press_scale"
    )

    val shape = when (iconShape) {
        IconShape.SQUIRCLE -> RoundedCornerShape(16.dp)
        IconShape.CIRCLE -> CircleShape
        IconShape.ROUNDED_SQUARE -> RoundedCornerShape(10.dp)
        IconShape.PEBBLE -> RoundedCornerShape(22.dp)
    }

    Column(
        modifier = modifier
            .testTag("app_icon_${app.packageName}")
            .scale(scale)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .shadow(
                    elevation = if (iconTheme == IconThemeStyle.LIQUID_GLASS) 8.dp else 4.dp,
                    shape = shape,
                    spotColor = accentColor.copy(alpha = 0.35f)
                )
                .clip(shape)
                .then(
                    when (iconTheme) {
                        IconThemeStyle.LIQUID_GLASS -> {
                            Modifier
                                .drawBehind {
                                    // Specular refraction line on the top rim
                                    drawLine(
                                        brush = Brush.horizontalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.70f),
                                                accentColor.copy(alpha = 0.40f),
                                                Color.Transparent
                                            )
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width * 0.75f, 0f),
                                        strokeWidth = 3f
                                    )
                                    // Subtle diagonal meniscus glare sweep
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.25f),
                                                Color.Transparent
                                            ),
                                            center = Offset(size.width * 0.25f, size.height * 0.25f),
                                            radius = size.width * 0.5f
                                        )
                                    )
                                }
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0x40FFFFFF),
                                            Color(0x15FFFFFF),
                                            Color(0x280D1230)
                                        )
                                    )
                                )
                                .border(
                                    width = 1.2.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.65f),
                                            accentColor.copy(alpha = 0.50f),
                                            Color.White.copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = shape
                                )
                        }

                        IconThemeStyle.FROSTED_PEBBLE -> {
                            Modifier
                                .background(Color(0x35FFFFFF))
                                .border(
                                    width = 1.dp,
                                    brush = Brush.linearGradient(
                                        listOf(Color.White.copy(alpha = 0.4f), Color.Transparent)
                                    ),
                                    shape = shape
                                )
                        }

                        IconThemeStyle.TINTED_NEON -> {
                            Modifier
                                .background(Color(0x550A0E25))
                                .border(
                                    width = 1.5.dp,
                                    brush = Brush.linearGradient(
                                        listOf(accentColor, accentColor.copy(alpha = 0.4f))
                                    ),
                                    shape = shape
                                )
                        }

                        IconThemeStyle.ORIGINAL -> {
                            Modifier
                                .background(Color(0x20FFFFFF))
                                .border(
                                    width = 0.8.dp,
                                    brush = Brush.linearGradient(
                                        listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)
                                    ),
                                    shape = shape
                                )
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            val contentScaleFactor = if (iconTheme == IconThemeStyle.LIQUID_GLASS) 0.82f else 1f

            if (app.iconBitmap != null) {
                Image(
                    bitmap = app.iconBitmap.asImageBitmap(),
                    contentDescription = app.label,
                    modifier = Modifier
                        .size(iconSize * contentScaleFactor)
                        .clip(if (iconTheme == IconThemeStyle.LIQUID_GLASS) RoundedCornerShape(10.dp) else shape)
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Android,
                    contentDescription = app.label,
                    tint = accentColor,
                    modifier = Modifier.size(iconSize * 0.65f)
                )
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = app.label,
                color = TextPrimary,
                fontSize = labelFontSize,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
