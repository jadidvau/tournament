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
import com.example.data.session.SessionManager
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
    private var sessionManager: SessionManager? = null

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

    private fun getAuth(): FirebaseAuth? {
        if (auth == null) {
            try {
                com.example.TournamentApplication.instance?.let {
                    com.example.TournamentApplication.initFirebase(it)
                }
                auth = FirebaseAuth.getInstance()
            } catch (e: Throwable) {
                Log.w(tag, "FirebaseAuth initialization check: ${e.message}")
            }
        }
        return auth
    }

    private fun getFirestore(): FirebaseFirestore? {
        if (firestore == null) {
            try {
                com.example.TournamentApplication.instance?.let {
                    com.example.TournamentApplication.initFirebase(it)
                }
                firestore = FirebaseFirestore.getInstance()
                setupFirestoreListeners()
            } catch (e: Throwable) {
                Log.w(tag, "FirebaseFirestore initialization check: ${e.message}")
            }
        }
        return firestore
    }

    init {
        try {
            com.example.TournamentApplication.instance?.let {
                com.example.TournamentApplication.initFirebase(it)
            }
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            setupFirestoreListeners()
        } catch (e: Exception) {
            Log.w(tag, "Firebase not initialized, using local reactive state fallback: ${e.message}")
        }
        seedInitialData()
    }

    fun initSession(manager: SessionManager) {
        sessionManager = manager
        val fbUser = try { getAuth()?.currentUser } catch (e: Exception) { null }
        val savedUser = manager.getSession()
        if (fbUser != null) {
            val email = fbUser.email ?: savedUser?.email ?: ""
            val isOrganizer = email.trim().equals(ORGANIZER_EMAIL, ignoreCase = true)
            val role = if (isOrganizer) UserRole.ADMIN else UserRole.PLAYER
            val user = UserProfile(
                uid = fbUser.uid,
                fullName = savedUser?.fullName?.ifBlank { null } ?: fbUser.displayName ?: if (isOrganizer) "Jadid Mollik (Organizer)" else email.substringBefore("@"),
                email = email,
                phoneNumber = savedUser?.phoneNumber ?: "",
                inGameId = savedUser?.inGameId ?: "",
                inGameUsername = savedUser?.inGameUsername ?: "",
                role = role,
                createdAt = savedUser?.createdAt ?: System.currentTimeMillis()
            )
            _currentUser.value = user
            manager.saveSession(user)
            refreshFirestoreListeners()
            Log.d(tag, "Restored persistent session for ${user.email} (${user.role})")
        } else {
            _currentUser.value = null
            manager.clearSession()
            clearListeners()
        }
    }

    fun refreshFirestoreListeners() {
        clearListeners()
        setupFirestoreListeners()
    }

    fun clearListeners() {
        for (l in listeners) {
            try {
                l.remove()
            } catch (ignored: Throwable) {}
        }
        listeners.clear()
    }

    private fun setupFirestoreListeners() {
        val db = getFirestore() ?: return
        if (listeners.isNotEmpty()) return

        // To avoid unauthenticated PERMISSION_DENIED exceptions on locked Firestore rules,
        // snapshot listeners are attached once a valid Firebase user is signed in
        if (getAuth()?.currentUser == null) {
            Log.d(tag, "Postponing snapshot listeners until user is authenticated")
            return
        }

        // 1. Tournaments listener
        try {
            var tListener: ListenerRegistration? = null
            tListener = db.collection("tournaments").document("dhaka_efootball_2026")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Tournament listener notice: ${error.code} - ${error.message}")
                        tListener?.let { listeners.remove(it) }
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
        } catch (e: Throwable) {
            Log.w(tag, "Failed to attach tournament listener: ${e.message}")
        }

        // 2. Registrations listener
        try {
            var rListener: ListenerRegistration? = null
            rListener = db.collection("registrations")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Registrations listener notice: ${error.code} - ${error.message}")
                        rListener?.let { listeners.remove(it) }
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
        } catch (e: Throwable) {
            Log.w(tag, "Failed to attach registrations listener: ${e.message}")
        }

        // 3. Matches listener
        try {
            var mListener: ListenerRegistration? = null
            mListener = db.collection("matches")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Matches listener notice: ${error.code} - ${error.message}")
                        mListener?.let { listeners.remove(it) }
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
        } catch (e: Throwable) {
            Log.w(tag, "Failed to attach matches listener: ${e.message}")
        }

        // 4. Tournament Rules listener (settings/rules)
        try {
            var rulesListener: ListenerRegistration? = null
            rulesListener = db.collection("settings").document("rules")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Rules listener notice: ${error.code} - ${error.message}")
                        rulesListener?.let { listeners.remove(it) }
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
        } catch (e: Throwable) {
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
            title = "Dhaka eFootball Championship",
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
        val clean = emailOrPhone.trim()
        if (clean.isBlank()) {
            onError("Please enter your email or phone number")
            return
        }

        val isAdmin = clean.equals(ORGANIZER_EMAIL, ignoreCase = true) || role == UserRole.ADMIN
        val finalRole = if (isAdmin) UserRole.ADMIN else UserRole.PLAYER
        val uid = if (finalRole == UserRole.ADMIN) "admin_super_01" else "user_" + UUID.randomUUID().toString().take(6)
        val profile = UserProfile(
            uid = uid,
            fullName = if (finalRole == UserRole.ADMIN) "Dhaka eFootball Host" else "Player (${clean.take(8)})",
            email = if (clean.contains("@")) clean else if (finalRole == UserRole.ADMIN) ORGANIZER_EMAIL else "$clean@dhaka-efootball.com",
            phoneNumber = if (clean.startsWith("+880") || clean.startsWith("01")) clean else "+8801904031478",
            inGameId = if (finalRole == UserRole.ADMIN) "EF-ADMIN-01" else "772-" + (100..999).random() + "-001",
            inGameUsername = if (finalRole == UserRole.ADMIN) "AdminHost" else "eF_Pro_" + (10..99).random(),
            role = finalRole
        )
        _currentUser.value = profile
        sessionManager?.saveSession(profile)
        onSuccess()
    }

    // Player Registration with Email Verification Flow
    fun registerPlayer(
        ign: String,
        konamiId: String,
        email: String,
        pass: String,
        phone: String,
        onVerificationRequired: (userId: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanEmail = email.trim()
        val cleanIgn = ign.trim()
        val cleanKonamiId = konamiId.trim()
        val cleanPhone = phone.trim()

        if (cleanIgn.isBlank()) {
            onError("Please enter your IGN (In-Game Name)")
            return
        }
        if (cleanKonamiId.isBlank()) {
            onError("Please enter your Konami ID")
            return
        }
        if (cleanEmail.isBlank() || !cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            onError("Please enter a valid email address")
            return
        }
        if (pass.length < 6) {
            onError("Password must be at least 6 characters")
            return
        }
        if (cleanPhone.isBlank()) {
            onError("Please enter your phone number")
            return
        }

        try {
            val fa = getAuth()
            if (fa == null) {
                // Offline/Preview fallback
                val localUserId = "user_" + UUID.randomUUID().toString().take(8)
                Log.i(tag, "Offline/Preview mode: Verification required for $cleanEmail")
                onVerificationRequired(localUserId)
                return
            }

            fa.createUserWithEmailAndPassword(cleanEmail, pass)
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    if (user == null) {
                        onError("Registration failed. Could not create account.")
                        return@addOnSuccessListener
                    }
                    val userId = user.uid
                    user.sendEmailVerification()
                        .addOnSuccessListener {
                            Log.d(tag, "Verification email dispatched to $cleanEmail")
                        }
                        .addOnFailureListener { e ->
                            Log.w(tag, "Failed to send verification email: ${e.message}")
                        }
                    onVerificationRequired(userId)
                }
                .addOnFailureListener { exception ->
                    val msg = exception.message ?: ""
                    if (exception is IllegalStateException || msg.contains("FirebaseApp", ignoreCase = true) || msg.contains("Default FirebaseApp", ignoreCase = true)) {
                        val localUserId = "user_" + UUID.randomUUID().toString().take(8)
                        onVerificationRequired(localUserId)
                    } else {
                        onError(parseAuthErrorMessage(exception))
                    }
                }
        } catch (e: Throwable) {
            Log.w(tag, "Register caught exception: ${e.message}")
            val localUserId = "user_" + UUID.randomUUID().toString().take(8)
            onVerificationRequired(localUserId)
        }
    }

    fun completePlayerVerification(
        userId: String,
        ign: String,
        konamiId: String,
        email: String,
        phone: String,
        onSuccess: (UserProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanEmail = email.trim()
        val isOrganizer = cleanEmail.equals(ORGANIZER_EMAIL, ignoreCase = true)
        val role = if (isOrganizer) UserRole.ADMIN else UserRole.PLAYER
        val roleStr = if (isOrganizer) "ADMIN" else "PLAYER"

        val profile = UserProfile(
            uid = userId,
            fullName = ign.trim(),
            email = cleanEmail,
            phoneNumber = phone.trim(),
            inGameId = konamiId.trim(),
            inGameUsername = ign.trim(),
            role = role,
            isEmailVerified = true,
            createdAt = System.currentTimeMillis()
        )

        val firestoreMap = hashMapOf<String, Any>(
            "uid" to userId,
            "ign" to profile.inGameUsername,
            "konamiId" to profile.inGameId,
            "inGameUsername" to profile.inGameUsername,
            "inGameId" to profile.inGameId,
            "fullName" to profile.fullName,
            "email" to profile.email,
            "phoneNumber" to profile.phoneNumber,
            "role" to roleStr,
            "isEmailVerified" to true,
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        val db = getFirestore()
        if (db != null) {
            try {
                db.collection("users").document(userId).set(firestoreMap)
                    .addOnSuccessListener {
                        Log.d(tag, "Player profile written to Firestore users/$userId with role=$roleStr")
                    }
                    .addOnFailureListener { e ->
                        Log.w(tag, "Failed to write user profile to Firestore: ${e.message}")
                    }
            } catch (e: Throwable) {
                Log.w(tag, "Firestore write error: ${e.message}")
            }
        }

        _currentUser.value = profile
        sessionManager?.saveSession(profile)
        refreshFirestoreListeners()
        onSuccess(profile)
    }

    fun checkEmailVerificationStatus(
        userId: String,
        ign: String,
        konamiId: String,
        email: String,
        phone: String,
        onVerified: (UserProfile) -> Unit,
        onNotVerified: () -> Unit,
        onError: (String) -> Unit
    ) {
        val fa = getAuth()
        val user = fa?.currentUser
        if (user == null) {
            onNotVerified()
            return
        }

        user.reload()
            .addOnSuccessListener {
                if (user.isEmailVerified) {
                    val uidToUse = user.uid.ifBlank { userId }
                    completePlayerVerification(uidToUse, ign, konamiId, email, phone, onVerified, onError)
                } else {
                    onNotVerified()
                }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to check email verification status")
            }
    }

    fun resendVerificationEmail(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val fa = getAuth()
        val user = fa?.currentUser
        if (user != null) {
            user.sendEmailVerification()
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onError(e.message ?: "Could not resend verification email") }
        } else {
            onError("No active registration session found. Please register again.")
        }
    }

    fun loginWithEmailPassword(
        email: String,
        pass: String,
        isSignUp: Boolean,
        fullName: String = "",
        inGameUsername: String = "",
        inGameId: String = "",
        onSuccess: (UserProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank()) {
            onError("Please enter your email address")
            return
        }
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            onError("Please enter a valid email address")
            return
        }
        if (pass.length < 6) {
            onError("Password must be at least 6 characters")
            return
        }

        try {
            val fa = getAuth()
            if (fa == null) {
                // Graceful Offline/Preview Mocking for AI Studio Web Simulator
                Log.w(tag, "FirebaseAuth instance null. Falling back to local preview simulation.")
                simulateLocalAuth(cleanEmail, isSignUp, fullName, inGameUsername, inGameId, onSuccess)
                return
            }
            val db = getFirestore()

            if (isSignUp) {
                if (fullName.isBlank()) {
                    onError("Please enter your Full Name / In-Game Name")
                    return
                }
                val usernameOrId = inGameUsername.ifBlank { inGameId }.trim()
                if (usernameOrId.isBlank()) {
                    onError("Please enter your eFootball Username / ID")
                    return
                }

                fa.createUserWithEmailAndPassword(cleanEmail, pass)
                    .addOnSuccessListener { authResult ->
                        val user = authResult.user
                        if (user == null) {
                            onError("Registration failed. Could not create account.")
                            return@addOnSuccessListener
                        }
                        val uid = user.uid
                        val isOrganizer = cleanEmail.equals(ORGANIZER_EMAIL, ignoreCase = true)
                        val role = if (isOrganizer) UserRole.ADMIN else UserRole.PLAYER

                        val profile = UserProfile(
                            uid = uid,
                            fullName = fullName.trim(),
                            email = cleanEmail,
                            phoneNumber = "",
                            inGameId = if (inGameId.isNotBlank()) inGameId.trim() else usernameOrId,
                            inGameUsername = if (inGameUsername.isNotBlank()) inGameUsername.trim() else usernameOrId,
                            role = role,
                            createdAt = System.currentTimeMillis()
                        )

                        val firestoreMap = hashMapOf<String, Any>(
                            "uid" to uid,
                            "name" to profile.fullName,
                            "fullName" to profile.fullName,
                            "email" to profile.email,
                            "phoneNumber" to "",
                            "inGameId" to profile.inGameId,
                            "inGameUsername" to profile.inGameUsername,
                            "role" to if (isOrganizer) "admin" else "player",
                            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                        )

                        if (db != null) {
                            try {
                                db.collection("users").document(uid).set(firestoreMap)
                                    .addOnSuccessListener {
                                        Log.d(tag, "Profile saved to Firestore users/$uid with role=${firestoreMap["role"]}")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(tag, "Failed to write user profile to Firestore: ${e.message}")
                                    }
                            } catch (e: Throwable) {
                                Log.w(tag, "Firestore set exception: ${e.message}")
                            }
                        }

                        _currentUser.value = profile
                        sessionManager?.saveSession(profile)
                        refreshFirestoreListeners()
                        onSuccess(profile)
                    }
                    .addOnFailureListener { exception ->
                        onError(parseAuthErrorMessage(exception))
                    }
            } else {
                // SIGN IN TAB
                fa.signInWithEmailAndPassword(cleanEmail, pass)
                    .addOnSuccessListener { authResult ->
                        val user = authResult.user
                        if (user == null) {
                            onError("Sign in failed. No user found.")
                            return@addOnSuccessListener
                        }
                        val uid = user.uid
                        val isOrganizer = cleanEmail.equals(ORGANIZER_EMAIL, ignoreCase = true)
                        val role = if (isOrganizer) UserRole.ADMIN else UserRole.PLAYER

                        if (db != null) {
                            try {
                                db.collection("users").document(uid).get()
                                    .addOnSuccessListener { doc ->
                                        val profile = if (doc.exists()) {
                                            UserProfile(
                                                uid = uid,
                                                fullName = doc.getString("fullName") ?: doc.getString("name") ?: if (isOrganizer) "Jadid Mollik (Organizer)" else cleanEmail.substringBefore("@"),
                                                email = cleanEmail,
                                                phoneNumber = doc.getString("phoneNumber") ?: "",
                                                inGameId = doc.getString("inGameId") ?: "",
                                                inGameUsername = doc.getString("inGameUsername") ?: "",
                                                role = role,
                                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                            )
                                        } else {
                                            UserProfile(
                                                uid = uid,
                                                fullName = if (isOrganizer) "Jadid Mollik (Organizer)" else cleanEmail.substringBefore("@"),
                                                email = cleanEmail,
                                                phoneNumber = "",
                                                inGameId = "",
                                                inGameUsername = "",
                                                role = role,
                                                createdAt = System.currentTimeMillis()
                                            )
                                        }
                                        _currentUser.value = profile
                                        sessionManager?.saveSession(profile)
                                        refreshFirestoreListeners()
                                        onSuccess(profile)
                                    }
                                    .addOnFailureListener {
                                        val fallback = UserProfile(
                                            uid = uid,
                                            fullName = if (isOrganizer) "Jadid Mollik (Organizer)" else cleanEmail.substringBefore("@"),
                                            email = cleanEmail,
                                            role = role
                                        )
                                        _currentUser.value = fallback
                                        sessionManager?.saveSession(fallback)
                                        refreshFirestoreListeners()
                                        onSuccess(fallback)
                                    }
                            } catch (e: Throwable) {
                                val fallback = UserProfile(
                                    uid = uid,
                                    fullName = if (isOrganizer) "Jadid Mollik (Organizer)" else cleanEmail.substringBefore("@"),
                                    email = cleanEmail,
                                    role = role
                                )
                                _currentUser.value = fallback
                                sessionManager?.saveSession(fallback)
                                refreshFirestoreListeners()
                                onSuccess(fallback)
                            }
                        } else {
                            val profile = UserProfile(
                                uid = uid,
                                fullName = if (isOrganizer) "Jadid Mollik (Organizer)" else cleanEmail.substringBefore("@"),
                                email = cleanEmail,
                                role = role
                            )
                            _currentUser.value = profile
                            sessionManager?.saveSession(profile)
                            refreshFirestoreListeners()
                            onSuccess(profile)
                        }
                    }
                    .addOnFailureListener { exception ->
                        onError(parseAuthErrorMessage(exception))
                    }
            }
        } catch (e: Throwable) {
            Log.w(tag, "Auth operation caught exception: ${e.message}")
            val msg = e.message ?: ""
            if (e is IllegalStateException || msg.contains("Default FirebaseApp is not initialized", ignoreCase = true) || msg.contains("FirebaseApp", ignoreCase = true)) {
                Log.w(tag, "FirebaseApp not initialized in runtime. Simulating successful local auth for preview.")
                simulateLocalAuth(cleanEmail, isSignUp, fullName, inGameUsername, inGameId, onSuccess)
            } else {
                onError(parseAuthErrorMessage(e))
            }
        }
    }

    private fun simulateLocalAuth(
        cleanEmail: String,
        isSignUp: Boolean,
        fullName: String,
        inGameUsername: String,
        inGameId: String,
        onSuccess: (UserProfile) -> Unit
    ) {
        val isOrganizer = cleanEmail.equals(ORGANIZER_EMAIL, ignoreCase = true)
        val role = if (isOrganizer) UserRole.ADMIN else UserRole.PLAYER
        val uid = "local_" + UUID.randomUUID().toString().take(8)

        val profileName = if (isSignUp && fullName.isNotBlank()) {
            fullName.trim()
        } else if (isOrganizer) {
            "Jadid Mollik (Organizer)"
        } else {
            cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
        }

        val gameId = if (isSignUp) {
            inGameId.ifBlank { inGameUsername }.ifBlank { "ef_${cleanEmail.substringBefore("@")}" }
        } else {
            "ef_${cleanEmail.substringBefore("@")}"
        }

        val profile = UserProfile(
            uid = uid,
            fullName = profileName,
            email = cleanEmail,
            phoneNumber = "",
            inGameId = gameId,
            inGameUsername = if (isSignUp && inGameUsername.isNotBlank()) inGameUsername.trim() else gameId,
            role = role,
            createdAt = System.currentTimeMillis()
        )

        _currentUser.value = profile
        sessionManager?.saveSession(profile)
        Log.i(tag, "Simulated successful local authentication for: ${profile.email} as $role")
        onSuccess(profile)
    }

    fun handleFirebaseUser(
        firebaseUser: com.google.firebase.auth.FirebaseUser,
        onSuccess: (UserProfile) -> Unit
    ) {
        val email = firebaseUser.email ?: ""
        val uid = firebaseUser.uid
        val isOrganizer = email.trim().equals(ORGANIZER_EMAIL, ignoreCase = true)
        val role = if (isOrganizer) UserRole.ADMIN else UserRole.PLAYER

        val db = getFirestore()
        try {
            if (db != null) {
                db.collection("users").document(uid).get()
                    .addOnSuccessListener { doc ->
                        val profile = if (doc.exists()) {
                            UserProfile(
                                uid = uid,
                                fullName = doc.getString("fullName") ?: doc.getString("name") ?: firebaseUser.displayName ?: if (isOrganizer) "Jadid Mollik (Organizer)" else email.substringBefore("@"),
                                email = email,
                                phoneNumber = doc.getString("phoneNumber") ?: firebaseUser.phoneNumber ?: "",
                                inGameId = doc.getString("inGameId") ?: "",
                                inGameUsername = doc.getString("inGameUsername") ?: "",
                                role = role,
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                            )
                        } else {
                            val newProfile = UserProfile(
                                uid = uid,
                                fullName = firebaseUser.displayName ?: if (isOrganizer) "Jadid Mollik (Organizer)" else email.substringBefore("@"),
                                email = email,
                                phoneNumber = firebaseUser.phoneNumber ?: "",
                                inGameId = "",
                                inGameUsername = "",
                                role = role,
                                createdAt = System.currentTimeMillis()
                            )
                            val firestoreMap = hashMapOf<String, Any>(
                                "uid" to uid,
                                "name" to newProfile.fullName,
                                "fullName" to newProfile.fullName,
                                "email" to newProfile.email,
                                "role" to if (isOrganizer) "admin" else "player",
                                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                            )
                            db.collection("users").document(uid).set(firestoreMap)
                            newProfile
                        }
                        _currentUser.value = profile
                        sessionManager?.saveSession(profile)
                        onSuccess(profile)
                    }
                    .addOnFailureListener {
                        val fallback = UserProfile(
                            uid = uid,
                            fullName = firebaseUser.displayName ?: if (isOrganizer) "Jadid Mollik (Organizer)" else email.substringBefore("@"),
                            email = email,
                            role = role
                        )
                        _currentUser.value = fallback
                        sessionManager?.saveSession(fallback)
                        onSuccess(fallback)
                    }
            } else {
                val fallback = UserProfile(
                    uid = uid,
                    fullName = firebaseUser.displayName ?: if (isOrganizer) "Jadid Mollik (Organizer)" else email.substringBefore("@"),
                    email = email,
                    role = role
                )
                _currentUser.value = fallback
                sessionManager?.saveSession(fallback)
                onSuccess(fallback)
            }
        } catch (e: Throwable) {
            val fallback = UserProfile(
                uid = uid,
                fullName = firebaseUser.displayName ?: if (isOrganizer) "Jadid Mollik (Organizer)" else email.substringBefore("@"),
                email = email,
                role = role
            )
            _currentUser.value = fallback
            sessionManager?.saveSession(fallback)
            onSuccess(fallback)
        }
    }

    private fun parseAuthErrorMessage(e: Throwable): String {
        val msg = e.localizedMessage ?: e.message ?: ""
        return when {
            e is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ||
            msg.contains("invalid-credential", ignoreCase = true) ||
            msg.contains("wrong password", ignoreCase = true) ||
            msg.contains("password is invalid", ignoreCase = true) -> "Invalid password or email. Please check your credentials."

            e is com.google.firebase.auth.FirebaseAuthInvalidUserException ||
            msg.contains("user-not-found", ignoreCase = true) -> "No account found with this email. Please register."

            e is com.google.firebase.auth.FirebaseAuthUserCollisionException ||
            msg.contains("email-already-in-use", ignoreCase = true) -> "Email already in use. Please sign in instead."

            e is com.google.firebase.auth.FirebaseAuthWeakPasswordException ||
            msg.contains("weak-password", ignoreCase = true) -> "Password is too weak. Please use at least 6 characters."

            msg.contains("network", ignoreCase = true) -> "Network error. Please check your internet connection."
            msg.contains("too-many-requests", ignoreCase = true) -> "Too many failed attempts. Please try again later."
            else -> msg.ifBlank { "Authentication failed. Please check your details and try again." }
        }
    }

    fun logout() {
        try {
            getAuth()?.signOut()
        } catch (e: Throwable) {
            Log.w(tag, "Sign out error: ${e.message}")
        }
        clearListeners()
        sessionManager?.clearSession()
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
        getFirestore()?.collection("users")?.document(updated.uid)?.set(updated)
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
        getFirestore()?.collection("registrations")?.document(registration.id)?.set(registration)

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

            getFirestore()?.collection("registrations")?.document(registrationId)?.set(updated)

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

            getFirestore()?.collection("registrations")?.document(registrationId)?.set(updated)

            addNotification(
                "Registration Rejected",
                "Registration for ${updated.fullName} was rejected: ${updated.rejectionReason}"
            )
        }
    }

    // Admin Roster Edit & Delete
    fun updateRegistration(registration: TournamentRegistration) {
        val list = _registrations.value.toMutableList()
        val index = list.indexOfFirst { it.id == registration.id }
        if (index != -1) {
            list[index] = registration
        } else {
            list.add(registration)
        }
        _registrations.value = list
        getFirestore()?.collection("registrations")?.document(registration.id)?.set(registration)
        addNotification("Roster Updated", "Participant ${registration.fullName} was updated.")
    }

    fun deleteRegistration(registrationId: String) {
        val list = _registrations.value.filter { it.id != registrationId }
        _registrations.value = list
        getFirestore()?.collection("registrations")?.document(registrationId)?.delete()
        addNotification("Player Removed", "Participant removed from tournament roster.")
    }

    fun addParticipant(registration: TournamentRegistration) {
        val list = _registrations.value + registration
        _registrations.value = list
        getFirestore()?.collection("registrations")?.document(registration.id)?.set(registration)
        addNotification("Player Added", "Player ${registration.fullName} manually added to roster.")
    }

    // Admin Match Fixtures Management
    fun addMatchFixture(match: TournamentMatch) {
        val list = _matches.value.toMutableList()
        list.add(match)
        _matches.value = list
        getFirestore()?.collection("matches")?.document(match.id)?.set(match)
        addNotification("Match Scheduled", "New match fixture added: ${match.startTime}")
    }

    fun updateMatchFixture(match: TournamentMatch) {
        val list = _matches.value.toMutableList()
        val index = list.indexOfFirst { it.id == match.id }
        if (index != -1) {
            list[index] = match
            _matches.value = list
            getFirestore()?.collection("matches")?.document(match.id)?.set(match)
            addNotification("Match Updated", "Match fixture updated (${match.startTime})")
        }
    }

    fun deleteMatchFixture(matchId: String) {
        val list = _matches.value.filter { it.id != matchId }
        _matches.value = list
        getFirestore()?.collection("matches")?.document(matchId)?.delete()
        addNotification("Match Removed", "Match fixture removed from tournament.")
    }

    fun rescheduleMatch(matchId: String, newStartTime: String, newRound: Int) {
        val list = _matches.value.toMutableList()
        val index = list.indexOfFirst { it.id == matchId }
        if (index != -1) {
            val updated = list[index].copy(startTime = newStartTime, round = newRound)
            list[index] = updated
            _matches.value = list
            getFirestore()?.collection("matches")?.document(matchId)?.set(updated)
            addNotification("Match Rescheduled", "Match rescheduled to $newStartTime (Round $newRound).")
        }
    }

    fun resetBracket() {
        _matches.value = emptyList()
        val updatedTourney = _tournament.value.copy(totalRounds = 0, champion = null, championUsername = null)
        _tournament.value = updatedTourney
        getFirestore()?.let { db ->
            db.collection("tournaments").document(updatedTourney.id).set(updatedTourney)
        }
        addNotification("Bracket Reset", "Tournament bracket cleared by Admin Host.")
    }

    fun advancePlayer(matchId: String, winnerId: String, p1Score: Int, p2Score: Int) {
        updateMatchScore(matchId, p1Score, p2Score, MatchStatus.COMPLETED)
    }

    // Admin tournament settings
    fun updateTournamentSettings(
        title: String,
        entryFee: Int,
        prizePool: String,
        bkashNumber: String,
        nagadNumber: String,
        isRegistrationOpen: Boolean,
        matchDurationMinutes: Int
    ) {
        val updated = _tournament.value.copy(
            title = title,
            entryFee = entryFee,
            prizePool = prizePool,
            bkashNumber = bkashNumber,
            nagadNumber = nagadNumber,
            isRegistrationOpen = isRegistrationOpen,
            matchDurationMinutes = matchDurationMinutes
        )
        _tournament.value = updated
        getFirestore()?.collection("tournaments")?.document(updated.id)?.set(updated)
        addNotification("Settings Updated", "Tournament parameters and prize pool ($prizePool) updated.")
    }

    // Admin Bracket Generation
    fun generateMatchmakingBracket(): Int {
        val approved = _registrations.value.filter { it.status == RegistrationStatus.JOINED }
        val (generatedMatches, updatedTourney) = BracketEngine.generateBracket(approved, _tournament.value)
        _matches.value = generatedMatches
        _tournament.value = updatedTourney

        // Write batch to Firestore
        getFirestore()?.let { db ->
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

        getFirestore()?.let { db ->
            val match = updatedMatches.firstOrNull { it.id == matchId }
            if (match != null) {
                db.collection("matches").document(match.id).set(match)
            }
            db.collection("tournaments").document(updatedTourney.id).set(updatedTourney)
        }

        if (updatedTourney.champion != null) {
            addNotification(
                "CHAMPION CROWNED! 🏆",
                "${updatedTourney.champion} (${updatedTourney.championUsername}) has won the Dhaka eFootball Championship!"
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

        getFirestore()?.let { db ->
            db.collection("settings").document("rules").set(rulesWithMeta)
                .addOnSuccessListener {
                    Log.d(tag, "Rules saved to Firestore settings/rules successfully")
                }
                .addOnFailureListener { e ->
                    Log.w(tag, "Notice: rules saved locally; Firestore sync status: ${e.message}")
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
