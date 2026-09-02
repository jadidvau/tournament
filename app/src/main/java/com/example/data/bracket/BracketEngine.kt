package com.example.data.bracket

import com.example.data.model.MatchPlayer
import com.example.data.model.MatchStatus
import com.example.data.model.TournamentInfo
import com.example.data.model.TournamentMatch
import com.example.data.model.TournamentRegistration
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.pow

object BracketEngine {

    /**
     * Calculates the nearest power of 2 greater than or equal to the count.
     */
    fun nextPowerOf2(n: Int): Int {
        if (n <= 2) return 2
        var power = 2
        while (power < n) {
            power *= 2
        }
        return power
    }

    /**
     * Generates a single-elimination tournament bracket for the approved players.
     */
    fun generateBracket(
        approvedRegistrations: List<TournamentRegistration>,
        tournament: TournamentInfo
    ): Pair<List<TournamentMatch>, TournamentInfo> {
        if (approvedRegistrations.isEmpty()) {
            return Pair(emptyList(), tournament.copy(totalRounds = 0, champion = null))
        }

        // Shuffle approved players for fair randomized seeding
        val players = approvedRegistrations.shuffled().map { reg ->
            MatchPlayer(
                id = reg.userId,
                name = reg.fullName,
                inGameUsername = reg.inGameUsername,
                inGameId = reg.inGameId
            )
        }

        val count = players.size
        val bracketSize = nextPowerOf2(count)
        val totalRounds = (log2(bracketSize.toDouble())).toInt()
        val byeCount = bracketSize - count

        val allMatches = mutableListOf<TournamentMatch>()

        // 1. Generate Round 1
        val r1MatchCount = bracketSize / 2
        val r1Matches = mutableListOf<TournamentMatch>()

        // Distribute players and byes
        // Players with byes automatically win Round 1
        var playerIndex = 0
        var byesLeft = byeCount

        for (i in 0 until r1MatchCount) {
            val matchId = UUID.randomUUID().toString()
            val p1 = if (playerIndex < players.size) players[playerIndex++] else null
            
            // Check if this matchup gets a BYE
            val p2 = if (byesLeft > 0) {
                byesLeft--
                null // Player 2 is a BYE
            } else {
                if (playerIndex < players.size) players[playerIndex++] else null
            }

            val isByeMatch = p2 == null && p1 != null

            val match = TournamentMatch(
                id = matchId,
                tournamentId = tournament.id,
                round = 1,
                matchIndex = i,
                player1 = p1,
                player2 = p2,
                player1Score = if (isByeMatch) 1 else 0,
                player2Score = 0,
                winnerId = if (isByeMatch) p1?.id else null,
                status = if (isByeMatch) MatchStatus.COMPLETED else MatchStatus.SCHEDULED,
                isBye = isByeMatch,
                startTime = if (isByeMatch) "BYE - Automatic Advance" else "Round 1 - Match ${i + 1}"
            )
            r1Matches.add(match)
        }
        allMatches.addAll(r1Matches)

        // 2. Generate Future Rounds (Round 2 to Final)
        var matchesInRound = r1MatchCount / 2
        for (r in 2..totalRounds) {
            val roundMatches = mutableListOf<TournamentMatch>()
            for (i in 0 until matchesInRound) {
                val matchId = UUID.randomUUID().toString()
                val roundName = when (r) {
                    totalRounds -> "Grand Final"
                    totalRounds - 1 -> "Semi-Final ${i + 1}"
                    totalRounds - 2 -> "Quarter-Final ${i + 1}"
                    else -> "Round $r - Match ${i + 1}"
                }
                
                val match = TournamentMatch(
                    id = matchId,
                    tournamentId = tournament.id,
                    round = r,
                    matchIndex = i,
                    player1 = null,
                    player2 = null,
                    player1Score = 0,
                    player2Score = 0,
                    winnerId = null,
                    status = MatchStatus.SCHEDULED,
                    isBye = false,
                    startTime = roundName
                )
                roundMatches.add(match)
            }
            allMatches.addAll(roundMatches)
            matchesInRound /= 2
        }

        // 3. Propagate BYE winners from Round 1 into Round 2
        if (totalRounds >= 2) {
            for (r1 in r1Matches) {
                if (r1.isBye && r1.winnerId != null && r1.player1 != null) {
                    val targetMatchIndex = r1.matchIndex / 2
                    val isPlayer1Slot = (r1.matchIndex % 2 == 0)

                    val r2MatchIdx = allMatches.indexOfFirst { it.round == 2 && it.matchIndex == targetMatchIndex }
                    if (r2MatchIdx != -1) {
                        val r2Match = allMatches[r2MatchIdx]
                        val updatedR2 = if (isPlayer1Slot) {
                            r2Match.copy(player1 = r1.player1)
                        } else {
                            r2Match.copy(player2 = r1.player1)
                        }
                        allMatches[r2MatchIdx] = updatedR2
                    }
                }
            }
        }

        val updatedTournament = tournament.copy(
            totalRounds = totalRounds,
            champion = null,
            championUsername = null
        )

        return Pair(allMatches, updatedTournament)
    }

