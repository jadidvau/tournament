package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppNotification
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
import com.example.data.repository.FirebaseRepository
import com.example.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TournamentViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application.applicationContext)
    val repository: FirebaseRepository = FirebaseRepository().apply {
        initSession(sessionManager)
    }

    val currentUser: StateFlow<UserProfile?> = repository.currentUser
    val tournament: StateFlow<TournamentInfo> = repository.tournament
    val rules: StateFlow<TournamentRules> = repository.rules
    val registrations: StateFlow<List<TournamentRegistration>> = repository.registrations
    val matches: StateFlow<List<TournamentMatch>> = repository.matches
    val notifications: StateFlow<List<AppNotification>> = repository.notifications

    // UI state
    private val _selectedRoundFilter = MutableStateFlow(1)
    val selectedRoundFilter: StateFlow<Int> = _selectedRoundFilter.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Current player registration status for the logged-in user
    val userRegistration: StateFlow<TournamentRegistration?> = combine(
        currentUser,
        registrations
    ) { user, list ->
        if (user == null) null else list.firstOrNull { it.userId == user.uid }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Current active match for the logged-in user
    val userCurrentMatch: StateFlow<TournamentMatch?> = combine(
        currentUser,
        matches
    ) { user, matchList ->
        if (user == null) null
        else matchList.firstOrNull { m ->
            (m.player1?.id == user.uid || m.player2?.id == user.uid) && m.status != MatchStatus.COMPLETED
        } ?: matchList.lastOrNull { m ->
            (m.player1?.id == user.uid || m.player2?.id == user.uid)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Stats for admin dashboard
    val pendingCount: StateFlow<Int> = registrations.combine(MutableStateFlow(Unit)) { list, _ ->
        list.count { it.status == RegistrationStatus.PENDING }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val approvedCount: StateFlow<Int> = registrations.combine(MutableStateFlow(Unit)) { list, _ ->
        list.count { it.status == RegistrationStatus.JOINED }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val completedMatchesCount: StateFlow<Int> = matches.combine(MutableStateFlow(Unit)) { list, _ ->
        list.count { it.status == MatchStatus.COMPLETED }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun setRoundFilter(round: Int) {
        _selectedRoundFilter.value = round
    }

    fun showSnackbar(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun login(emailOrPhone: String, role: UserRole) {
        repository.login(
            emailOrPhone = emailOrPhone,
            role = role,
            onSuccess = {
                showSnackbar("Logged in successfully as ${if (role == UserRole.ADMIN) "Admin" else "Player"}")
            },
            onError = { err ->
                showSnackbar(err)
            }
        )
    }

    fun loginWithEmailPassword(
        email: String,
        pass: String,
        isSignUp: Boolean,
        fullName: String = "",
        inGameUsername: String = "",
        inGameId: String = "",
        onSuccess: (UserProfile) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        repository.loginWithEmailPassword(
            email = email,
            pass = pass,
            isSignUp = isSignUp,
            fullName = fullName,
            inGameUsername = inGameUsername,
            inGameId = inGameId,
            onSuccess = { profile ->
                val isAdmin = profile.email.trim().equals(ORGANIZER_EMAIL, ignoreCase = true)
                showSnackbar("Welcome ${profile.fullName}! (${if (isAdmin) "Admin Host" else "Participant"})")
                onSuccess(profile)
            },
            onError = { err ->
                showSnackbar(err)
                onError(err)
            }
        )
    }

    fun registerPlayer(
        ign: String,
        konamiId: String,
        email: String,
        pass: String,
        phone: String,
        onVerificationRequired: (userId: String) -> Unit,
        onError: (String) -> Unit
    ) {
        repository.registerPlayer(
            ign = ign,
            konamiId = konamiId,
            email = email,
            pass = pass,
            phone = phone,
            onVerificationRequired = { userId ->
                showSnackbar("Verification email sent to $email")
                onVerificationRequired(userId)
            },
            onError = { err ->
                showSnackbar(err)
                onError(err)
            }
        )
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
        repository.checkEmailVerificationStatus(
            userId = userId,
            ign = ign,
            konamiId = konamiId,
            email = email,
            phone = phone,
            onVerified = { profile ->
                showSnackbar("Email verified! Welcome ${profile.fullName}")
                onVerified(profile)
            },
            onNotVerified = onNotVerified,
            onError = { err ->
                showSnackbar(err)
                onError(err)
            }
        )
    }

    fun resendVerificationEmail(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        repository.resendVerificationEmail(
            onSuccess = {
                showSnackbar("Verification email resent!")
                onSuccess()
            },
            onError = { err ->
                showSnackbar(err)
                onError(err)
            }
        )
    }

    fun handleFirebaseUser(
        firebaseUser: com.google.firebase.auth.FirebaseUser,
        onSuccess: (UserProfile) -> Unit = {}
    ) {
        repository.handleFirebaseUser(
            firebaseUser = firebaseUser,
            onSuccess = { profile ->
                val isAdmin = profile.email.trim().equals(ORGANIZER_EMAIL, ignoreCase = true)
                showSnackbar("Welcome ${profile.fullName}! (${if (isAdmin) "Admin Host" else "Participant"})")
                onSuccess(profile)
            }
        )
    }

    fun switchRole(role: UserRole) {
        repository.switchUserRole(role)
        showSnackbar("Switched role to ${role.name}")
    }

    fun logout() {
        repository.logout()
        showSnackbar("Logged out successfully")
    }

    fun updateProfile(fullName: String, phoneNumber: String, inGameId: String, inGameUsername: String) {
        repository.updateProfile(fullName, phoneNumber, inGameId, inGameUsername)
        showSnackbar("Profile updated successfully")
    }

    fun submitRegistration(paymentMethod: PaymentMethod, trxId: String) {
        repository.submitRegistration(
            paymentMethod = paymentMethod,
            trxId = trxId,
            onSuccess = {
                showSnackbar("Registration submitted! Pending admin review.")
            },
            onError = { err ->
                showSnackbar(err)
            }
        )
    }

    fun approveRegistration(regId: String) {
        val adminUid = currentUser.value?.uid ?: "admin_super_01"
        repository.approveRegistration(regId, adminUid)
        showSnackbar("Player approved and added to participant pool!")
    }

    fun rejectRegistration(regId: String, reason: String) {
        val adminUid = currentUser.value?.uid ?: "admin_super_01"
        repository.rejectRegistration(regId, reason, adminUid)
        showSnackbar("Registration rejected.")
    }

    fun updateRegistration(registration: TournamentRegistration) {
        repository.updateRegistration(registration)
        showSnackbar("Participant roster updated for ${registration.fullName}.")
    }

    fun deleteRegistration(regId: String) {
        repository.deleteRegistration(regId)
        showSnackbar("Player entry removed from tournament roster.")
    }

    fun addParticipant(registration: TournamentRegistration) {
        repository.addParticipant(registration)
        showSnackbar("Participant added to roster.")
    }

    fun updateTournamentSettings(
        title: String,
        entryFee: Int,
        prizePool: String,
        bkashNumber: String,
        nagadNumber: String,
        isRegistrationOpen: Boolean,
        matchDurationMinutes: Int
    ) {
        repository.updateTournamentSettings(
            title = title,
            entryFee = entryFee,
            prizePool = prizePool,
            bkashNumber = bkashNumber,
            nagadNumber = nagadNumber,
            isRegistrationOpen = isRegistrationOpen,
            matchDurationMinutes = matchDurationMinutes
        )
        showSnackbar("Tournament settings updated and synced to Firestore.")
    }

    fun generateBracket() {
        val count = repository.generateMatchmakingBracket()
        if (count == 0) {
            showSnackbar("No approved players yet! Approve players from the queue first.")
        } else {
            showSnackbar("1v1 Bracket generated with $count players!")
            _selectedRoundFilter.value = 1
        }
    }

    fun resetBracket() {
        repository.resetBracket()
        showSnackbar("Tournament bracket has been reset.")
    }

    fun addMatchFixture(match: TournamentMatch) {
        repository.addMatchFixture(match)
        showSnackbar("Match fixture added to schedule.")
    }

    fun updateMatchFixture(match: TournamentMatch) {
        repository.updateMatchFixture(match)
        showSnackbar("Match fixture updated.")
    }

    fun deleteMatchFixture(matchId: String) {
        repository.deleteMatchFixture(matchId)
        showSnackbar("Match fixture removed.")
    }

    fun rescheduleMatch(matchId: String, newStartTime: String, newRound: Int) {
        repository.rescheduleMatch(matchId, newStartTime, newRound)
        showSnackbar("Match rescheduled to $newStartTime (Round $newRound).")
    }

    fun advancePlayer(matchId: String, winnerId: String, p1Score: Int, p2Score: Int) {
        repository.advancePlayer(matchId, winnerId, p1Score, p2Score)
        showSnackbar("Winner advanced to next bracket round!")
    }

    fun updateMatchScore(matchId: String, p1Score: Int, p2Score: Int, status: MatchStatus) {
        repository.updateMatchScore(matchId, p1Score, p2Score, status)
        showSnackbar("Match score & status updated!")
    }

    fun updateRules(rules: TournamentRules) {
        val adminName = currentUser.value?.fullName ?: "Organizer"
        repository.updateRules(rules, adminName)
        showSnackbar("Rules saved & broadcasted to all players!")
    }
}
