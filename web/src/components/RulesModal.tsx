import React, { useState } from 'react';
import {
  BookOpen,
  ChevronDown,
  ChevronUp,
  X,
  ShieldCheck,
  Gamepad2,
  Wifi,
  Camera,
  AlertTriangle,
  Clock,
  Phone,
  MessageCircle,
  ExternalLink,
  Code2,
  Sparkles,
  CheckCircle2
} from 'lucide-react';

interface RulesModalProps {
  isOpen: boolean;
  onClose: () => void;
  tournamentTitle?: string;
}

interface RuleSection {
  id: string;
  title: string;
  icon: React.ElementType;
  badge: string;
  badgeColor: string;
  rules: {
    heading: string;
    description: string;
    highlight?: string;
  }[];
}

const RULE_SECTIONS: RuleSection[] = [
  {
    id: 'match_settings',
    title: 'Match Settings & Game Configuration',
    icon: Gamepad2,
    badge: 'Standard Setup',
    badgeColor: 'border-cyan-500/30 bg-cyan-500/10 text-cyan-400',
    rules: [
      {
        heading: 'Match Duration & Time',
        description: 'Match time must be set to exactly 10 Minutes (Regular Match Duration).',
        highlight: '10 Mins'
      },
      {
        heading: 'Extra Time & Penalties (PK)',
        description: 'Both Extra Time (ET) and Penalty Shootout (PK) must be enabled (ON) to decide ties in knockout stages.',
        highlight: 'ET & PK: ON'
      },
      {
        heading: 'Player Condition & Home/Away',
        description: 'Player condition must be set to "Excellent" or standard Neutral condition as instructed by room host.',
        highlight: 'Condition: Excellent / Normal'
      },
      {
        heading: 'Substitutions & Injuries',
        description: 'Maximum 5 substitutions allowed in regular time (+1 in extra time). Injuries must be set to OFF.',
        highlight: '5 Subs (+1 ET), Injuries: OFF'
      }
    ]
  },
  {
    id: 'squad_rules',
    title: 'Squad, Team & Player Restrictions',
    icon: ShieldCheck,
    badge: 'Fair Play',
    badgeColor: 'border-purple-500/30 bg-purple-500/10 text-purple-400',
    rules: [
      {
        heading: 'Team Building & Collective Strength',
        description: 'Players may use Dream Team squads unless an Authentic or capped collective strength limit is specified for the round.',
        highlight: 'Dream Team / Authentic'
      },
      {
        heading: 'Exploits & Glitch Usage',
        description: 'Kick-off glitch abuse, lag generation, pausing during active opponent attacks, or corner exploit traps are strictly prohibited.',
        highlight: 'Zero Tolerance'
      },
      {
        heading: 'Tactics & Pausing',
        description: 'Pauses may only be taken when the ball is out of play (throw-in, goal kick, foul). No spamming tactical pauses.',
        highlight: 'Max 3 pauses / match'
      }
    ]
  },
  {
    id: 'network_rules',
    title: 'Network, Disconnections & Lag Disputes',
    icon: Wifi,
    badge: 'Connectivity',
    badgeColor: 'border-amber-500/30 bg-amber-500/10 text-amber-400',
    rules: [
      {
        heading: 'Required Connection',
        description: 'All competitors must ensure a stable Wi-Fi or high-speed 4G/5G connection before entering the room.',
        highlight: 'Ping < 80ms Recommended'
      },
      {
        heading: 'Early Disconnection (Before 15th In-Game Min)',
        description: 'If a disconnect occurs before 15 in-game minutes without goals, an immediate rematch is played.',
        highlight: 'Rematch with 0-0'
      },
      {
        heading: 'Late Disconnection (After 15th Min)',
        description: 'Scores at the time of disconnection stand. The remaining minutes are played in a new match, or forfeit is issued if intentional.',
        highlight: 'Score Preserved'
      },
      {
        heading: 'Intentional Rage Quits / Network Sabotage',
        description: 'Any competitor caught disconnecting intentionally to avoid defeat will forfeit 0-3 and face immediate ban.',
        highlight: 'Immediate 0-3 Loss & Disqualification'
      }
    ]
  },
  {
    id: 'reporting_rules',
    title: 'Result Submission & Match Evidence',
    icon: Camera,
    badge: 'Verification',
    badgeColor: 'border-emerald-500/30 bg-emerald-500/10 text-emerald-400',
    rules: [
      {
        heading: 'Mandatory Match Screenshots',
        description: 'Both the winner and loser must take clear screenshots of the Final Score Screen and Match Stats.',
        highlight: 'Full Screen Required'
      },
      {
        heading: '15-Minute Reporting Deadline',
        description: 'Scores and proof must be submitted to the Admin / Match Hub within 15 minutes of the final whistle.',
        highlight: '15 Mins Window'
      },
      {
        heading: 'Dispute Resolution & Video Proof',
        description: 'In case of conflicting score reports, video clips or screen records will be audited by the tournament organizers.',
        highlight: 'Admin Decision Final'
      }
    ]
  },
  {
    id: 'conduct_rules',
    title: 'Punctuality, Conduct & Administration',
    icon: AlertTriangle,
    badge: 'Tournament Ethics',
    badgeColor: 'border-rose-500/30 bg-rose-500/10 text-rose-400',
    rules: [
      {
        heading: 'Grace Period (10 Minutes)',
        description: 'Players have a 10-minute grace window from scheduled match time. Absence beyond 10 mins results in an automatic Walkover (WO).',
        highlight: '10 Mins Max'
      },
      {
        heading: 'Sportsmanship & Toxic Behavior',
        description: 'Profanity, toxic verbal abuse, or harassing opponents in WhatsApp / Discord or in-game results in instant expulsion.',
        highlight: 'Strict Ban'
      },
      {
        heading: 'Final Authority',
        description: 'Tournament rules may be amended by Head Admin Jadid Mollik to ensure fair play. Decisions are irrevocable.',
        highlight: 'Organizer Authority'
      }
    ]
  }
];

