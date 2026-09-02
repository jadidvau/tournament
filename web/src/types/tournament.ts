export type UserRole = 'player' | 'admin';

export type PaymentMethod = 'bKash' | 'Nagad';

export type RegistrationStatus = 'pending' | 'joined' | 'rejected';

export type MatchStatus = 'scheduled' | 'live' | 'completed';

export interface UserProfile {
  uid: string;
  fullName: string;
  email?: string;
  photoURL?: string;
  phoneNumber: string;
  inGameId: string;
  inGameUsername: string;
  role: UserRole;
  createdAt: number;
}

export interface TournamentRegistration {
  id: string;
  userId: string;
  fullName: string;
  phoneNumber: string;
  inGameId: string;
  inGameUsername: string;
  paymentMethod: PaymentMethod;
  trxId: string;
  status: RegistrationStatus;
  rejectionReason?: string;
  feeAmount: number;
  submittedAt: number;
  reviewedAt?: number;
  reviewedBy?: string;
}

export interface MatchPlayer {
  id: string;
  name: string;
  inGameUsername: string;
  inGameId: string;
}

export interface TournamentMatch {
  id: string;
  tournamentId: string;
  round: number;
  matchIndex: number;
  player1: MatchPlayer | null;
  player2: MatchPlayer | null;
  player1Score: number;
  player2Score: number;
  winnerId: string | null;
  status: MatchStatus;
  isBye: boolean;
  startTime: string;
}

export interface TournamentInfo {
  id: string;
  title: string;
  entryFee: number;
  bkashNumber: string;
  nagadNumber: string;
  isRegistrationOpen: boolean;
  matchDurationMinutes: number;
  totalRounds: number;
  champion: string | null;
  championUsername: string | null;
  createdAt: number;
}
