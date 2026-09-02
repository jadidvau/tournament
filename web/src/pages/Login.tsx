import React, { useState } from 'react';
import { Trophy, Mail, Phone, Lock, ArrowRight, ShieldCheck, User } from 'lucide-react';
import { UserRole } from '../types/tournament';

interface LoginProps {
  onLogin: (emailOrPhone: string, role: UserRole) => void;
  onGoogleSignIn: () => void;
}

export const Login: React.FC<LoginProps> = ({ onLogin, onGoogleSignIn }) => {
  const [isPhoneAuth, setIsPhoneAuth] = useState(false);
  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<UserRole>('player');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onLogin(identifier || (isPhoneAuth ? '+8801904031478' : 'player@dhaka-efootball.com'), role);
  };

  return (
    <div className="min-h-[85vh] flex items-center justify-center px-4 py-12">
      <div className="max-w-md w-full bg-slate-900/60 rounded-[32px] p-8 border border-cyan-500/20 shadow-2xl backdrop-blur-md space-y-6">
        {/* Header */}
        <div className="text-center space-y-2">
          <div className="w-16 h-16 rounded-3xl bg-slate-900 border border-cyan-500/40 flex items-center justify-center mx-auto shadow-[0_0_20px_rgba(6,182,212,0.2)]">
            <Trophy className="w-8 h-8 text-cyan-400" />
          </div>
          <p className="text-[11px] font-bold text-cyan-500 uppercase tracking-widest leading-none mb-1">
            Season 04
          </p>
          <h2 className="font-esports text-2xl font-black italic tracking-tight text-white uppercase">
            Dhaka <span className="text-cyan-400">eFootball</span> Open
          </h2>
          <p className="text-xs text-slate-400">
            Sign in with Google or credentials to access tournament hub
          </p>
        </div>

        {/* Google One-Click Sign-In */}
        <button
          type="button"
          onClick={onGoogleSignIn}
          className="w-full py-3.5 px-4 rounded-2xl bg-white hover:bg-slate-100 active:bg-slate-200 text-slate-900 font-bold text-sm transition-all flex items-center justify-center gap-3 shadow-lg hover:shadow-cyan-500/20"
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
          <span>Continue with Google</span>
        </button>

        <div className="flex items-center gap-3">
          <div className="flex-1 h-px bg-slate-800"></div>
          <span className="text-[10px] uppercase font-bold text-slate-500 tracking-wider">or sign in with ID</span>
          <div className="flex-1 h-px bg-slate-800"></div>
        </div>

        {/* Role Toggle Selector */}
        <div>
          <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-2">
            Select Portal Role
          </label>
          <div className="grid grid-cols-2 gap-3">
            <button
              type="button"
              onClick={() => setRole('player')}
              className={`py-3 rounded-2xl text-xs font-bold transition-all flex items-center justify-center gap-2 border ${
                role === 'player'
                  ? 'bg-cyan-500/10 text-cyan-400 border-cyan-500/40 font-black shadow-[0_0_15px_rgba(6,182,212,0.2)]'
                  : 'bg-slate-950/80 text-slate-400 border-slate-800 hover:border-slate-700'
              }`}
            >
              <User className="w-4 h-4" />
              Player App
            </button>
            <button
              type="button"
              onClick={() => setRole('admin')}
              className={`py-3 rounded-2xl text-xs font-bold transition-all flex items-center justify-center gap-2 border ${
                role === 'admin'
                  ? 'bg-purple-500/10 text-purple-300 border-purple-500/40 font-black shadow-[0_0_15px_rgba(168,85,247,0.2)]'
                  : 'bg-slate-950/80 text-slate-400 border-slate-800 hover:border-slate-700'
              }`}
            >
              <ShieldCheck className="w-4 h-4" />
              Admin Host
            </button>
          </div>
        </div>

        {/* Auth Method Switch */}
        <div className="flex border-b border-slate-800/80 text-xs font-bold">
          <button
            type="button"
            onClick={() => setIsPhoneAuth(false)}
            className={`pb-2.5 flex-1 text-center transition-all ${
              !isPhoneAuth ? 'text-cyan-400 border-b-2 border-cyan-500' : 'text-slate-500'
            }`}
          >
            Email & Password
          </button>
          <button
            type="button"
            onClick={() => setIsPhoneAuth(true)}
            className={`pb-2.5 flex-1 text-center transition-all ${
              isPhoneAuth ? 'text-cyan-400 border-b-2 border-cyan-500' : 'text-slate-500'
            }`}
          >
            Phone Number (+880)
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-2">
              {isPhoneAuth ? 'Bangladesh Phone Number' : 'Email Address'}
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500">
                {isPhoneAuth ? <Phone className="w-4 h-4" /> : <Mail className="w-4 h-4" />}
              </div>
              <input
                type={isPhoneAuth ? 'tel' : 'email'}
                required
                placeholder={isPhoneAuth ? '+8801904031478' : 'player@dhaka-efootball.com'}
                value={identifier}
                onChange={e => setIdentifier(e.target.value)}
                className="w-full bg-slate-950/80 border border-slate-800 rounded-2xl pl-10 pr-4 py-3 text-sm text-white placeholder:text-slate-600 focus:outline-none focus:border-cyan-500 transition-colors"
              />
            </div>
          </div>

          <div>
            <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-2">Password</label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500">
                <Lock className="w-4 h-4" />
              </div>
              <input
                type="password"
                required
                placeholder="••••••••"
                value={password}
                onChange={e => setPassword(e.target.value)}
                className="w-full bg-slate-950/80 border border-slate-800 rounded-2xl pl-10 pr-4 py-3 text-sm text-white placeholder:text-slate-600 focus:outline-none focus:border-cyan-500 transition-colors"
              />
            </div>
          </div>

          <button
            type="submit"
            className="w-full py-4 rounded-2xl bg-cyan-500 hover:bg-cyan-400 active:bg-cyan-600 text-slate-950 font-black text-sm uppercase tracking-wider shadow-[0_8px_24px_rgba(6,182,212,0.3)] transition-all flex items-center justify-center gap-2"
          >
            Enter {role === 'admin' ? 'Admin App' : 'Player App'}
            <ArrowRight className="w-4 h-4" />
          </button>
        </form>
      </div>
    </div>
  );
};
