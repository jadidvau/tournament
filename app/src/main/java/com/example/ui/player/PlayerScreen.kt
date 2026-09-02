package com.example.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MatchPlayer
import com.example.data.model.MatchStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.RegistrationStatus
import com.example.data.model.TournamentInfo
import com.example.data.model.TournamentMatch
import com.example.data.model.TournamentRegistration
import com.example.data.model.UserProfile
import com.example.ui.bracket.BracketViewer
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberGlassCard
import com.example.ui.components.MatchCardNode
import com.example.ui.components.MatchStatusBadge
import com.example.ui.components.PaymentNumberCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyanDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanSurface
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
import com.example.ui.viewmodel.TournamentViewModel

@Composable
fun PlayerScreen(
    viewModel: TournamentViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val tournament by viewModel.tournament.collectAsState()
    val matches by viewModel.matches.collectAsState()
    val registration by viewModel.userRegistration.collectAsState()
    val currentMatch by viewModel.userCurrentMatch.collectAsState()
    val selectedRound by viewModel.selectedRoundFilter.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showProfileDialog by remember { mutableStateOf(false) }

    val tabs = listOf("Overview & Pay", "My Match", "Full Bracket", "My Profile")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        // Player Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Slate900,
            contentColor = NeonCyanBright,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NeonCyanBright,
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Black else FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (selectedTab == index) NeonCyanBright else Slate400
                        )
                    },
                    modifier = Modifier.testTag("player_tab_$index")
                )
            }
        }

        when (selectedTab) {
            0 -> OverviewAndPaymentTab(
                tournament = tournament,
                registration = registration,
                currentUser = currentUser,
                onSubmitPayment = { method, trxId ->
                    viewModel.submitRegistration(method, trxId)
                },
                onEditProfile = { showProfileDialog = true }
            )
            1 -> MyMatchTab(
                userMatch = currentMatch,
                currentUser = currentUser,
                registration = registration,
                tournament = tournament
            )
            2 -> BracketViewer(
                tournament = tournament,
                matches = matches,
                selectedRound = selectedRound,
                onSelectRound = { viewModel.setRoundFilter(it) },
                currentUserId = currentUser?.uid
            )
            3 -> PlayerProfileTab(
                user = currentUser,
                registration = registration,
                onEditProfile = { showProfileDialog = true }
            )
        }
    }

    if (showProfileDialog && currentUser != null) {
        ProfileEditDialog(
            user = currentUser!!,
            onDismiss = { showProfileDialog = false },
            onSave = { name, phone, igId, igUser ->
                viewModel.updateProfile(name, phone, igId, igUser)
                showProfileDialog = false
            }
        )
    }
}

