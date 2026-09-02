import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Trophy, Shield, User, LogOut } from 'lucide-react';
import { UserProfile } from '../types/tournament';

interface NavbarProps {
  currentUser: UserProfile | null;
  onLogout: () => void;
  onGoogleSignIn: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({ currentUser, onLogout, onGoogleSignIn }) => {
  const location = useLocation();
  const isPlayerApp = location.pathname.startsWith('/player');
  const isAdminApp = location.pathname.startsWith('/admin');

  return (
    <header className="sticky top-0 z-50 bg-slate-950/80 backdrop-blur-xl border-b border-slate-800/50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Brand Logo & Title */}
        <Link to="/player" className="flex items-center space-x-3 group">
          <div className="w-10 h-10 rounded-2xl bg-slate-900 border border-cyan-500/30 flex items-center justify-center group-hover:border-cyan-400 shadow-[0_0_15px_rgba(6,182,212,0.15)] transition-all">
            <Trophy className="w-5 h-5 text-cyan-400" />
          </div>
          <div className="flex flex-col">
            <p className="text-[10px] sm:text-[11px] font-bold text-cyan-500 uppercase tracking-widest leading-none mb-0.5">
              Season 04
            </p>
            <h1 className="text-sm sm:text-base font-black italic tracking-tighter text-white uppercase flex items-center gap-1">
              Dhaka <span className="text-cyan-400">eFootball</span> Open
            </h1>
          </div>
        </Link>

        {/* Dual Portal Switcher & User Profile */}
        <div className="flex items-center space-x-3">
          {/* Public Player App Route */}
          <Link
            to="/player"
            className={`px-3.5 py-1.5 rounded-full text-xs font-bold transition-all flex items-center gap-1.5 ${
              isPlayerApp
                ? 'bg-cyan-500/10 border border-cyan-500/30 text-cyan-400 shadow-[0_0_12px_rgba(6,182,212,0.2)]'
                : 'text-slate-400 hover:text-slate-200 bg-slate-900/50 border border-slate-800'
            }`}
          >
            <User className="w-3.5 h-3.5" />
            Player App
          </Link>

          {/* Admin Host App Route */}
          <Link
            to="/admin"
            className={`px-3.5 py-1.5 rounded-full text-xs font-bold transition-all flex items-center gap-1.5 ${
              isAdminApp
                ? 'bg-purple-500/10 border border-purple-500/30 text-purple-300 shadow-[0_0_12px_rgba(168,85,247,0.2)]'
                : 'text-slate-400 hover:text-slate-200 bg-slate-900/50 border border-slate-800'
            }`}
          >
            <Shield className="w-3.5 h-3.5" />
            Admin App
          </Link>

          {/* Google Sign In Button when logged out */}
          {!currentUser ? (
            <button
              onClick={onGoogleSignIn}
              className="px-3.5 py-1.5 rounded-full text-xs font-bold text-white bg-slate-900 hover:bg-slate-800 border border-slate-700/80 hover:border-cyan-500/60 shadow-[0_0_15px_rgba(6,182,212,0.15)] transition-all flex items-center gap-2 group"
            >
              <svg className="w-3.5 h-3.5" viewBox="0 0 24 24">
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
              <span className="hidden sm:inline">Sign in with Google</span>
              <span className="sm:hidden">Sign In</span>
            </button>
          ) : (
            /* User Status / Avatar & Log Out */
            <div className="flex items-center space-x-3 pl-3 border-l border-slate-800/80">
              <div className="relative">
                {currentUser.photoURL ? (
                  <img
                    src={currentUser.photoURL}
                    alt={currentUser.fullName}
                    className="w-9 h-9 rounded-full object-cover border border-cyan-500/40 shadow-[0_0_15px_rgba(6,182,212,0.2)]"
                  />
                ) : (
                  <div className="w-9 h-9 rounded-full bg-slate-900 border border-cyan-500/30 flex items-center justify-center shadow-[0_0_15px_rgba(6,182,212,0.15)]">
                    <span className="text-xs font-black text-cyan-400">
                      {currentUser.fullName ? currentUser.fullName.slice(0, 2).toUpperCase() : 'TA'}
                    </span>
                  </div>
                )}
                <div className="absolute -bottom-0.5 -right-0.5 w-3 h-3 bg-green-500 border-2 border-slate-950 rounded-full"></div>
              </div>

              <div className="hidden md:flex flex-col">
                <span className="text-xs text-slate-200 font-bold leading-tight flex items-center gap-1.5">
                  {currentUser.fullName}
                  {currentUser.email && (
                    <span className="text-[9px] px-1.5 py-0.2 rounded bg-cyan-950 text-cyan-400 border border-cyan-500/30">
                      Google
                    </span>
                  )}
                </span>
                <span className="text-[10px] text-cyan-400/80 font-mono">
                  @{currentUser.inGameUsername || 'Player'}
                </span>
              </div>

              <button
                onClick={onLogout}
                title="Log Out"
                className="p-2 text-slate-400 hover:text-rose-400 hover:bg-slate-900/80 rounded-xl transition-colors border border-transparent hover:border-slate-800"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
