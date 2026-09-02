package com.example.ui.auth

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNotification
import com.example.data.model.UserRole
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberGlassCard
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.StatusPurple

@Composable
fun AuthDialog(
    onDismiss: () -> Unit,
    onLogin: (String, UserRole) -> Unit
) {
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.PLAYER) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        title = {
            Text(
                text = "Firebase Sign-In / Account Access",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Sign in via Email or Phone (+880). Supports both Player App and Admin Host access.",
                    color = Slate400,
                    fontSize = 12.sp
                )

                // Role Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { selectedRole = UserRole.PLAYER },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedRole == UserRole.PLAYER) NeonCyanBright else Slate800
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "PLAYER",
                            color = if (selectedRole == UserRole.PLAYER) Slate950 else Slate400,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { selectedRole = UserRole.ADMIN },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedRole == UserRole.ADMIN) StatusPurple else Slate800
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "ADMIN HOST",
                            color = if (selectedRole == UserRole.ADMIN) Color.White else Slate400,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                OutlinedTextField(
                    value = emailOrPhone,
                    onValueChange = { emailOrPhone = it },
                    label = { Text("Email or Phone (+880...)", color = Slate400) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Slate400) },
                    modifier = Modifier.fillMaxWidth().testTag("auth_email_phone_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonCyanBright,
                        unfocusedBorderColor = Slate700
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = Slate400) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Slate400) },
                    modifier = Modifier.fillMaxWidth().testTag("auth_password_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonCyanBright,
                        unfocusedBorderColor = Slate700
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val credential = if (emailOrPhone.isNotBlank()) emailOrPhone else "player@dhaka-efootball.com"
                    onLogin(credential, selectedRole)
                    onDismiss()
                },
                modifier = Modifier.testTag("auth_confirm_btn")
            ) {
                Text("LOG IN", color = NeonCyanBright, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Slate400)
            }
        }
    )
}

@Composable
fun NotificationDialog(
    notifications: List<AppNotification>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = NeonCyanBright)
                Text(
                    text = "Tournament Alerts & FCM",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            if (notifications.isEmpty()) {
                Text(text = "No alerts at this moment.", color = Slate400, fontSize = 13.sp)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notifications, key = { it.id }) { notif ->
                        CyberGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = Slate800,
                            backgroundColor = Slate950
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = notif.title,
                                    color = NeonCyanBright,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notif.message,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = NeonCyanBright, fontWeight = FontWeight.Bold)
            }
        }
    )
}