    /**
     * Updates a match score and status, and auto-advances the winner to the next round.
     */
    fun updateMatchAndAdvance(
        matches: List<TournamentMatch>,
        tournament: TournamentInfo,
        matchId: String,
        p1Score: Int,
        p2Score: Int,
        status: MatchStatus
    ): Pair<List<TournamentMatch>, TournamentInfo> {
        val updatedMatches = matches.toMutableList()
        val matchIndex = updatedMatches.indexOfFirst { it.id == matchId }
        if (matchIndex == -1) return Pair(matches, tournament)

        val currentMatch = updatedMatches[matchIndex]
        val p1 = currentMatch.player1
        val p2 = currentMatch.player2

        // Determine winner
        val winner: MatchPlayer? = when {
            status == MatchStatus.COMPLETED && p1Score > p2Score -> p1
            status == MatchStatus.COMPLETED && p2Score > p1Score -> p2
            status == MatchStatus.COMPLETED && p1 != null && p2 == null -> p1
            else -> null
        }

        val updatedCurrent = currentMatch.copy(
            player1Score = p1Score,
            player2Score = p2Score,
            winnerId = winner?.id,
            status = status
        )
        updatedMatches[matchIndex] = updatedCurrent

        var updatedTournament = tournament

        // If completed and in the Final round, crown champion
        if (currentMatch.round == tournament.totalRounds && status == MatchStatus.COMPLETED && winner != null) {
            updatedTournament = tournament.copy(
                champion = winner.name,
                championUsername = winner.inGameUsername
            )
        } else if (currentMatch.round < tournament.totalRounds && winner != null) {
            // Advance winner to the next round
            val nextRound = currentMatch.round + 1
            val nextMatchIdx = currentMatch.matchIndex / 2
            val isSlot1 = (currentMatch.matchIndex % 2 == 0)

            val targetIdx = updatedMatches.indexOfFirst { it.round == nextRound && it.matchIndex == nextMatchIdx }
            if (targetIdx != -1) {
                val targetMatch = updatedMatches[targetIdx]
                val updatedTarget = if (isSlot1) {
                    targetMatch.copy(player1 = winner)
                } else {
                    targetMatch.copy(player2 = winner)
                }
                updatedMatches[targetIdx] = updatedTarget
            }
        }

        return Pair(updatedMatches, updatedTournament)
    }

    fun getRoundName(round: Int, totalRounds: Int): String {
        return when {
            totalRounds <= 1 -> "Championship Final"
            round == totalRounds -> "Grand Final"
            round == totalRounds - 1 -> "Semi-Finals"
            round == totalRounds - 2 -> "Quarter-Finals"
            round == 1 -> "Round 1"
            else -> "Round $round"
        }
    }
}
