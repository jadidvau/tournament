# Dhaka eFootball Open Championship 🏆

A tournament registration, manual payment verification (bKash & Nagad), and live 1v1 matchmaking platform for eFootball players in Bangladesh.

---

## 🎮 Features

### 1. Player Portal (`/player`)
- **Authentication**: Email/Password and Bangladesh Phone number (+880).
- **Profile Setup**: Full name, phone number, eFootball in-game ID, and in-game username.
- **Tournament Overview**: Real-time view of entry fee (default: `100 BDT`), match length (`10 Mins`), and registration status.
- **Manual Payment Gateways**:
  - **bKash Personal**: `01904031478` with one-tap copy.
  - **Nagad Personal**: `01904031478` with one-tap copy.
- **Payment Verification Submission**: Select gateway, submit Transaction ID (TrxID), and track real-time verification status (`pending` ➔ `joined` / `rejected` with reason).
- **My Match Hub**: Shows upcoming 1v1 matchup, opponent details (Gamertag & In-game ID for room matchmaking), scheduled time, and live scores.
- **Live Bracket Viewer**: Read-only interactive view across Round 1, Quarter-Finals, Semi-Finals, and Grand Finals, with custom BYE handling.

### 2. Admin Host Portal (`/admin`)
- **Protected Role Gatekeeper**: Restricted to users with `role: 'admin'`.
- **Pending Registrations Queue**: Review payment submissions, verify TrxIDs, with single-click **Approve** or **Reject** (with custom feedback reason).
- **1v1 Knockout Bracket Generator**: Seeds all verified players into a single-elimination bracket, automatically calculating power-of-2 byes and round advancement paths.
- **Live Match & Score Management**: Click any match to update live scores, set match status (`scheduled` ➔ `live` ➔ `completed`), auto-advance winners into the next round, and crown the Grand Champion.
- **Championship Settings**: Configure title, entry fee, payment numbers, and toggle registration status open/closed.
- **Statistics & Prize Pool**: Live dashboard tracking total registered, pending queue, verified players, matches played, and total prize pool in BDT.

---

## 🛠️ Tech Stack & Architecture

- **Web Stack**: React 18, Vite, TypeScript, TailwindCSS, Lucide Icons, React Router v6.
- **Android App**: Kotlin, Jetpack Compose, Material 3, ViewModel, StateFlow, Coroutines.
- **Backend & Database**: Firebase Authentication, Cloud Firestore (real-time collections for `users`, `registrations`, `tournaments`, and `matches`), and Firestore Security Rules.
- **Visual Design**: Dark esports aesthetic (`slate-950` canvas, `cyan-500` neon accents, glassmorphic cards, glowing borders).

---

## 🚀 Firebase Setup Guide

1. Create a Firebase project at [https://console.firebase.google.com](https://console.firebase.google.com).
2. Enable **Email/Password** and **Phone** sign-in providers in **Authentication**.
3. Create a **Cloud Firestore** database.
4. Deploy the security rules from `firestore.rules`:
   ```bash
   firebase deploy --only firestore:rules
   ```
5. Copy your Firebase web credentials into `.env` (refer to `.env.example`):
   ```env
   VITE_FIREBASE_API_KEY=your_key
   VITE_FIREBASE_AUTH_DOMAIN=your_project.firebaseapp.com
   VITE_FIREBASE_PROJECT_ID=your_project
   VITE_FIREBASE_STORAGE_BUCKET=your_project.appspot.com
   VITE_FIREBASE_MESSAGING_SENDER_ID=your_sender_id
   VITE_FIREBASE_APP_ID=your_app_id
   ```
6. Build and deploy the web app:
   ```bash
   cd web
   npm install
   npm run build
   firebase deploy --only hosting
   ```
