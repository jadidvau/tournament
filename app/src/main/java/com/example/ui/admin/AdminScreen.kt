package com.example.ui.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MatchPlayer
import com.example.data.model.MatchStatus
import com.example.data.model.ORGANIZER_EMAIL
import com.example.data.model.PaymentMethod
import com.example.data.model.RegistrationStatus
import com.example.data.model.TournamentInfo
import com.example.data.model.TournamentMatch
import com.example.data.model.TournamentRegistration
import com.example.data.model.TournamentRules
import com.example.data.model.UserRole
import com.example.ui.bracket.BracketViewer
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberGlassCard
import com.example.ui.components.MatchStatusBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.TournamentViewModel
import java.util.UUID

@Composable
fun AdminScreen(
    viewModel: TournamentViewModel,
    onReturnToPlayer: () -> Unit = {},
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
    val rules by viewModel.rules.collectAsState()

    var selectedAdminTab by remember { mutableIntStateOf(0) }

    // Dialog States
    var selectedMatchForEdit by remember { mutableStateOf<TournamentMatch?>(null) }
    var rejectDialogRegistration by remember { mutableStateOf<TournamentRegistration?>(null) }
    var editRegistrationDialog by remember { mutableStateOf<TournamentRegistration?>(null) }
    var deleteConfirmRegistration by remember { mutableStateOf<TournamentRegistration?>(null) }
    var showAddParticipantDialog by remember { mutableStateOf(false) }

    var showAddFixtureDialog by remember { mutableStateOf(false) }
    var rescheduleMatchDialog by remember { mutableStateOf<TournamentMatch?>(null) }
    var advanceWinnerMatchDialog by remember { mutableStateOf<TournamentMatch?>(null) }
    var deleteConfirmMatch by remember { mutableStateOf<TournamentMatch?>(null) }
    var showResetBracketConfirm by remember { mutableStateOf(false) }

    val isDesignatedAdmin = currentUser?.email?.trim().equals(ORGANIZER_EMAIL, ignoreCase = true) == true && currentUser?.role == UserRole.ADMIN

    // Admin Auth Gatekeeper Check
    if (!isDesignatedAdmin) {
        AdminUnauthorizedGatekeeper(
            onReturnToPlayer = onReturnToPlayer,
            modifier = modifier
        )
        return
    }

    val adminTabs = listOf(
        "Participants ($approvedCount)",
        "Bracket Control",
        "Match Management (${matches.size})",
        "Edit Rules",
        "Settings",
        "Stats"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        // Admin Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedAdminTab,
            containerColor = Slate900,
            contentColor = StatusPurple,
            edgePadding = 12.dp,
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
            0 -> ParticipantRosterTab(
                registrations = registrations,
                onApprove = { viewModel.approveRegistration(it.id) },
                onReject = { rejectDialogRegistration = it },
                onEdit = { editRegistrationDialog = it },
                onDelete = { deleteConfirmRegistration = it },
                onAddNewParticipant = { showAddParticipantDialog = true },
                onSeedBracket = { viewModel.generateBracket() }
            )
            1 -> BracketControlTab(
                tournament = tournament,
                matches = matches,
                selectedRound = selectedRound,
                onSelectRound = { viewModel.setRoundFilter(it) },
                onMatchClick = { selectedMatchForEdit = it },
                onSeedBracket = { viewModel.generateBracket() },
                onResetBracket = { showResetBracketConfirm = true }
            )
            2 -> MatchManagementTab(
                matches = matches,
                onAddFixture = { showAddFixtureDialog = true },
                onEditMatch = { selectedMatchForEdit = it },
                onRescheduleMatch = { rescheduleMatchDialog = it },
                onAdvanceWinner = { advanceWinnerMatchDialog = it },
                onDeleteMatch = { deleteConfirmMatch = it }
            )
            3 -> EditRulesAdminTab(
                rules = rules,
                onSaveAndBroadcast = { updatedRules ->
                    viewModel.updateRules(updatedRules)
                }
            )
            4 -> TournamentSettingsTab(
                tournament = tournament,
                onSaveSettings = { title, fee, bkash, nagad, isOpen, duration, prizePool ->
                    viewModel.updateTournamentSettings(
                        title = title,
                        entryFee = fee,
                        prizePool = prizePool,
                        bkashNumber = bkash,
                        nagadNumber = nagad,
                        isRegistrationOpen = isOpen,
                        matchDurationMinutes = duration
                    )
                }
            )
            5 -> AdminStatsTab(
                registrations = registrations,
                tournament = tournament,
                pendingCount = pendingCount,
                approvedCount = approvedCount,
                completedMatchesCount = completedMatchesCount,
                totalMatches = matches.size
            )
        }
    }

    // --- DIALOGS ---

    // 1. Add Participant Manually Dialog
    if (showAddParticipantDialog) {
        AddParticipantDialog(
            defaultFee = tournament.entryFee,
            onDismiss = { showAddParticipantDialog = false },
            onConfirm = { newReg ->
                viewModel.addParticipant(newReg)
                showAddParticipantDialog = false
            }
        )
    }

    // 2. Edit Participant Dialog
    if (editRegistrationDialog != null) {
        EditParticipantDialog(
            registration = editRegistrationDialog!!,
            onDismiss = { editRegistrationDialog = null },
            onSave = { updated ->
                viewModel.updateRegistration(updated)
                editRegistrationDialog = null
            }
        )
    }

    // 3. Delete Participant Confirm Dialog
    if (deleteConfirmRegistration != null) {
        AlertDialog(
            onDismissRequest = { deleteConfirmRegistration = null },
            containerColor = Slate900,
            title = {
                Text(
                    text = "Delete Participant Entry?",
                    color = StatusRose,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete player ${deleteConfirmRegistration!!.fullName} (TrxID: ${deleteConfirmRegistration!!.trxId})? This action cannot be undone.",
                    color = Slate200,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRegistration(deleteConfirmRegistration!!.id)
                        deleteConfirmRegistration = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRose)
                ) {
                    Text("DELETE", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmRegistration = null }) {
                    Text("CANCEL", color = Slate400)
                }
            }
        )
    }

    // 4. Add Match Fixture Dialog
    if (showAddFixtureDialog) {
        AddMatchFixtureDialog(
            registrations = registrations.filter { it.status == RegistrationStatus.JOINED },
            onDismiss = { showAddFixtureDialog = false },
            onAdd = { match ->
                viewModel.addMatchFixture(match)
                showAddFixtureDialog = false
            }
        )
    }

    // 5. Match Score & Status Editor Dialog
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

    // 6. Reschedule Match Dialog
    if (rescheduleMatchDialog != null) {
        RescheduleMatchDialog(
            match = rescheduleMatchDialog!!,
            onDismiss = { rescheduleMatchDialog = null },
            onReschedule = { newTime, newRound ->
                viewModel.rescheduleMatch(rescheduleMatchDialog!!.id, newTime, newRound)
                rescheduleMatchDialog = null
            }
        )
    }

    // 7. Advance Winner Dialog
    if (advanceWinnerMatchDialog != null) {
        AdvanceWinnerDialog(
            match = advanceWinnerMatchDialog!!,
            onDismiss = { advanceWinnerMatchDialog = null },
            onAdvance = { winnerId, p1Score, p2Score ->
                viewModel.advancePlayer(advanceWinnerMatchDialog!!.id, winnerId, p1Score, p2Score)
                advanceWinnerMatchDialog = null
            }
        )
    }

    // 8. Delete Match Confirm Dialog
    if (deleteConfirmMatch != null) {
        AlertDialog(
            onDismissRequest = { deleteConfirmMatch = null },
            containerColor = Slate900,
            title = {
                Text(
                    text = "Delete Match Fixture?",
                    color = StatusRose,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove match (${deleteConfirmMatch!!.player1?.name ?: "TBD"} vs ${deleteConfirmMatch!!.player2?.name ?: "TBD"}) at ${deleteConfirmMatch!!.startTime}?",
                    color = Slate200,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMatchFixture(deleteConfirmMatch!!.id)
                        deleteConfirmMatch = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRose)
                ) {
                    Text("REMOVE", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmMatch = null }) {
                    Text("CANCEL", color = Slate400)
                }
            }
        )
    }

    // 9. Reset Bracket Confirm Dialog
    if (showResetBracketConfirm) {
        AlertDialog(
            onDismissRequest = { showResetBracketConfirm = false },
            containerColor = Slate900,
            title = {
                Text(
                    text = "Reset Tournament Bracket?",
                    color = StatusRose,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Text(
                    text = "This will erase all generated match fixtures and reset the tournament draw. All approved participants will remain safely in the roster so you can re-seed at any time.",
                    color = Slate200,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetBracket()
                        showResetBracketConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRose)
                ) {
                    Text("RESET BRACKET", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetBracketConfirm = false }) {
                    Text("CANCEL", color = Slate400)
                }
            }
        )
    }

    // 10. Rejection Reason Dialog
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
    onReturnToPlayer: () -> Unit = {},
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
            text = "ADMIN ACCESS RESTRICTED",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "This dashboard is locked to tournament organizer $ORGANIZER_EMAIL only. Regular accounts are restricted to Player View.",
            color = Slate400,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        CyberButton(
            text = "Return to Player View",
            onClick = onReturnToPlayer,
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            testTag = "return_to_player_btn"
        )
    }
}

