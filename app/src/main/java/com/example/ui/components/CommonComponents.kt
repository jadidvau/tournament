package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNotification
import com.example.data.model.MatchPlayer
import com.example.data.model.MatchStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.RegistrationStatus
import com.example.data.model.TournamentMatch
import com.example.data.model.UserRole
import com.example.ui.theme.CyanDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanSurface
import com.example.ui.theme.GoldCrown
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusEmerald
import com.example.ui.theme.StatusPurple
import com.example.ui.theme.StatusRose

@Composable
fun CyberGlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = NeonCyan.copy(alpha = 0.3f),
    backgroundColor: Color = Slate900.copy(alpha = 0.85f),
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .border(
                BorderStroke(1.dp, borderColor),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        content()
    }
}

@Composable
fun StatusBadge(
    status: RegistrationStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status) {
        RegistrationStatus.PENDING -> Triple(StatusAmber.copy(alpha = 0.2f), StatusAmber, "PENDING VERIFICATION")
        RegistrationStatus.JOINED -> Triple(StatusEmerald.copy(alpha = 0.2f), StatusEmerald, "APPROVED & JOINED")
        RegistrationStatus.REJECTED -> Triple(StatusRose.copy(alpha = 0.2f), StatusRose, "REJECTED")
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.6f))
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun MatchStatusBadge(
    status: MatchStatus,
    isBye: Boolean = false,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when {
        isBye -> Triple(StatusPurple.copy(alpha = 0.2f), StatusPurple, "BYE ADVANCE")
        status == MatchStatus.LIVE -> Triple(StatusRose.copy(alpha = 0.25f), StatusRose, "● LIVE NOW")
        status == MatchStatus.COMPLETED -> Triple(StatusEmerald.copy(alpha = 0.2f), StatusEmerald, "COMPLETED")
        else -> Triple(NeonCyan.copy(alpha = 0.15f), NeonCyan, "SCHEDULED")
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.5f))
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun CyberButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    isSecondary: Boolean = false,
    testTag: String = ""
) {
    val bgBrush = if (isSecondary) {
        Brush.horizontalGradient(listOf(Slate800, Slate700))
    } else {
        Brush.horizontalGradient(listOf(CyanDark, NeonCyan))
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .testTag(if (testTag.isNotEmpty()) testTag else "cyber_button_$text")
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Slate800.copy(alpha = 0.5f)
        ),
        border = if (isSecondary) BorderStroke(1.dp, Slate600) else BorderStroke(1.dp, NeonCyanBright),
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Box(
            modifier = Modifier
                .background(if (enabled) bgBrush else Brush.horizontalGradient(listOf(Slate800, Slate800)), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSecondary) Slate200 else Slate950,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text.uppercase(),
                    color = if (isSecondary) Slate200 else Slate950,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun PaymentNumberCard(
    method: PaymentMethod,
    number: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val brandColor = if (method == PaymentMethod.bKash) Color(0xFFE2136E) else Color(0xFFF7941D)

    CyberGlassCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = brandColor.copy(alpha = 0.5f),
        backgroundColor = Slate900
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = brandColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, brandColor),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (method == PaymentMethod.bKash) "bK" else "NG",
                            color = brandColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${method.name} Personal Number (Send Money)",
                        color = Slate400,
                        fontSize = 11.sp
                    )
                    Text(
                        text = number,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("${method.name} Number", number)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "${method.name} number copied: $number", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .testTag("copy_${method.name.lowercase()}_btn")
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy ${method.name} number",
                    tint = brandColor
                )
            }
        }
    }
}

