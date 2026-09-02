package com.example.data.repository

import android.util.Log
import com.example.data.bracket.BracketEngine
import com.example.data.model.AppNotification
import com.example.data.model.MatchPlayer
import com.example.data.model.MatchStatus
import com.example.data.model.ORGANIZER_EMAIL
import com.example.data.model.PaymentMethod
import com.example.data.model.RegistrationStatus
import com.example.data.model.TournamentInfo
import com.example.data.model.TournamentMatch
import com.example.data.model.TournamentRegistration
import com.example.data.model.TournamentRules
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class FirebaseRepository {
    private val tag = "FirebaseRepository"

    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    // Real-time StateFlows
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _tournament = MutableStateFlow(TournamentInfo())
    val tournament: StateFlow<TournamentInfo> = _tournament.asStateFlow()

    private val _registrations = MutableStateFlow<List<TournamentRegistration>>(emptyList())
    val registrations: StateFlow<List<TournamentRegistration>> = _registrations.asStateFlow()

    private val _matches = MutableStateFlow<List<TournamentMatch>>(emptyList())
    val matches: StateFlow<List<TournamentMatch>> = _matches.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _rules = MutableStateFlow(TournamentRules())
    val rules: StateFlow<TournamentRules> = _rules.asStateFlow()

    private val listeners = mutableListOf<ListenerRegistration>()
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        try {
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            setupFirestoreListeners()
        } catch (e: Exception) {
            Log.w(tag, "Firebase not initialized, using local reactive state fallback: ${e.message}")
        }
        seedInitialData()
    }

    private fun setupFirestoreListeners() {
        val db = firestore ?: return

        // 1. Tournaments listener
        try {
            val tListener = db.collection("tournaments").document("dhaka_efootball_2026")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(tag, "Tournament listener error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val t = snapshot.toObject(TournamentInfo::class.java)
                        if (t != null) {
                            _tournament.value = t
                        }
                    }
                }
            listeners.add(tListener)
        } catch (e: Exception) {
            Log.w(tag, "Failed to attach tournament listener: ${e.message}")
        }

        // 2. Registrations listener
        try {
            val rListener = db.collection("registrations")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(tag, "Registrations listener error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { it.toObject(TournamentRegistration::class.java) }
                        if (list.isNotEmpty()) {
                            _registrations.value = list
                        }
                    }
                }
            listeners.add(rListener)
        } catch (e: Exception) {
            Log.w(tag, "Failed to attach registrations listener: ${e.message}")
        }

        // 3. Matches listener
        try {
            val mListener = db.collection("matches")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(tag, "Matches listener error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { it.toObject(TournamentMatch::class.java) }
                        if (list.isNotEmpty()) {
                            _matches.value = list
                        }
                    }
                }
            listeners.add(mListener)
        } catch (e: Exception) {
            Log.w(tag, "Failed to attach matches listener: ${e.message}")
        }

        // 4. Tournament Rules listener (settings/rules)
        try {
            val rulesListener = db.collection("settings").document("rules")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(tag, "Rules listener error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val r = snapshot.toObject(TournamentRules::class.java)
                        if (r != null) {
                            _rules.value = r
                            Log.d(tag, "Rules updated in real-time from settings/rules: ${r.matchDuration}, ${r.extraTimePk}")
                        }
                    }
                }
            listeners.add(rulesListener)
        } catch (e: Exception) {
            Log.w(tag, "Failed to attach rules listener: ${e.message}")
        }
    }

    private fun seedInitialData() {
        val defaultAdmin = UserProfile(
            uid = "admin_super_01",
            fullName = "Dhaka eFootball Host",
            phoneNumber = "+8801904031478",
            inGameId = "EF-HOST-01",
            inGameUsername = "DhakaHostAdmin",
            role = UserRole.ADMIN
        )
        
        // Seed default tournament
        val defaultTourney = TournamentInfo(
            id = "dhaka_efootball_2026",
            title = "Dhaka eFootball Open Championship",
            entryFee = 100,
            bkashNumber = "01904031478",
            nagadNumber = "01904031478",
            isRegistrationOpen = true,
            matchDurationMinutes = 10,
            totalRounds = 3
        )
        _tournament.value = defaultTourney

        // Pre-populate approved players and 1 pending player for realistic testing
        val sampleRegistrations = listOf(
            TournamentRegistration(
                id = "reg_001",
                userId = "user_tanvir",
                fullName = "Tanvir Ahmed",
                phoneNumber = "+8801711223344",
                inGameId = "109-882-901",
                inGameUsername = "ProGamer_BD",
                paymentMethod = PaymentMethod.bKash,
                trxId = "9J28A190KZ",
                status = RegistrationStatus.JOINED,
                feeAmount = 100,
                submittedAt = System.currentTimeMillis() - 86400000L,
                reviewedAt = System.currentTimeMillis() - 80000000L,
                reviewedBy = "admin_super_01"
            ),
            TournamentRegistration(
                id = "reg_002",
                userId = "user_shakib",
                fullName = "Shakib Al Hasan",
                phoneNumber = "+8801819998877",
                inGameId = "441-290-332",
                inGameUsername = "DhakaStriker",
                paymentMethod = PaymentMethod.Nagad,
                trxId = "NG8821BBAQ",
                status = RegistrationStatus.JOINED,
                feeAmount = 100,
                submittedAt = System.currentTimeMillis() - 72000000L,
                reviewedAt = System.currentTimeMillis() - 70000000L,
                reviewedBy = "admin_super_01"
            ),
            TournamentRegistration(
                id = "reg_003",
                userId = "user_rifat",
                fullName = "Rifat Hossain",
                phoneNumber = "+8801912345678",
                inGameId = "889-112-409",
                inGameUsername = "BengalTiger",
                paymentMethod = PaymentMethod.bKash,
                trxId = "BK7734MM10",
                status = RegistrationStatus.JOINED,
                feeAmount = 100,
                submittedAt = System.currentTimeMillis() - 60000000L,
                reviewedAt = System.currentTimeMillis() - 55000000L,
                reviewedBy = "admin_super_01"
            ),
            TournamentRegistration(
                id = "reg_004",
                userId = "user_zubair",
                fullName = "Zubair Rahman",
                phoneNumber = "+8801555667788",
                inGameId = "302-887-190",
                inGameUsername = "eF_King_24",
                paymentMethod = PaymentMethod.Nagad,
                trxId = "NG190288KC",
                status = RegistrationStatus.JOINED,
                feeAmount = 100,
                submittedAt = System.currentTimeMillis() - 50000000L,
                reviewedAt = System.currentTimeMillis() - 48000000L,
                reviewedBy = "admin_super_01"
            ),
            TournamentRegistration(
                id = "reg_005",
                userId = "user_me",
                fullName = "Jadid Nogorigang",
                phoneNumber = "+8801904031478",
                inGameId = "772-990-123",
                inGameUsername = "CyberStriker_BD",
                paymentMethod = PaymentMethod.bKash,
                trxId = "BK9900AABB",
                status = RegistrationStatus.JOINED,
                feeAmount = 100,
                submittedAt = System.currentTimeMillis() - 36000000L,
                reviewedAt = System.currentTimeMillis() - 30000000L,
                reviewedBy = "admin_super_01"
            ),
            TournamentRegistration(
                id = "reg_006",
                userId = "user_nafis",
                fullName = "Nafis Imtiaz",
                phoneNumber = "+8801611002233",
                inGameId = "551-998-212",
                inGameUsername = "DhakaBlaze",
                paymentMethod = PaymentMethod.bKash,
                trxId = "BK4455FF12",
                status = RegistrationStatus.PENDING,
                feeAmount = 100,
                submittedAt = System.currentTimeMillis() - 1200000L
            )
        )
        _registrations.value = sampleRegistrations

        // Initial state is unauthenticated (logged out)
        _currentUser.value = null

        // Generate initial bracket with joined players
        val approved = sampleRegistrations.filter { it.status == RegistrationStatus.JOINED }
        val (generatedMatches, updatedTourney) = BracketEngine.generateBracket(approved, defaultTourney)
        _matches.value = generatedMatches
        _tournament.value = updatedTourney

        // Seed initial notifications
        _notifications.value = listOf(
            AppNotification(
                title = "Registration Confirmed",
                message = "Your bKash TrxID BK9900AABB has been verified. You are in the Dhaka eFootball Championship bracket!"
            ),
            AppNotification(
                title = "Match Assigned",
                message = "Round 1 match is live! Check My Match for opponent details and room rules."
            )
        )
    }

    // User authentication and profile
    fun login(emailOrPhone: String, role: UserRole, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (emailOrPhone.isBlank()) {
            onError("Please enter your email or phone number")
            return
        }

        val uid = if (role == UserRole.ADMIN) "admin_super_01" else "user_" + UUID.randomUUID().toString().take(6)
        val profile = UserProfile(
            uid = uid,
            fullName = if (role == UserRole.ADMIN) "Dhaka eFootball Host" else "Player (${emailOrPhone.take(8)})",
            phoneNumber = if (emailOrPhone.startsWith("+880") || emailOrPhone.startsWith("01")) emailOrPhone else "+8801904031478",
            inGameId = if (role == UserRole.ADMIN) "EF-ADMIN-01" else "772-" + (100..999).random() + "-001",
            inGameUsername = if (role == UserRole.ADMIN) "AdminHost" else "eF_Pro_" + (10..99).random(),
            role = role
        )
        _currentUser.value = profile
        onSuccess()
    }

    fun signInWithGoogle(
        email: String = ORGANIZER_EMAIL,
        fullName: String? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val googleUid = "user_google_" + UUID.randomUUID().toString().take(8)
        val isAdmin = email.trim().equals(ORGANIZER_EMAIL, ignoreCase = true)
        val role = if (isAdmin) UserRole.ADMIN else UserRole.PLAYER
        val displayName = fullName ?: if (isAdmin) "Jadid Mollik (Organizer)" else "Tanvir Hossain"
        val phone = if (isAdmin) "+8801980000601" else "+8801904031478"
        val igId = if (isAdmin) "EF-ADMIN-01" else "772-" + (100..999).random() + "-123"
        val igName = if (isAdmin) "Admin_Jadid" else "CyberStriker_BD"

        val profile = UserProfile(
            uid = googleUid,
            fullName = displayName,
            email = email.trim(),
            phoneNumber = phone,
            inGameId = igId,
            inGameUsername = igName,
            role = role
        )
        _currentUser.value = profile
        onSuccess()
    }

    fun logout() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.w(tag, "Sign out error: ${e.message}")
        }
        _currentUser.value = null
    }

    fun switchUserRole(role: UserRole) {
        val current = _currentUser.value ?: return
        // Role switching only permitted if user is verified organizer
        if (current.email.trim().equals(ORGANIZER_EMAIL, ignoreCase = true)) {
            _currentUser.value = current.copy(role = role)
        }
    }

    fun updateProfile(fullName: String, phoneNumber: String, inGameId: String, inGameUsername: String) {
        val current = _currentUser.value ?: return
        val updated = current.copy(
            fullName = fullName,
            phoneNumber = phoneNumber,
            inGameId = inGameId,
            inGameUsername = inGameUsername
        )
        _currentUser.value = updated
        
        // Push to Firestore if online
        firestore?.collection("users")?.document(updated.uid)?.set(updated)
    }

    // Player registration & payment submission
    fun submitRegistration(
        paymentMethod: PaymentMethod,
        trxId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = _currentUser.value
        if (user == null) {
            onError("You must be logged in to register")
            return
        }
        if (trxId.isBlank() || trxId.length < 6) {
            onError("Please enter a valid bKash/Nagad Transaction ID (at least 6 characters)")
            return
        }

        val registration = TournamentRegistration(
            id = "reg_" + UUID.randomUUID().toString().take(8),
            userId = user.uid,
            fullName = user.fullName,
            phoneNumber = user.phoneNumber,
            inGameId = user.inGameId,
            inGameUsername = user.inGameUsername,
            paymentMethod = paymentMethod,
            trxId = trxId.trim().uppercase(),
            status = RegistrationStatus.PENDING,
            feeAmount = _tournament.value.entryFee,
            submittedAt = System.currentTimeMillis()
        )

        val updatedList = _registrations.value.filter { it.userId != user.uid } + registration
        _registrations.value = updatedList

        // Push to Firestore
        firestore?.collection("registrations")?.document(registration.id)?.set(registration)

        addNotification("Payment Submitted", "Your $paymentMethod payment with TrxID ${registration.trxId} has been submitted for admin verification.")
        onSuccess()
    }

    // Admin approvals
    fun approveRegistration(registrationId: String, adminUid: String) {
        val list = _registrations.value.toMutableList()
        val index = list.indexOfFirst { it.id == registrationId }
        if (index != -1) {
            val updated = list[index].copy(
                status = RegistrationStatus.JOINED,
                reviewedAt = System.currentTimeMillis(),
                reviewedBy = adminUid,
                rejectionReason = ""
            )
            list[index] = updated
            _registrations.value = list

            firestore?.collection("registrations")?.document(registrationId)?.set(updated)

            addNotification(
                "Registration Approved!",
                "Player ${updated.fullName} (${updated.inGameUsername}) has been approved for the championship draw."
            )
        }
    }

    fun rejectRegistration(registrationId: String, reason: String, adminUid: String) {
        val list = _registrations.value.toMutableList()
        val index = list.indexOfFirst { it.id == registrationId }
        if (index != -1) {
            val updated = list[index].copy(
                status = RegistrationStatus.REJECTED,
                rejectionReason = reason.ifBlank { "Invalid Transaction ID or payment not received." },
                reviewedAt = System.currentTimeMillis(),
                reviewedBy = adminUid
            )
            list[index] = updated
            _registrations.value = list

            firestore?.collection("registrations")?.document(registrationId)?.set(updated)

            addNotification(
                "Registration Rejected",
                "Registration for ${updated.fullName} was rejected: ${updated.rejectionReason}"
            )
        }
    }

    // Admin tournament settings
    fun updateTournamentSettings(
        title: String,
        entryFee: Int,
        bkashNumber: String,
        nagadNumber: String,
        isRegistrationOpen: Boolean,
        matchDurationMinutes: Int
    ) {
        val updated = _tournament.value.copy(
            title = title,
            entryFee = entryFee,
            bkashNumber = bkashNumber,
            nagadNumber = nagadNumber,
            isRegistrationOpen = isRegistrationOpen,
            matchDurationMinutes = matchDurationMinutes
        )
        _tournament.value = updated
        firestore?.collection("tournaments")?.document(updated.id)?.set(updated)
    }

    // Admin Bracket Generation
    fun generateMatchmakingBracket(): Int {
        val approved = _registrations.value.filter { it.status == RegistrationStatus.JOINED }
        val (generatedMatches, updatedTourney) = BracketEngine.generateBracket(approved, _tournament.value)
        _matches.value = generatedMatches
        _tournament.value = updatedTourney

        // Write batch to Firestore
        firestore?.let { db ->
            db.collection("tournaments").document(updatedTourney.id).set(updatedTourney)
            for (m in generatedMatches) {
                db.collection("matches").document(m.id).set(m)
            }
        }

        addNotification(
            "Bracket Generated!",
            "1v1 Single-Elimination Matchmaking Bracket has been generated with ${approved.size} players across ${updatedTourney.totalRounds} rounds."
        )

        return approved.size
    }

    // Admin Live Match Score update & advancement
    fun updateMatchScore(matchId: String, p1Score: Int, p2Score: Int, status: MatchStatus) {
        val (updatedMatches, updatedTourney) = BracketEngine.updateMatchAndAdvance(
            matches = _matches.value,
            tournament = _tournament.value,
            matchId = matchId,
            p1Score = p1Score,
            p2Score = p2Score,
            status = status
        )
        _matches.value = updatedMatches
        _tournament.value = updatedTourney

        firestore?.let { db ->
            val match = updatedMatches.firstOrNull { it.id == matchId }
            if (match != null) {
                db.collection("matches").document(match.id).set(match)
            }
            db.collection("tournaments").document(updatedTourney.id).set(updatedTourney)
        }

        if (updatedTourney.champion != null) {
            addNotification(
                "CHAMPION CROWNED! 🏆",
                "${updatedTourney.champion} (${updatedTourney.championUsername}) has won the Dhaka eFootball Open Championship!"
            )
        }
    }

    // Admin Update Tournament Rules in Firestore (settings/rules)
    fun updateRules(newRules: TournamentRules, updatedBy: String = "Organizer") {
        val rulesWithMeta = newRules.copy(
            lastUpdatedBy = updatedBy,
            updatedAt = System.currentTimeMillis()
        )
        _rules.value = rulesWithMeta

        firestore?.let { db ->
            db.collection("settings").document("rules").set(rulesWithMeta)
                .addOnSuccessListener {
                    Log.d(tag, "Rules saved to Firestore settings/rules successfully")
                }
                .addOnFailureListener { e ->
                    Log.e(tag, "Failed to write rules to Firestore settings/rules", e)
                }
        }

        addNotification(
            "Rules Updated & Broadcasted",
            "Updated regulations: ${rulesWithMeta.matchDuration}, ${rulesWithMeta.extraTimePk}, ${rulesWithMeta.substitutions}, ${rulesWithMeta.rematchRule}."
        )
    }

    fun addNotification(title: String, message: String) {
        val notif = AppNotification(title = title, message = message)
        _notifications.value = listOf(notif) + _notifications.value.take(20)
    }
}