export const RulesModal: React.FC<RulesModalProps> = ({ isOpen, onClose, tournamentTitle }) => {
  const [openSections, setOpenSections] = useState<Record<string, boolean>>({
    match_settings: true,
    squad_rules: true,
    network_rules: false,
    reporting_rules: false,
    conduct_rules: false
  });

  if (!isOpen) return null;

  const toggleSection = (id: string) => {
    setOpenSections(prev => ({
      ...prev,
      [id]: !prev[id]
    }));
  };

  const expandAll = () => {
    const allExpanded: Record<string, boolean> = {};
    RULE_SECTIONS.forEach(s => (allExpanded[s.id] = true));
    setOpenSections(allExpanded);
  };

  const collapseAll = () => {
    setOpenSections({});
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6 overflow-y-auto bg-slate-950/80 backdrop-blur-md animate-in fade-in duration-200">
      <div className="relative w-full max-w-3xl bg-slate-900/95 border border-cyan-500/30 rounded-[32px] shadow-[0_0_50px_rgba(6,182,212,0.15)] overflow-hidden flex flex-col max-h-[90vh]">
        {/* Modal Header */}
        <div className="p-6 sm:p-7 border-b border-slate-800 flex items-center justify-between bg-slate-950/60 sticky top-0 z-20 backdrop-blur-md">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-2xl bg-cyan-500/10 border border-cyan-500/30 flex items-center justify-center text-cyan-400 shadow-[0_0_15px_rgba(6,182,212,0.2)]">
              <BookOpen className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="text-[10px] font-black uppercase tracking-widest text-cyan-400 bg-cyan-950/80 px-2 py-0.5 rounded border border-cyan-500/30">
                  Official Regulations
                </span>
                <span className="text-xs text-slate-400 font-mono">eFootball 2026</span>
              </div>
              <h2 className="font-esports text-xl sm:text-2xl font-black italic tracking-tight text-white uppercase mt-0.5">
                Tournament Rulebook
              </h2>
            </div>
          </div>

          <button
            onClick={onClose}
            className="w-10 h-10 rounded-2xl bg-slate-800/80 hover:bg-slate-700 text-slate-300 hover:text-white flex items-center justify-center transition-colors border border-slate-700/60"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Action Controls & Summary Bar */}
        <div className="px-6 py-3 bg-slate-950/40 border-b border-slate-800/60 flex flex-wrap items-center justify-between gap-3 text-xs text-slate-400">
          <p className="flex items-center gap-1.5">
            <Sparkles className="w-3.5 h-3.5 text-cyan-400" />
            <span>Click any section below to toggle accordion details</span>
          </p>
          <div className="flex items-center gap-2">
            <button
              onClick={expandAll}
              className="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-[11px] font-bold text-slate-200 transition-colors"
            >
              Expand All
            </button>
            <button
              onClick={collapseAll}
              className="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-[11px] font-bold text-slate-200 transition-colors"
            >
              Collapse All
            </button>
          </div>
        </div>

        {/* Modal Body: Collapsible Accordion Sections */}
        <div className="p-6 sm:p-7 overflow-y-auto space-y-4 flex-1 custom-scrollbar">
          {RULE_SECTIONS.map((section, idx) => {
            const Icon = section.icon;
            const isOpen = !!openSections[section.id];

            return (
              <div
                key={section.id}
                className="rounded-2xl bg-slate-950/70 border border-slate-800 hover:border-slate-700 transition-all overflow-hidden"
              >
                {/* Accordion Trigger Header */}
                <button
                  type="button"
                  onClick={() => toggleSection(section.id)}
                  className="w-full p-4.5 sm:p-5 flex items-center justify-between text-left gap-4 hover:bg-slate-900/50 transition-colors"
                >
                  <div className="flex items-center gap-3.5 min-w-0">
                    <div className="w-9 h-9 rounded-xl bg-slate-900 border border-slate-700/80 flex items-center justify-center text-cyan-400 shrink-0">
                      <Icon className="w-4.5 h-4.5" />
                    </div>
                    <div className="min-w-0">
                      <div className="flex items-center gap-2 mb-0.5">
                        <span className="text-[10px] font-black text-slate-500 uppercase tracking-wider">
                          Section 0{idx + 1}
                        </span>
                        <span className={`text-[10px] font-bold uppercase px-2 py-0.2 rounded-full border ${section.badgeColor}`}>
                          {section.badge}
                        </span>
                      </div>
                      <h3 className="text-sm sm:text-base font-bold text-white truncate">
                        {section.title}
                      </h3>
                    </div>
                  </div>

                  <div className="shrink-0 w-8 h-8 rounded-lg bg-slate-900 border border-slate-800 flex items-center justify-center text-slate-400">
                    {isOpen ? <ChevronUp className="w-4 h-4 text-cyan-400" /> : <ChevronDown className="w-4 h-4" />}
                  </div>
                </button>

                {/* Collapsible Content */}
                {isOpen && (
                  <div className="p-4.5 sm:p-5 pt-0 border-t border-slate-800/80 bg-slate-900/30 space-y-3.5 animate-in slide-in-from-top-1 duration-200">
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-3">
                      {section.rules.map((rule, rIdx) => (
                        <div
                          key={rIdx}
                          className="p-3.5 rounded-xl bg-slate-950/80 border border-slate-800/90 flex flex-col justify-between space-y-2"
                        >
                          <div>
                            <div className="flex items-center justify-between gap-2 mb-1">
                              <h4 className="text-xs font-bold text-slate-200 flex items-center gap-1.5">
                                <CheckCircle2 className="w-3.5 h-3.5 text-cyan-400 shrink-0" />
                                {rule.heading}
                              </h4>
                            </div>
                            <p className="text-[11px] text-slate-400 leading-relaxed">
                              {rule.description}
                            </p>
                          </div>
                          {rule.highlight && (
                            <div className="pt-1.5 border-t border-slate-900 flex items-center justify-between">
                              <span className="text-[9px] uppercase font-bold text-slate-500 tracking-wider">Rule Spec</span>
                              <span className="text-[10px] font-mono font-bold text-cyan-400 bg-cyan-950/60 px-2 py-0.5 rounded border border-cyan-500/20">
                                {rule.highlight}
                              </span>
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            );
          })}

          {/* DEVELOPER INFO & ORGANIZER BANNER */}
          <div className="rounded-2xl bg-gradient-to-br from-slate-900 to-slate-950 border border-cyan-500/30 p-5 sm:p-6 shadow-xl relative overflow-hidden mt-6">
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
              <div className="flex items-center gap-3.5">
                <div className="w-12 h-12 rounded-2xl bg-cyan-500/10 border border-cyan-500/40 flex items-center justify-center text-cyan-400 shadow-[0_0_15px_rgba(6,182,212,0.2)] shrink-0">
                  <Code2 className="w-6 h-6" />
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <span className="text-[10px] font-black uppercase tracking-widest text-cyan-400 bg-cyan-950 px-2 py-0.5 rounded border border-cyan-500/30">
                      Developer & Organizer
                    </span>
                  </div>
                  <h4 className="text-base font-black text-white tracking-wide mt-0.5">
                    JADID MOLLIK
                  </h4>
                  <p className="text-xs text-slate-400">
                    Lead Esports Platform Engineer & Tournament Administrator
                  </p>
                </div>
              </div>

              {/* WhatsApp Contact Action */}
              <div className="flex flex-wrap items-center gap-2.5 w-full sm:w-auto">
                <a
                  href="https://wa.me/8801980000601"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="w-full sm:w-auto px-4 py-2.5 rounded-xl bg-emerald-500 hover:bg-emerald-400 active:bg-emerald-600 text-slate-950 font-black text-xs uppercase tracking-wider flex items-center justify-center gap-2 shadow-[0_0_15px_rgba(16,185,129,0.3)] transition-all"
                >
                  <MessageCircle className="w-4 h-4 fill-slate-950" />
                  <span>WhatsApp: 01980000601</span>
                </a>
              </div>
            </div>
          </div>
        </div>

        {/* Modal Footer */}
        <div className="p-4 sm:p-5 border-t border-slate-800 bg-slate-950/80 flex items-center justify-between gap-4">
          <p className="text-[11px] text-slate-500 font-mono">
            By participating, players agree to all terms enforced by Jadid Mollik.
          </p>
          <button
            onClick={onClose}
            className="px-5 py-2.5 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-black text-xs uppercase tracking-wider transition-all shadow-[0_0_15px_rgba(6,182,212,0.2)]"
          >
            I Understand & Agree
          </button>
        </div>
      </div>
    </div>
  );
};
