package com.example.ui.admin

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MatchStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.RegistrationStatus
import com.example.data.model.TournamentInfo
import com.example.data.model.TournamentMatch
import com.example.data.model.TournamentRegistration
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.bracket.BracketViewer
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberGlassCard
import com.example.ui.components.StatusBadge
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
import com.example.ui.viewmodel.TournamentViewModel

@Composable
fun AdminScreen(
    viewModel: TournamentViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val tournament by viewModel.tournament.collectAsState()
    val registrations by viewModel.registrations.collectAsState()
    val matches by viewModel.matches.collectAsState()
    val selectedRound by viewModel.selectedRoundFilter.collectAsState()

    val pendingCount by viewModel.pendingCount.collectAsState()
    val approvedCount by viewModel.approvedCount.collectAsState()
    val completedMatchesCount by viewModel.completedMatchesCount.collectAsState()

    var selectedAdminTab by remember { mutableIntStateOf(0) }
    var selectedMatchForEdit by remember { mutableStateOf<TournamentMatch?>(null) }
    var rejectDialogRegistration by remember { mutableStateOf<TournamentRegistration?>(null) }

    val adminTabs = listOf("Queue ($pendingCount)", "Live Bracket", "Settings", "Stats")

    // Admin Auth Gatekeeper Check
    if (currentUser?.role != UserRole.ADMIN) {
        AdminUnauthorizedGatekeeper(
            onElevateToAdmin = {
                viewModel.switchRole(UserRole.ADMIN)
            },
            modifier = modifier
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        // Admin Navigation Tabs
        TabRow(
            selectedTabIndex = selectedAdminTab,
            containerColor = Slate900,
            contentColor = StatusPurple,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedAdminTab]),
                    color = StatusPurple,
                    height = 3.dp
                )
            }
        ) {
            adminTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedAdminTab == index,
                    onClick = { selectedAdminTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedAdminTab == index) FontWeight.Black else FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (selectedAdminTab == index) StatusPurple else Slate400
                        )
                    },
                    modifier = Modifier.testTag("admin_tab_$index")
                )
            }
        }

        when (selectedAdminTab) {
            0 -> RegistrationsQueueTab(
                registrations = registrations,
                onApprove = { viewModel.approveRegistration(it.id) },
                onReject = { rejectDialogRegistration = it },
                onGenerateBracket = { viewModel.generateBracket() },
                approvedCount = approvedCount
            )
            1 -> LiveBracketAdminTab(
                tournament = tournament,
                matches = matches,
                selectedRound = selectedRound,
                onSelectRound = { viewModel.setRoundFilter(it) },
                onMatchClick = { selectedMatchForEdit = it },
                onGenerateBracket = { viewModel.generateBracket() }
            )
            2 -> TournamentSettingsTab(
                tournament = tournament,
                onSaveSettings = { title, fee, bkash, nagad, isOpen, duration ->
                    viewModel.updateTournamentSettings(title, fee, bkash, nagad, isOpen, duration)
                }
            )
            3 -> AdminStatsTab(
                registrations = registrations,
                tournament = tournament,
                pendingCount = pendingCount,
                approvedCount = approvedCount,
                completedMatchesCount = completedMatchesCount,
                totalMatches = matches.size
            )
        }
    }

    // Match Score & Status Editor Dialog
    if (selectedMatchForEdit != null) {
        MatchScoreEditDialog(
            match = selectedMatchForEdit!!,
            onDismiss = { selectedMatchForEdit = null },
            onSave = { p1Score, p2Score, status ->
                viewModel.updateMatchScore(selectedMatchForEdit!!.id, p1Score, p2Score, status)
                selectedMatchForEdit = null
            }
        )
    }

    // Rejection Reason Dialog
    if (rejectDialogRegistration != null) {
        RejectReasonDialog(
            registration = rejectDialogRegistration!!,
            onDismiss = { rejectDialogRegistration = null },
            onConfirmReject = { reason ->
                viewModel.rejectRegistration(rejectDialogRegistration!!.id, reason)
                rejectDialogRegistration = null
            }
        )
    }
}

@Composable
fun AdminUnauthorizedGatekeeper(
    onElevateToAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(StatusRose.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = StatusRose,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "ADMIN HOST ACCESS ONLY",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your current account does not have role: 'admin' in Firestore. Only authorized tournament administrators can verify payments and manage the draw.",
            color = Slate400,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        CyberButton(
            text = "Switch To Admin Host Role",
            onClick = onElevateToAdmin,
            icon = Icons.Default.AdminPanelSettings,
            testTag = "elevate_to_admin_btn"
        )
    }
}

