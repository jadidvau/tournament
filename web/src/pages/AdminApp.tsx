import React, { useState } from 'react';
import {
  ShieldAlert,
  Check,
  X,
  Settings,
  Layers,
  BarChart3,
  Trophy,
  DollarSign,
  Users,
  Play,
  RotateCcw,
  Sparkles,
  Edit2,
  Lock
} from 'lucide-react';
import {
  MatchStatus,
  TournamentInfo,
  TournamentMatch,
  TournamentRegistration,
  UserProfile
} from '../types/tournament';
import { BracketViewer } from '../components/BracketViewer';

interface AdminAppProps {
  currentUser: UserProfile;
  tournament: TournamentInfo;
  registrations: TournamentRegistration[];
  matches: TournamentMatch[];
  onApproveRegistration: (id: string) => Promise<void>;
  onRejectRegistration: (id: string, reason: string) => Promise<void>;
  onGenerateBracket: () => Promise<void>;
  onUpdateMatchScore: (
    matchId: string,
    p1Score: number,
    p2Score: number,
    status: MatchStatus
  ) => Promise<void>;
  onUpdateSettings: (
    title: string,
    fee: number,
    bkash: string,
    nagad: string,
    isOpen: boolean,
    duration: number
  ) => Promise<void>;
  onElevateToAdmin: () => void;
}