// -------------------------------------------------------------
// TAB 0: PARTICIPANT ROSTER (Search, Add, Approve, Edit, Delete)
// -------------------------------------------------------------
@Composable
fun ParticipantRosterTab(
    registrations: List<TournamentRegistration>,
    onApprove: (TournamentRegistration) -> Unit,
    onReject: (TournamentRegistration) -> Unit,
    onEdit: (TournamentRegistration) -> Unit,
    onDelete: (TournamentRegistration) -> Unit,
    onAddNewParticipant: () -> Unit,
    onSeedBracket: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("ALL") } // ALL, PENDING, APPROVED, REJECTED

    val filteredList = registrations.filter { reg ->
        val matchesSearch = searchQuery.isBlank() ||
                reg.fullName.contains(searchQuery, ignoreCase = true) ||
                reg.inGameUsername.contains(searchQuery, ignoreCase = true) ||
                reg.inGameId.contains(searchQuery, ignoreCase = true) ||
                reg.trxId.contains(searchQuery, ignoreCase = true) ||
                reg.phoneNumber.contains(searchQuery, ignoreCase = true)

        val matchesStatus = when (statusFilter) {
            "PENDING" -> reg.status == RegistrationStatus.PENDING
            "APPROVED" -> reg.status == RegistrationStatus.JOINED
            "REJECTED" -> reg.status == RegistrationStatus.REJECTED
            else -> true
        }

        matchesSearch && matchesStatus
    }

    val approvedCount = registrations.count { it.status == RegistrationStatus.JOINED }
    val pendingCount = registrations.count { it.status == RegistrationStatus.PENDING }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Quick Action Bar: Add Participant & Seed Bracket
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
                            text = "ROSTER: ${registrations.size} ($approvedCount Approved)",
                            color = StatusEmerald,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "$pendingCount pending verifications",
                            color = if (pendingCount > 0) StatusAmber else Slate400,
                            fontSize = 11.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onAddNewParticipant,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanDark),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("add_participant_btn")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = NeonCyanBright, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Player", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        CyberButton(
                            text = "Seed",
                            onClick = onSeedBracket,
                            icon = Icons.Default.AccountTree,
                            testTag = "generate_bracket_action_btn"
                        )
                    }
                }
            }
        }

        // Search Bar & Filter Chips
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by Name, In-Game Tag, ID, Phone, or TrxID...", color = Slate500, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyanBright)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Slate400)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_participant_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Slate200,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Slate700
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ALL", "PENDING", "APPROVED", "REJECTED").forEach { filterKey ->
                        val isSelected = statusFilter == filterKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { statusFilter = filterKey },
                            label = {
                                Text(
                                    text = when (filterKey) {
                                        "PENDING" -> "Pending ($pendingCount)"
                                        "APPROVED" -> "Approved ($approvedCount)"
                                        "REJECTED" -> "Rejected"
                                        else -> "All (${registrations.size})"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusPurple.copy(alpha = 0.3f),
                                selectedLabelColor = StatusPurple
                            )
                        )
                    }
                }
            }
        }

        // Participant Roster Cards
        if (filteredList.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Slate900
                ) {
                    Text(
                        text = "No participants found matching your criteria.",
                        color = Slate400,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(filteredList, key = { it.id }) { reg ->
                ParticipantRosterCard(
                    registration = reg,
                    onApprove = { onApprove(reg) },
                    onReject = { onReject(reg) },
                    onEdit = { onEdit(reg) },
                    onDelete = { onDelete(reg) }
                )
            }
        }
    }
}