@Composable
fun RegistrationsQueueTab(
    registrations: List<TournamentRegistration>,
    onApprove: (TournamentRegistration) -> Unit,
    onReject: (TournamentRegistration) -> Unit,
    onGenerateBracket: () -> Unit,
    approvedCount: Int
) {
    val pendingList = registrations.filter { it.status == RegistrationStatus.PENDING }
    val reviewedList = registrations.filter { it.status != RegistrationStatus.PENDING }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Quick Action Bar: Generate Matchmaking Bracket
        item {
            CyberGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = StatusPurple.copy(alpha = 0.6f),
                backgroundColor = Slate900
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "APPROVED PLAYERS: $approvedCount",
                            color = StatusEmerald,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Ready to generate 1v1 bracket",
                            color = Slate400,
                            fontSize = 11.sp
                        )
                    }

                    CyberButton(
                        text = "Seed Bracket",
                        onClick = onGenerateBracket,
                        icon = Icons.Default.AccountTree,
                        testTag = "generate_bracket_action_btn"
                    )
                }
            }
        }

        // Section: Pending Verifications
        item {
            Text(
                text = "PENDING PAYMENT VERIFICATIONS (${pendingList.size})",
                color = StatusAmber,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
        }

        if (pendingList.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Slate900
                ) {
                    Text(
                        text = "No pending payments in the queue. All submissions verified!",
                        color = Slate400,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(pendingList, key = { it.id }) { reg ->
                RegistrationQueueCard(
                    registration = reg,
                    onApprove = { onApprove(reg) },
                    onReject = { onReject(reg) }
                )
            }
        }

        // Section: Already Reviewed
        if (reviewedList.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "RECENTLY REVIEWED (${reviewedList.size})",
                    color = Slate400,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }

            items(reviewedList, key = { it.id }) { reg ->
                ReviewedRegistrationCard(registration = reg)
            }
        }
    }
}

