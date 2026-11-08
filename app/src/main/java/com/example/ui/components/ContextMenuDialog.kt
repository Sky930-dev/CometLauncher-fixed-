package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppInfo
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextMenuDialog(
    app: AppInfo,
    isPinnedToDock: Boolean,
    isPinnedToHome: Boolean,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onToggleDock: () -> Unit,
    onToggleHome: () -> Unit,
    onAddToFolder: () -> Unit,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit,
    accentColor: Color = NeonCyan
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        LiquidGlassCard(
            shape = RoundedCornerShape(28.dp),
            backgroundColor = Color(0xF20E1438),
            borderColor = accentColor.copy(alpha = 0.5f),
            accentGlow = accentColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with App Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x33FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (app.iconBitmap != null) {
                            Image(
                                bitmap = app.iconBitmap.asImageBitmap(),
                                contentDescription = app.label,
                                modifier = Modifier.size(52.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Android,
                                contentDescription = app.label,
                                tint = accentColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.label,
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = app.packageName,
                            color = TextSecondary,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))

                // Menu items
                MenuItemRow(
                    icon = Icons.Rounded.OpenInNew,
                    title = "Launch Application",
                    accent = accentColor,
                    onClick = {
                        onDismiss()
                        onLaunch()
                    }
                )

                MenuItemRow(
                    icon = Icons.Rounded.PushPin,
                    title = if (isPinnedToDock) "Remove from Dock" else "Pin to Dock",
                    accent = accentColor,
                    onClick = {
                        onDismiss()
                        onToggleDock()
                    }
                )

                MenuItemRow(
                    icon = Icons.Rounded.AddCircleOutline,
                    title = if (isPinnedToHome) "Remove from Home" else "Add to Home Screen",
                    accent = accentColor,
                    onClick = {
                        onDismiss()
                        onToggleHome()
                    }
                )

                MenuItemRow(
                    icon = Icons.Rounded.Folder,
                    title = "Add to Folder",
                    accent = accentColor,
                    onClick = {
                        onDismiss()
                        onAddToFolder()
                    }
                )

                MenuItemRow(
                    icon = Icons.Rounded.Info,
                    title = "App Information",
                    accent = accentColor,
                    onClick = {
                        onDismiss()
                        onAppInfo()
                    }
                )

                if (!app.isSystemApp) {
                    MenuItemRow(
                        icon = Icons.Rounded.Delete,
                        title = "Uninstall",
                        accent = Color(0xFFFF4D6D),
                        onClick = {
                            onDismiss()
                            onUninstall()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuItemRow(
    icon: ImageVector,
    title: String,
    accent: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = accent,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
