import React, { useState } from 'react';
import {
  Trophy,
  CreditCard,
  Copy,
  Check,
  Send,
  Gamepad2,
  Clock,
  ShieldCheck,
  AlertCircle,
  Hourglass,
  UserCheck,
  Calendar,
  Sparkles,
  Key,
  Mail,
  User,
  BookOpen,
  MessageCircle,
  Code2,
  ChevronRight
} from 'lucide-react';
import {
  PaymentMethod,
  TournamentInfo,
  TournamentMatch,
  TournamentRegistration,
  UserProfile
} from '../types/tournament';
import { BracketViewer } from '../components/BracketViewer';
import { RulesModal } from '../components/RulesModal';

interface PlayerAppProps {
  currentUser: UserProfile | null;
  tournament: TournamentInfo;
  registration: TournamentRegistration | null;
  userMatch: TournamentMatch | null;
  matches: TournamentMatch[];
  onSubmitPayment: (method: PaymentMethod, trxId: string) => Promise<void>;
  onUpdateProfile: (name: string, phone: string, igId: string, igUsername: string) => Promise<void>;
  onGoogleSignIn: () => void;
}

export const PlayerApp: React.FC<PlayerAppProps> = ({
  currentUser,
  tournament,
  registration,
  userMatch,
  matches,
  onSubmitPayment,
  onUpdateProfile,
  onGoogleSignIn
}) => {
  const [activeTab, setActiveTab] = useState<'overview' | 'match' | 'bracket' | 'profile'>('overview');
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('bKash');
  const [trxId, setTrxId] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [copiedField, setCopiedField] = useState<string | null>(null);
  const [isRulesModalOpen, setIsRulesModalOpen] = useState(false);

  // Profile Form States
  const [fullName, setFullName] = useState(currentUser?.fullName || '');
  const [phoneNumber, setPhoneNumber] = useState(currentUser?.phoneNumber || '');
  const [inGameId, setInGameId] = useState(currentUser?.inGameId || '');
  const [inGameUsername, setInGameUsername] = useState(currentUser?.inGameUsername || '');
  const [isUpdatingProfile, setIsUpdatingProfile] = useState(false);
  const [profileSuccessMsg, setProfileSuccessMsg] = useState(false);

  // Sync state if currentUser changes
  React.useEffect(() => {
    if (currentUser) {
      setFullName(currentUser.fullName || '');
      setPhoneNumber(currentUser.phoneNumber || '');
      setInGameId(currentUser.inGameId || '');
      setInGameUsername(currentUser.inGameUsername || '');
    }
  }, [currentUser]);

  const handleCopy = (text: string, field: string) => {
    navigator.clipboard.writeText(text);
    setCopiedField(field);
    setTimeout(() => setCopiedField(null), 2000);
  };

  const handlePaymentSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!trxId.trim() || !currentUser) return;
    setIsSubmitting(true);
    try {
      await onSubmitPayment(paymentMethod, trxId.trim().toUpperCase());
      setTrxId('');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleProfileSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!currentUser) return;
    setIsUpdatingProfile(true);
    try {
      await onUpdateProfile(fullName, phoneNumber, inGameId, inGameUsername);
      setProfileSuccessMsg(true);
      setTimeout(() => setProfileSuccessMsg(false), 3000);
    } finally {
      setIsUpdatingProfile(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Player App Navigation Sub-Bar */}
      <div className="flex items-center gap-2 overflow-x-auto pb-2 border-b border-slate-800/60">
        {[
          { id: 'overview', label: 'Overview & Registration', icon: CreditCard },
          { id: 'match', label: 'My Match Hub', icon: Gamepad2 },
          { id: 'bracket', label: 'Championship Bracket', icon: Trophy },
          { id: 'profile', label: 'Player Profile', icon: UserCheck }
        ].map(tab => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id as any)}
              className={`px-4 py-2 rounded-full text-xs font-bold whitespace-nowrap transition-all flex items-center gap-2 border ${
                isActive
                  ? 'bg-cyan-500/10 border-cyan-500/30 text-cyan-400 shadow-[0_0_12px_rgba(6,182,212,0.2)]'
                  : 'border-transparent text-slate-400 hover:text-slate-200 bg-slate-900/40 hover:bg-slate-900/70'
              }`}
            >
              <Icon className="w-3.5 h-3.5" />
              {tab.label}
            </button>
          );
        })}
      </div>

      {/* User Status Bar if Logged In */}
      {currentUser && (
        <div className="p-4 rounded-2xl bg-slate-900/50 border border-slate-800 flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            {currentUser.photoURL ? (
              <img
                src={currentUser.photoURL}
                alt={currentUser.fullName}
                className="w-10 h-10 rounded-full object-cover border border-cyan-500/40 shadow-[0_0_12px_rgba(6,182,212,0.2)]"
              />
            ) : (
              <div className="w-10 h-10 rounded-full bg-slate-900 border border-cyan-500/30 flex items-center justify-center">
                <span className="text-xs font-black text-cyan-400">
                  {currentUser.fullName ? currentUser.fullName.slice(0, 2).toUpperCase() : 'TA'}
                </span>
              </div>
            )}
            <div>
              <div className="flex items-center gap-2">
                <span className="text-sm font-bold text-white">{currentUser.fullName}</span>
                {currentUser.email && (
                  <span className="text-[10px] px-2 py-0.5 rounded-full bg-cyan-950 text-cyan-400 border border-cyan-500/30">
                    Google Connected
                  </span>
                )}
              </div>
              <p className="text-xs text-slate-400 font-mono">
                Firestore UID: <span className="text-cyan-400 font-semibold">{currentUser.uid}</span>
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => handleCopy(currentUser.uid, 'uid_badge')}
              className="px-3 py-1.5 rounded-xl bg-slate-950 hover:bg-slate-900 border border-slate-700 text-xs font-bold text-slate-300 transition-all flex items-center gap-1.5"
            >
              {copiedField === 'uid_badge' ? <Check className="w-3.5 h-3.5 text-green-400" /> : <Copy className="w-3.5 h-3.5" />}
              {copiedField === 'uid_badge' ? 'UID Copied' : 'Copy UID'}
            </button>
          </div>
        </div>
      )}

      {/* TAB 1: Overview & Manual Payment Submission */}
      {activeTab === 'overview' && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Tournament Details */}
          <div className="lg:col-span-2 space-y-6">
            <div className="relative overflow-hidden rounded-[32px] bg-slate-900/60 border border-cyan-500/20 p-6 sm:p-8 shadow-2xl backdrop-blur-md">
              <div className="absolute top-0 right-0 p-6 opacity-5 pointer-events-none text-cyan-400">
                <Gamepad2 className="w-32 h-32" />
              </div>

              <div className="flex flex-wrap items-center justify-between gap-4 mb-4 relative z-10">
                <span className="px-3.5 py-1 bg-cyan-500/10 border border-cyan-500/30 text-cyan-400 text-[10px] font-bold uppercase tracking-wider rounded-full">
                  Competitive 1v1 Single-Elimination
                </span>
                <span
                  className={`text-[10px] font-bold uppercase tracking-wider px-3.5 py-1 rounded-full ${
                    tournament.isRegistrationOpen
                      ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/30'
                      : 'bg-rose-500/10 text-rose-400 border border-rose-500/30'
                  }`}
                >
                  {tournament.isRegistrationOpen ? 'Registration Open' : 'Registration Closed'}
                </span>
              </div>

              <div className="relative z-10">
                <p className="text-[11px] font-bold text-cyan-500 uppercase tracking-widest leading-none mb-1">
                  Championship
                </p>
                <h1 className="font-esports text-2xl sm:text-3xl font-black italic tracking-tighter text-white uppercase mb-3">
                  {tournament.title}
                </h1>
                <p className="text-slate-400 text-sm leading-relaxed mb-6">
                  Join the ultimate nationwide esports competition for eFootball mobile and console players in Bangladesh.
                  Verify your entry fee, challenge top competitors, and compete for championship glory.
                </p>
              </div>

              <div className="grid grid-cols-3 gap-3 border-t border-slate-800/80 pt-6 relative z-10">
                <div className="bg-slate-900/40 border border-slate-800/80 rounded-[20px] p-3.5 flex flex-col justify-between">
                  <p className="text-[10px] text-slate-500 font-bold uppercase tracking-widest">Entry Fee</p>
                  <p className="text-lg sm:text-xl font-black text-cyan-400 mt-1">{tournament.entryFee} BDT</p>
                </div>
                <div className="bg-slate-900/40 border border-slate-800/80 rounded-[20px] p-3.5 flex flex-col justify-between">
                  <p className="text-[10px] text-slate-500 font-bold uppercase tracking-widest">Match Length</p>
                  <p className="text-lg sm:text-xl font-black text-white mt-1">{tournament.matchDurationMinutes} Mins</p>
                </div>
                <div className="bg-slate-900/40 border border-slate-800/80 rounded-[20px] p-3.5 flex flex-col justify-between">
                  <p className="text-[10px] text-slate-500 font-bold uppercase tracking-widest">Game Format</p>
                  <p className="text-lg sm:text-xl font-black text-purple-400 mt-1">1v1 Knockout</p>
                </div>
              </div>

              {/* Tournament Rules Modal Trigger Button */}
              <div className="mt-5 pt-5 border-t border-slate-800/60 relative z-10">
                <button
                  type="button"
                  onClick={() => setIsRulesModalOpen(true)}
                  className="w-full py-3.5 px-4 rounded-2xl bg-cyan-500/10 hover:bg-cyan-500/20 active:bg-cyan-500/30 border border-cyan-500/40 text-cyan-300 font-bold text-xs sm:text-sm flex items-center justify-between transition-all group shadow-[0_0_15px_rgba(6,182,212,0.1)]"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-xl bg-cyan-500/20 border border-cyan-500/40 flex items-center justify-center text-cyan-400 group-hover:scale-105 transition-transform">
                      <BookOpen className="w-4 h-4" />
                    </div>
                    <div className="text-left">
                      <p className="text-xs sm:text-sm font-bold text-white group-hover:text-cyan-300 transition-colors">
                        eFootball Tournament Rules & Regulations
                      </p>
                      <p className="text-[10px] text-slate-400">
                        Expandable Accordion: Match settings, squad caps, disconnections & conduct
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-1.5 text-xs text-cyan-400 font-bold">
                    <span>View Rules</span>
                    <ChevronRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
                  </div>
                </button>
              </div>
            </div>

            {/* Registration Status Tracking Banner */}
            {registration && (
              <div
                className={`rounded-[24px] p-5 border backdrop-blur-md ${
                  registration.status === 'joined'
                    ? 'bg-emerald-950/20 border-emerald-500/30'
                    : registration.status === 'pending'
                    ? 'bg-amber-950/20 border-amber-500/30'
                    : 'bg-rose-950/20 border-rose-500/30'
                }`}
              >
                <div className="flex items-center justify-between mb-3">
                  <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">
                    Registration Status (UID: {registration.userId})
                  </span>
                  <span
                    className={`text-[10px] font-black uppercase px-3 py-1 rounded-full ${
                      registration.status === 'joined'
                        ? 'bg-emerald-500/10 text-emerald-300 border border-emerald-500/40'
                        : registration.status === 'pending'
                        ? 'bg-amber-500/10 text-amber-300 border border-amber-500/40'
                        : 'bg-rose-500/10 text-rose-300 border border-rose-500/40'
                    }`}
                  >
                    {registration.status}
                  </span>
                </div>

                {registration.status === 'joined' && (
                  <div className="flex items-start gap-3">
                    <ShieldCheck className="w-5 h-5 text-emerald-400 shrink-0 mt-0.5" />
                    <div>
                      <p className="text-sm font-bold text-white">Payment Verified & Approved!</p>
                      <p className="text-xs text-slate-400 mt-1">
                        You are officially entered into the tournament bracket. Head to "My Match Hub" to see your opponent.
                      </p>
                    </div>
                  </div>
                )}

                {registration.status === 'pending' && (
                  <div className="flex items-start gap-3">
                    <Hourglass className="w-5 h-5 text-amber-400 shrink-0 mt-0.5" />
                    <div>
                      <p className="text-sm font-bold text-white">Payment Under Review (TrxID: {registration.trxId})</p>
                      <p className="text-xs text-slate-400 mt-1">
                        The tournament host is verifying your transaction. Status updates automatically in real-time.
                      </p>
                    </div>
                  </div>
                )}

                {registration.status === 'rejected' && (
                  <div className="flex items-start gap-3">
                    <AlertCircle className="w-5 h-5 text-rose-400 shrink-0 mt-0.5" />
                    <div>
                      <p className="text-sm font-bold text-rose-400">Rejection Reason: {registration.rejectionReason}</p>
                      <p className="text-xs text-slate-400 mt-1">
                        Please check your transaction details and re-submit payment below.
                      </p>
                    </div>
                  </div>
                )}
              </div>
            )}

            {/* Manual Payment Numbers Cards */}
            <div className="space-y-4">
              <h3 className="text-[11px] font-black uppercase text-slate-500 tracking-[0.2em]">
                Official Payment Accounts (Send Money)
              </h3>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {/* bKash Card */}
                <div className="rounded-[24px] bg-slate-900/40 border border-[#E2136E]/30 p-5 relative overflow-hidden backdrop-blur-md">
                  <div className="flex items-center justify-between mb-3">
                    <span className="text-[11px] font-black uppercase text-[#E2136E] tracking-wider">bKash Personal</span>
                    <span className="text-xs text-slate-400 font-semibold">{tournament.entryFee} BDT</span>
                  </div>
                  <div className="flex items-center justify-between bg-slate-950/80 p-3 rounded-2xl border border-slate-800">
                    <span className="font-mono text-sm sm:text-base font-black text-white">{tournament.bkashNumber}</span>
                    <button
                      onClick={() => handleCopy(tournament.bkashNumber, 'bkash')}
                      className="px-2.5 py-1 rounded-xl bg-[#E2136E]/10 text-[#E2136E] border border-[#E2136E]/30 hover:bg-[#E2136E]/20 transition-all flex items-center gap-1 text-xs font-bold"
                    >
                      {copiedField === 'bkash' ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
                      {copiedField === 'bkash' ? 'Copied' : 'Copy'}
                    </button>
                  </div>
                </div>

                {/* Nagad Card */}
                <div className="rounded-[24px] bg-slate-900/40 border border-[#F7941D]/30 p-5 relative overflow-hidden backdrop-blur-md">
                  <div className="flex items-center justify-between mb-3">
                    <span className="text-[11px] font-black uppercase text-[#F7941D] tracking-wider">Nagad Personal</span>
                    <span className="text-xs text-slate-400 font-semibold">{tournament.entryFee} BDT</span>
                  </div>
                  <div className="flex items-center justify-between bg-slate-950/80 p-3 rounded-2xl border border-slate-800">
                    <span className="font-mono text-sm sm:text-base font-black text-white">{tournament.nagadNumber}</span>
                    <button
                      onClick={() => handleCopy(tournament.nagadNumber, 'nagad')}
                      className="px-2.5 py-1 rounded-xl bg-[#F7941D]/10 text-[#F7941D] border border-[#F7941D]/30 hover:bg-[#F7941D]/20 transition-all flex items-center gap-1 text-xs font-bold"
                    >
                      {copiedField === 'nagad' ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
                      {copiedField === 'nagad' ? 'Copied' : 'Copy'}
                    </button>
                  </div>
                </div>
              </div>
            </div>

            {/* DEVELOPER INFO CARD */}
            <div className="rounded-[28px] bg-gradient-to-br from-slate-900/90 via-slate-900/60 to-slate-950 border border-cyan-500/30 p-6 shadow-2xl relative overflow-hidden backdrop-blur-md">
              <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                <div className="flex items-center gap-4">
                  <div className="w-14 h-14 rounded-2xl bg-cyan-500/10 border border-cyan-500/40 flex items-center justify-center text-cyan-400 shadow-[0_0_20px_rgba(6,182,212,0.25)] shrink-0">
                    <Code2 className="w-7 h-7" />
                  </div>
                  <div>
                    <div className="flex items-center gap-2 mb-0.5">
                      <span className="text-[10px] font-black uppercase tracking-widest text-cyan-400 bg-cyan-950/80 px-2.5 py-0.5 rounded-full border border-cyan-500/30">
                        Lead Developer & Host
                      </span>
                    </div>
                    <h3 className="font-esports text-lg font-black italic tracking-tight text-white uppercase">
                      JADID MOLLIK
                    </h3>
                    <p className="text-xs text-slate-400">
                      Tournament Architect & Full-Stack Platform Engineer
                    </p>
                  </div>
                </div>

                <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-2.5 w-full sm:w-auto">
                  <a
                    href="https://wa.me/8801980000601"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="px-4 py-3 rounded-2xl bg-emerald-500 hover:bg-emerald-400 active:bg-emerald-600 text-slate-950 font-black text-xs uppercase tracking-wider flex items-center justify-center gap-2 shadow-[0_0_20px_rgba(16,185,129,0.35)] transition-all"
                  >
                    <MessageCircle className="w-4 h-4 fill-slate-950" />
                    <span>WhatsApp: 01980000601</span>
                  </a>
                  <button
                    type="button"
                    onClick={() => setIsRulesModalOpen(true)}
                    className="px-4 py-3 rounded-2xl bg-slate-900 hover:bg-slate-800 border border-cyan-500/30 text-cyan-300 font-bold text-xs uppercase tracking-wider flex items-center justify-center gap-2 transition-all"
                  >
                    <BookOpen className="w-4 h-4" />
                    <span>Rules Modal</span>
                  </button>
                </div>
              </div>
            </div>
          </div>

          {/* Payment Submission Form or Sign-In Prompt */}
          <div className="space-y-6">
            {!currentUser ? (
              <div className="rounded-[32px] bg-slate-900/60 border border-cyan-500/30 p-6 sm:p-7 shadow-2xl backdrop-blur-md text-center space-y-4">
                <div className="w-14 h-14 rounded-2xl bg-cyan-500/10 border border-cyan-500/30 flex items-center justify-center mx-auto text-cyan-400">
                  <UserCheck className="w-7 h-7" />
                </div>
                <h3 className="font-esports text-lg font-black italic tracking-tighter text-white uppercase">
                  Authentication Required
                </h3>
                <p className="text-xs text-slate-400">
                  Sign in with your Google account to register for the tournament and synchronize your matches.
                </p>
                <button
                  type="button"
                  onClick={onGoogleSignIn}
                  className="w-full py-3.5 px-4 rounded-2xl bg-white hover:bg-slate-100 active:bg-slate-200 text-slate-900 font-bold text-sm transition-all flex items-center justify-center gap-3 shadow-lg"
                >
                  <svg className="w-5 h-5" viewBox="0 0 24 24">
                    <path
                      fill="#4285F4"
                      d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                    />
                    <path
                      fill="#34A853"
                      d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                    />
                    <path
                      fill="#FBBC05"
                      d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
                    />
                    <path
                      fill="#EA4335"
                      d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
                    />
                  </svg>
                  <span>Sign in with Google</span>
                </button>
              </div>
            ) : (
              <div className="rounded-[32px] bg-slate-900/60 border border-cyan-500/20 p-6 sm:p-7 shadow-2xl backdrop-blur-md">
                <h3 className="font-esports text-lg font-black italic tracking-tighter text-white uppercase mb-1">
                  Submit Payment TrxID
                </h3>
                <p className="text-xs text-slate-400 mb-6">
                  Send {tournament.entryFee} BDT to either bKash or Nagad number, then submit your transaction ID below.
                </p>

                <form onSubmit={handlePaymentSubmit} className="space-y-4">
                  {/* Method selector */}
                  <div>
                    <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-2">
                      Payment Gateway
                    </label>
                    <div className="grid grid-cols-2 gap-3">
                      <button
                        type="button"
                        onClick={() => setPaymentMethod('bKash')}
                        className={`py-3 rounded-2xl font-bold text-xs transition-all border ${
                          paymentMethod === 'bKash'
                            ? 'bg-[#E2136E]/20 text-white border-[#E2136E] shadow-[0_0_12px_rgba(226,19,110,0.3)]'
                            : 'bg-slate-950/80 text-slate-400 border-slate-800 hover:border-slate-700'
                        }`}
                      >
                        bKash
                      </button>
                      <button
                        type="button"
                        onClick={() => setPaymentMethod('Nagad')}
                        className={`py-3 rounded-2xl font-bold text-xs transition-all border ${
                          paymentMethod === 'Nagad'
                            ? 'bg-[#F7941D]/20 text-white border-[#F7941D] shadow-[0_0_12px_rgba(247,148,29,0.3)]'
                            : 'bg-slate-950/80 text-slate-400 border-slate-800 hover:border-slate-700'
                        }`}
                      >
                        Nagad
                      </button>
                    </div>
                  </div>

                  {/* TrxID Input */}
                  <div>
                    <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-2">
                      Transaction ID (TrxID)
                    </label>
                    <input
                      type="text"
                      required
                      placeholder="e.g. 9J28A190KZ"
                      value={trxId}
                      onChange={e => setTrxId(e.target.value.toUpperCase())}
                      className="w-full bg-slate-950/90 border border-slate-800 rounded-2xl px-4 py-3 text-sm text-white font-mono placeholder:text-slate-600 focus:outline-none focus:border-cyan-500 transition-colors uppercase"
                    />
                  </div>

                  {/* Readonly info review */}
                  <div className="p-3.5 bg-slate-950/60 rounded-2xl border border-slate-800/80 text-[11px] text-slate-400 space-y-1">
                    <p><strong className="text-slate-300">Player:</strong> {currentUser.fullName}</p>
                    <p><strong className="text-slate-300">Firebase UID:</strong> <span className="font-mono text-cyan-400">{currentUser.uid}</span></p>
                    <p><strong className="text-slate-300">eFootball ID:</strong> {currentUser.inGameId} (@{currentUser.inGameUsername || 'Player'})</p>
                  </div>

                  <button
                    type="submit"
                    disabled={isSubmitting || !tournament.isRegistrationOpen || registration?.status === 'joined'}
                    className="w-full py-4 rounded-2xl bg-cyan-500 hover:bg-cyan-400 active:bg-cyan-600 text-slate-950 font-black text-sm uppercase tracking-wider shadow-[0_8px_24px_rgba(6,182,212,0.3)] transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                  >
                    <Send className="w-4 h-4" />
                    {isSubmitting ? 'Submitting...' : `Submit Verification (${tournament.entryFee} BDT)`}
                  </button>
                </form>
              </div>
            )}
          </div>
        </div>
      )}

      {/* TAB 2: My Match Hub */}
      {activeTab === 'match' && (
        <div className="max-w-3xl mx-auto space-y-6">
          {!currentUser ? (
            <div className="text-center py-16 glass-panel rounded-[32px] p-8 space-y-4">
              <UserCheck className="w-12 h-12 text-cyan-400 mx-auto" />
              <h2 className="text-xl font-bold text-white">Sign In Required</h2>
              <p className="text-sm text-slate-400 max-w-md mx-auto">
                Please sign in with Google to access your personalized match hub.
              </p>
              <button
                onClick={onGoogleSignIn}
                className="px-6 py-3 rounded-2xl bg-white hover:bg-slate-100 text-slate-950 font-bold text-sm inline-flex items-center gap-2 shadow-lg"
              >
                Sign in with Google
              </button>
            </div>
          ) : registration?.status !== 'joined' ? (
            <div className="text-center py-16 glass-panel rounded-[32px] p-8">
              <Hourglass className="w-12 h-12 text-amber-400 mx-auto mb-4" />
              <h2 className="text-xl font-bold text-white mb-2">Registration Required</h2>
              <p className="text-sm text-slate-400 max-w-md mx-auto">
                You must have an approved registration before your 1v1 match assignment will appear.
              </p>
            </div>
          ) : !userMatch ? (
            <div className="text-center py-16 glass-panel rounded-[32px] p-8">
              <Gamepad2 className="w-12 h-12 text-cyan-400 mx-auto mb-4" />
              <h2 className="text-xl font-bold text-white mb-2">Waiting For Bracket Seed</h2>
              <p className="text-sm text-slate-400 max-w-md mx-auto">
                Your payment is verified! As soon as the host generates the tournament draw, your match and opponent details will be displayed here.
              </p>
            </div>
          ) : (
            <div className="rounded-[32px] bg-slate-900/60 border border-cyan-500/20 p-6 sm:p-8 shadow-2xl backdrop-blur-md relative overflow-hidden space-y-6">
              <div className="absolute top-0 right-0 p-4 opacity-5 pointer-events-none text-cyan-400">
                <Gamepad2 className="w-40 h-40" />
              </div>

              <div className="flex justify-between items-start mb-4 relative z-10">
                <div>
                  <p className="text-[10px] font-bold uppercase tracking-wider text-slate-400 mb-1">Upcoming Match</p>
                  <p className="text-lg font-bold text-white">Round {userMatch.round}</p>
                </div>
                <div className="bg-cyan-500/10 border border-cyan-500/30 px-3 py-1 rounded-full">
                  <p className="text-[10px] font-bold text-cyan-400">{userMatch.startTime}</p>
                </div>
              </div>

              {/* Head to Head 1v1 */}
              <div className="flex items-center justify-between py-2 relative z-10">
                {/* Me */}
                <div className="flex flex-col items-center flex-1 text-center">
                  <div className="w-16 h-16 rounded-3xl bg-slate-800 border-2 border-cyan-500/40 mb-2 flex items-center justify-center overflow-hidden shadow-[0_0_20px_rgba(6,182,212,0.1)]">
                    {currentUser.photoURL ? (
                      <img src={currentUser.photoURL} alt={currentUser.fullName} className="w-full h-full object-cover" />
                    ) : (
                      <div className="w-full h-full bg-gradient-to-br from-cyan-500/20 to-transparent flex items-center justify-center font-black text-2xl text-cyan-100">
                        {currentUser.fullName ? currentUser.fullName.slice(0, 2).toUpperCase() : 'TA'}
                      </div>
                    )}
                  </div>
                  <p className="text-sm font-bold truncate w-28 text-white">
                    {userMatch.player1?.id === currentUser.uid ? userMatch.player1?.name : userMatch.player2?.name} (You)
                  </p>
                  <p className="text-[10px] text-cyan-400/80 font-mono">
                    #{userMatch.player1?.id === currentUser.uid ? userMatch.player1?.inGameId : userMatch.player2?.inGameId}
                  </p>
                </div>

                {/* VS Divider or Live Scores */}
                <div className="flex flex-col items-center px-4">
                  {userMatch.status !== 'scheduled' ? (
                    <div className="text-3xl font-black text-white font-mono">
                      {userMatch.player1Score} - {userMatch.player2Score}
                    </div>
                  ) : (
                    <>
                      <span className="text-2xl font-black italic text-slate-600 mb-1">VS</span>
                      <div className="h-8 w-[1px] bg-cyan-500/20"></div>
                    </>
                  )}
                </div>

                {/* Opponent */}
                <div className="flex flex-col items-center flex-1 text-center">
                  <div className="w-16 h-16 rounded-3xl bg-slate-800 border-2 border-slate-700 mb-2 flex items-center justify-center overflow-hidden">
                    <div className="w-full h-full bg-gradient-to-br from-slate-700/20 to-transparent flex items-center justify-center font-black text-2xl text-slate-400">
                      {userMatch.player1?.id === currentUser.uid
                        ? (userMatch.player2?.name ? userMatch.player2.name.slice(0, 2).toUpperCase() : 'BY')
                        : (userMatch.player1?.name ? userMatch.player1.name.slice(0, 2).toUpperCase() : 'BY')}
                    </div>
                  </div>
                  <p className="text-sm font-bold truncate w-28 text-slate-300">
                    {userMatch.player1?.id === currentUser.uid
                      ? userMatch.player2?.name || (userMatch.isBye ? 'BYE (Advance)' : 'TBD')
                      : userMatch.player1?.name || (userMatch.isBye ? 'BYE (Advance)' : 'TBD')}
                  </p>
                  <p className="text-[10px] text-slate-500 font-mono">
                    #{userMatch.player1?.id === currentUser.uid
                      ? (userMatch.player2?.inGameId || '000-000-000')
                      : (userMatch.player1?.inGameId || '000-000-000')}
                  </p>
                </div>
              </div>

              {/* Match Instructions Card */}
              <div className="bg-slate-900/40 border border-slate-800/80 rounded-[24px] p-5 text-xs text-slate-400 space-y-2 relative z-10">
                <p className="font-bold text-white flex items-center gap-1.5">
                  <Gamepad2 className="w-4 h-4 text-cyan-400" />
                  eFootball Room Matchmaking Guide
                </p>
                <p>1. Open eFootball Mobile / Console → Select Friend Match Room.</p>
                <p>2. Connect with your opponent using their in-game ID:</p>
                {userMatch.player1?.id === currentUser.uid && userMatch.player2 ? (
                  <div className="p-2.5 bg-slate-950/80 rounded-xl border border-cyan-500/30 text-cyan-400 font-mono font-bold text-sm">
                    Opponent ID: {userMatch.player2.inGameId} (@{userMatch.player2.inGameUsername})
                  </div>
                ) : userMatch.player2?.id === currentUser.uid && userMatch.player1 ? (
                  <div className="p-2.5 bg-slate-950/80 rounded-xl border border-cyan-500/30 text-cyan-400 font-mono font-bold text-sm">
                    Opponent ID: {userMatch.player1.inGameId} (@{userMatch.player1.inGameUsername})
                  </div>
                ) : (
                  <p className="text-slate-500 italic">Opponent will be determined by winner of prior match.</p>
                )}
                <p>3. Play your match and results will be reviewed & updated in live bracket.</p>
              </div>
            </div>
          )}
        </div>
      )}

      {/* TAB 3: Bracket Viewer */}
      {activeTab === 'bracket' && (
        <BracketViewer
          tournament={tournament}
          matches={matches}
          currentUserId={currentUser?.uid || ''}
        />
      )}

      {/* TAB 4: Profile Editor */}
      {activeTab === 'profile' && (
        <div className="max-w-xl mx-auto rounded-[32px] bg-slate-900/60 border border-slate-800/80 p-6 sm:p-8 space-y-6 shadow-2xl backdrop-blur-md">
          <div>
            <p className="text-[10px] font-bold text-cyan-500 uppercase tracking-widest leading-none mb-1">
              Settings
            </p>
            <h3 className="font-esports text-lg font-black italic tracking-tighter text-white uppercase">
              Player Profile & Firebase Credentials
            </h3>
          </div>

          {!currentUser ? (
            <div className="p-6 rounded-2xl bg-slate-950/80 border border-slate-800 text-center space-y-4">
              <User className="w-10 h-10 text-cyan-400 mx-auto" />
              <p className="text-sm text-slate-300 font-bold">You are currently logged out</p>
              <p className="text-xs text-slate-500">Sign in with Google to manage your player profile and sync with Firestore.</p>
              <button
                type="button"
                onClick={onGoogleSignIn}
                className="w-full py-3 px-4 rounded-2xl bg-white hover:bg-slate-100 text-slate-900 font-bold text-sm flex items-center justify-center gap-2 shadow-lg"
              >
                Sign in with Google
              </button>
            </div>
          ) : (
            <>
              {/* Account Card */}
              <div className="p-4 rounded-2xl bg-slate-950/80 border border-slate-800 flex items-center gap-4">
                {currentUser.photoURL ? (
                  <img
                    src={currentUser.photoURL}
                    alt={currentUser.fullName}
                    className="w-14 h-14 rounded-full object-cover border-2 border-cyan-500/50 shadow-[0_0_15px_rgba(6,182,212,0.2)]"
                  />
                ) : (
                  <div className="w-14 h-14 rounded-full bg-slate-900 border-2 border-cyan-500/40 flex items-center justify-center font-black text-lg text-cyan-400">
                    {currentUser.fullName ? currentUser.fullName.slice(0, 2).toUpperCase() : 'TA'}
                  </div>
                )}
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <h4 className="text-sm font-bold text-white truncate">{currentUser.fullName}</h4>
                    <span className="text-[9px] uppercase px-2 py-0.5 rounded bg-cyan-950 text-cyan-400 border border-cyan-500/30">
                      {currentUser.role}
                    </span>
                  </div>
                  {currentUser.email && (
                    <p className="text-xs text-slate-400 truncate flex items-center gap-1 mt-0.5">
                      <Mail className="w-3 h-3 text-slate-500" />
                      {currentUser.email}
                    </p>
                  )}
                  <p className="text-[10px] text-cyan-400 font-mono truncate mt-1">
                    Firebase UID: {currentUser.uid}
                  </p>
                </div>
              </div>

              {profileSuccessMsg && (
                <div className="p-3.5 rounded-2xl bg-emerald-950/40 border border-emerald-500/40 text-xs text-emerald-300 font-bold flex items-center gap-2">
                  <Check className="w-4 h-4" /> Profile & Firestore document updated successfully!
                </div>
              )}

              <form onSubmit={handleProfileSave} className="space-y-4">
                <div>
                  <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-2">Full Name</label>
                  <input
                    type="text"
                    required
                    value={fullName}
                    onChange={e => setFullName(e.target.value)}
                    className="w-full bg-slate-950/80 border border-slate-800 rounded-2xl px-4 py-3 text-sm text-white focus:outline-none focus:border-cyan-500 transition-colors"
                  />
                </div>
                <div>
                  <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-2">Phone Number (+880)</label>
                  <input
                    type="text"
                    required
                    value={phoneNumber}
                    onChange={e => setPhoneNumber(e.target.value)}
                    className="w-full bg-slate-950/80 border border-slate-800 rounded-2xl px-4 py-3 text-sm text-white focus:outline-none focus:border-cyan-500 transition-colors"
                  />
                </div>
                <div>
                  <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-2">eFootball In-Game ID</label>
                  <input
                    type="text"
                    required
                    value={inGameId}
                    onChange={e => setInGameId(e.target.value)}
                    className="w-full bg-slate-950/80 border border-slate-800 rounded-2xl px-4 py-3 text-sm text-white font-mono focus:outline-none focus:border-cyan-500 transition-colors"
                  />
                </div>
                <div>
                  <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-2">eFootball Gamertag / Username</label>
                  <input
                    type="text"
                    required
                    value={inGameUsername}
                    onChange={e => setInGameUsername(e.target.value)}
                    className="w-full bg-slate-950/80 border border-slate-800 rounded-2xl px-4 py-3 text-sm text-white focus:outline-none focus:border-cyan-500 transition-colors"
                  />
                </div>

                <button
                  type="submit"
                  disabled={isUpdatingProfile}
                  className="w-full py-4 rounded-2xl bg-cyan-500 hover:bg-cyan-400 active:bg-cyan-600 text-slate-950 font-black text-sm uppercase tracking-wider shadow-[0_8px_24px_rgba(6,182,212,0.3)] transition-all"
                >
                  {isUpdatingProfile ? 'Saving to Firestore...' : 'Save Profile Changes'}
                </button>
              </form>
            </>
          )}
        </div>
      )}

      {/* Expandable Rules Modal */}
      <RulesModal
        isOpen={isRulesModalOpen}
        onClose={() => setIsRulesModalOpen(false)}
        tournamentTitle={tournament.title}
      />
    </div>
  );
};