@Composable
fun RegistrationQueueCard(
    registration: TournamentRegistration,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val methodColor = if (registration.paymentMethod == PaymentMethod.bKash) Color(0xFFE2136E) else Color(0xFFF7941D)

    CyberGlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = StatusAmber.copy(alpha = 0.5f),
        backgroundColor = Slate900
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = registration.fullName,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "eFootball: @${registration.inGameUsername} (ID: ${registration.inGameId})",
                        color = NeonCyanBright,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = methodColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, methodColor)
                ) {
                    Text(
                        text = registration.paymentMethod.name,
                        color = methodColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Slate950,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "TRANSACTION ID (TrxID)", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = registration.trxId,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = "${registration.feeAmount} BDT",
                        color = StatusEmerald,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Reject Button
                Button(
                    onClick = onReject,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("reject_reg_btn_${registration.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRose.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, StatusRose),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = StatusRose, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("REJECT", color = StatusRose, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }

                // Approve Button
                Button(
                    onClick = onApprove,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("approve_reg_btn_${registration.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusEmerald),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Slate950, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("APPROVE", color = Slate950, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ReviewedRegistrationCard(registration: TournamentRegistration) {
    CyberGlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = Slate800,
        backgroundColor = Slate900.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = registration.fullName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "${registration.paymentMethod} • TrxID: ${registration.trxId}",
                    color = Slate400,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            StatusBadge(status = registration.status)
        }
    }
}

@Composable
fun LiveBracketAdminTab(
    tournament: TournamentInfo,
    matches: List<TournamentMatch>,
    selectedRound: Int,
    onSelectRound: (Int) -> Unit,
    onMatchClick: (TournamentMatch) -> Unit,
    onGenerateBracket: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "CLICK ANY MATCH TO EDIT LIVE SCORE ✎",
                color = NeonCyanBright,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )

            TextButton(
                onClick = onGenerateBracket,
                modifier = Modifier.testTag("reseed_bracket_btn")
            ) {
                Text("RE-SEED DRAW", color = StatusPurple, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }

        BracketViewer(
            tournament = tournament,
            matches = matches,
            selectedRound = selectedRound,
            onSelectRound = onSelectRound,
            currentUserId = null,
            onMatchClick = onMatchClick
        )
    }
}

@Composable
fun TournamentSettingsTab(
    tournament: TournamentInfo,
    onSaveSettings: (String, Int, String, String, Boolean, Int) -> Unit
) {
    var title by remember(tournament) { mutableStateOf(tournament.title) }
    var fee by remember(tournament) { mutableStateOf(tournament.entryFee.toString()) }
    var bkash by remember(tournament) { mutableStateOf(tournament.bkashNumber) }
    var nagad by remember(tournament) { mutableStateOf(tournament.nagadNumber) }
    var isOpen by remember(tournament) { mutableStateOf(tournament.isRegistrationOpen) }
    var duration by remember(tournament) { mutableStateOf(tournament.matchDurationMinutes.toString()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            CyberGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = StatusPurple.copy(alpha = 0.5f),
                backgroundColor = Slate900
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "TOURNAMENT CONFIGURATION",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Championship Title", color = Slate400) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = StatusPurple,
                            unfocusedBorderColor = Slate700
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = fee,
                            onValueChange = { fee = it },
                            label = { Text("Entry Fee (BDT)", color = Slate400) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = StatusPurple,
                                unfocusedBorderColor = Slate700
                            )
                        )

                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("Match Duration (Mins)", color = Slate400) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = StatusPurple,
                                unfocusedBorderColor = Slate700
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = bkash,
                        onValueChange = { bkash = it },
                        label = { Text("bKash Personal Number", color = Slate400) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = StatusPurple,
                            unfocusedBorderColor = Slate700
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = nagad,
                        onValueChange = { nagad = it },
                        label = { Text("Nagad Personal Number", color = Slate400) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = StatusPurple,
                            unfocusedBorderColor = Slate700
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Registration Toggle Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate950, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Registration Open / Closed", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = if (isOpen) "Accepting new player payments" else "Registrations locked for bracket", color = Slate400, fontSize = 11.sp)
                        }
                        Switch(
                            checked = isOpen,
                            onCheckedChange = { isOpen = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = StatusEmerald,
                                uncheckedThumbColor = Slate400,
                                uncheckedTrackColor = Slate800
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CyberButton(
                        text = "Save Settings",
                        onClick = {
                            val parsedFee = fee.toIntOrNull() ?: 100
                            val parsedDur = duration.toIntOrNull() ?: 10
                            onSaveSettings(title, parsedFee, bkash, nagad, isOpen, parsedDur)
                        },
                        icon = Icons.Default.Save,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "save_settings_btn"
                    )
                }
            }
        }
    }
}

@Composable
fun AdminStatsTab(
    registrations: List<TournamentRegistration>,
    tournament: TournamentInfo,
    pendingCount: Int,
    approvedCount: Int,
    completedMatchesCount: Int,
    totalMatches: Int
) {
    val totalCollected = approvedCount * tournament.entryFee

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "CHAMPIONSHIP OVERVIEW METRICS",
                color = NeonCyanBright,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                letterSpacing = 1.sp
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminMetricCard(
                    title = "Total Registered",
                    value = registrations.size.toString(),
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Pending Queue",
                    value = pendingCount.toString(),
                    color = StatusAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminMetricCard(
                    title = "Approved Players",
                    value = approvedCount.toString(),
                    color = StatusEmerald,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Matches Played",
                    value = "$completedMatchesCount / $totalMatches",
                    color = StatusPurple,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            CyberGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = GoldCrown,
                backgroundColor = Slate900
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "TOTAL PRIZE POOL / COLLECTED FEES", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalCollected BDT",
                        color = GoldCrown,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Calculated from $approvedCount approved registrations @ ${tournament.entryFee} BDT",
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AdminMetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    CyberGlassCard(
        modifier = modifier,
        borderColor = color.copy(alpha = 0.4f),
        backgroundColor = Slate900
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, color = color, fontSize = 22.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun MatchScoreEditDialog(
    match: TournamentMatch,
    onDismiss: () -> Unit,
    onSave: (Int, Int, MatchStatus) -> Unit
) {
    var p1Score by remember { mutableIntStateOf(match.player1Score) }
    var p2Score by remember { mutableIntStateOf(match.player2Score) }
    var status by remember { mutableStateOf(match.status) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        title = {
            Text(
                text = "Edit Match Score (${match.startTime})",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Player 1 Score Controller
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = match.player1?.name ?: "Player 1",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (p1Score > 0) p1Score-- }) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = NeonCyanBright)
                        }
                        Text(
                            text = p1Score.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = { p1Score++ }) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = NeonCyanBright)
                        }
                    }
                }

                Divider(color = Slate800)

                // Player 2 Score Controller
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = match.player2?.name ?: "Player 2",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (p2Score > 0) p2Score-- }) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = NeonCyanBright)
                        }
                        Text(
                            text = p2Score.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = { p2Score++ }) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = NeonCyanBright)
                        }
                    }
                }

                Divider(color = Slate800)

                // Status Selector
                Text(text = "Set Match Status:", color = Slate400, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MatchStatus.values().forEach { st ->
                        val isSelected = status == st
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { status = st },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) NeonCyan.copy(alpha = 0.25f) else Slate800,
                            border = BorderStroke(1.dp, if (isSelected) NeonCyanBright else Slate700)
                        ) {
                            Text(
                                text = st.name,
                                color = if (isSelected) NeonCyanBright else Slate400,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(p1Score, p2Score, status) },
                modifier = Modifier.testTag("save_match_score_btn")
            ) {
                Text("APPLY & ADVANCE", color = NeonCyanBright, fontWeight = FontWeight.Black)
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
fun RejectReasonDialog(
    registration: TournamentRegistration,
    onDismiss: () -> Unit,
    onConfirmReject: (String) -> Unit
) {
    var reason by remember { mutableStateOf("Invalid Transaction ID or payment not received.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        title = {
            Text(
                text = "Reject Registration for ${registration.fullName}",
                color = StatusRose,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Provide a clear reason so the player can correct their payment or TrxID:",
                    color = Slate400,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Rejection Reason", color = Slate400) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = StatusRose,
                        unfocusedBorderColor = Slate700
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirmReject(reason) },
                modifier = Modifier.testTag("confirm_reject_btn")
            ) {
                Text("CONFIRM REJECT", color = StatusRose, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Slate400)
            }
        }
    )
}