@Composable
fun EsportsHeader(
    title: String,
    subtitle: String,
    currentRole: UserRole,
    onSwitchRole: (UserRole) -> Unit,
    notificationCount: Int,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Slate950,
        border = BorderStroke(0.dp, Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Branding Logo & Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(NeonCyan.copy(alpha = glowAlpha), CyanSurface)
                                ),
                                shape = CircleShape
                            )
                            .border(1.5.dp, NeonCyanBright, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsSoccer,
                            contentDescription = "Esports Logo",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "DHAKA eFOOTBALL",
                            color = NeonCyanBright,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "OPEN CHAMPIONSHIP 2026",
                            color = Slate400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                // Header actions: Role Toggle Badge & Notification Icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Portal Switcher Chip
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                onSwitchRole(if (currentRole == UserRole.ADMIN) UserRole.PLAYER else UserRole.ADMIN)
                            }
                            .testTag("role_switch_toggle"),
                        color = if (currentRole == UserRole.ADMIN) StatusPurple.copy(alpha = 0.25f) else CyanSurface,
                        border = BorderStroke(1.dp, if (currentRole == UserRole.ADMIN) StatusPurple else NeonCyan)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (currentRole == UserRole.ADMIN) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                contentDescription = null,
                                tint = if (currentRole == UserRole.ADMIN) StatusPurple else NeonCyanBright,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (currentRole == UserRole.ADMIN) "ADMIN HOST" else "PLAYER APP",
                                color = if (currentRole == UserRole.ADMIN) StatusPurple else NeonCyanBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Notifications Icon with badge
                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("notifications_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (notificationCount > 0) {
                                    Badge(
                                        containerColor = StatusRose,
                                        contentColor = Color.White
                                    ) {
                                        Text(text = notificationCount.toString(), fontSize = 9.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Slate200,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatchCardNode(
    match: TournamentMatch,
    isUserMatch: Boolean = false,
    onAdminEdit: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isUserMatch -> NeonCyanBright
        match.status == MatchStatus.LIVE -> StatusRose
        match.status == MatchStatus.COMPLETED -> StatusEmerald.copy(alpha = 0.5f)
        else -> Slate700
    }

    CyberGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = onAdminEdit != null) { onAdminEdit?.invoke() },
        borderColor = borderColor,
        backgroundColor = if (isUserMatch) CyanSurface.copy(alpha = 0.6f) else Slate900
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Match Header Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = match.startTime,
                    color = if (isUserMatch) NeonCyanBright else Slate400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                MatchStatusBadge(status = match.status, isBye = match.isBye)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Player 1 Row
            MatchPlayerRow(
                player = match.player1,
                score = match.player1Score,
                isWinner = match.winnerId != null && match.winnerId == match.player1?.id,
                isPending = match.player1 == null
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Player 2 Row
            MatchPlayerRow(
                player = match.player2,
                score = match.player2Score,
                isWinner = match.winnerId != null && match.winnerId == match.player2?.id,
                isPending = match.player2 == null,
                isBye = match.isBye
            )

            if (onAdminEdit != null && !match.isBye) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "EDIT SCORE / STATUS ➔",
                        color = NeonCyanBright,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MatchPlayerRow(
    player: MatchPlayer?,
    score: Int,
    isWinner: Boolean,
    isPending: Boolean,
    isBye: Boolean = false
) {
    val bg = when {
        isWinner -> StatusEmerald.copy(alpha = 0.15f)
        else -> Slate800.copy(alpha = 0.6f)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = bg,
        border = if (isWinner) BorderStroke(1.dp, StatusEmerald.copy(alpha = 0.8f)) else BorderStroke(0.5.dp, Slate700)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (isWinner) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Winner",
                        tint = StatusEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Column {
                    Text(
                        text = when {
                            isBye -> "BYE (Auto Pass)"
                            isPending -> "TBD (Awaiting Match Winner)"
                            else -> player?.name ?: "TBD"
                        },
                        color = when {
                            isWinner -> StatusEmerald
                            isPending || isBye -> Slate400
                            else -> Color.White
                        },
                        fontWeight = if (isWinner) FontWeight.Black else FontWeight.Medium,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                    if (player != null && !isBye) {
                        Text(
                            text = "@${player.inGameUsername} • ID: ${player.inGameId}",
                            color = Slate400,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            if (!isPending && !isBye) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isWinner) StatusEmerald else Slate700,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = score.toString(),
                        color = if (isWinner) Slate950 else Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
