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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppInfo
import com.example.model.Folder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderIconView(
    folder: Folder,
    appsInFolder: List<AppInfo>,
    modifier: Modifier = Modifier,
    iconSize: Dp = 54.dp,
    accentColor: Color = NeonCyan,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
        label = "folder_scale"
    )

    val shape = RoundedCornerShape(18.dp)

    Column(
        modifier = modifier
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
                .shadow(elevation = 6.dp, shape = shape, spotColor = accentColor.copy(alpha = 0.35f))
                .clip(shape)
                .background(Color(0x35171D47))
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.45f),
                            accentColor.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    ),
                    shape = shape
                )
                .padding(5.dp),
            contentAlignment = Alignment.Center
        ) {
            if (appsInFolder.isEmpty()) {
                Icon(
                    imageVector = Icons.Rounded.Folder,
                    contentDescription = folder.name,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                // 2x2 grid of mini preview icons
                val previewApps = appsInFolder.take(4)
                Column(
                    modifier = Modifier.matchParentSize(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (i in 0 until 2) {
                            if (i < previewApps.size && previewApps[i].iconBitmap != null) {
                                Image(
                                    bitmap = previewApps[i].iconBitmap!!.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(1.5.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                            } else {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (i in 2 until 4) {
                            if (i < previewApps.size && previewApps[i].iconBitmap != null) {
                                Image(
                                    bitmap = previewApps[i].iconBitmap!!.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(1.5.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                            } else {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = folder.name,
            color = TextPrimary,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
