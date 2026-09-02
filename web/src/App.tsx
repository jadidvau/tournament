import React, { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import { Navbar } from './components/Navbar';
import { PlayerApp } from './pages/PlayerApp';
import { AdminApp } from './pages/AdminApp';
import { Login } from './pages/Login';
import {
  MatchStatus,
  PaymentMethod,
  TournamentInfo,
  TournamentMatch,
  TournamentRegistration,
  UserProfile,
  UserRole
} from './types/tournament';
import { generateBracket, advanceMatchWinner } from './utils/bracketEngine';

export function App() {
  // Initial Unauthenticated State (User must sign in)
  const [currentUser, setCurrentUser] = useState<UserProfile | null>(null);

  const [tournament, setTournament] = useState<TournamentInfo>({
    id: 'dhaka_championship_2026',
    title: 'Dhaka eFootball Open Championship 2026',
    entryFee: 100,
    bkashNumber: '01904031478',
    nagadNumber: '01904031478',
    isRegistrationOpen: true,
    matchDurationMinutes: 10,
    totalRounds: 3,
    champion: null,
    championUsername: null,
    createdAt: Date.now()
  });

  const [registrations, setRegistrations] = useState<TournamentRegistration[]>([
    {
      id: 'reg_1',
      userId: 'user_player_tanvir',
      fullName: 'Tanvir Hossain',
      phoneNumber: '+8801904031478',
      inGameId: '549-218-093',
      inGameUsername: 'TanvirDhaka99',
      paymentMethod: 'bKash',
      trxId: '9J28A190KZ',
      status: 'joined',
      feeAmount: 100,
      submittedAt: Date.now() - 3600000 * 4
    },
    {
      id: 'reg_2',
      userId: 'user_rakib',
      fullName: 'Rakibul Islam',
      phoneNumber: '+8801711223344',
      inGameId: '109-445-882',
      inGameUsername: 'RakibStriker',
      paymentMethod: 'Nagad',
      trxId: 'NGD88231AA',
      status: 'joined',
      feeAmount: 100,
      submittedAt: Date.now() - 3600000 * 3
    },
    {
      id: 'reg_3',
      userId: 'user_shakil',
      fullName: 'Shakil Ahmed',
      phoneNumber: '+8801822334455',
      inGameId: '774-129-402',
      inGameUsername: 'ShakilPES_BD',
      paymentMethod: 'bKash',
      trxId: 'BK778102ZZ',
      status: 'joined',
      feeAmount: 100,
      submittedAt: Date.now() - 3600000 * 2
    },
    {
      id: 'reg_4',
      userId: 'user_fahim',
      fullName: 'Fahim Chowdhury',
      phoneNumber: '+8801633445566',
      inGameId: '882-901-314',
      inGameUsername: 'FahimMaster9',
      paymentMethod: 'bKash',
      trxId: 'BK994821PQ',
      status: 'joined',
      feeAmount: 100,
      submittedAt: Date.now() - 3600000 * 1
    },
    {
      id: 'reg_5',
      userId: 'user_mehedi',
      fullName: 'Mehedi Hasan',
      phoneNumber: '+8801544556677',
      inGameId: '331-884-902',
      inGameUsername: 'MehediCaptain',
      paymentMethod: 'Nagad',
      trxId: 'NGD55120KK',
      status: 'pending',
      feeAmount: 100,
      submittedAt: Date.now() - 1800000
    }
  ]);

  const [matches, setMatches] = useState<TournamentMatch[]>([]);

  // Seed initial bracket with pre-approved players
  useEffect(() => {
    const approved = registrations.filter(r => r.status === 'joined');
    const { matches: initialMatches, updatedTournament } = generateBracket(approved, tournament);
    setMatches(initialMatches);
    setTournament(updatedTournament);
  }, []);

  const userRegistration = registrations.find(r => r.userId === currentUser?.uid) || null;

  const userMatch = matches.find(
    m =>
      currentUser &&
      (m.player1?.id === currentUser.uid || m.player2?.id === currentUser.uid) &&
      m.status !== 'completed'
  ) || null;

  // Handlers
  const handleLogin = (identifier: string, role: UserRole) => {
    setCurrentUser({
      uid: `user_${identifier.replace(/[^a-zA-Z0-9]/g, '_')}`,
      fullName: role === 'admin' ? 'Host Administrator' : 'Dhaka Contender',
      phoneNumber: identifier.includes('@') ? '+8801904031478' : identifier,
      inGameId: '549-218-093',
      inGameUsername: role === 'admin' ? 'DhakaHostAdmin' : 'DhakaPlayer1',
      role: role,
      createdAt: Date.now()
    });
  };

  const handleLogout = () => {
    setCurrentUser(null);
  };

  const handleGoogleSignIn = () => {
    const randomSuffix = Math.random().toString(36).substring(2, 8);
    const googleUser: UserProfile = {
      uid: `usr_ggl_${randomSuffix}`,
      fullName: 'Jadid Nogorigang',
      email: 'nogorigangjadid@gmail.com',
      phoneNumber: '+8801980000601',
      inGameId: '772-990-123',
      inGameUsername: 'Nogorigang_Jadid',
      role: 'player',
      createdAt: Date.now()
    };
    setCurrentUser(googleUser);
  };

  const handleSubmitPayment = async (method: PaymentMethod, trxId: string) => {
    if (!currentUser) return;
    const newReg: TournamentRegistration = {
      id: `reg_${Date.now()}`,
      userId: currentUser.uid,
      fullName: currentUser.fullName,
      phoneNumber: currentUser.phoneNumber,
      inGameId: currentUser.inGameId,
      inGameUsername: currentUser.inGameUsername,
      paymentMethod: method,
      trxId: trxId,
      status: 'pending',
      feeAmount: tournament.entryFee,
      submittedAt: Date.now()
    };
    setRegistrations(prev => [newReg, ...prev.filter(r => r.userId !== currentUser.uid)]);
  };

  const handleUpdateProfile = async (
    name: string,
    phone: string,
    igId: string,
    igUsername: string
  ) => {
    if (!currentUser) return;
    setCurrentUser({
      ...currentUser,
      fullName: name,
      phoneNumber: phone,
      inGameId: igId,
      inGameUsername: igUsername
    });
  };

  const handleApproveRegistration = async (id: string) => {
    setRegistrations(prev =>
      prev.map(r => (r.id === id ? { ...r, status: 'joined' as const, reviewedAt: Date.now() } : r))
    );
  };

  const handleRejectRegistration = async (id: string, reason: string) => {
    setRegistrations(prev =>
      prev.map(r =>
        r.id === id
          ? { ...r, status: 'rejected' as const, rejectionReason: reason, reviewedAt: Date.now() }
          : r
      )
    );
  };

  const handleGenerateBracket = async () => {
    const approved = registrations.filter(r => r.status === 'joined');
    const { matches: newMatches, updatedTournament } = generateBracket(approved, tournament);
    setMatches(newMatches);
    setTournament(updatedTournament);
  };

  const handleUpdateMatchScore = async (
    matchId: string,
    p1Score: number,
    p2Score: number,
    status: MatchStatus
  ) => {
    const { matches: updatedMatches, updatedTournament } = advanceMatchWinner(
      matches,
      tournament,
      matchId,
      p1Score,
      p2Score,
      status
    );
    setMatches(updatedMatches);
    setTournament(updatedTournament);
  };

  const handleUpdateSettings = async (
    title: string,
    fee: number,
    bkash: string,
    nagad: string,
    isOpen: boolean,
    duration: number
  ) => {
    setTournament(prev => ({
      ...prev,
      title,
      entryFee: fee,
      bkashNumber: bkash,
      nagadNumber: nagad,
      isRegistrationOpen: isOpen,
      matchDurationMinutes: duration
    }));
  };

  return (
    <BrowserRouter>
      <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col">
        <Navbar
          currentUser={currentUser}
          onLogout={handleLogout}
          onGoogleSignIn={handleGoogleSignIn}
        />

        <main className="flex-1">
          <Routes>
            <Route
              path="/player"
              element={
                <PlayerApp
                  currentUser={currentUser}
                  tournament={tournament}
                  registration={userRegistration}
                  userMatch={userMatch}
                  matches={matches}
                  onSubmitPayment={handleSubmitPayment}
                  onUpdateProfile={handleUpdateProfile}
                  onGoogleSignIn={handleGoogleSignIn}
                  onLogout={handleLogout}
                />
              }
            />

            <Route
              path="/admin"
              element={
                currentUser ? (
                  <AdminApp
                    currentUser={currentUser}
                    tournament={tournament}
                    registrations={registrations}
                    matches={matches}
                    onApproveRegistration={handleApproveRegistration}
                    onRejectRegistration={handleRejectRegistration}
                    onGenerateBracket={handleGenerateBracket}
                    onUpdateMatchScore={handleUpdateMatchScore}
                    onUpdateSettings={handleUpdateSettings}
                    onElevateToAdmin={() =>
                      setCurrentUser(prev => (prev ? { ...prev, role: 'admin' } : null))
                    }
                  />
                ) : (
                  <Login onLogin={handleLogin} onGoogleSignIn={handleGoogleSignIn} />
                )
              }
            />

            <Route path="*" element={<Navigate to="/player" replace />} />
          </Routes>
        </main>

        {/* Global Footer */}
        <footer className="border-t border-slate-900 bg-slate-950/90 py-6 px-4 sm:px-6">
          <div className="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-slate-400">
            <div className="flex items-center gap-2">
              <span className="font-esports font-black text-cyan-400 text-sm tracking-wider">
                eFOOTBALL BD
              </span>
              <span className="text-slate-600">|</span>
              <span>Official Tournament Platform</span>
            </div>

            <div className="flex flex-wrap items-center gap-4 text-center sm:text-right">
              <span>
                Developer: <strong className="text-white font-bold">JADID MOLLIK</strong>
              </span>
              <a
                href="https://wa.me/8801980000601"
                target="_blank"
                rel="noopener noreferrer"
                className="text-emerald-400 hover:text-emerald-300 font-bold transition-colors inline-flex items-center gap-1.5"
              >
                <span>WhatsApp: 01980000601</span>
              </a>
            </div>
          </div>
        </footer>
      </div>
    </BrowserRouter>
  );
}

export default App;