export const AdminApp: React.FC<AdminAppProps> = ({
  currentUser,
  tournament,
  registrations,
  matches,
  onApproveRegistration,
  onRejectRegistration,
  onGenerateBracket,
  onUpdateMatchScore,
  onUpdateSettings,
  onElevateToAdmin
}) => {
  const [adminTab, setAdminTab] = useState<'queue' | 'bracket' | 'settings' | 'stats'>('queue');
  const [selectedMatch, setSelectedMatch] = useState<TournamentMatch | null>(null);

  // Rejection Dialog State
  const [rejectingReg, setRejectingReg] = useState<TournamentRegistration | null>(null);
  const [rejectReason, setRejectReason] = useState('Payment not received or invalid TrxID.');

  // Score Editor State
  const [p1Score, setP1Score] = useState(0);
  const [p2Score, setP2Score] = useState(0);
  const [matchStatus, setMatchStatus] = useState<MatchStatus>('scheduled');

  // Settings Form State
  const [title, setTitle] = useState(tournament.title);
  const [fee, setFee] = useState(tournament.entryFee.toString());
  const [bkash, setBkash] = useState(tournament.bkashNumber);
  const [nagad, setNagad] = useState(tournament.nagadNumber);
  const [isOpen, setIsOpen] = useState(tournament.isRegistrationOpen);
  const [duration, setDuration] = useState(tournament.matchDurationMinutes.toString());
  const [settingsSaved, setSettingsSaved] = useState(false);

  // Gatekeeper: Reject non-admins
  if (currentUser.role !== 'admin') {
    return (
      <div className="max-w-md mx-auto my-20 p-8 glass-panel rounded-3xl text-center space-y-4 border border-rose-500/40">
        <div className="w-16 h-16 rounded-2xl bg-rose-500/20 border border-rose-500 flex items-center justify-center mx-auto text-rose-400">
          <Lock className="w-8 h-8" />
        </div>
        <h2 className="font-esports text-xl font-black text-white">Admin Access Restricted</h2>
        <p className="text-xs text-slate-400">
          This dashboard is reserved exclusively for the organizer (<code>nogorigangjadid@gmail.com</code>). Regular players are locked to the Player View.
        </p>
        <a
          href="/player"
          className="inline-block w-full py-3 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-black text-xs uppercase tracking-wider shadow-neon transition-all"
        >
          Return to Player View
        </a>
      </div>
    );
  }

  const pendingList = registrations.filter(r => r.status === 'pending');
  const approvedList = registrations.filter(r => r.status === 'joined');
  const completedMatches = matches.filter(m => m.status === 'completed');

  const openScoreEditor = (match: TournamentMatch) => {
    setSelectedMatch(match);
    setP1Score(match.player1Score);
    setP2Score(match.player2Score);
    setMatchStatus(match.status);
  };

  const handleScoreSave = async () => {
    if (!selectedMatch) return;
    await onUpdateMatchScore(selectedMatch.id, p1Score, p2Score, matchStatus);
    setSelectedMatch(null);
  };

  const handleSettingsSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await onUpdateSettings(
      title,
      parseInt(fee) || 100,
      bkash,
      nagad,
      isOpen,
      parseInt(duration) || 10
    );
    setSettingsSaved(true);
    setTimeout(() => setSettingsSaved(false), 3000);
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Admin Sub Navigation Tabs */}
      <div className="flex items-center gap-2 overflow-x-auto pb-2 border-b border-slate-800/60">
        {[
          { id: 'queue', label: `Pending Queue (${pendingList.length})`, icon: ShieldAlert },
          { id: 'bracket', label: 'Live Bracket & Scores', icon: Layers },
          { id: 'settings', label: 'Tournament Settings', icon: Settings },
          { id: 'stats', label: 'Stats & Prize Pool', icon: BarChart3 }
        ].map(tab => {
          const Icon = tab.icon;
          const isActive = adminTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => setAdminTab(tab.id as any)}
              className={`px-4 py-2 rounded-full text-xs font-bold whitespace-nowrap transition-all flex items-center gap-2 border ${
                isActive
                  ? 'bg-purple-500/10 border-purple-500/30 text-purple-300 shadow-[0_0_12px_rgba(168,85,247,0.2)]'
                  : 'border-transparent text-slate-400 hover:text-slate-200 bg-slate-900/40 hover:bg-slate-900/70'
              }`}
            >
              <Icon className="w-3.5 h-3.5" />
              {tab.label}
            </button>
          );
        })}
      </div>

      {/* TAB 1: Pending Registrations Queue */}
      {adminTab === 'queue' && (
        <div className="space-y-6">
          {/* Action Header Card */}
          <div className="flex flex-wrap items-center justify-between gap-4 p-6 bg-slate-900/60 border border-purple-500/20 rounded-[28px] shadow-2xl backdrop-blur-md">
            <div>
              <p className="text-[10px] font-bold text-purple-400 uppercase tracking-widest leading-none mb-1">
                Moderation
              </p>
              <h2 className="text-lg font-black italic tracking-tight text-white uppercase">
                Registration Verifications
              </h2>
              <p className="text-xs text-slate-400">
                {approvedList.length} players approved and ready for single-elimination matchmaking draw.
              </p>
            </div>
            <button
              onClick={onGenerateBracket}
              className="px-5 py-3 rounded-2xl bg-purple-600 hover:bg-purple-500 text-white font-black text-xs uppercase tracking-wider shadow-[0_8px_20px_rgba(168,85,247,0.3)] transition-all flex items-center gap-2"
            >
              <RotateCcw className="w-4 h-4" />
              Seed 1v1 Bracket ({approvedList.length} Players)
            </button>
          </div>

          {/* Queue List */}
          {pendingList.length === 0 ? (
            <div className="text-center py-16 glass-panel rounded-[32px] p-8">
              <Check className="w-12 h-12 text-emerald-400 mx-auto mb-3" />
              <h3 className="text-base font-bold text-white mb-1">Queue is Clear</h3>
              <p className="text-xs text-slate-400">No pending payments waiting for review.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {pendingList.map(reg => (
                <div
                  key={reg.id}
                  className="rounded-[24px] bg-slate-900/60 border border-amber-500/30 p-5 space-y-4 backdrop-blur-md shadow-xl"
                >
                  <div className="flex items-center justify-between">
                    <div>
                      <h4 className="font-bold text-white text-base">{reg.fullName}</h4>
                      <p className="text-xs text-cyan-400 font-medium">
                        @{reg.inGameUsername} • ID: {reg.inGameId}
                      </p>
                    </div>
                    <span
                      className={`text-[10px] font-black uppercase px-2.5 py-1 rounded-full ${
                        reg.paymentMethod === 'bKash'
                          ? 'bg-[#E2136E]/10 text-[#E2136E] border border-[#E2136E]/30'
                          : 'bg-[#F7941D]/10 text-[#F7941D] border border-[#F7941D]/30'
                      }`}
                    >
                      {reg.paymentMethod}
                    </span>
                  </div>

                  <div className="bg-slate-950/80 p-3.5 rounded-2xl border border-slate-800 flex items-center justify-between text-xs">
                    <div>
                      <span className="text-slate-500 block uppercase font-bold text-[10px] tracking-wider">TrxID</span>
                      <span className="font-mono text-white font-bold">{reg.trxId}</span>
                    </div>
                    <span className="font-black text-emerald-400 font-mono text-sm">{reg.feeAmount} BDT</span>
                  </div>

                  {/* Actions */}
                  <div className="grid grid-cols-2 gap-3 pt-1">
                    <button
                      onClick={() => setRejectingReg(reg)}
                      className="py-3 rounded-2xl bg-rose-950/40 hover:bg-rose-950/70 border border-rose-500/40 text-rose-300 font-bold text-xs uppercase transition-all flex items-center justify-center gap-1.5"
                    >
                      <X className="w-3.5 h-3.5" /> Reject
                    </button>
                    <button
                      onClick={() => onApproveRegistration(reg.id)}
                      className="py-3 rounded-2xl bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-black text-xs uppercase tracking-wider transition-all flex items-center justify-center gap-1.5 shadow-[0_4px_16px_rgba(16,185,129,0.3)]"
                    >
                      <Check className="w-3.5 h-3.5" /> Approve
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* TAB 2: Live Bracket & Score Management */}
      {adminTab === 'bracket' && (
        <div className="space-y-6">
          <div className="flex items-center justify-between p-4 bg-slate-900/60 border border-slate-800 rounded-2xl text-xs text-slate-400 backdrop-blur-md">
            <span>Click any match card below to enter scores or update match state.</span>
            <button
              onClick={onGenerateBracket}
              className="text-purple-400 font-bold hover:underline"
            >
              Re-Seed Draw
            </button>
          </div>

          <BracketViewer
            tournament={tournament}
            matches={matches}
            onMatchClick={openScoreEditor}
          />
        </div>
      )}

      {/* TAB 3: Tournament Settings */}
      {adminTab === 'settings' && (
        <div className="max-w-xl mx-auto rounded-[32px] bg-slate-900/60 border border-purple-500/20 p-6 sm:p-8 space-y-6 shadow-2xl backdrop-blur-md">
          <div>
            <p className="text-[10px] font-bold text-purple-400 uppercase tracking-widest leading-none mb-1">
              Configuration
            </p>
            <h3 className="font-esports text-lg font-black italic tracking-tight text-white uppercase">
              Championship Settings
            </h3>
          </div>

          {settingsSaved && (
            <div className="p-3.5 rounded-2xl bg-emerald-950/40 border border-emerald-500/40 text-xs text-emerald-300 font-bold flex items-center gap-2">
              <Check className="w-4 h-4" /> Settings updated successfully!
            </div>
          )}

          <form onSubmit={handleSettingsSubmit} className="space-y-4">
            <div>
              <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-2">Tournament Title</label>
              <input
                type="text"
                required
                value={title}
                onChange={e => setTitle(e.target.value)}
                className="w-full bg-slate-950/80 border border-slate-800 rounded-2xl px-4 py-3 text-sm text-white focus:outline-none focus:border-purple-500 transition-colors"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-2">Entry Fee (BDT)</label>
                <input
                  type="number"
                  required
                  value={fee}
                  onChange={e => setFee(e.target.value)}
                  className="w-full bg-slate-950/80 border border-slate-800 rounded-2xl px-4 py-3 text-sm text-white focus:outline-none focus:border-purple-500 transition-colors"
                />
              </div>
              <div>
                <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-2">Match Duration (Mins)</label>
                <input
                  type="number"
                  required
                  value={duration}
                  onChange={e => setDuration(e.target.value)}
                  className="w-full bg-slate-950/80 border border-slate-800 rounded-2xl px-4 py-3 text-sm text-white focus:outline-none focus:border-purple-500 transition-colors"
                />
              </div>
            </div>

            <div>
              <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-2">bKash Personal Number</label>
              <input
                type="text"
                required
                value={bkash}
                onChange={e => setBkash(e.target.value)}
                className="w-full bg-slate-950/80 border border-slate-800 rounded-2xl px-4 py-3 text-sm text-white font-mono focus:outline-none focus:border-purple-500 transition-colors"
              />
            </div>

            <div>
              <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-2">Nagad Personal Number</label>
              <input
                type="text"
                required
                value={nagad}
                onChange={e => setNagad(e.target.value)}
                className="w-full bg-slate-950/80 border border-slate-800 rounded-2xl px-4 py-3 text-sm text-white font-mono focus:outline-none focus:border-purple-500 transition-colors"
              />
            </div>

            {/* Registration Open Toggle */}
            <div className="flex items-center justify-between p-4 bg-slate-950/80 rounded-2xl border border-slate-800">
              <div>
                <span className="text-sm font-bold text-white block">Registration Status</span>
                <span className="text-xs text-slate-500">Allow players to submit new entries</span>
              </div>
              <input
                type="checkbox"
                checked={isOpen}
                onChange={e => setIsOpen(e.target.checked)}
                className="w-5 h-5 accent-purple-600 rounded cursor-pointer"
              />
            </div>

            <button
              type="submit"
              className="w-full py-4 rounded-2xl bg-purple-600 hover:bg-purple-500 text-white font-black text-sm uppercase tracking-wider shadow-[0_8px_20px_rgba(168,85,247,0.3)] transition-all"
            >
              Save Tournament Settings
            </button>
          </form>
        </div>
      )}

      {/* TAB 4: Stats & Prize Pool */}
      {adminTab === 'stats' && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="p-6 rounded-[28px] bg-slate-900/60 border border-slate-800 space-y-2 backdrop-blur-md shadow-xl">
            <span className="text-[10px] font-bold uppercase tracking-widest text-slate-500">Total Registrations</span>
            <p className="text-3xl font-black text-white">{registrations.length}</p>
          </div>
          <div className="p-6 rounded-[28px] bg-slate-900/60 border border-amber-500/20 space-y-2 backdrop-blur-md shadow-xl">
            <span className="text-[10px] font-bold uppercase tracking-widest text-amber-400">Pending Approvals</span>
            <p className="text-3xl font-black text-amber-400">{pendingList.length}</p>
          </div>
          <div className="p-6 rounded-[28px] bg-slate-900/60 border border-emerald-500/20 space-y-2 backdrop-blur-md shadow-xl">
            <span className="text-[10px] font-bold uppercase tracking-widest text-emerald-400">Approved Players</span>
            <p className="text-3xl font-black text-emerald-400">{approvedList.length}</p>
          </div>
          <div className="p-6 rounded-[28px] bg-slate-900/60 border border-purple-500/20 space-y-2 backdrop-blur-md shadow-xl">
            <span className="text-[10px] font-bold uppercase tracking-widest text-purple-400">Total Prize Pool</span>
            <p className="text-3xl font-black text-purple-400 font-mono">
              {approvedList.length * tournament.entryFee} BDT
            </p>
          </div>
        </div>
      )}

      {/* Rejection Modal */}
      {rejectingReg && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-rose-500/40 rounded-[32px] p-6 sm:p-8 max-w-md w-full space-y-4 shadow-2xl">
            <h3 className="text-lg font-black text-rose-400">
              Reject Registration for {rejectingReg.fullName}
            </h3>
            <p className="text-xs text-slate-400">
              State the rejection reason so the player can correct their payment or transaction ID.
            </p>
            <input
              type="text"
              value={rejectReason}
              onChange={e => setRejectReason(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-2xl px-4 py-3 text-sm text-white focus:outline-none focus:border-rose-500"
            />
            <div className="flex justify-end gap-3 pt-2">
              <button
                onClick={() => setRejectingReg(null)}
                className="px-4 py-2 text-xs font-bold text-slate-400 hover:text-white"
              >
                Cancel
              </button>
              <button
                onClick={async () => {
                  await onRejectRegistration(rejectingReg.id, rejectReason);
                  setRejectingReg(null);
                }}
                className="px-5 py-2.5 rounded-2xl bg-rose-600 hover:bg-rose-500 text-white font-bold text-xs shadow-[0_4px_16px_rgba(244,63,94,0.3)]"
              >
                Confirm Rejection
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Match Score Editor Modal */}
      {selectedMatch && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-cyan-500/30 rounded-[32px] p-6 sm:p-8 max-w-md w-full space-y-6 shadow-2xl">
            <div>
              <span className="text-[10px] font-bold text-cyan-400 uppercase tracking-widest">{selectedMatch.startTime}</span>
              <h3 className="text-lg font-black text-white mt-1">Live Match Scoring</h3>
            </div>

            {/* Scores */}
            <div className="space-y-4">
              <div className="flex items-center justify-between bg-slate-950/80 p-4 rounded-2xl border border-slate-800">
                <span className="text-sm font-bold text-white truncate max-w-[150px]">
                  {selectedMatch.player1?.name || 'Player 1'}
                </span>
                <div className="flex items-center gap-3">
                  <button
                    onClick={() => setP1Score(Math.max(0, p1Score - 1))}
                    className="w-8 h-8 rounded-xl bg-slate-800 text-white font-bold hover:bg-slate-700"
                  >
                    -
                  </button>
                  <span className="text-xl font-black text-cyan-400 w-6 text-center">{p1Score}</span>
                  <button
                    onClick={() => setP1Score(p1Score + 1)}
                    className="w-8 h-8 rounded-xl bg-slate-800 text-white font-bold hover:bg-slate-700"
                  >
                    +
                  </button>
                </div>
              </div>

              <div className="flex items-center justify-between bg-slate-950/80 p-4 rounded-2xl border border-slate-800">
                <span className="text-sm font-bold text-white truncate max-w-[150px]">
                  {selectedMatch.player2?.name || 'Player 2'}
                </span>
                <div className="flex items-center gap-3">
                  <button
                    onClick={() => setP2Score(Math.max(0, p2Score - 1))}
                    className="w-8 h-8 rounded-xl bg-slate-800 text-white font-bold hover:bg-slate-700"
                  >
                    -
                  </button>
                  <span className="text-xl font-black text-cyan-400 w-6 text-center">{p2Score}</span>
                  <button
                    onClick={() => setP2Score(p2Score + 1)}
                    className="w-8 h-8 rounded-xl bg-slate-800 text-white font-bold hover:bg-slate-700"
                  >
                    +
                  </button>
                </div>
              </div>
            </div>

            {/* Status */}
            <div>
              <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-2">Match Status</label>
              <div className="grid grid-cols-3 gap-2">
                {(['scheduled', 'live', 'completed'] as MatchStatus[]).map(st => (
                  <button
                    key={st}
                    type="button"
                    onClick={() => setMatchStatus(st)}
                    className={`py-2 text-xs font-bold uppercase rounded-xl border ${
                      matchStatus === st
                        ? 'bg-cyan-500 text-slate-950 border-cyan-400 font-black'
                        : 'bg-slate-950 text-slate-400 border-slate-800'
                    }`}
                  >
                    {st}
                  </button>
                ))}
              </div>
            </div>

            <div className="flex justify-end gap-3 pt-2 border-t border-slate-800">
              <button
                onClick={() => setSelectedMatch(null)}
                className="px-4 py-2 text-xs font-bold text-slate-400 hover:text-white"
              >
                Cancel
              </button>
              <button
                onClick={handleScoreSave}
                className="px-5 py-3 rounded-2xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-black text-xs uppercase tracking-wider shadow-[0_4px_16px_rgba(6,182,212,0.3)]"
              >
                Apply & Advance Winner
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
