package com.example.ui.bracket

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.bracket.BracketEngine
import com.example.data.model.TournamentInfo
import com.example.data.model.TournamentMatch
import com.example.ui.components.CyberGlassCard
import com.example.ui.components.MatchCardNode
import com.example.ui.theme.CyanDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.GoldCrown
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.StatusEmerald

@Composable
fun BracketViewer(
    tournament: TournamentInfo,
    matches: List<TournamentMatch>,
    selectedRound: Int,
    onSelectRound: (Int) -> Unit,
    currentUserId: String?,
    onMatchClick: ((TournamentMatch) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val totalRounds = tournament.totalRounds

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        // Champion Banner if crowned
        AnimatedVisibility(visible = tournament.champion != null) {
            ChampionBanner(
                championName = tournament.champion ?: "",
                championUsername = tournament.championUsername ?: "",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (matches.isEmpty() || totalRounds == 0) {
            EmptyBracketState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            )
        } else {
            // Round Filter Tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(totalRounds) { index ->
                    val roundNum = index + 1
                    val isSelected = roundNum == selectedRound
                    val roundTitle = BracketEngine.getRoundName(roundNum, totalRounds)

                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectRound(roundNum) },
                        label = {
                            Text(
                                text = roundTitle.uppercase(),
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isSelected) Slate950 else Slate400
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyanBright,
                            containerColor = Slate900
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) NeonCyanBright else Slate700,
                            selectedBorderColor = NeonCyanBright,
                            borderWidth = 1.dp
                        ),
                        modifier = Modifier.testTag("round_tab_$roundNum")
                    )
                }
            }

            // Matches in selected round
            val roundMatches = matches.filter { it.round == selectedRound }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(roundMatches, key = { it.id }) { match ->
                    val isUserMatch = currentUserId != null &&
                            (match.player1?.id == currentUserId || match.player2?.id == currentUserId)

                    MatchCardNode(
                        match = match,
                        isUserMatch = isUserMatch,
                        onAdminEdit = if (onMatchClick != null) { { onMatchClick(match) } } else null
                    )
                }
            }
        }
    }
}

@Composable
fun ChampionBanner(
    championName: String,
    championUsername: String,
    modifier: Modifier = Modifier
) {
    CyberGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = GoldCrown),
        borderColor = GoldCrown,
        backgroundColor = Slate900
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(GoldCrown.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(GoldCrown.copy(alpha = 0.2f), CircleShape)
                        .border(1.5.dp, GoldCrown, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Champion Trophy",
                        tint = GoldCrown,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "TOURNAMENT CHAMPION",
                    color = GoldCrown,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp
                )

                Text(
                    text = championName,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )

                if (championUsername.isNotEmpty()) {
                    Text(
                        text = "eFootball Gamertag: @$championUsername",
                        color = Slate400,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyBracketState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AccountTree,
            contentDescription = null,
            tint = Slate700,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tournament Bracket Not Generated Yet",
            color = Slate400,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Once the Host finishes registration verification, the 1v1 matchmaking draw will appear here live.",
            color = Slate400.copy(alpha = 0.7f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}