@Composable
fun ParticipantRosterCard(
    registration: TournamentRegistration,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val methodColor = if (registration.paymentMethod == PaymentMethod.bKash) Color(0xFFE2136E) else Color(0xFFF7941D)

    CyberGlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = when (registration.status) {
            RegistrationStatus.PENDING -> StatusAmber.copy(alpha = 0.6f)
            RegistrationStatus.JOINED -> StatusEmerald.copy(alpha = 0.5f)
            RegistrationStatus.REJECTED -> StatusRose.copy(alpha = 0.5f)
        },
        backgroundColor = Slate900
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Player Name + Status Badge
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
                        fontSize = 15.sp
                    )
                    Text(
                        text = "eFootball: @${registration.inGameUsername} (ID: ${registration.inGameId})",
                        color = NeonCyanBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Phone: ${registration.phoneNumber}",
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }

                StatusBadge(status = registration.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Payment TrxID Box
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = methodColor.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, methodColor)
                            ) {
                                Text(
                                    text = registration.paymentMethod.name,
                                    color = methodColor,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TrxID: ${registration.trxId}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Text(
                        text = "${registration.feeAmount} BDT",
                        color = StatusEmerald,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Toolbar (Approve, Reject, Edit, Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (registration.status == RegistrationStatus.PENDING) {
                    // Quick Approve
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusEmerald),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Slate950, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve", color = Slate950, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }

                    // Quick Reject
                    OutlinedButton(
                        onClick = onReject,
                        border = BorderStroke(1.dp, StatusRose),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = StatusRose, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", color = StatusRose, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                // Edit Player & TrxID
                OutlinedButton(
                    onClick = onEdit,
                    border = BorderStroke(1.dp, Slate700),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = NeonCyanBright, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", color = Slate200, fontSize = 11.sp)
                }

                // Delete Entry
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusRose.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 1: BRACKET CONTROL (Seed, Reset, Advance Winner, Tree)
// -------------------------------------------------------------
@Composable
fun BracketControlTab(
    tournament: TournamentInfo,
    matches: List<TournamentMatch>,
    selectedRound: Int,
    onSelectRound: (Int) -> Unit,
    onMatchClick: (TournamentMatch) -> Unit,
    onSeedBracket: () -> Unit,
    onResetBracket: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        CyberGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            borderColor = StatusPurple.copy(alpha = 0.5f),
            backgroundColor = Slate900
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
                        text = "BRACKET CONTROLS",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Total Fixtures: ${matches.size} | Tap match to edit score",
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onResetBracket,
                        border = BorderStroke(1.dp, StatusRose),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("reset_bracket_btn")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, tint = StatusRose, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", color = StatusRose, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onSeedBracket,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusPurple),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("reseed_bracket_btn")
                    ) {
                        Icon(Icons.Default.AccountTree, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Seed Draw", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
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

// -------------------------------------------------------------
// TAB 2: MATCH MANAGEMENT (Add, Reschedule, Edit, Delete, Advance)
// -------------------------------------------------------------
@Composable
fun MatchManagementTab(
    matches: List<TournamentMatch>,
    onAddFixture: () -> Unit,
    onEditMatch: (TournamentMatch) -> Unit,
    onRescheduleMatch: (TournamentMatch) -> Unit,
    onAdvanceWinner: (TournamentMatch) -> Unit,
    onDeleteMatch: (TournamentMatch) -> Unit
) {
    var filterRound by remember { mutableIntStateOf(0) } // 0 = All

    val filteredMatches = if (filterRound == 0) matches else matches.filter { it.round == filterRound }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header & Add Fixture Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "MATCH FIXTURES & SCHEDULE",
                        color = NeonCyanBright,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Manage schedules, live scores, and winner advancements",
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = onAddFixture,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("admin_add_fixture_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Slate950, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Fixture", color = Slate950, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }

        // Round Selector Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filterRound == 0,
                        onClick = { filterRound = 0 },
                        label = { Text("All Rounds (${matches.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StatusPurple.copy(alpha = 0.3f), selectedLabelColor = StatusPurple)
                    )
                }
                listOf(1 to "Round 1", 2 to "Quarter-Finals", 3 to "Semi-Finals", 4 to "Grand Final").forEach { (r, name) ->
                    val count = matches.count { it.round == r }
                    item {
                        FilterChip(
                            selected = filterRound == r,
                            onClick = { filterRound = r },
                            label = { Text("$name ($count)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StatusPurple.copy(alpha = 0.3f), selectedLabelColor = StatusPurple)
                        )
                    }
                }
            }
        }

        if (filteredMatches.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Slate900
                ) {
                    Text(
                        text = "No match fixtures found in this round. Tap 'Add Fixture' or 'Seed Bracket' to generate matches.",
                        color = Slate400,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(filteredMatches, key = { it.id }) { match ->
                AdminMatchFixtureCard(
                    match = match,
                    onEdit = { onEditMatch(match) },
                    onReschedule = { onRescheduleMatch(match) },
                    onAdvance = { onAdvanceWinner(match) },
                    onDelete = { onDeleteMatch(match) }
                )
            }
        }
    }
}

@Composable
fun AdminMatchFixtureCard(
    match: TournamentMatch,
    onEdit: () -> Unit,
    onReschedule: () -> Unit,
    onAdvance: () -> Unit,
    onDelete: () -> Unit
) {
    CyberGlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = when (match.status) {
            MatchStatus.LIVE -> StatusRose
            MatchStatus.COMPLETED -> StatusEmerald.copy(alpha = 0.6f)
            else -> Slate700
        },
        backgroundColor = Slate900
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Round, Match #, Time, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = StatusPurple.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, StatusPurple.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "R${match.round} M#${match.matchIndex}",
                            color = StatusPurple,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = match.startTime,
                        color = Slate300,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                MatchStatusBadge(status = match.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Score Board
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Slate950,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Player 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (match.winnerId != null && match.winnerId == match.player1?.id) {
                                Icon(Icons.Default.MilitaryTech, contentDescription = "Winner", tint = StatusGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = match.player1?.name ?: "TBD",
                                color = if (match.player1 != null) Color.White else Slate500,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = match.player1Score.toString(),
                            color = NeonCyanBright,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }

                    Divider(color = Slate800, modifier = Modifier.padding(vertical = 6.dp))

                    // Player 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (match.winnerId != null && match.winnerId == match.player2?.id) {
                                Icon(Icons.Default.MilitaryTech, contentDescription = "Winner", tint = StatusGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = match.player2?.name ?: "TBD",
                                color = if (match.player2 != null) Color.White else Slate500,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = match.player2Score.toString(),
                            color = NeonCyanBright,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Edit Score, Reschedule, Advance Winner, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Edit Scores
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanDark),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = NeonCyanBright, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Score", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Reschedule Time
                OutlinedButton(
                    onClick = onReschedule,
                    border = BorderStroke(1.dp, Slate700),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Slate300, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Time", color = Slate200, fontSize = 11.sp)
                }

                // Advance Winner
                if (match.status != MatchStatus.COMPLETED && match.player1 != null && match.player2 != null) {
                    Button(
                        onClick = onAdvance,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusEmerald),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Text("Advance", color = Slate950, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }

                // Delete Fixture
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusRose.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: EDIT RULES (Firestore settings/rules + Broadcast)
// -------------------------------------------------------------
@Composable
fun EditRulesAdminTab(
    rules: TournamentRules,
    onSaveAndBroadcast: (TournamentRules) -> Unit
) {
    var matchDuration by remember(rules) { mutableStateOf(rules.matchDuration) }
    var extraTimePk by remember(rules) { mutableStateOf(rules.extraTimePk) }
    var substitutions by remember(rules) { mutableStateOf(rules.substitutions) }
    var rematchRule by remember(rules) { mutableStateOf(rules.rematchRule) }
    var walkoverGrace by remember(rules) { mutableStateOf(rules.walkoverGrace) }

    var matchSettingsDetails by remember(rules) { mutableStateOf(rules.matchSettingsDetails) }
    var squadFairPlayDetails by remember(rules) { mutableStateOf(rules.squadFairPlayDetails) }
    var networkDisputesDetails by remember(rules) { mutableStateOf(rules.networkDisputesDetails) }
    var scoreReportingDetails by remember(rules) { mutableStateOf(rules.scoreReportingDetails) }
    var punctualityConductDetails by remember(rules) { mutableStateOf(rules.punctualityConductDetails) }
    var organizerContact by remember(rules) { mutableStateOf(rules.organizerContact) }

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
                        text = "TOURNAMENT RULES & REGULATIONS",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Firestore doc: settings/rules • Real-time broadcast to all players",
                        color = NeonCyanBright,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "QUICK BADGES (Live on Player Modal)",
                        color = Slate400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = matchDuration,
                            onValueChange = { matchDuration = it },
                            label = { Text("Match Duration", color = Slate400) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = StatusPurple, unfocusedBorderColor = Slate700)
                        )

                        OutlinedTextField(
                            value = extraTimePk,
                            onValueChange = { extraTimePk = it },
                            label = { Text("Extra Time & PK", color = Slate400) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = StatusPurple, unfocusedBorderColor = Slate700)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = substitutions,
                            onValueChange = { substitutions = it },
                            label = { Text("Substitutions", color = Slate400) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = StatusPurple, unfocusedBorderColor = Slate700)
                        )

                        OutlinedTextField(
                            value = walkoverGrace,
                            onValueChange = { walkoverGrace = it },
                            label = { Text("Walkover Grace", color = Slate400) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = StatusPurple, unfocusedBorderColor = Slate700)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = rematchRule,
                        onValueChange = { rematchRule = it },
                        label = { Text("Disconnection Rematch Rule", color = Slate400) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = StatusPurple, unfocusedBorderColor = Slate700)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "DETAILED REGULATION CLAUSES (2-COLUMN CARDS)",
                        color = Slate400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = matchSettingsDetails,
                        onValueChange = { matchSettingsDetails = it },
                        label = { Text("Match Settings Clause", color = Slate400) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = StatusPurple, unfocusedBorderColor = Slate700)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = squadFairPlayDetails,
                        onValueChange = { squadFairPlayDetails = it },
                        label = { Text("Squad & Fair Play Clause", color = Slate400) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = StatusPurple, unfocusedBorderColor = Slate700)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = networkDisputesDetails,
                        onValueChange = { networkDisputesDetails = it },
                        label = { Text("Network & Disconnections Clause", color = Slate400) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = StatusPurple, unfocusedBorderColor = Slate700)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = scoreReportingDetails,
                        onValueChange = { scoreReportingDetails = it },
                        label = { Text("Score Reporting & Screenshot Clause", color = Slate400) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = StatusPurple, unfocusedBorderColor = Slate700)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = punctualityConductDetails,
                        onValueChange = { punctualityConductDetails = it },
                        label = { Text("Punctuality, Walkover & Conduct Clause", color = Slate400) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = StatusPurple, unfocusedBorderColor = Slate700)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = organizerContact,
                        onValueChange = { organizerContact = it },
                        label = { Text("Official Organizer Contact & Support", color = Slate400) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = StatusPurple, unfocusedBorderColor = Slate700)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    CyberButton(
                        text = "Save & Broadcast to Firestore",
                        onClick = {
                            val updated = rules.copy(
                                matchDuration = matchDuration.trim(),
                                extraTimePk = extraTimePk.trim(),
                                substitutions = substitutions.trim(),
                                rematchRule = rematchRule.trim(),
                                walkoverGrace = walkoverGrace.trim(),
                                matchSettingsDetails = matchSettingsDetails.trim(),
                                squadFairPlayDetails = squadFairPlayDetails.trim(),
                                networkDisputesDetails = networkDisputesDetails.trim(),
                                scoreReportingDetails = scoreReportingDetails.trim(),
                                punctualityConductDetails = punctualityConductDetails.trim(),
                                organizerContact = organizerContact.trim()
                            )
                            onSaveAndBroadcast(updated)
                        },
                        icon = Icons.AutoMirrored.Filled.Send,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_broadcast_rules_btn")
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 4: TOURNAMENT SETTINGS (Title, Fee, Prize Pool, bKash, Nagad)
// -------------------------------------------------------------
@Composable
fun TournamentSettingsTab(
    tournament: TournamentInfo,
    onSaveSettings: (String, Int, String, String, Boolean, Int, String) -> Unit
) {
    var title by remember(tournament) { mutableStateOf(tournament.title) }
    var prizePool by remember(tournament) { mutableStateOf(tournament.prizePool) }
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
                    Text(
                        text = "Synced directly to Firebase Firestore tournaments/dhaka_efootball_2026",
                        color = NeonCyanBright,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Championship Title", color = Slate400) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = StatusPurple, unfocusedBorderColor = Slate700)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = prizePool,
                        onValueChange = { prizePool = it },
                        label = { Text("Prize Pool (BDT)", color = Slate400) },
                        placeholder = { Text("e.g. 15,000 BDT") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = StatusPurple, unfocusedBorderColor = Slate700)
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
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = StatusPurple, unfocusedBorderColor = Slate700)
                        )

                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("Match Duration (Mins)", color = Slate400) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = StatusPurple, unfocusedBorderColor = Slate700)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = bkash,
                        onValueChange = { bkash = it },
                        label = { Text("bKash Personal Number", color = Slate400) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = StatusPurple, unfocusedBorderColor = Slate700)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = nagad,
                        onValueChange = { nagad = it },
                        label = { Text("Nagad Personal Number", color = Slate400) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = StatusPurple, unfocusedBorderColor = Slate700)
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
                        text = "Save Settings & Sync to Firestore",
                        onClick = {
                            val parsedFee = fee.toIntOrNull() ?: 100
                            val parsedDur = duration.toIntOrNull() ?: 10
                            onSaveSettings(title, parsedFee, bkash, nagad, isOpen, parsedDur, prizePool)
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

// -------------------------------------------------------------
// TAB 5: STATS & OVERVIEW
// -------------------------------------------------------------
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
                borderColor = StatusEmerald.copy(alpha = 0.5f),
                backgroundColor = Slate900
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Total Collected Fees", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalCollected BDT",
                        color = StatusEmerald,
                        fontSize = 24.sp,
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

// -------------------------------------------------------------
// DIALOGS: ADD / EDIT / RESCHEDULE / ADVANCE
// -------------------------------------------------------------

@Composable
fun AddParticipantDialog(
    defaultFee: Int,
    onDismiss: () -> Unit,
    onConfirm: (TournamentRegistration) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+8801") }
    var inGameUsername by remember { mutableStateOf("") }
    var inGameId by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.bKash) }
    var trxId by remember { mutableStateOf("MANUAL-${(1000..9999).random()}") }
    var feeAmount by remember { mutableStateOf(defaultFee.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        title = {
            Text(text = "Add Participant Manually", color = Color.White, fontWeight = FontWeight.Black)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Legal Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Slate200, focusedBorderColor = NeonCyan, unfocusedBorderColor = Slate700)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Slate200, focusedBorderColor = NeonCyan, unfocusedBorderColor = Slate700)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = inGameUsername,
                        onValueChange = { inGameUsername = it },
                        label = { Text("eFootball Tag") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Slate200, focusedBorderColor = NeonCyan, unfocusedBorderColor = Slate700)
                    )

                    OutlinedTextField(
                        value = inGameId,
                        onValueChange = { inGameId = it },
                        label = { Text("Owner ID") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Slate200, focusedBorderColor = NeonCyan, unfocusedBorderColor = Slate700)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = trxId,
                        onValueChange = { trxId = it },
                        label = { Text("TrxID") },
                        modifier = Modifier.weight(1.2f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Slate200, focusedBorderColor = NeonCyan, unfocusedBorderColor = Slate700)
                    )

                    OutlinedTextField(
                        value = feeAmount,
                        onValueChange = { feeAmount = it },
                        label = { Text("Fee (BDT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.8f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Slate200, focusedBorderColor = NeonCyan, unfocusedBorderColor = Slate700)
                    )
                }

                // Payment Method Selector
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentMethod.values().forEach { method ->
                        val isSelected = paymentMethod == method
                        FilterChip(
                            selected = isSelected,
                            onClick = { paymentMethod = method },
                            label = { Text(method.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isNotBlank()) {
                        val reg = TournamentRegistration(
                            id = "reg_${UUID.randomUUID().toString().take(8)}",
                            userId = "user_manual_${UUID.randomUUID().toString().take(6)}",
                            fullName = fullName.trim(),
                            phoneNumber = phone.trim(),
                            inGameUsername = inGameUsername.trim().ifEmpty { fullName.trim() },
                            inGameId = inGameId.trim().ifEmpty { "${(100..999).random()}-${(100..999).random()}-${(100..999).random()}" },
                            paymentMethod = paymentMethod,
                            trxId = trxId.trim(),
                            feeAmount = feeAmount.toIntOrNull() ?: defaultFee,
                            status = RegistrationStatus.JOINED,
                            submittedAt = System.currentTimeMillis()
                        )
                        onConfirm(reg)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Slate950)
            ) {
                Text("ADD TO ROSTER", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = Slate400) }
        }
    )
}

@Composable
fun EditParticipantDialog(
    registration: TournamentRegistration,
    onDismiss: () -> Unit,
    onSave: (TournamentRegistration) -> Unit
) {
    var fullName by remember { mutableStateOf(registration.fullName) }
    var phone by remember { mutableStateOf(registration.phoneNumber) }
    var inGameUsername by remember { mutableStateOf(registration.inGameUsername) }
    var inGameId by remember { mutableStateOf(registration.inGameId) }
    var paymentMethod by remember { mutableStateOf(registration.paymentMethod) }
    var trxId by remember { mutableStateOf(registration.trxId) }
    var feeAmount by remember { mutableStateOf(registration.feeAmount.toString()) }
    var status by remember { mutableStateOf(registration.status) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        title = {
            Text(text = "Edit Participant Entry", color = Color.White, fontWeight = FontWeight.Black)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Legal Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Slate200, focusedBorderColor = StatusPurple, unfocusedBorderColor = Slate700)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Slate200, focusedBorderColor = StatusPurple, unfocusedBorderColor = Slate700)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = inGameUsername,
                        onValueChange = { inGameUsername = it },
                        label = { Text("eFootball Tag") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Slate200, focusedBorderColor = StatusPurple, unfocusedBorderColor = Slate700)
                    )

                    OutlinedTextField(
                        value = inGameId,
                        onValueChange = { inGameId = it },
                        label = { Text("Owner ID") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Slate200, focusedBorderColor = StatusPurple, unfocusedBorderColor = Slate700)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = trxId,
                        onValueChange = { trxId = it },
                        label = { Text("TrxID") },
                        modifier = Modifier.weight(1.2f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Slate200, focusedBorderColor = StatusPurple, unfocusedBorderColor = Slate700)
                    )

                    OutlinedTextField(
                        value = feeAmount,
                        onValueChange = { feeAmount = it },
                        label = { Text("Fee (BDT)") },
                        modifier = Modifier.weight(0.8f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Slate200, focusedBorderColor = StatusPurple, unfocusedBorderColor = Slate700)
                    )
                }

                // Status Chips
                Text(text = "Registration Status:", color = Slate400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RegistrationStatus.values().forEach { st ->
                        val isSelected = status == st
                        FilterChip(
                            selected = isSelected,
                            onClick = { status = st },
                            label = { Text(st.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = registration.copy(
                        fullName = fullName.trim(),
                        phoneNumber = phone.trim(),
                        inGameUsername = inGameUsername.trim(),
                        inGameId = inGameId.trim(),
                        paymentMethod = paymentMethod,
                        trxId = trxId.trim(),
                        feeAmount = feeAmount.toIntOrNull() ?: registration.feeAmount,
                        status = status
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = StatusPurple)
            ) {
                Text("SAVE CHANGES", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = Slate400) }
        }
    )
}

@Composable
fun AddMatchFixtureDialog(
    registrations: List<TournamentRegistration>,
    onDismiss: () -> Unit,
    onAdd: (TournamentMatch) -> Unit
) {
    var round by remember { mutableIntStateOf(1) }
    var matchIndex by remember { mutableIntStateOf(1) }
    var startTime by remember { mutableStateOf("08:00 PM (BST)") }
    var p1Name by remember { mutableStateOf(registrations.firstOrNull()?.fullName ?: "Player 1") }
    var p2Name by remember { mutableStateOf(registrations.drop(1).firstOrNull()?.fullName ?: "Player 2") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        title = {
            Text(text = "Create Match Fixture", color = Color.White, fontWeight = FontWeight.Black)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = round.toString(),
                        onValueChange = { round = it.toIntOrNull() ?: 1 },
                        label = { Text("Round (1-4)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Slate200, focusedBorderColor = NeonCyan, unfocusedBorderColor = Slate700)
                    )

                    OutlinedTextField(
                        value = matchIndex.toString(),
                        onValueChange = { matchIndex = it.toIntOrNull() ?: 1 },
                        label = { Text("Match #") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Slate200, focusedBorderColor = NeonCyan, unfocusedBorderColor = Slate700)
                    )
                }

                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Start Time") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Slate200, focusedBorderColor = NeonCyan, unfocusedBorderColor = Slate700)
                )

                OutlinedTextField(
                    value = p1Name,
                    onValueChange = { p1Name = it },
                    label = { Text("Player 1 Name / eFootball Tag") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Slate200, focusedBorderColor = NeonCyan, unfocusedBorderColor = Slate700)
                )

                OutlinedTextField(
                    value = p2Name,
                    onValueChange = { p2Name = it },
                    label = { Text("Player 2 Name / eFootball Tag") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Slate200, focusedBorderColor = NeonCyan, unfocusedBorderColor = Slate700)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p1Reg = registrations.find { it.fullName.equals(p1Name, true) }
                    val p2Reg = registrations.find { it.fullName.equals(p2Name, true) }

                    val match = TournamentMatch(
                        id = "match_${UUID.randomUUID().toString().take(8)}",
                        tournamentId = "dhaka_efootball_2026",
                        round = round,
                        matchIndex = matchIndex,
                        player1 = MatchPlayer(id = p1Reg?.userId ?: "p1_${UUID.randomUUID().toString().take(4)}", name = p1Name, inGameUsername = p1Reg?.inGameUsername ?: p1Name, inGameId = p1Reg?.inGameId ?: "ID-001"),
                        player2 = MatchPlayer(id = p2Reg?.userId ?: "p2_${UUID.randomUUID().toString().take(4)}", name = p2Name, inGameUsername = p2Reg?.inGameUsername ?: p2Name, inGameId = p2Reg?.inGameId ?: "ID-002"),
                        player1Score = 0,
                        player2Score = 0,
                        status = MatchStatus.SCHEDULED,
                        startTime = startTime.trim(),
                        winnerId = null
                    )
                    onAdd(match)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Slate950)
            ) {
                Text("ADD FIXTURE", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = Slate400) }
        }
    )
}

@Composable
fun RescheduleMatchDialog(
    match: TournamentMatch,
    onDismiss: () -> Unit,
    onReschedule: (newTime: String, newRound: Int) -> Unit
) {
    var newTime by remember { mutableStateOf(match.startTime) }
    var newRound by remember { mutableIntStateOf(match.round) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        title = {
            Text(text = "Reschedule Match", color = Color.White, fontWeight = FontWeight.Black)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${match.player1?.name ?: "TBD"} vs ${match.player2?.name ?: "TBD"}",
                    color = NeonCyanBright,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = newTime,
                    onValueChange = { newTime = it },
                    label = { Text("New Scheduled Start Time") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Slate200, focusedBorderColor = NeonCyan, unfocusedBorderColor = Slate700)
                )

                OutlinedTextField(
                    value = newRound.toString(),
                    onValueChange = { newRound = it.toIntOrNull() ?: match.round },
                    label = { Text("Round Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Slate200, focusedBorderColor = NeonCyan, unfocusedBorderColor = Slate700)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onReschedule(newTime.trim(), newRound) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Slate950)
            ) {
                Text("CONFIRM RESCHEDULE", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = Slate400) }
        }
    )
}

@Composable
fun AdvanceWinnerDialog(
    match: TournamentMatch,
    onDismiss: () -> Unit,
    onAdvance: (winnerId: String, p1Score: Int, p2Score: Int) -> Unit
) {
    var selectedWinnerId by remember { mutableStateOf(match.player1?.id ?: "") }
    var p1Score by remember { mutableIntStateOf(if (match.player1Score > 0) match.player1Score else 2) }
    var p2Score by remember { mutableIntStateOf(match.player2Score) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        title = {
            Text(text = "Advance Winner to Next Round", color = Color.White, fontWeight = FontWeight.Black)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Select the match winner and enter final scores:",
                    color = Slate400,
                    fontSize = 12.sp
                )

                // Winner selector buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Player 1 option
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                selectedWinnerId = match.player1?.id ?: ""
                                if (p1Score <= p2Score) p1Score = p2Score + 1
                            }
                            .border(1.dp, if (selectedWinnerId == match.player1?.id) StatusEmerald else Slate700, RoundedCornerShape(8.dp)),
                        color = if (selectedWinnerId == match.player1?.id) StatusEmerald.copy(alpha = 0.2f) else Slate800
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = match.player1?.name ?: "P1", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                            Text(text = "WINNER", color = if (selectedWinnerId == match.player1?.id) StatusEmerald else Slate500, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    // Player 2 option
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                selectedWinnerId = match.player2?.id ?: ""
                                if (p2Score <= p1Score) p2Score = p1Score + 1
                            }
                            .border(1.dp, if (selectedWinnerId == match.player2?.id) StatusEmerald else Slate700, RoundedCornerShape(8.dp)),
                        color = if (selectedWinnerId == match.player2?.id) StatusEmerald.copy(alpha = 0.2f) else Slate800
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = match.player2?.name ?: "P2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                            Text(text = "WINNER", color = if (selectedWinnerId == match.player2?.id) StatusEmerald else Slate500, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                // Final Score controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "${match.player1?.name}: $p1Score", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row {
                        IconButton(onClick = { if (p1Score > 0) p1Score-- }) { Icon(Icons.Default.Remove, contentDescription = null, tint = NeonCyanBright) }
                        IconButton(onClick = { p1Score++ }) { Icon(Icons.Default.Add, contentDescription = null, tint = NeonCyanBright) }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "${match.player2?.name}: $p2Score", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row {
                        IconButton(onClick = { if (p2Score > 0) p2Score-- }) { Icon(Icons.Default.Remove, contentDescription = null, tint = NeonCyanBright) }
                        IconButton(onClick = { p2Score++ }) { Icon(Icons.Default.Add, contentDescription = null, tint = NeonCyanBright) }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdvance(selectedWinnerId, p1Score, p2Score) },
                colors = ButtonDefaults.buttonColors(containerColor = StatusEmerald, contentColor = Slate950)
            ) {
                Text("ADVANCE WINNER", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = Slate400) }
        }
    )
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
