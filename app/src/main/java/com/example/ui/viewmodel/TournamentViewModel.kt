package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppNotification
import com.example.data.model.MatchStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.RegistrationStatus
import com.example.data.model.TournamentInfo
import com.example.data.model.TournamentMatch
import com.example.data.model.TournamentRegistration
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TournamentViewModel(
    private val repository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    val currentUser: StateFlow<UserProfile?> = repository.currentUser
    val tournament: StateFlow<TournamentInfo> = repository.tournament
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

    fun switchRole(role: UserRole) {
        repository.switchUserRole(role)
        showSnackbar("Switched role to ${role.name}")
    }

    fun signInWithGoogle() {
        repository.signInWithGoogle(
            onSuccess = {
                showSnackbar("Signed in with Google successfully")
            },
            onError = { err ->
                showSnackbar(err)
            }
        )
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

    fun updateTournamentSettings(
        title: String,
        entryFee: Int,
        bkashNumber: String,
        nagadNumber: String,
        isRegistrationOpen: Boolean,
        matchDurationMinutes: Int
    ) {
        repository.updateTournamentSettings(
            title,
            entryFee,
            bkashNumber,
            nagadNumber,
            isRegistrationOpen,
            matchDurationMinutes
        )
        showSnackbar("Tournament settings updated.")
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

    fun updateMatchScore(matchId: String, p1Score: Int, p2Score: Int, status: MatchStatus) {
        repository.updateMatchScore(matchId, p1Score, p2Score, status)
        showSnackbar("Match score & status updated!")
    }
}
