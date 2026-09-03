package com.example.ui.auth

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ORGANIZER_EMAIL
import com.example.ui.components.CyberCard
import com.example.ui.theme.CyanBorder
import com.example.ui.theme.CyanSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.StatusEmerald
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRose
import com.example.ui.viewmodel.TournamentViewModel

@Composable
fun AuthScreen(
    viewModel: TournamentViewModel,
    onAuthSuccess: (isAdmin: Boolean) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Sign In, 1: Register Player

    // Input fields - strictly empty by default
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Registration specific fields: IGN, Konami ID, Phone
    var ign by remember { mutableStateOf("") }
    var konamiId by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // Email Verification Screen State
    var isVerifyingEmail by remember { mutableStateOf(false) }
    var pendingUserId by remember { mutableStateOf("") }
    var verificationWarningMessage by remember { mutableStateOf<String?>(null) }
    var verificationSuccessMessage by remember { mutableStateOf<String?>(null) }
    var isCheckingVerification by remember { mutableStateOf(false) }
    var isResendingEmail by remember { mutableStateOf(false) }
    var resendCooldownSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(resendCooldownSeconds) {
        if (resendCooldownSeconds > 0) {
            delay(1000L)
            resendCooldownSeconds -= 1
        }
    }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("auth_screen_gatekeeper"),
        color = Slate950
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Tournament Branding & Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(CyanSurface, Slate900)
                        )
                    )
                    .border(2.dp, CyanBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = "Esports Controller",
                    tint = NeonCyan,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "DHAKA eFOOTBALL",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "OFFICIAL BANGLADESH CHAMPIONSHIP",
                style = MaterialTheme.typography.labelMedium,
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Security Gatekeeper Banner
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Slate900,
                border = BorderStroke(1.dp, Slate800)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(StatusGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isVerifyingEmail) "EMAIL VERIFICATION REQUIRED • GATEWAY" else "AUTHENTICATION GATEWAY • LOGIN REQUIRED",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // VERIFICATION SCREEN OR TABS
            if (isVerifyingEmail) {
                // Authentic Firebase Email Verification Screen
                CyberCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("verification_screen")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.12f))
                                .border(1.5.dp, NeonCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MarkEmailRead,
                                contentDescription = "Email Verification",
                                tint = NeonCyan,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Verify Your Email",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "We sent a verification link to:",
                            fontSize = 13.sp,
                            color = Slate400,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = email.trim(),
                            fontSize = 14.sp,
                            color = NeonCyanBright,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Esports-styled Instruction Card
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Slate900,
                            border = BorderStroke(1.dp, Slate800),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("verification_instructions_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(NeonCyan.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "1",
                                            color = NeonCyan,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text(
                                        text = "Open your email inbox.",
                                        color = Slate200,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(NeonCyan.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "2",
                                            color = NeonCyan,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text(
                                        text = "Click the verification link from Dhaka eFootball Open.",
                                        color = Slate200,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(NeonCyan.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "3",
                                            color = NeonCyan,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text(
                                        text = "Return to this screen and tap 'I Have Verified'.",
                                        color = Slate200,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Warning Banner if not verified
                        if (verificationWarningMessage != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF381216),
                                border = BorderStroke(1.dp, StatusRose),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("verification_warning_banner")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Warning",
                                        tint = StatusRose,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = verificationWarningMessage ?: "",
                                        color = StatusRose,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Notification / Success Banner (e.g. resend link sent)
                        if (verificationSuccessMessage != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0C2B1D),
                                border = BorderStroke(1.dp, StatusGreen),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("verification_success_banner")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = StatusGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = verificationSuccessMessage ?: "",
                                        color = StatusGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Primary Button: "I HAVE VERIFIED (CONTINUE)"
                        Button(
                            onClick = {
                                isCheckingVerification = true
                                verificationWarningMessage = null
                                verificationSuccessMessage = null
                                viewModel.checkEmailVerificationStatus(
                                    userId = pendingUserId,
                                    ign = ign.trim(),
                                    konamiId = konamiId.trim(),
                                    email = email.trim(),
                                    phone = phone.trim(),
                                    onVerified = { profile ->
                                        isCheckingVerification = false
                                        val isAdmin = profile.email.trim().equals(ORGANIZER_EMAIL, ignoreCase = true)
                                        onAuthSuccess(isAdmin)
                                    },
                                    onNotVerified = {
                                        isCheckingVerification = false
                                        verificationWarningMessage = "Email not yet verified. Please tap the link in your inbox first."
                                    },
                                    onError = { err ->
                                        isCheckingVerification = false
                                        verificationWarningMessage = err
                                    }
                                )
                            },
                            enabled = !isCheckingVerification && !isResendingEmail,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("verify_continue_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = Slate950
                            )
                        ) {
                            if (isCheckingVerification) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Slate950,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "I HAVE VERIFIED (CONTINUE)",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Secondary Button: "Resend Verification Email"
                        OutlinedButton(
                            onClick = {
                                if (resendCooldownSeconds > 0) return@OutlinedButton
                                isResendingEmail = true
                                verificationWarningMessage = null
                                verificationSuccessMessage = null
                                viewModel.resendVerificationEmail(
                                    onSuccess = {
                                        isResendingEmail = false
                                        resendCooldownSeconds = 60
                                        verificationSuccessMessage = "New verification link sent to your inbox."
                                    },
                                    onError = { err ->
                                        isResendingEmail = false
                                        verificationWarningMessage = err
                                    }
                                )
                            },
                            enabled = !isCheckingVerification && !isResendingEmail && resendCooldownSeconds == 0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("resend_verification_email_btn"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                if (resendCooldownSeconds > 0) Slate700 else NeonCyan.copy(alpha = 0.6f)
                            )
                        ) {
                            if (isResendingEmail) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = NeonCyan,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = if (resendCooldownSeconds > 0) Slate500 else NeonCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (resendCooldownSeconds > 0) "Resend in ${resendCooldownSeconds}s" else "Resend Verification Email",
                                        color = if (resendCooldownSeconds > 0) Slate400 else NeonCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Footer Link: "Wrong email? Back to Register" (signs out and clears fields)
                        TextButton(
                            onClick = {
                                viewModel.logout()
                                email = ""
                                password = ""
                                ign = ""
                                konamiId = ""
                                phone = ""
                                isVerifyingEmail = false
                                verificationWarningMessage = null
                                verificationSuccessMessage = null
                                errorMessage = null
                                resendCooldownSeconds = 0
                            },
                            modifier = Modifier.testTag("back_to_register_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Slate400,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Wrong email? Back to Register",
                                    color = Slate400,
                                    fontSize = 12.5.sp,
                                    textDecoration = TextDecoration.Underline
                                )
                            }
                        }
                    }
                }
            } else {
                // Normal Sign In & Register Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Slate900,
                    contentColor = NeonCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = NeonCyan,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Slate800, RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            errorMessage = null
                        },
                        modifier = Modifier.testTag("tab_signin"),
                        text = {
                            Text(
                                text = "SIGN IN",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) NeonCyan else Slate400
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            errorMessage = null
                        },
                        modifier = Modifier.testTag("tab_signup"),
                        text = {
                            Text(
                                text = "REGISTER PLAYER",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) NeonCyan else Slate400
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // On-Screen Clean Error Banner
                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF3B1219),
                        border = BorderStroke(1.dp, StatusRose),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("auth_error_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = StatusRose,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = StatusRose,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Main Input Fields Form
                CyberCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Register Player Specific Fields: IGN, Konami ID, Phone
                        AnimatedVisibility(visible = selectedTab == 1) {
                            Column {
                                // 1. IGN (In-Game Name)
                                OutlinedTextField(
                                    value = ign,
                                    onValueChange = {
                                        ign = it
                                        errorMessage = null
                                    },
                                    label = { Text("IGN (In-Game Name)") },
                                    placeholder = { Text("e.g. ProGamer_BD", color = Slate700) },
                                    leadingIcon = {
                                        Icon(Icons.Default.SportsEsports, contentDescription = null, tint = NeonCyan)
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_player_ign"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = Slate700,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Slate200
                                    )
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // 2. Konami ID
                                OutlinedTextField(
                                    value = konamiId,
                                    onValueChange = {
                                        konamiId = it
                                        errorMessage = null
                                    },
                                    label = { Text("Konami ID") },
                                    placeholder = { Text("e.g. 109-882-901", color = Slate700) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Badge, contentDescription = null, tint = NeonCyan)
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_konami_id"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = Slate700,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Slate200
                                    )
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        // 3. Email Address
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                errorMessage = null
                            },
                            label = { Text("Email Address") },
                            placeholder = { Text("name@example.com", color = Slate700) },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = NeonCyan)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_email"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Slate200
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 4. Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            label = { Text("Password (Min 6 Characters)") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = NeonCyan)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility",
                                        tint = Slate400
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_password"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Slate200
                            )
                        )

                        // 5. Phone Number (under REGISTER PLAYER)
                        AnimatedVisibility(visible = selectedTab == 1) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = phone,
                                    onValueChange = {
                                        phone = it
                                        errorMessage = null
                                    },
                                    label = { Text("Phone Number (bKash / Nagad)") },
                                    placeholder = { Text("e.g. 01711223344", color = Slate700) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = NeonCyan)
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_phone"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = Slate700,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Slate200
                                    )
                                )
                            }
                        }

                        // Clean Error Message directly under inputs
                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("inline_auth_error"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = StatusRose,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    color = StatusRose,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Submit Action Button
                        Button(
                            onClick = {
                                if (selectedTab == 0) {
                                    // Sign In flow
                                    if (email.isBlank()) {
                                        errorMessage = "Please enter your email address"
                                        return@Button
                                    }
                                    if (password.length < 6) {
                                        errorMessage = "Password must be at least 6 characters"
                                        return@Button
                                    }

                                    isLoading = true
                                    errorMessage = null

                                    viewModel.loginWithEmailPassword(
                                        email = email.trim(),
                                        pass = password,
                                        isSignUp = false,
                                        onSuccess = { profile ->
                                            isLoading = false
                                            val isAdmin = profile.email.trim().equals(ORGANIZER_EMAIL, ignoreCase = true)
                                            onAuthSuccess(isAdmin)
                                        },
                                        onError = { err ->
                                            isLoading = false
                                            errorMessage = err
                                        }
                                    )
                                } else {
                                    // Register Player flow: IGN, Konami ID, Email, Password, Phone
                                    if (ign.isBlank()) {
                                        errorMessage = "Please enter your IGN (In-Game Name)"
                                        return@Button
                                    }
                                    if (konamiId.isBlank()) {
                                        errorMessage = "Please enter your Konami ID"
                                        return@Button
                                    }
                                    if (email.isBlank() || !email.contains("@") || !email.contains(".")) {
                                        errorMessage = "Please enter a valid email address"
                                        return@Button
                                    }
                                    if (password.length < 6) {
                                        errorMessage = "Password must be at least 6 characters"
                                        return@Button
                                    }
                                    if (phone.isBlank()) {
                                        errorMessage = "Please enter your phone number"
                                        return@Button
                                    }

                                    isLoading = true
                                    errorMessage = null

                                    viewModel.registerPlayer(
                                        ign = ign.trim(),
                                        konamiId = konamiId.trim(),
                                        email = email.trim(),
                                        pass = password,
                                        phone = phone.trim(),
                                        onVerificationRequired = { userId ->
                                            isLoading = false
                                            pendingUserId = userId
                                            isVerifyingEmail = true
                                            errorMessage = null
                                            verificationWarningMessage = null
                                            verificationSuccessMessage = null
                                            resendCooldownSeconds = 0
                                        },
                                        onError = { err ->
                                            isLoading = false
                                            errorMessage = err
                                        }
                                    )
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("submit_auth_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = Slate950
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Slate950,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (selectedTab == 0) "SIGN IN TO TOURNAMENT" else "REGISTER PLAYER",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer note on persistent session & rules
            Text(
                text = "Session state is securely saved. Returning users bypass login automatically.",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Developer Info Bottom Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Slate900.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, Slate800),
                onClick = { showAboutDialog = true },
                modifier = Modifier.testTag("auth_developer_info_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Developer",
                        tint = NeonCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Developer: ",
                        color = Slate400,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Jadid Mollik",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•",
                        color = Slate500,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ABOUT INFO",
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showAboutDialog) {
        AboutDeveloperDialog(
            onDismiss = { showAboutDialog = false }
        )
    }
}

