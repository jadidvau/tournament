import { MatchPlayer, MatchStatus, TournamentInfo, TournamentMatch, TournamentRegistration } from '../types/tournament';

export function nextPowerOf2(n: number): number {
  if (n <= 2) return 2;
  let power = 2;
  while (power < n) {
    power *= 2;
  }
  return power;
}

export function generateBracket(
  approvedRegistrations: TournamentRegistration[],
  tournament: TournamentInfo
): { matches: TournamentMatch[]; updatedTournament: TournamentInfo } {
  if (approvedRegistrations.length === 0) {
    return {
      matches: [],
      updatedTournament: { ...tournament, totalRounds: 0, champion: null, championUsername: null }
    };
  }

  // Shuffle players
  const players: MatchPlayer[] = [...approvedRegistrations]
    .sort(() => Math.random() - 0.5)
    .map(reg => ({
      id: reg.userId,
      name: reg.fullName,
      inGameUsername: reg.inGameUsername,
      inGameId: reg.inGameId
    }));

  const count = players.length;
  const bracketSize = nextPowerOf2(count);
  const totalRounds = Math.log2(bracketSize);
  const byeCount = bracketSize - count;

  const allMatches: TournamentMatch[] = [];

  // Round 1
  const r1MatchCount = bracketSize / 2;
  const r1Matches: TournamentMatch[] = [];

  let playerIndex = 0;
  let byesLeft = byeCount;

  for (let i = 0; i < r1MatchCount; i++) {
    const matchId = `match_r1_${i}_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`;
    const p1 = playerIndex < players.length ? players[playerIndex++] : null;
    let p2: MatchPlayer | null = null;

    if (byesLeft > 0) {
      byesLeft--;
      p2 = null; // BYE
    } else {
      p2 = playerIndex < players.length ? players[playerIndex++] : null;
    }

    const isByeMatch = p2 === null && p1 !== null;

    const match: TournamentMatch = {
      id: matchId,
      tournamentId: tournament.id,
      round: 1,
      matchIndex: i,
      player1: p1,
      player2: p2,
      player1Score: isByeMatch ? 1 : 0,
      player2Score: 0,
      winnerId: isByeMatch && p1 ? p1.id : null,
      status: isByeMatch ? 'completed' : 'scheduled',
      isBye: isByeMatch,
      startTime: isByeMatch ? 'BYE (Automatic Advance)' : `Round 1 - Match ${i + 1}`
    };
    r1Matches.push(match);
  }
  allMatches.push(...r1Matches);

  // Future Rounds
  let matchesInRound = r1MatchCount / 2;
  for (let r = 2; r <= totalRounds; r++) {
    const roundMatches: TournamentMatch[] = [];
    for (let i = 0; i < matchesInRound; i++) {
      const matchId = `match_r${r}_${i}_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`;
      const roundName =
        r === totalRounds
          ? 'Grand Final'
          : r === totalRounds - 1
          ? `Semi-Final ${i + 1}`
          : r === totalRounds - 2
          ? `Quarter-Final ${i + 1}`
          : `Round ${r} - Match ${i + 1}`;

      const match: TournamentMatch = {
        id: matchId,
        tournamentId: tournament.id,
        round: r,
        matchIndex: i,
        player1: null,
        player2: null,
        player1Score: 0,
        player2Score: 0,
        winnerId: null,
        status: 'scheduled',
        isBye: false,
        startTime: roundName
      };
      roundMatches.push(match);
    }
    allMatches.push(...roundMatches);
    matchesInRound /= 2;
  }

  // Advance BYE winners to Round 2
  if (totalRounds >= 2) {
    for (const r1 of r1Matches) {
      if (r1.isBye && r1.winnerId && r1.player1) {
        const targetMatchIndex = Math.floor(r1.matchIndex / 2);
        const isSlot1 = r1.matchIndex % 2 === 0;

        const r2Idx = allMatches.findIndex(m => m.round === 2 && m.matchIndex === targetMatchIndex);
        if (r2Idx !== -1) {
          if (isSlot1) {
            allMatches[r2Idx].player1 = r1.player1;
          } else {
            allMatches[r2Idx].player2 = r1.player1;
          }
        }
      }
    }
  }

  const updatedTournament: TournamentInfo = {
    ...tournament,
    totalRounds,
    champion: null,
    championUsername: null
  };

  return { matches: allMatches, updatedTournament };
}

export function advanceMatchWinner(
  matches: TournamentMatch[],
  tournament: TournamentInfo,
  matchId: string,
  p1Score: number,
  p2Score: number,
  status: MatchStatus
): { matches: TournamentMatch[]; updatedTournament: TournamentInfo } {
  const updatedMatches = [...matches];
  const matchIndex = updatedMatches.findIndex(m => m.id === matchId);
  if (matchIndex === -1) return { matches, updatedTournament: tournament };

  const currentMatch = { ...updatedMatches[matchIndex] };
  const p1 = currentMatch.player1;
  const p2 = currentMatch.player2;

  let winner: MatchPlayer | null = null;
  if (status === 'completed') {
    if (p1Score > p2Score && p1) winner = p1;
    else if (p2Score > p1Score && p2) winner = p2;
    else if (p1 && !p2) winner = p1;
  }

  currentMatch.player1Score = p1Score;
  currentMatch.player2Score = p2Score;
  currentMatch.winnerId = winner ? winner.id : null;
  currentMatch.status = status;
  updatedMatches[matchIndex] = currentMatch;

  let updatedTournament = { ...tournament };

  if (currentMatch.round === tournament.totalRounds && status === 'completed' && winner) {
    updatedTournament.champion = winner.name;
    updatedTournament.championUsername = winner.inGameUsername;
  } else if (currentMatch.round < tournament.totalRounds && winner) {
    const nextRound = currentMatch.round + 1;
    const nextMatchIdx = Math.floor(currentMatch.matchIndex / 2);
    const isSlot1 = currentMatch.matchIndex % 2 === 0;

    const targetIdx = updatedMatches.findIndex(m => m.round === nextRound && m.matchIndex === nextMatchIdx);
    if (targetIdx !== -1) {
      const targetMatch = { ...updatedMatches[targetIdx] };
      if (isSlot1) {
        targetMatch.player1 = winner;
      } else {
        targetMatch.player2 = winner;
      }
      updatedMatches[targetIdx] = targetMatch;
    }
  }

  return { matches: updatedMatches, updatedTournament };
}
