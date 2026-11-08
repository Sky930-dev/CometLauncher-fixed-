package com.example.ui.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AssistantStatus
import com.example.ai.CometAiManager
import com.example.ai.CometMessage
import com.example.model.AppInfo
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.CyberPink
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CometSiriSheet(
    visible: Boolean,
    aiManager: CometAiManager,
    allInstalledApps: List<AppInfo>,
    accentColor: Color = NeonCyan,
    onLaunchApp: (AppInfo) -> Unit = {},
    onDismiss: () -> Unit
) {
    if (!visible) return

    val status by aiManager.status.collectAsState()
    val messages by aiManager.messages.collectAsState()
    val streamingText by aiManager.currentStreamingText.collectAsState()
    val isMuted by aiManager.isTtsMuted.collectAsState()

    var inputPrompt by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, streamingText) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE6070B1F))
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Chat Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Mini glowing Siri Orb avatar
                        MiniGlowingOrb(status = status, accentColor = accentColor)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Comet AI",
                                    color = TextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(accentColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Gemini 3.8",
                                        color = accentColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = when (status) {
                                    AssistantStatus.LISTENING -> "Listening to your voice..."
                                    AssistantStatus.THINKING -> "Thinking..."
                                    AssistantStatus.SPEAKING -> "Speaking..."
                                    else -> "Ready for messages & commands"
                                },
                                color = if (status == AssistantStatus.LISTENING) accentColor else TextSecondary,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    // Top Action Icons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Clear chat history
                        IconButton(
                            onClick = { aiManager.clearHistory() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ClearAll,
                                contentDescription = "Clear Chat",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Voice speech toggle
                        IconButton(
                            onClick = { aiManager.toggleMuteTts() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
                                contentDescription = "Voice Output",
                                tint = if (isMuted) TextSecondary else accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Close button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Chat History Messages
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        ChatMessageItem(
                            message = msg,
                            accentColor = accentColor,
                            onLaunchApp = onLaunchApp,
                            onSpeakMessage = { aiManager.speakOutLoud(msg.text) }
                        )
                    }

                    // Streaming Response Chunk
                    if (streamingText.isNotBlank()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.90f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0x35192255))
                                        .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = streamingText,
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        CircularProgressIndicator(
                                            color = accentColor,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Suggestion Chips Carousel
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val suggestions = listOf(
                        "Open Spotify",
                        "Open Camera",
                        "What's the weather today?",
                        "Play music",
                        "Tell me a fun fact",
                        "Open Settings",
                        "Pause music",
                        "What can you do?"
                    )
                    items(suggestions) { s ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x22FFFFFF))
                                .clickable {
                                    aiManager.askComet(s, allInstalledApps)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = s, color = TextPrimary, fontSize = 11.5.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Chat Input Bar with Voice & Send
                LiquidGlassCard(
                    shape = RoundedCornerShape(26.dp),
                    backgroundColor = Color(0x38121844),
                    borderColor = accentColor.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Microphone Voice Dictation Button with glowing animation
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    if (status == AssistantStatus.LISTENING) {
                                        accentColor.copy(alpha = 0.4f)
                                    } else {
                                        Color.White.copy(alpha = 0.1f)
                                    }
                                )
                                .clickable {
                                    if (status == AssistantStatus.LISTENING) {
                                        aiManager.stopListening()
                                    } else if (status == AssistantStatus.SPEAKING) {
                                        aiManager.stopSpeaking()
                                    } else {
                                        aiManager.startListening()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (status) {
                                    AssistantStatus.LISTENING -> Icons.Rounded.Stop
                                    AssistantStatus.SPEAKING -> Icons.Rounded.Stop
                                    else -> Icons.Rounded.Mic
                                },
                                contentDescription = "Voice Input",
                                tint = if (status == AssistantStatus.LISTENING) accentColor else Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Text Field for Normal Typing Messages
                        OutlinedTextField(
                            value = inputPrompt,
                            onValueChange = { inputPrompt = it },
                            placeholder = {
                                Text(
                                    text = when (status) {
                                        AssistantStatus.LISTENING -> "Listening to your voice..."
                                        AssistantStatus.THINKING -> "Comet is thinking..."
                                        AssistantStatus.SPEAKING -> "Comet is talking..."
                                        else -> "Ask Comet or type a message..."
                                    },
                                    color = TextSecondary,
                                    fontSize = 13.5.sp
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        // Send Button
                        if (inputPrompt.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                                    .clickable {
                                        val text = inputPrompt
                                        inputPrompt = ""
                                        aiManager.askComet(text, allInstalledApps)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Send,
                                    contentDescription = "Send Message",
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: CometMessage,
    accentColor: Color,
    onLaunchApp: (AppInfo) -> Unit,
    onSpeakMessage: () -> Unit
) {
    val isComet = message.sender == "comet"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isComet) Arrangement.Start else Arrangement.End
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(if (isComet) 0.90f else 0.82f),
            horizontalAlignment = if (isComet) Alignment.Start else Alignment.End
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (isComet) 4.dp else 20.dp,
                            bottomEnd = if (isComet) 20.dp else 4.dp
                        )
                    )
                    .background(
                        if (isComet) {
                            Color(0x35192255)
                        } else {
                            accentColor.copy(alpha = 0.35f)
                        }
                    )
                    .border(
                        1.dp,
                        if (isComet) Color.White.copy(alpha = 0.15f) else accentColor.copy(alpha = 0.5f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                Column {
                    Text(
                        text = message.text,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    // Interactive App Action Card if Comet matched or launched an app!
                    if (message.actionApp != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        InteractiveAppBadgeCard(
                            app = message.actionApp,
                            accentColor = accentColor,
                            onClick = { onLaunchApp(message.actionApp) }
                        )
                    }
                }
            }

            // Quick speech replay option for Comet responses
            if (isComet && message.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(onClick = onSpeakMessage)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VolumeUp,
                        contentDescription = "Read aloud",
                        tint = TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Read aloud", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun InteractiveAppBadgeCard(
    app: AppInfo,
    accentColor: Color,
    onClick: () -> Unit
) {
    LiquidGlassCard(
        shape = RoundedCornerShape(16.dp),
        backgroundColor = Color(0x40151C4A),
        borderColor = accentColor.copy(alpha = 0.6f),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x25FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                if (app.iconBitmap != null) {
                    Image(
                        bitmap = app.iconBitmap.asImageBitmap(),
                        contentDescription = app.label,
                        modifier = Modifier.size(36.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Android,
                        contentDescription = app.label,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    color = TextPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap to open application",
                    color = accentColor,
                    fontSize = 11.sp
                )
            }

            Icon(
                imageVector = Icons.Rounded.OpenInNew,
                contentDescription = "Open",
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun MiniGlowingOrb(
    status: AssistantStatus,
    accentColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mini_orb")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = if (status == AssistantStatus.LISTENING || status == AssistantStatus.SPEAKING) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mini_pulse"
    )

    Box(
        modifier = Modifier
            .size(36.dp)
            .scale(pulseScale),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentColor,
                        ElectricViolet,
                        CyberPink.copy(alpha = 0.5f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                )
            )
        }

        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}