@Composable
fun OverviewAndPaymentTab(
    tournament: TournamentInfo,
    registration: TournamentRegistration?,
    currentUser: UserProfile?,
    onSubmitPayment: (PaymentMethod, String) -> Unit,
    onEditProfile: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethod.bKash) }
    var trxIdInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Championship Card
        item {
            CyberGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonCyan.copy(alpha = 0.5f),
                backgroundColor = Slate900
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (tournament.isRegistrationOpen) StatusEmerald.copy(alpha = 0.2f) else StatusRose.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, if (tournament.isRegistrationOpen) StatusEmerald else StatusRose)
                        ) {
                            Text(
                                text = if (tournament.isRegistrationOpen) "REGISTRATION OPEN" else "REGISTRATION CLOSED",
                                color = if (tournament.isRegistrationOpen) StatusEmerald else StatusRose,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "Single-Elimination 1v1",
                            color = Slate400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = tournament.title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Official competitive esports tournament for eFootball Mobile & Console contenders across Bangladesh.",
                        color = Slate400,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "ENTRY FEE", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${tournament.entryFee} BDT",
                                color = NeonCyanBright,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Column {
                            Text(text = "MATCH DURATION", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${tournament.matchDurationMinutes} Mins",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Column {
                            Text(text = "BRACKET FORMAT", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "1v1 Knockout",
                                color = StatusPurple,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // Registration Status Card (if user has already submitted or joined)
        if (registration != null) {
            item {
                CyberGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = when (registration.status) {
                        RegistrationStatus.JOINED -> StatusEmerald
                        RegistrationStatus.PENDING -> StatusAmber
                        RegistrationStatus.REJECTED -> StatusRose
                    },
                    backgroundColor = Slate900
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "YOUR REGISTRATION STATUS",
                                color = Slate400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            StatusBadge(status = registration.status)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        when (registration.status) {
                            RegistrationStatus.JOINED -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusEmerald)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Payment verified! You are officially seeded into the championship bracket.",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            RegistrationStatus.PENDING -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = StatusAmber)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Your TrxID (${registration.trxId}) is under review by the tournament host. Approval will reflect here automatically.",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            RegistrationStatus.REJECTED -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusRose)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Reason: ${registration.rejectionReason}",
                                            color = StatusRose,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Please verify your TrxID and re-submit below.",
                                            color = Slate400,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Payment Instructions Section
        item {
            Column {
                Text(
                    text = "MANUAL PAYMENT INSTRUCTIONS",
                    color = NeonCyanBright,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "1. Send ${tournament.entryFee} BDT via 'Send Money' to either bKash or Nagad number below.\n2. Copy the Transaction ID (TrxID) from your SMS or app.\n3. Enter the TrxID below to confirm your spot in the bracket.",
                    color = Slate400,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // bKash Number Card
        item {
            PaymentNumberCard(
                method = PaymentMethod.bKash,
                number = tournament.bkashNumber
            )
        }

        // Nagad Number Card
        item {
            PaymentNumberCard(
                method = PaymentMethod.Nagad,
                number = tournament.nagadNumber
            )
        }

        // Payment Submission Form (Only if not already joined or if rejected)
        if (registration == null || registration.status == RegistrationStatus.REJECTED) {
            item {
                CyberGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = NeonCyanBright,
                    backgroundColor = Slate900
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SUBMIT PAYMENT VERIFICATION",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Player Profile Snapshot Info
                        if (currentUser != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Slate800,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = "Player: ${currentUser.fullName}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "eFootball ID: ${currentUser.inGameId} (@${currentUser.inGameUsername})", color = Slate400, fontSize = 11.sp)
                                    }
                                    IconButton(
                                        onClick = onEditProfile,
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = NeonCyanBright, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = "Select Payment Gateway", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // bKash Radio Option
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedMethod = PaymentMethod.bKash }
                                    .testTag("select_bkash_radio"),
                                color = if (selectedMethod == PaymentMethod.bKash) Color(0xFFE2136E).copy(alpha = 0.2f) else Slate800,
                                border = BorderStroke(1.dp, if (selectedMethod == PaymentMethod.bKash) Color(0xFFE2136E) else Slate700)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedMethod == PaymentMethod.bKash,
                                        onClick = { selectedMethod = PaymentMethod.bKash },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFE2136E))
                                    )
                                    Text(
                                        text = "bKash",
                                        color = if (selectedMethod == PaymentMethod.bKash) Color.White else Slate400,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            // Nagad Radio Option
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedMethod = PaymentMethod.Nagad }
                                    .testTag("select_nagad_radio"),
                                color = if (selectedMethod == PaymentMethod.Nagad) Color(0xFFF7941D).copy(alpha = 0.2f) else Slate800,
                                border = BorderStroke(1.dp, if (selectedMethod == PaymentMethod.Nagad) Color(0xFFF7941D) else Slate700)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedMethod == PaymentMethod.Nagad,
                                        onClick = { selectedMethod = PaymentMethod.Nagad },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFF7941D))
                                    )
                                    Text(
                                        text = "Nagad",
                                        color = if (selectedMethod == PaymentMethod.Nagad) Color.White else Slate400,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = trxIdInput,
                            onValueChange = { trxIdInput = it.uppercase() },
                            label = { Text("Transaction ID (TrxID)", color = Slate400) },
                            placeholder = { Text("e.g. 9J28A190KZ", color = Slate600) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                keyboardType = KeyboardType.Ascii
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("trx_id_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonCyanBright,
                                unfocusedBorderColor = Slate700,
                                focusedContainerColor = Slate950,
                                unfocusedContainerColor = Slate950
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        CyberButton(
                            text = "Submit Verification (${tournament.entryFee} BDT)",
                            onClick = { onSubmitPayment(selectedMethod, trxIdInput) },
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Default.Send,
                            enabled = tournament.isRegistrationOpen && trxIdInput.isNotBlank(),
                            testTag = "submit_payment_btn"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MyMatchTab(
    userMatch: TournamentMatch?,
    currentUser: UserProfile?,
    registration: TournamentRegistration?,
    tournament: TournamentInfo
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (userMatch == null) Arrangement.Center else Arrangement.Top
    ) {
        if (registration == null || registration.status != RegistrationStatus.JOINED) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(56.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Registration Required",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "You must submit payment and get approved by the host before your matchup is assigned.",
                color = Slate400,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        } else if (userMatch == null) {
            Icon(Icons.Default.SportsEsports, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(56.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Awaiting Bracket Generation",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "You are approved! As soon as the host seeds the bracket, your 1v1 opponent and match details will appear here.",
                color = Slate400,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        } else {
            // Active Match Hub
            Text(
                text = "MY MATCH HUB",
                color = NeonCyanBright,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            CyberGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonCyanBright,
                backgroundColor = Slate900
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = userMatch.startTime,
                            color = NeonCyanBright,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                        MatchStatusBadge(status = userMatch.status, isBye = userMatch.isBye)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1v1 Head to Head Display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Player 1
                        PlayerMatchProfileBox(
                            player = userMatch.player1,
                            isMe = userMatch.player1?.id == currentUser?.uid,
                            modifier = Modifier.weight(1f)
                        )

                        // Score / VS Badge
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            if (userMatch.status != MatchStatus.SCHEDULED) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = userMatch.player1Score.toString(),
                                        color = if (userMatch.winnerId == userMatch.player1?.id) StatusEmerald else Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(text = " - ", color = Slate400, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = userMatch.player2Score.toString(),
                                        color = if (userMatch.winnerId == userMatch.player2?.id) StatusEmerald else Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            } else {
                                Surface(
                                    shape = CircleShape,
                                    color = NeonCyan.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, NeonCyanBright)
                                ) {
                                    Text(
                                        text = "VS",
                                        color = NeonCyanBright,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // Player 2
                        PlayerMatchProfileBox(
                            player = userMatch.player2,
                            isMe = userMatch.player2?.id == currentUser?.uid,
                            isBye = userMatch.isBye,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Divider(color = Slate800)
                    Spacer(modifier = Modifier.height(14.dp))

                    // In-Game Connection Instructions
                    val opponent = if (userMatch.player1?.id == currentUser?.uid) userMatch.player2 else userMatch.player1
                    if (opponent != null) {
                        Column {
                            Text(
                                text = "OPPONENT DETAILS FOR eFOOTBALL ROOM:",
                                color = Slate400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Gamertag: @${opponent.inGameUsername}",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "In-Game ID: ${opponent.inGameId}",
                                color = NeonCyanBright,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (userMatch.isBye) {
                        Text(
                            text = "You received a BYE for this round and advance automatically to Round 2!",
                            color = StatusPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerMatchProfileBox(
    player: MatchPlayer?,
    isMe: Boolean,
    isBye: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = if (isMe) NeonCyan.copy(alpha = 0.2f) else Slate800,
            border = BorderStroke(1.5.dp, if (isMe) NeonCyanBright else Slate700),
            modifier = Modifier.size(50.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = if (isMe) NeonCyanBright else Slate400,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = when {
                isBye -> "BYE"
                player == null -> "Awaiting"
                isMe -> "${player.name} (You)"
                else -> player.name
            },
            color = if (isMe) NeonCyanBright else Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        if (player != null && !isBye) {
            Text(
                text = "@${player.inGameUsername}",
                color = Slate400,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PlayerProfileTab(
    user: UserProfile?,
    registration: TournamentRegistration?,
    onEditProfile: () -> Unit
) {
    if (user == null) return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CyberGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonCyan.copy(alpha = 0.4f),
                backgroundColor = Slate900
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = NeonCyan.copy(alpha = 0.2f),
                                border = BorderStroke(2.dp, NeonCyanBright),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = NeonCyanBright,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = user.fullName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "@${user.inGameUsername}",
                                    color = NeonCyanBright,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onEditProfile,
                            modifier = Modifier.testTag("edit_profile_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = NeonCyanBright)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Slate800)
                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileFieldRow(label = "Phone Number", value = user.phoneNumber)
                    ProfileFieldRow(label = "eFootball In-Game ID", value = user.inGameId)
                    ProfileFieldRow(label = "Account Role", value = user.role.name)
                    ProfileFieldRow(label = "Player UID", value = user.uid)
                }
            }
        }
    }
}

@Composable
fun ProfileFieldRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Slate400, fontSize = 12.sp)
        Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ProfileEditDialog(
    user: UserProfile,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(user.fullName) }
    var phone by remember { mutableStateOf(user.phoneNumber) }
    var igId by remember { mutableStateOf(user.inGameId) }
    var igUsername by remember { mutableStateOf(user.inGameUsername) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        title = {
            Text(
                text = "Edit Player Profile",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name", color = Slate400) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonCyanBright,
                        unfocusedBorderColor = Slate700
                    )
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number (+880)", color = Slate400) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonCyanBright,
                        unfocusedBorderColor = Slate700
                    )
                )
                OutlinedTextField(
                    value = igId,
                    onValueChange = { igId = it },
                    label = { Text("eFootball In-Game ID", color = Slate400) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonCyanBright,
                        unfocusedBorderColor = Slate700
                    )
                )
                OutlinedTextField(
                    value = igUsername,
                    onValueChange = { igUsername = it },
                    label = { Text("In-Game Username", color = Slate400) },
                    singleLine = true,
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
                onClick = { onSave(name, phone, igId, igUsername) },
                modifier = Modifier.testTag("save_profile_btn")
            ) {
                Text("SAVE", color = NeonCyanBright, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Slate400)
            }
        }
    )
}
