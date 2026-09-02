import React, { useState } from 'react';
import { Trophy, CheckCircle2, ShieldAlert, Sparkles, Layers } from 'lucide-react';
import { TournamentInfo, TournamentMatch } from '../types/tournament';

interface BracketViewerProps {
  tournament: TournamentInfo;
  matches: TournamentMatch[];
  currentUserId?: string;
  onMatchClick?: (match: TournamentMatch) => void;
}

export const BracketViewer: React.FC<BracketViewerProps> = ({
  tournament,
  matches,
  currentUserId,
  onMatchClick
}) => {
  const [activeRound, setActiveRound] = useState<number>(1);
  const totalRounds = tournament.totalRounds || 1;

  const roundMatches = matches.filter(m => m.round === activeRound);

  const getRoundLabel = (r: number) => {
    if (r === totalRounds) return 'Grand Final';
    if (r === totalRounds - 1) return 'Semi-Finals';
    if (r === totalRounds - 2) return 'Quarter-Finals';
    return `Round ${r}`;
  };

  return (
    <div className="space-y-6">
      {/* Champion Crown Banner */}
      {tournament.champion && (
        <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-amber-500/20 via-yellow-500/10 to-transparent border border-yellow-500/50 p-6 shadow-neon-gold">
          <div className="flex items-center gap-4">
            <div className="w-14 h-14 rounded-2xl bg-yellow-500/20 border border-yellow-400 flex items-center justify-center shrink-0">
              <Trophy className="w-8 h-8 text-yellow-400 animate-bounce" />
            </div>
            <div>
              <div className="flex items-center gap-2 text-xs font-black uppercase tracking-widest text-yellow-400">
                <Sparkles className="w-3.5 h-3.5" />
                Tournament Champion
              </div>
              <h2 className="text-2xl font-black text-white">{tournament.champion}</h2>
              <p className="text-sm text-yellow-200/80 font-medium">
                eFootball Gamertag: @{tournament.championUsername}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Round Tabs */}
      {totalRounds > 0 && matches.length > 0 ? (
        <div className="flex items-center gap-2 overflow-x-auto pb-2 border-b border-slate-800/60">
          {Array.from({ length: totalRounds }, (_, i) => i + 1).map(r => (
            <button
              key={r}
              onClick={() => setActiveRound(r)}
              className={`px-4 py-2 rounded-full text-xs font-bold whitespace-nowrap transition-all flex items-center gap-2 border ${
                activeRound === r
                  ? 'bg-cyan-500/10 border-cyan-500/30 text-cyan-400 shadow-[0_0_12px_rgba(6,182,212,0.2)]'
                  : 'border-transparent text-slate-400 hover:text-slate-200 bg-slate-900/40 hover:bg-slate-900/70'
              }`}
            >
              <Layers className="w-3.5 h-3.5" />
              {getRoundLabel(r).toUpperCase()}
            </button>
          ))}
        </div>
      ) : (
        <div className="text-center py-16 glass-panel rounded-[32px] p-8">
          <ShieldAlert className="w-12 h-12 text-slate-600 mx-auto mb-3" />
          <h3 className="text-lg font-bold text-white mb-1">Bracket Not Generated Yet</h3>
          <p className="text-sm text-slate-400 max-w-md mx-auto">
            Once tournament registration closes, the host will seed the 1v1 matchmaking draw.
          </p>
        </div>
      )}

      {/* Matches Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {roundMatches.map(match => {
          const isUserInvolved =
            currentUserId &&
            (match.player1?.id === currentUserId || match.player2?.id === currentUserId);

          return (
            <div
              key={match.id}
              onClick={() => onMatchClick && onMatchClick(match)}
              className={`rounded-[28px] p-5 border transition-all backdrop-blur-md shadow-xl ${
                onMatchClick ? 'cursor-pointer hover:scale-[1.01]' : ''
              } ${
                isUserInvolved
                  ? 'bg-cyan-950/30 border-cyan-400/60 shadow-[0_0_20px_rgba(6,182,212,0.2)]'
                  : match.status === 'live'
                  ? 'bg-slate-900/90 border-rose-500 shadow-[0_0_20px_rgba(244,63,94,0.2)]'
                  : match.status === 'completed'
                  ? 'bg-slate-900/60 border-slate-800/80'
                  : 'bg-slate-900/40 border-slate-800/80 hover:border-slate-700'
              }`}
            >
              {/* Card Header */}
              <div className="flex items-center justify-between mb-4">
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">{match.startTime}</span>
                <span
                  className={`text-[10px] font-black uppercase px-2.5 py-0.5 rounded-full ${
                    match.isBye
                      ? 'bg-purple-900/30 text-purple-300 border border-purple-500/40'
                      : match.status === 'live'
                      ? 'bg-rose-900/30 text-rose-300 border border-rose-500 animate-pulse'
                      : match.status === 'completed'
                      ? 'bg-emerald-900/30 text-emerald-300 border border-emerald-500/40'
                      : 'bg-cyan-950/60 text-cyan-400 border border-cyan-500/30'
                  }`}
                >
                  {match.isBye ? 'BYE ADVANCE' : match.status}
                </span>
              </div>

              {/* Player 1 Row */}
              <div
                className={`flex items-center justify-between p-3.5 rounded-2xl mb-2.5 border transition-colors ${
                  match.winnerId && match.winnerId === match.player1?.id
                    ? 'bg-emerald-950/30 border-emerald-500/60 text-emerald-300 shadow-[0_0_12px_rgba(16,185,129,0.15)]'
                    : 'bg-slate-950/60 border-slate-800/80 text-white'
                }`}
              >
                <div className="flex items-center gap-2.5 truncate">
                  {match.winnerId === match.player1?.id && (
                    <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                  )}
                  <div>
                    <p className="text-sm font-bold truncate">
                      {match.player1 ? match.player1.name : 'TBD (Waiting Winner)'}
                    </p>
                    {match.player1 && (
                      <p className="text-[10px] text-slate-400 font-mono">
                        @{match.player1.inGameUsername} • {match.player1.inGameId}
                      </p>
                    )}
                  </div>
                </div>
                {match.player1 && !match.isBye && (
                  <span className="text-base font-black px-3 py-1 bg-slate-900/90 rounded-xl text-white font-mono border border-slate-800">
                    {match.player1Score}
                  </span>
                )}
              </div>

              {/* Player 2 Row */}
              <div
                className={`flex items-center justify-between p-3.5 rounded-2xl border transition-colors ${
                  match.winnerId && match.winnerId === match.player2?.id
                    ? 'bg-emerald-950/30 border-emerald-500/60 text-emerald-300 shadow-[0_0_12px_rgba(16,185,129,0.15)]'
                    : 'bg-slate-950/60 border-slate-800/80 text-white'
                }`}
              >
                <div className="flex items-center gap-2.5 truncate">
                  {match.winnerId === match.player2?.id && (
                    <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                  )}
                  <div>
                    <p className="text-sm font-bold truncate">
                      {match.isBye
                        ? 'BYE (Automatic Pass)'
                        : match.player2
                        ? match.player2.name
                        : 'TBD (Waiting Winner)'}
                    </p>
                    {match.player2 && !match.isBye && (
                      <p className="text-[10px] text-slate-400 font-mono">
                        @{match.player2.inGameUsername} • {match.player2.inGameId}
                      </p>
                    )}
                  </div>
                </div>
                {match.player2 && !match.isBye && (
                  <span className="text-base font-black px-3 py-1 bg-slate-900/90 rounded-xl text-white font-mono border border-slate-800">
                    {match.player2Score}
                  </span>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
