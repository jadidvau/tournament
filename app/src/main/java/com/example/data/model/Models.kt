package com.example.data.model

import java.util.UUID

enum class UserRole {
    PLAYER,
    ADMIN
}

enum class PaymentMethod {
    bKash,
    Nagad
}

enum class RegistrationStatus {
    PENDING,
    JOINED,
    REJECTED
}

enum class MatchStatus {
    SCHEDULED,
    LIVE,
    COMPLETED
}

const val ORGANIZER_EMAIL = "nogorigangjadid@gmail.com"

data class UserProfile(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val inGameId: String = "",
    val inGameUsername: String = "",
    val role: UserRole = UserRole.PLAYER,
    val createdAt: Long = System.currentTimeMillis()
)

data class TournamentRegistration(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val inGameId: String = "",
    val inGameUsername: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.bKash,
    val trxId: String = "",
    val status: RegistrationStatus = RegistrationStatus.PENDING,
    val rejectionReason: String = "",
    val feeAmount: Int = 100,
    val submittedAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null,
    val reviewedBy: String? = null
)

data class MatchPlayer(
    val id: String = "",
    val name: String = "",
    val inGameUsername: String = "",
    val inGameId: String = ""
)

data class TournamentMatch(
    val id: String = UUID.randomUUID().toString(),
    val tournamentId: String = "dhaka_efootball_2026",
    val round: Int = 1,
    val matchIndex: Int = 0,
    val player1: MatchPlayer? = null,
    val player2: MatchPlayer? = null,
    val player1Score: Int = 0,
    val player2Score: Int = 0,
    val winnerId: String? = null,
    val status: MatchStatus = MatchStatus.SCHEDULED,
    val isBye: Boolean = false,
    val startTime: String = "TBD"
)

data class TournamentInfo(
    val id: String = "dhaka_efootball_2026",
    val title: String = "Dhaka eFootball Open Championship",
    val entryFee: Int = 100,
    val bkashNumber: String = "01904031478",
    val nagadNumber: String = "01904031478",
    val isRegistrationOpen: Boolean = true,
    val matchDurationMinutes: Int = 10,
    val totalRounds: Int = 0,
    val champion: String? = null,
    val championUsername: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "info"
)

data class TournamentRules(
    val matchDuration: String = "10 Mins",
    val extraTimePk: String = "ET/PK ON",
    val substitutions: String = "5 Subs",
    val rematchRule: String = "15-min rematch rule",
    val walkoverGrace: String = "10-min walkover grace",
    val matchSettingsDetails: String = "10 Minutes standard full-time. Extra Time & Penalty Shootout enabled for all knockout matches. Equalized condition.",
    val squadFairPlayDetails: String = "Dream Team squad selection. Exploiting pause glitches, corner traps, or kick-off exploits will result in immediate disqualification.",
    val networkDisputesDetails: String = "Disconnect before 15th min at 0-0 permits rematch. Disconnect after 15th min forfeits as 0-3 unless verified server outage.",
    val scoreReportingDetails: String = "Both players must capture full-screen screenshots showing user IDs and final score. Winners must report within 15 mins.",
    val punctualityConductDetails: String = "10-minute maximum walkover grace period for room lobby joining. Unsportsmanlike conduct results in a permanent ban.",
    val organizerContact: String = "Jadid Mollik (WhatsApp: 01980000601)",
    val lastUpdatedBy: String = "Organizer",
    val updatedAt: Long = System.currentTimeMillis()
)
