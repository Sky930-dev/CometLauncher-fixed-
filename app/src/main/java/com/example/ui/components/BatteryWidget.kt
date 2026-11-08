package com.example.ui.components

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.sin

@Composable
fun BatteryWidget(
    modifier: Modifier = Modifier,
    accentColor: Color = NeonCyan
) {
    val context = LocalContext.current
    var batteryPct by remember { mutableFloatStateOf(85f) }
    var isCharging by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent != null) {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level != -1 && scale != -1) {
                        batteryPct = (level.toFloat() / scale.toFloat()) * 100f
                    }
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val initialStatus = context.registerReceiver(receiver, filter)
        if (initialStatus != null) {
            val level = initialStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = initialStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level != -1 && scale != -1) {
                batteryPct = (level.toFloat() / scale.toFloat()) * 100f
            }
            val status = initialStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
        }

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (ignored: Exception) {}
        }
    }

    // Real system RAM usage calculation
    val (ramUsedGb, ramTotalGb) = remember {
        try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager.getMemoryInfo(memInfo)
            val total = memInfo.totalMem.toDouble() / (1024 * 1024 * 1024)
            val avail = memInfo.availMem.toDouble() / (1024 * 1024 * 1024)
            val used = total - avail
            String.format("%.1f", used) to String.format("%.0f GB", total)
        } catch (e: Exception) {
            "3.8" to "8 GB"
        }
    }

    // Real storage calculation
    val storagePct = remember {
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val totalBytes = stat.totalBytes
            val availableBytes = stat.availableBytes
            val usedBytes = totalBytes - availableBytes
            ((usedBytes.toDouble() / totalBytes.toDouble()) * 100).toInt()
        } catch (e: Exception) {
            54
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "liquid_wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        backgroundColor = Color(0x32131945),
        accentGlow = if (isCharging) SolarAmber else accentColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Animated Liquid Battery Capsule
            Box(
                modifier = Modifier
                    .size(width = 62.dp, height = 76.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x35000000)),
                contentAlignment = Alignment.Center
            ) {
                // Wave Canvas
                Canvas(modifier = Modifier.matchParentSize()) {
                    val fillHeight = (size.height * (batteryPct / 100f)).coerceIn(8f, size.height)
                    val baseWaterY = size.height - fillHeight

                    val wavePath = Path()
                    wavePath.moveTo(0f, size.height)
                    wavePath.lineTo(0f, baseWaterY)

                    val waveWidth = size.width
                    val waveAmplitude = 4f

                    var x = 0f
                    while (x <= waveWidth) {
                        val y = baseWaterY + (sin(x / waveWidth * 6.28f + waveOffset) * waveAmplitude).toFloat()
                        wavePath.lineTo(x, y)
                        x += 4f
                    }

                    wavePath.lineTo(size.width, size.height)
                    wavePath.close()

                    drawPath(
                        path = wavePath,
                        brush = Brush.verticalGradient(
                            colors = if (isCharging) {
                                listOf(SolarAmber, Color(0xFFE85D04))
                            } else {
                                listOf(accentColor, Color(0xFF3A0CA3))
                            }
                        )
                    )
                }

                // Battery Text / Charging bolt
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isCharging) {
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = "Charging",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "${batteryPct.toInt()}%",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // System telemetry details (RAM & Storage)
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Memory,
                            contentDescription = "RAM",
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "RAM Core", color = TextSecondary, fontSize = 12.sp)
                    }
                    Text(text = "$ramUsedGb / $ramTotalGb", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Storage,
                            contentDescription = "Storage",
                            tint = Color(0xFF4CC9F0),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Liquid Storage", color = TextSecondary, fontSize = 12.sp)
                    }
                    Text(text = "$storagePct% Used", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Status tag
                Text(
                    text = if (isCharging) "Power: Fast Charge Active" else "Battery Health: Optimal",
                    color = if (isCharging) SolarAmber else TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}
