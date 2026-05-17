# GoatsArena Firebase Architecture

## Table of Contents
1. [Firestore Data Model](#1-firestore-data-model)
2. [Security Rules](#2-security-rules)
3. [Cloud Functions](#3-cloud-functions)
4. [Concurrency & Balance Safety](#4-concurrency--balance-safety)

---

## 1. Firestore Data Model

### Collection Hierarchy Overview

```
/users/{uid}
  /privateProfile/{uid}
  /deviceBindings/{deviceId}
/balances/{uid}
/walletLedger/{ledgerId}
/topupOrders/{orderId}
/tournaments/{tournamentId}
  /participants/{uid}
  /roomInfo/{tournamentId}
  /results/{uid}
  /disputes/{disputeId}
/withdrawals/{withdrawalId}
/leaderboards/{boardId}
  /entries/{uid}
/deviceResetRequests/{requestId}
/auditLogs/{logId}
/appConfig/{configId}
```

---

### 1.1 `/users/{uid}`

```typescript
interface UserDoc {
  uid: string;                    // "firebase_auth_uid_abc123"
  phone: string;                  // "+923001234567" (E.164)
  name: string;                   // "Ahmed Raza"
  avatarId: string;               // "avatar_05"
  freeFireUid: string;            // "12345678"
  region: string;                 // "Punjab"
  city: string;                   // "Lahore"
  role: "player" | "mod" | "admin";  // "player"
  suspiciousScore: number;        // 0-100, triggers review at >50
  suspiciousFlags: string[];      // ["multiple_device_attempt", "rapid_joins"]
  isBanned: boolean;              // false
  banReason?: string;             // "tos_violation"
  banExpiresAt?: Timestamp;       // null = permanent
  deviceId: string;               // bound device fingerprint hash
  createdAt: Timestamp;
  updatedAt: Timestamp;
  lastLoginAt: Timestamp;
  lastLoginDeviceId: string;
}

// Example
{
  uid: "uXk9mP2Qa",
  phone: "+923001234567",
  name: "Ahmed Raza",
  avatarId: "avatar_05",
  freeFireUid: "12345678",
  region: "Punjab",
  city: "Lahore",
  role: "player",
  suspiciousScore: 0,
  suspiciousFlags: [],
  isBanned: false,
  deviceId: "sha256:abc...def",
  createdAt: Timestamp,
  updatedAt: Timestamp,
  lastLoginAt: Timestamp,
  lastLoginDeviceId: "sha256:abc...def"
}
```

### 1.2 `/users/{uid}/privateProfile/{uid}`

```typescript
interface PrivateProfileDoc {
  // Subcollection doc ID = uid (single doc per user)
  fullPhone: string;              // "+923001234567"
  withdrawalAccounts: {
    easypaisa?: string;           // "03001234567"
    jazzcash?: string;            // "03001234567"
    bankIban?: string;            // "PK36SCBL0000001123456702"
  };
  totalWon: number;               // coins ever won
  totalDeposited: number;         // coins ever deposited
  totalWithdrawn: number;         // coins ever withdrawn
  kycStatus: "none" | "pending" | "verified" | "rejected";
}
```

### 1.3 `/balances/{uid}`

```typescript
// Doc ID = uid for O(1) lookup
interface BalanceDoc {
  uid: string;
  availableCoins: number;         // 1250  — spendable balance
  lockedCoins: number;            // 200   — reserved for active joins/withdrawals
  totalCoins: number;             // 1450  — availableCoins + lockedCoins (denorm)
  updatedAt: Timestamp;
  version: number;                // optimistic lock counter, increments on each write
}

// Example
{
  uid: "uXk9mP2Qa",
  availableCoins: 1250,
  lockedCoins: 200,
  totalCoins: 1450,
  updatedAt: Timestamp,
  version: 47
}
```

### 1.4 `/walletLedger/{ledgerId}`

```typescript
// IMMUTABLE — never update, only create
// ledgerId = auto-ID (ordered)
interface WalletLedgerDoc {
  ledgerId: string;               // auto
  uid: string;                    // "uXk9mP2Qa"
  type: LedgerType;
  // LedgerType = "TOPUP" | "TOPUP_BONUS" | "JOIN_LOCK" | "JOIN_UNLOCK"
  //            | "PRIZE" | "WITHDRAWAL_LOCK" | "WITHDRAWAL_FEE"
  //            | "WITHDRAWAL_PAYOUT" | "WITHDRAWAL_UNLOCK" | "ADMIN_ADJUST"
  //            | "REFUND"
  amount: number;                 // +610 (positive = credit, negative = debit)
  balanceBefore: number;          // available balance before
  balanceAfter: number;           // available balance after
  lockedBefore: number;
  lockedAfter: number;
  refType: "topupOrder" | "tournament" | "withdrawal" | "admin" | null;
  refId: string | null;           // orderId / tournamentId / withdrawalId
  note: string;                   // human-readable
  createdAt: Timestamp;
  createdBy: string;              // uid of actor (or "system")
  idempotencyKey: string;         // prevent duplicate ledger entries
}

// Example — topup
{
  ledgerId: "ldg_abc123",
  uid: "uXk9mP2Qa",
  type: "TOPUP",
  amount: 610,
  balanceBefore: 640,
  balanceAfter: 1250,
  lockedBefore: 200,
  lockedAfter: 200,
  refType: "topupOrder",
  refId: "ord_xyz789",
  note: "Topup 600 coins + 10 bonus",
  createdAt: Timestamp,
  createdBy: "admin_uid_001",
  idempotencyKey: "topup:ord_xyz789"
}
```

### 1.5 `/topupOrders/{orderId}`

```typescript
interface TopupOrderDoc {
  orderId: string;
  uid: string;
  packageId: string;              // "PKG_600" | "PKG_800" | "PKG_1000"
  baseCoins: number;              // 600
  bonusCoins: number;             // 10
  totalCoins: number;             // 610
  pkrAmount: number;              // 2400  (baseCoins * coinRate)
  coinRate: number;               // 4     (PKR per coin, snapshot at order time)
  paymentMethod: string;          // "easypaisa" | "jazzcash" | "bank_transfer"
  paymentReference: string;       // screenshot ref or TXN ID
  proofStoragePath: string;       // "topup_proofs/ord_xyz789.jpg"
  status: "PENDING" | "APPROVED" | "REJECTED";
  reviewedBy?: string;            // admin uid
  reviewedAt?: Timestamp;
  rejectReason?: string;
  createdAt: Timestamp;
  updatedAt: Timestamp;
  idempotencyKey: string;         // "topup:{uid}:{packageId}:{timestamp_minute}"
}
```

### 1.6 `/tournaments/{tournamentId}`

```typescript
interface TournamentDoc {
  tournamentId: string;
  title: string;                  // "Friday Night Solo"
  gameMode: "solo" | "duo" | "squad";
  maxSlots: number;               // 100
  filledSlots: number;            // 47
  entryFeeCoins: number;          // 50
  prizePoolCoins: number;         // 4500  (computed or manual)
  prizeDistribution: PrizeTier[]; // see below
  status: TournamentStatus;
  // TournamentStatus = "DRAFT" | "OPEN" | "FULL" | "CHECKIN" | "LIVE"
  //                  | "RESULT_PENDING" | "COMPLETED" | "CANCELLED"
  region: string | null;          // null = global
  scheduledAt: Timestamp;         // match start time
  checkInOpenAt: Timestamp;       // scheduledAt - 30min
  checkInCloseAt: Timestamp;      // scheduledAt - 5min
  roomRevealAt: Timestamp;        // scheduledAt - 2min
  resultSubmitDeadline: Timestamp; // scheduledAt + 3hr
  createdBy: string;              // admin uid
  modUid: string | null;          // assigned mod
  bannedUserIds: string[];        // per-tournament bans
  rules: string;                  // markdown
  createdAt: Timestamp;
  updatedAt: Timestamp;
}

interface PrizeTier {
  rank: number;                   // 1
  coins: number;                  // 2000
  label: string;                  // "1st Place"
}

// Example
{
  tournamentId: "trn_001",
  title: "Friday Night Solo",
  gameMode: "solo",
  maxSlots: 100,
  filledSlots: 47,
  entryFeeCoins: 50,
  prizePoolCoins: 4500,
  prizeDistribution: [
    { rank: 1, coins: 2000, label: "1st Place" },
    { rank: 2, coins: 1200, label: "2nd Place" },
    { rank: 3, coins: 800,  label: "3rd Place" },
    { rank: 4, coins: 500,  label: "4th-10th (shared)" }
  ],
  status: "OPEN",
  region: "Punjab",
  scheduledAt: Timestamp("2024-02-16T20:00:00+05:00"),
  checkInOpenAt: Timestamp("2024-02-16T19:30:00+05:00"),
  checkInCloseAt: Timestamp("2024-02-16T19:55:00+05:00"),
  roomRevealAt: Timestamp("2024-02-16T19:58:00+05:00"),
  resultSubmitDeadline: Timestamp("2024-02-16T23:00:00+05:00"),
  createdBy: "admin_uid_001",
  modUid: "mod_uid_001",
  bannedUserIds: [],
  rules: "# Rules\n- No emulators\n- Screenshot required",
  createdAt: Timestamp,
  updatedAt: Timestamp
}
```

### 1.7 `/tournaments/{tournamentId}/participants/{uid}`

```typescript
interface ParticipantDoc {
  uid: string;
  tournamentId: string;
  name: string;                   // denormalized
  freeFireUid: string;            // denormalized
  avatarId: string;
  region: string;
  status: ParticipantStatus;
  // ParticipantStatus = "JOINED" | "CHECKED_IN" | "ELIMINATED"
  //                   | "COMPLETED" | "DISQUALIFIED" | "NO_SHOW"
  slotNumber: number;             // 1-100, assigned on join
  coinsLocked: number;            // entry fee locked
  joinedAt: Timestamp;
  checkedInAt?: Timestamp;
  deviceId: string;               // snapshot for anti-cheat
}
```

### 1.8 `/tournaments/{tournamentId}/roomInfo/{tournamentId}`

```typescript
// Doc ID = tournamentId (single doc)
interface RoomInfoDoc {
  tournamentId: string;
  roomId: string;                 // "98765"
  roomPassword: string;           // "goats2024"
  revealedAt: Timestamp;
  setBy: string;                  // admin/mod uid
  // Access controlled: only CHECKED_IN participants can read
  // (enforced via Cloud Function endpoint, not direct Firestore read)
}
```

### 1.9 `/tournaments/{tournamentId}/results/{uid}`

```typescript
interface ResultDoc {
  uid: string;
  tournamentId: string;
  freeFireUid: string;
  kills: number;                  // 5
  placement: number;              // 3
  score: number;                  // computed: placement_pts + kill_pts
  proofStoragePath: string;       // "results/trn_001/uXk9mP2Qa.jpg"
  proofUploadedAt: Timestamp;
  status: "PENDING" | "APPROVED" | "REJECTED" | "DISPUTED";
  reviewedBy?: string;
  reviewedAt?: Timestamp;
  rejectReason?: string;
  submittedAt: Timestamp;
}
```

### 1.10 `/tournaments/{tournamentId}/disputes/{disputeId}`

```typescript
interface DisputeDoc {
  disputeId: string;
  tournamentId: string;
  raisedBy: string;               // uid
  againstUid: string;             // uid accused
  reason: string;                 // "cheating" | "wrong_result" | "other"
  description: string;
  evidenceStoragePaths: string[]; // ["disputes/trn_001/dsp_001_a.jpg"]
  status: "OPEN" | "REVIEWING" | "RESOLVED_UPHELD" | "RESOLVED_DISMISSED";
  assignedMod?: string;
  resolution?: string;
  resolvedAt?: Timestamp;
  createdAt: Timestamp;
}
```

### 1.11 `/withdrawals/{withdrawalId}`

```typescript
interface WithdrawalDoc {
  withdrawalId: string;
  uid: string;
  requestedCoins: number;         // 500  (what user wants)
  feeCoins: number;               // 25   (platform fee)
  netCoins: number;               // 475  (after fee)
  feePercent: number;             // 5    (snapshot from config)
  fixedFeeCoins: number;          // 0    (snapshot from config)
  coinRate: number;               // 4    (snapshot from config)
  netPkr: number;                 // 1900 (netCoins * coinRate)
  paymentMethod: string;          // "easypaisa"
  accountNumber: string;          // "03001234567"
  status: WithdrawalStatus;
  // WithdrawalStatus = "PENDING" | "APPROVED" | "PAID" | "REJECTED"
  weeklyCapSnapshot: number;      // 1200 (cap at time of request)
  weeklyUsedBefore: number;       // 200  (coins withdrawn this week before this)
  approvedBy?: string;
  approvedAt?: Timestamp;
  paidBy?: string;
  paidAt?: Timestamp;
  paymentTxnRef?: string;
  rejectReason?: string;
  createdAt: Timestamp;
  updatedAt: Timestamp;
  idempotencyKey: string;         // "withdraw:{uid}:{weekStart}:{requestedCoins}:{nonce}"
}
```

### 1.12 `/leaderboards/{boardId}`

```typescript
// boardId pattern: "global_weekly_2024-W07" | "Punjab_monthly_2024-02" | "global_season_S1"
interface LeaderboardDoc {
  boardId: string;
  scope: "global" | string;       // region name
  period: "weekly" | "monthly" | "season";
  periodKey: string;              // "2024-W07" | "2024-02" | "S1"
  weekStart?: Timestamp;          // for weekly boards
  weekEnd?: Timestamp;
  computedAt: Timestamp;
  totalPlayers: number;
  topEntries: LeaderboardEntry[]; // top 100 denormalized for fast render
}

// /leaderboards/{boardId}/entries/{uid}
interface LeaderboardEntryDoc {
  uid: string;
  name: string;
  avatarId: string;
  region: string;
  city: string;
  rank: number;
  totalKills: number;
  totalWins: number;              // 1st place finishes
  tournamentsPlayed: number;
  totalPrizesWon: number;         // coins
  score: number;                  // composite ranking score
  updatedAt: Timestamp;
}
```

### 1.13 `/deviceBindings/{bindingId}`

```typescript
// bindingId = sha256(deviceFingerprint)
interface DeviceBindingDoc {
  bindingId: string;              // "sha256:abc...def"
  uid: string;                    // bound user uid
  deviceModel: string;            // "Redmi Note 10"
  osVersion: string;              // "Android 12"
  appVersion: string;             // "1.2.3"
  boundAt: Timestamp;
  lastSeenAt: Timestamp;
  isActive: boolean;
}
```

### 1.14 `/deviceResetRequests/{requestId}`

```typescript
interface DeviceResetRequestDoc {
  requestId: string;
  uid: string;
  currentDeviceId: string;        // old bound device
  newDeviceId: string;            // device requesting reset
  reason: string;                 // "lost phone" | "new phone" | "other"
  status: "PENDING" | "APPROVED" | "REJECTED";
  reviewedBy?: string;
  reviewedAt?: Timestamp;
  rejectReason?: string;
  createdAt: Timestamp;
  suspiciousScore: number;        // copied from user at time of request
}
```

### 1.15 `/auditLogs/{logId}`

```typescript
// IMMUTABLE — append-only, admin-write only
interface AuditLogDoc {
  logId: string;
  action: string;                 // "APPROVE_TOPUP" | "REJECT_RESULT" | "BAN_USER" etc.
  performedBy: string;            // admin/mod uid
  targetUid?: string;             // affected user
  targetCollection?: string;      // "tournaments"
  targetDocId?: string;
  before?: Record<string, unknown>; // snapshot before
  after?: Record<string, unknown>;  // snapshot after
  metadata?: Record<string, unknown>;
  ip?: string;
  createdAt: Timestamp;
}
```

### 1.16 `/appConfig/{configId}`

```typescript
// configId = "main" (single doc for most settings)
interface AppConfigDoc {
  coinRate: number;               // 4  (PKR per coin)
  coinPackages: CoinPackage[];
  withdrawalConfig: {
    minWithdrawCoins: number;     // 100
    weeklyCapCoins: number;       // 1200
    feePercent: number;           // 5
    fixedFeeCoins: number;        // 0
    weekStartDay: number;         // 1 = Monday
    timezone: string;             // "Asia/Karachi"
  };
  suspiciousScoreThresholds: {
    review: number;               // 50
    autoban: number;              // 90
  };
  maintenanceMode: boolean;
  updatedBy: string;
  updatedAt: Timestamp;
}

interface CoinPackage {
  packageId: string;              // "PKG_600"
  baseCoins: number;              // 600
  bonusCoins: number;             // 10
  totalCoins: number;             // 610
  pkrPrice: number;               // 2400  (computed at display, stored for reference)
  isActive: boolean;
}

// Example
{
  coinRate: 4,
  coinPackages: [
    { packageId: "PKG_600",  baseCoins: 600,  bonusCoins: 10, totalCoins: 610,  pkrPrice: 2400, isActive: true },
    { packageId: "PKG_800",  baseCoins: 800,  bonusCoins: 20, totalCoins: 820,  pkrPrice: 3200, isActive: true },
    { packageId: "PKG_1000", baseCoins: 1000, bonusCoins: 50, totalCoins: 1050, pkrPrice: 4000, isActive: true }
  ],
  withdrawalConfig: {
    minWithdrawCoins: 100,
    weeklyCapCoins: 1200,
    feePercent: 5,
    fixedFeeCoins: 0,
    weekStartDay: 1,
    timezone: "Asia/Karachi"
  },
  suspiciousScoreThresholds: { review: 50, autoban: 90 },
  maintenanceMode: false,
  updatedBy: "admin_uid_001",
  updatedAt: Timestamp
}
```

---

## 2. Security Rules

### 2.1 Firestore Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // ─── Helper Functions ───────────────────────────────────────────────

    function isAuth() {
      return request.auth != null;
    }

    function uid() {
      return request.auth.uid;
    }

    function userDoc() {
      return get(/databases/$(database)/documents/users/$(uid())).data;
    }

    function isAdmin() {
      return isAuth() && userDoc().role == 'admin';
    }

    function isMod() {
      return isAuth() && (userDoc().role == 'mod' || userDoc().role == 'admin');
    }

    function isOwner(userId) {
      return isAuth() && uid() == userId;
    }

    function notBanned() {
      return isAuth() && userDoc().isBanned == false;
    }

    function isSystemWrite() {
      // Cloud Functions use Admin SDK — bypass these rules entirely.
      // This function is a documentation marker; Admin SDK ignores rules.
      return false;
    }

    function incomingData() {
      return request.resource.data;
    }

    function existingData() {
      return resource.data;
    }

    // Validate that only allowed fields are being set/changed
    function onlyFields(fields) {
      return incomingData().keys().hasOnly(fields);
    }

    function fieldUnchanged(field) {
      return incomingData()[field] == existingData()[field];
    }

    // ─── /users/{uid} ───────────────────────────────────────────────────

    match /users/{userId} {
      // Any authenticated user can read basic profile (for tournament display)
      // Sensitive fields (phone, deviceId) are in privateProfile subcollection
      allow read: if isAuth();

      // User can create their own profile (first time, via onboarding function)
      // In practice, createUserProfile Cloud Function handles this with Admin SDK
      // Client-side allowed for graceful degradation but field-restricted
      allow create: if isOwner(userId)
        && notBanned()
        && incomingData().role == 'player'
        && incomingData().isBanned == false
        && incomingData().suspiciousScore == 0
        && incomingData().uid == userId;

      // User can update only their own profile fields (not role, not ban, not suspiciousScore)
      allow update: if isOwner(userId)
        && notBanned()
        && fieldUnchanged('role')
        && fieldUnchanged('isBanned')
        && fieldUnchanged('suspiciousScore')
        && fieldUnchanged('suspiciousFlags')
        && fieldUnchanged('deviceId')
        && fieldUnchanged('uid');

      // Only admin can delete (soft delete preferred)
      allow delete: if isAdmin();

      // ── /users/{uid}/privateProfile/{uid} ──────────────────────────
      match /privateProfile/{profileId} {
        allow read: if isOwner(userId) || isAdmin();
        allow write: if isAdmin(); // Cloud Functions handle user updates
      }

      // ── /users/{uid}/deviceBindings/{deviceId} ─────────────────────
      match /deviceBindings/{deviceId} {
        allow read: if isOwner(userId) || isAdmin();
        allow write: if false; // Only Cloud Functions write via Admin SDK
      }
    }

    // ─── /balances/{uid} ────────────────────────────────────────────────

    match /balances/{userId} {
      // User can read their own balance
      allow read: if isOwner(userId) || isAdmin();
      // NO client writes — all balance mutations go through Cloud Functions
      allow write: if false;
    }

    // ─── /walletLedger/{ledgerId} ───────────────────────────────────────

    match /walletLedger/{ledgerId} {
      // User can read their own ledger entries; admin can read all
      allow read: if isAuth()
        && (resource.data.uid == uid() || isAdmin());
      // IMMUTABLE — no client writes ever
      allow write: if false;
    }

    // ─── /topupOrders/{orderId} ─────────────────────────────────────────

    match /topupOrders/{orderId} {
      // User can read their own orders
      allow read: if isAuth()
        && (resource.data.uid == uid() || isAdmin());

      // User can create a topup order (Cloud Function preferred path)
      allow create: if isAuth()
        && notBanned()
        && incomingData().uid == uid()
        && incomingData().status == 'PENDING'
        && incomingData().keys().hasAll(['uid','packageId','paymentMethod','paymentReference','proofStoragePath'])
        // Prevent status spoofing
        && !incomingData().keys().hasAny(['reviewedBy','reviewedAt','rejectReason']);

      // Only admin/mod can update (approve/reject)
      allow update: if isMod()
        && fieldUnchanged('uid')
        && fieldUnchanged('packageId')
        && fieldUnchanged('baseCoins')
        && fieldUnchanged('totalCoins')
        && (incomingData().status == 'APPROVED' || incomingData().status == 'REJECTED');

      allow delete: if false;
    }

    // ─── /tournaments/{tournamentId} ────────────────────────────────────

    match /tournaments/{tournamentId} {
      // Public browsing — everyone can read tournament list
      allow read: if true;

      // Only admin can create/update tournament metadata
      allow create: if isAdmin();
      allow update: if isAdmin()
        || (isMod() && onlyFields([
              'status','modUid','updatedAt','bannedUserIds'
            ]));
      allow delete: if false;

      // ── /tournaments/{tournamentId}/participants/{uid} ──────────────
      match /participants/{participantId} {
        // Participants list is public (leaderboard display)
        allow read: if true;
        // Joins handled exclusively by joinTournament Cloud Function
        allow write: if false;
      }

      // ── /tournaments/{tournamentId}/roomInfo/{roomInfoId} ───────────
      match /roomInfo/{roomInfoId} {
        // CRITICAL: Room info only readable by checked-in participants
        // Cloud Function enforces this and returns via callable (not direct read)
        // Direct Firestore read restricted to mod/admin
        allow read: if isMod();
        allow write: if isMod();
      }

      // ── /tournaments/{tournamentId}/results/{uid} ───────────────────
      match /results/{resultId} {
        // Participants can read all results in their tournament
        allow read: if isAuth();

        // User can submit their own result once
        allow create: if isAuth()
          && notBanned()
          && incomingData().uid == uid()
          && resultId == uid()
          && incomingData().status == 'PENDING'
          && !existingData() == true; // no existing result

        // Only mod/admin can approve/reject
        allow update: if isMod()
          && fieldUnchanged('uid')
          && fieldUnchanged('tournamentId')
          && fieldUnchanged('kills')
          && fieldUnchanged('placement')
          && fieldUnchanged('proofStoragePath');

        allow delete: if false;
      }

      // ── /tournaments/{tournamentId}/disputes/{disputeId} ────────────
      match /disputes/{disputeId} {
        allow read: if isAuth()
          && (resource.data.raisedBy == uid() || isMod());

        allow create: if isAuth()
          && notBanned()
          && incomingData().raisedBy == uid()
          && incomingData().status == 'OPEN';

        allow update: if isMod();
        allow delete: if false;
      }
    }

    // ─── /withdrawals/{withdrawalId} ────────────────────────────────────

    match /withdrawals/{withdrawalId} {
      allow read: if isAuth()
        && (resource.data.uid == uid() || isAdmin());

      // User creates withdrawal request (Cloud Function is the preferred path)
      allow create: if isAuth()
        && notBanned()
        && incomingData().uid == uid()
        && incomingData().status == 'PENDING'
        && !incomingData().keys().hasAny(['approvedBy','approvedAt','paidBy','paidAt','paymentTxnRef']);

      // Only admin can approve/pay/reject
      allow update: if isAdmin()
        && fieldUnchanged('uid')
        && fieldUnchanged('requestedCoins')
        && fieldUnchanged('feeCoins')
        && fieldUnchanged('netCoins');

      allow delete: if false;
    }

    // ─── /leaderboards/{boardId} ────────────────────────────────────────

    match /leaderboards/{boardId} {
      allow read: if true; // Public leaderboards

      match /entries/{entryId} {
        allow read: if true;
        allow write: if false; // Only Cloud Functions write snapshots
      }

      allow write: if false;
    }

    // ─── /deviceResetRequests/{requestId} ───────────────────────────────

    match /deviceResetRequests/{requestId} {
      allow read: if isAuth()
        && (resource.data.uid == uid() || isAdmin());

      allow create: if isAuth()
        && incomingData().uid == uid()
        && incomingData().status == 'PENDING';

      allow update: if isAdmin();
      allow delete: if false;
    }

    // ─── /auditLogs/{logId} ─────────────────────────────────────────────

    match /auditLogs/{logId} {
      allow read: if isAdmin();
      // Append-only via Admin SDK in Cloud Functions only
      allow write: if false;
    }

    // ─── /appConfig/{configId} ──────────────────────────────────────────

    match /appConfig/{configId} {
      allow read: if isAuth(); // All users can read config (coin rate, packages)
      allow write: if isAdmin();
    }

    // ─── /deviceBindings/{bindingId} ────────────────────────────────────

    match /deviceBindings/{bindingId} {
      allow read: if isAdmin();
      allow write: if false; // Cloud Functions only
    }
  }
}
```

### 2.2 Firebase Storage Rules

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {

    function isAuth() {
      return request.auth != null;
    }

    function uid() {
      return request.auth.uid;
    }

    function isAdmin() {
      // Custom claim set by Cloud Function on role assignment
      return isAuth() && request.auth.token.role == 'admin';
    }

    function isMod() {
      return isAuth()
        && (request.auth.token.role == 'mod' || request.auth.token.role == 'admin');
    }

    function isValidImage() {
      return request.resource.contentType.matches('image/.*')
        && request.resource.size < 5 * 1024 * 1024; // 5MB max
    }

    // ── Topup Proof Uploads ──────────────────────────────────────────────
    // Path: topup_proofs/{orderId}
    match /topup_proofs/{orderId} {
      allow read: if isMod();
      allow write: if isAuth()
        && isValidImage()
        && orderId.matches('[a-zA-Z0-9_-]+');
    }

    // ── Result Proof Uploads ─────────────────────────────────────────────
    // Path: results/{tournamentId}/{uid}
    match /results/{tournamentId}/{userId} {
      allow read: if isMod();
      allow write: if isAuth()
        && uid() == userId
        && isValidImage();
    }

    // ── Dispute Evidence ─────────────────────────────────────────────────
    // Path: disputes/{tournamentId}/{disputeId}_{suffix}
    match /disputes/{tournamentId}/{filename} {
      allow read: if isMod();
      allow write: if isAuth()
        && isValidImage()
        && request.resource.size < 10 * 1024 * 1024; // 10MB for disputes
    }

    // ── Avatars (public) ─────────────────────────────────────────────────
    match /avatars/{avatarId} {
      allow read: if true;
      allow write: if isAdmin();
    }

    // Deny all other paths
    match /{allPaths=**} {
      allow read, write: if false;
    }
  }
}
```

---

## 3. Cloud Functions

### Setup & Shared Utilities

```typescript
// functions/src/index.ts
import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { onDocumentWritten } from "firebase-functions/v2/firestore";
import { defineSecret } from "firebase-functions/params";
import { DateTime } from "luxon";
import { RateLimiterMemory } from "rate-limiter-flexible";

admin.initializeApp();
const db = admin.firestore();
const storage = admin.storage();

// ─── Shared Types ──────────────────────────────────────────────────────────

type LedgerType =
  | "TOPUP" | "TOPUP_BONUS" | "JOIN_LOCK" | "JOIN_UNLOCK"
  | "PRIZE" | "WITHDRAWAL_LOCK" | "WITHDRAWAL_FEE"
  | "WITHDRAWAL_PAYOUT" | "WITHDRAWAL_UNLOCK"
  | "ADMIN_ADJUST" | "REFUND";

interface BalanceDoc {
  uid: string;
  availableCoins: number;
  lockedCoins: number;
  totalCoins: number;
  version: number;
  updatedAt: admin.firestore.Timestamp;
}

interface AppConfig {
  coinRate: number;
  coinPackages: CoinPackage[];
  withdrawalConfig: {
    minWithdrawCoins: number;
    weeklyCapCoins: number;
    feePercent: number;
    fixedFeeCoins: number;
    timezone: string;
  };
  suspiciousScoreThresholds: { review: number; autoban: number };
}

interface CoinPackage {
  packageId: string;
  baseCoins: number;
  bonusCoins: number;
  totalCoins: number;
  isActive: boolean;
}

// ─── Shared Utilities ─────────────────────────────────────────────────────

const MAX_RETRIES = 5;

async function getAppConfig(): Promise<AppConfig> {
  const snap = await db.collection("appConfig").doc("main").get();
  if (!snap.exists) throw new HttpsError("not-found", "App config missing");
  return snap.data() as AppConfig;
}

async function assertRole(
  uid: string,
  roles: string[]
): Promise<void> {
  const userSnap = await db.collection("users").doc(uid).get();
  if (!userSnap.exists) throw new HttpsError("not-found", "User not found");
  const user = userSnap.data()!;
  if (user.isBanned) throw new HttpsError("permission-denied", "User is banned");
  if (!roles.includes(user.role)) {
    throw new HttpsError("permission-denied", `Requires role: ${roles.join(" or ")}`);
  }
}

async function assertNotBanned(uid: string): Promise<void> {
  const userSnap = await db.collection("users").doc(uid).get();
  if (!userSnap.exists) throw new HttpsError("not-found", "User not found");
  if (userSnap.data()!.isBanned) {
    throw new HttpsError("permission-denied", "Account is banned");
  }
}

// Write an immutable audit log entry
async function writeAuditLog(
  t: admin.firestore.Transaction,
  data: {
    action: string;
    performedBy: string;
    targetUid?: string;
    targetCollection?: string;
    targetDocId?: string;
    before?: Record<string, unknown>;
    after?: Record<string, unknown>;
    metadata?: Record<string, unknown>;
  }
): Promise<void> {
  const logRef = db.collection("auditLogs").doc();
  t.set(logRef, {
    logId: logRef.id,
    ...data,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });
}

// Write an immutable wallet ledger entry within a transaction
function writeLedgerEntry(
  t: admin.firestore.Transaction,
  data: {
    uid: string;
    type: LedgerType;
    amount: number;
    balanceBefore: number;
    balanceAfter: number;
    lockedBefore: number;
    lockedAfter: number;
    refType: string | null;
    refId: string | null;
    note: string;
    createdBy: string;
    idempotencyKey: string;
  }
): void {
  const ledgerRef = db.collection("walletLedger").doc();
  t.set(ledgerRef, {
    ledgerId: ledgerRef.id,
    ...data,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });
}

// ─── Rate Limiters ────────────────────────────────────────────────────────

const joinLimiter = new RateLimiterMemory({
  points: 5,        // 5 join attempts
  duration: 60,     // per 60 seconds per user
});

const withdrawLimiter = new RateLimiterMemory({
  points: 3,
  duration: 3600,   // 3 attempts per hour
});

const topupLimiter = new RateLimiterMemory({
  points: 10,
  duration: 3600,
});

async function checkRateLimit(
  limiter: RateLimiterMemory,
  key: string,
  errorMsg: string
): Promise<void> {
  try {
    await limiter.consume(key);
  } catch {
    throw new HttpsError("resource-exhausted", errorMsg);
  }
}

// ─── Suspicious Score Helper ──────────────────────────────────────────────

async function incrementSuspiciousScore(
  uid: string,
  points: number,
  flag: string
): Promise<void> {
  const userRef = db.collection("users").doc(uid);
  await db.runTransaction(async (t) => {
    const snap = await t.get(userRef);
    const user = snap.data()!;
    const newScore = Math.min(100, (user.suspiciousScore || 0) + points);
    const flags = user.suspiciousFlags || [];
    if (!flags.includes(flag)) flags.push(flag);

    const config = await getAppConfig();
    const update: Record<string, unknown> = {
      suspiciousScore: newScore,
      suspiciousFlags: flags,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    };

    if (newScore >= config.suspiciousScoreThresholds.autoban) {
      update.isBanned = true;
      update.banReason = "auto_suspicious_score";
    }

    t.update(userRef, update);
  });
}
```

---

### 3.1 `createTopupOrder`

```typescript
// ─── createTopupOrder ─────────────────────────────────────────────────────
// Called by user to initiate a coin purchase

export const createTopupOrder = onCall(
  { region: "asia-south1" },
  async (request) => {
    const uid = request.auth?.uid;
    if (!uid) throw new HttpsError("unauthenticated", "Login required");

    await checkRateLimit(topupLimiter, uid, "Too many topup requests. Try later.");
    await assertNotBanned(uid);

    const { packageId, paymentMethod, paymentReference, proofStoragePath } =
      request.data as {
        packageId: string;
        paymentMethod: string;
        paymentReference: string;
        proofStoragePath: string;
      };

    // Validate inputs
    if (!packageId || !paymentMethod || !paymentReference || !proofStoragePath) {
      throw new HttpsError("invalid-argument", "Missing required fields");
    }

    // Load config
    const config = await getAppConfig();
    const pkg = config.coinPackages.find(
      (p) => p.packageId === packageId && p.isActive
    );
    if (!pkg) throw new HttpsError("not-found", `Package ${packageId} not found`);

    // Idempotency: prevent duplicate orders within same minute
    const minuteKey = Math.floor(Date.now() / 60000);
    const idempotencyKey = `topup:${uid}:${packageId}:${minuteKey}`;

    const existingQuery = await db
      .collection("topupOrders")
      .where("idempotencyKey", "==", idempotencyKey)
      .limit(1)
      .get();

    if (!existingQuery.empty) {
      return { orderId: existingQuery.docs[0].id, duplicate: true };
    }

    const pkrAmount = pkg.baseCoins * config.coinRate;
    const orderRef = db.collection("topupOrders").doc();

    await orderRef.set({
      orderId: orderRef.id,
      uid,
      packageId,
      baseCoins: pkg.baseCoins,
      bonusCoins: pkg.bonusCoins,
      totalCoins: pkg.totalCoins,
      pkrAmount,
      coinRate: config.coinRate,
      paymentMethod,
      paymentReference,
      proofStoragePath,
      status: "PENDING",
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      idempotencyKey,
    });

    return { orderId: orderRef.id, pkrAmount, totalCoins: pkg.totalCoins };
  }
);
```

---

### 3.2 `approveTopupOrder`

```typescript
// ─── approveTopupOrder ────────────────────────────────────────────────────
// Admin/mod approves a pending topup — credits coins with ledger entries

export const approveTopupOrder = onCall(
  { region: "asia-south1" },
  async (request) => {
    const adminUid = request.auth?.uid;
    if (!adminUid) throw new HttpsError("unauthenticated", "Login required");
    await assertRole(adminUid, ["admin", "mod"]);

    const { orderId, action, rejectReason } = request.data as {
      orderId: string;
      action: "APPROVED" | "REJECTED";
      rejectReason?: string;
    };

    if (!orderId || !action) {
      throw new HttpsError("invalid-argument", "orderId and action required");
    }

    const orderRef = db.collection("topupOrders").doc(orderId);

    await db.runTransaction(async (t) => {
      const orderSnap = await t.get(orderRef);
      if (!orderSnap.exists) throw new HttpsError("not-found", "Order not found");

      const order = orderSnap.data()!;
      if (order.status !== "PENDING") {
        throw new HttpsError("failed-precondition", `Order is already ${order.status}`);
      }

      // Check idempotency — prevent double approval
      const idempotencyKey = `topup:${orderId}`;
      const existingLedger = await db
        .collection("walletLedger")
        .where("idempotencyKey", "==", idempotencyKey)
        .limit(1)
        .get();

      if (!existingLedger.empty) {
        throw new HttpsError("already-exists", "Order already processed");
      }

      if (action === "APPROVED") {
        const balanceRef = db.collection("balances").doc(order.uid);
        const balanceSnap = await t.get(balanceRef);

        let currentBalance: BalanceDoc;
        if (!balanceSnap.exists) {
          // Initialize balance doc if first topup
          currentBalance = {
            uid: order.uid,
            availableCoins: 0,
            lockedCoins: 0,
            totalCoins: 0,
            version: 0,
            updatedAt: admin.firestore.FieldValue.serverTimestamp() as any,
          };
        } else {
          currentBalance = balanceSnap.data() as BalanceDoc;
        }

        const newAvailable = currentBalance.availableCoins + order.totalCoins;
        const newTotal = newAvailable + currentBalance.lockedCoins;

        // Update balance atomically
        t.set(balanceRef, {
          uid: order.uid,
          availableCoins: newAvailable,
          lockedCoins: currentBalance.lockedCoins,
          totalCoins: newTotal,
          version: admin.firestore.FieldValue.increment(1),
          updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        });

        // Write single combined ledger entry (base + bonus are one topup action)
        writeLedgerEntry(t, {
          uid: order.uid,
          type: "TOPUP",
          amount: order.totalCoins,
          balanceBefore: currentBalance.availableCoins,
          balanceAfter: newAvailable,
          lockedBefore: currentBalance.lockedCoins,
          lockedAfter: currentBalance.lockedCoins,
          refType: "topupOrder",
          refId: orderId,
          note: `Topup ${order.baseCoins} coins + ${order.bonusCoins} bonus = ${order.totalCoins} coins`,
          createdBy: adminUid,
          idempotencyKey,
        });

        // Update order status
        t.update(orderRef, {
          status: "APPROVED",
          reviewedBy: adminUid,
          reviewedAt: admin.firestore.FieldValue.serverTimestamp(),
          updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        });

      } else {
        // REJECTED
        t.update(orderRef, {
          status: "REJECTED",
          reviewedBy: adminUid,
          reviewedAt: admin.firestore.FieldValue.serverTimestamp(),
          rejectReason: rejectReason || "Not approved",
          updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        });
      }

      // Audit log
      await writeAuditLog(t, {
        action: action === "APPROVED" ? "APPROVE_TOPUP" : "REJECT_TOPUP",
        performedBy: adminUid,
        targetUid: order.uid,
        targetCollection: "topupOrders",
        targetDocId: orderId,
        after: { status: action, totalCoins: order.totalCoins },
        metadata: { rejectReason },
      });
    });

    return { success: true, action };
  }
);
```

---

### 3.3 `joinTournament`

```typescript
// ─── joinTournament ───────────────────────────────────────────────────────
// Atomically: validate entry fee, lock coins, add participant, update slot count

export const joinTournament = onCall(
  { region: "asia-south1" },
  async (request) => {
    const uid = request.auth?.uid;
    if (!uid) throw new HttpsError("unauthenticated", "Login required");

    await checkRateLimit(joinLimiter, uid, "Too many join attempts. Slow down.");
    await assertNotBanned(uid);

    const { tournamentId } = request.data as { tournamentId: string };
    if (!tournamentId) throw new HttpsError("invalid-argument", "tournamentId required");

    const idempotencyKey = `join:${uid}:${tournamentId}`;

    // Check if already joined (outside transaction for performance)
    const participantRef = db
      .collection("tournaments")
      .doc(tournamentId)
      .collection("participants")
      .doc(uid);
    const existingSnap = await participantRef.get();
    if (existingSnap.exists) {
      throw new HttpsError("already-exists", "Already joined this tournament");
    }

    let retries = 0;
    while (retries < MAX_RETRIES) {
      try {
        const result = await db.runTransaction(async (t) => {
          // Read all docs needed
          const tournamentRef = db.collection("tournaments").doc(tournamentId);
          const balanceRef = db.collection("balances").doc(uid);
          const userRef = db.collection("users").doc(uid);

          const [tournamentSnap, balanceSnap, userSnap, participantSnap] =
            await Promise.all([
              t.get(tournamentRef),
              t.get(balanceRef),
              t.get(userRef),
              t.get(participantRef),
            ]);

          // Validate tournament
          if (!tournamentSnap.exists) {
            throw new HttpsError("not-found", "Tournament not found");
          }
          const tournament = tournamentSnap.data()!;
          if (tournament.status !== "OPEN") {
            throw new HttpsError(
              "failed-precondition",
              `Tournament is ${tournament.status}, not open for joining`
            );
          }
          if (tournament.filledSlots >= tournament.maxSlots) {
            throw new HttpsError("resource-exhausted", "Tournament is full");
          }
          if (tournament.bannedUserIds?.includes(uid)) {
            throw new HttpsError("permission-denied", "You are banned from this tournament");
          }

          // Double-check participant (within transaction)
          if (participantSnap.exists) {
            throw new HttpsError("already-exists", "Already joined this tournament");
          }

          // Validate balance
          if (!balanceSnap.exists) {
            throw new HttpsError("failed-precondition", "No balance account found");
          }
          const balance = balanceSnap.data() as BalanceDoc;
          if (balance.availableCoins < tournament.entryFeeCoins) {
            throw new HttpsError(
              "failed-precondition",
              `Insufficient coins. Need ${tournament.entryFeeCoins}, have ${balance.availableCoins}`
            );
          }

          const user = userSnap.data()!;
          const newAvailable = balance.availableCoins - tournament.entryFeeCoins;
          const newLocked = balance.lockedCoins + tournament.entryFeeCoins;
          const slotNumber = tournament.filledSlots + 1;

          // Update balance: deduct available, add to locked
          t.update(balanceRef, {
            availableCoins: newAvailable,
            lockedCoins: newLocked,
            totalCoins: newAvailable + newLocked, // total unchanged
            version: admin.firestore.FieldValue.increment(1),
            updatedAt: admin.firestore.FieldValue.serverTimestamp(),
          });

          // Write ledger: JOIN_LOCK
          writeLedgerEntry(t, {
            uid,
            type: "JOIN_LOCK",
            amount: -tournament.entryFeeCoins,
            balanceBefore: balance.availableCoins,
            balanceAfter: newAvailable,
            lockedBefore: balance.lockedCoins,
            lockedAfter: newLocked,
            refType: "tournament",
            refId: tournamentId,
            note: `Entry fee locked for tournament: ${tournament.title}`,
            createdBy: uid,
            idempotencyKey,
          });

          // Add participant
          t.set(participantRef, {
            uid,
            tournamentId,
            name: user.name,
            freeFireUid: user.freeFireUid,
            avatarId: user.avatarId,
            region: user.region,
            status: "JOINED",
            slotNumber,
            coinsLocked: tournament.entryFeeCoins,
            joinedAt: admin.firestore.FieldValue.serverTimestamp(),
            deviceId: user.deviceId,
          });

          // Increment filled slots; auto-close if full
          const newFilledSlots = tournament.filledSlots + 1;
          t.update(tournamentRef, {
            filledSlots: newFilledSlots,
            status: newFilledSlots >= tournament.maxSlots ? "FULL" : "OPEN",
            updatedAt: admin.firestore.FieldValue.serverTimestamp(),
          });

          return { slotNumber, newAvailable };
        });

        return {
          success: true,
          slotNumber: result.slotNumber,
          remainingCoins: result.newAvailable,
        };
      } catch (error: any) {
        // Retry only on contention errors
        if (error.code === 10 && retries < MAX_RETRIES - 1) {
          retries++;
          await new Promise((r) => setTimeout(r, 100 * Math.pow(2, retries)));
          continue;
        }
        throw error;
      }
    }

    throw new HttpsError("aborted", "Transaction failed after retries. Try again.");
  }
);
```

---

### 3.4 `checkInTournament`

```typescript
// ─── checkInTournament ────────────────────────────────────────────────────
// Player confirms attendance during check-in window

export const checkInTournament = onCall(
  { region: "asia-south1" },
  async (request) => {
    const uid = request.auth?.uid;
    if (!uid) throw new HttpsError("unauthenticated", "Login required");
    await assertNotBanned(uid);

    const { tournamentId } = request.data as { tournamentId: string };

    await db.runTransaction(async (t) => {
      const tournamentRef = db.collection("tournaments").doc(tournamentId);
      const participantRef = tournamentRef.collection("participants").doc(uid);

      const [tournamentSnap, participantSnap] = await Promise.all([
        t.get(tournamentRef),
        t.get(participantRef),
      ]);

      if (!tournamentSnap.exists) throw new HttpsError("not-found", "Tournament not found");
      const tournament = tournamentSnap.data()!;

      // Validate check-in window
      const now = admin.firestore.Timestamp.now().toMillis();
      const checkInOpen = tournament.checkInOpenAt.toMillis();
      const checkInClose = tournament.checkInCloseAt.toMillis();

      if (now < checkInOpen) {
        throw new HttpsError("failed-precondition", "Check-in not open yet");
      }
      if (now > checkInClose) {
        throw new HttpsError("failed-precondition", "Check-in window closed");
      }
      if (!["OPEN", "FULL", "CHECKIN"].includes(tournament.status)) {
        throw new HttpsError("failed-precondition", `Cannot check in: tournament is ${tournament.status}`);
      }

      if (!participantSnap.exists) {
        throw new HttpsError("not-found", "You are not a participant");
      }
      const participant = participantSnap.data()!;
      if (participant.status === "CHECKED_IN") {
        return; // Already checked in — idempotent
      }
      if (participant.status !== "JOINED") {
        throw new HttpsError("failed-precondition", `Cannot check in: status is ${participant.status}`);
      }

      t.update(participantRef, {
        status: "CHECKED_IN",
        checkedInAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      // Move tournament to CHECKIN status if not already
      if (tournament.status !== "CHECKIN") {
        t.update(tournamentRef, {
          status: "CHECKIN",
          updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        });
      }
    });

    return { success: true };
  }
);
```

---

### 3.5 `scheduledRoomReveal` (Scheduled Function)

```typescript
// ─── scheduledRoomReveal ──────────────────────────────────────────────────
// Runs every minute; reveals room info for tournaments at their reveal time
// Room info must be pre-set by admin/mod before scheduledAt

export const scheduledRoomReveal = onSchedule(
  { schedule: "every 1 minutes", region: "asia-south1", timeZone: "Asia/Karachi" },
  async () => {
    const now = admin.firestore.Timestamp.now();
    // Find tournaments whose roomRevealAt has passed but still in CHECKIN/FULL status
    const tournamentsSnap = await db
      .collection("tournaments")
      .where("status", "in", ["CHECKIN", "FULL"])
      .where("roomRevealAt", "<=", now)
      .get();

    const batch = db.batch();
    for (const doc of tournamentsSnap.docs) {
      const tournament = doc.data();

      // Validate room info exists before revealing
      const roomInfoRef = doc.ref.collection("roomInfo").doc(doc.id);
      const roomInfoSnap = await roomInfoRef.get();
      if (!roomInfoSnap.exists) {
        console.warn(`Tournament ${doc.id}: No room info set, skipping reveal`);
        continue;
      }

      // Mark no-shows: participants who didn't check in lose their locked coins
      const participantsSnap = await doc.ref
        .collection("participants")
        .where("status", "==", "JOINED") // JOINED but not CHECKED_IN = no show
        .get();

      for (const pDoc of participantsSnap.docs) {
        const participant = pDoc.data();
        batch.update(pDoc.ref, { status: "NO_SHOW" });

        // Unlock coins for no-shows? Policy decision:
        // Option A: Forfeit entry fee (no refund)
        // Option B: Refund no-shows
        // Implementing Option A (forfeit) — coins remain locked, moved to platform
        // This is handled in settleTournament
      }

      batch.update(doc.ref, {
        status: "LIVE",
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      console.log(`Room revealed for tournament: ${doc.id}`);
    }

    await batch.commit();
  }
);

// ─── getRoomInfo (Callable) ───────────────────────────────────────────────
// Checked-in participants call this to get room credentials
// NOT a direct Firestore read (enforces business logic)

export const getRoomInfo = onCall(
  { region: "asia-south1" },
  async (request) => {
    const uid = request.auth?.uid;
    if (!uid) throw new HttpsError("unauthenticated", "Login required");
    await assertNotBanned(uid);

    const { tournamentId } = request.data as { tournamentId: string };

    const [tournamentSnap, participantSnap] = await Promise.all([
      db.collection("tournaments").doc(tournamentId).get(),
      db.collection("tournaments").doc(tournamentId)
        .collection("participants").doc(uid).get(),
    ]);

    if (!tournamentSnap.exists) throw new HttpsError("not-found", "Tournament not found");
    const tournament = tournamentSnap.data()!;

    if (!["LIVE", "RESULT_PENDING"].includes(tournament.status)) {
      throw new HttpsError("failed-precondition", "Room info not available yet");
    }

    if (!participantSnap.exists) {
      throw new HttpsError("permission-denied", "Not a participant");
    }
    const participant = participantSnap.data()!;

    if (!["CHECKED_IN", "COMPLETED"].includes(participant.status)) {
      throw new HttpsError(
        "permission-denied",
        "Must be checked in to view room info"
      );
    }

    // Log access attempt
    console.log(`Room info accessed by ${uid} for tournament ${tournamentId}`);

    const roomInfoSnap = await db
      .collection("tournaments")
      .doc(tournamentId)
      .collection("roomInfo")
      .doc(tournamentId)
      .get();

    if (!roomInfoSnap.exists) {
      throw new HttpsError("not-found", "Room info not set");
    }

    const roomInfo = roomInfoSnap.data()!;
    return {
      roomId: roomInfo.roomId,
      roomPassword: roomInfo.roomPassword,
    };
  }
);
```

---

### 3.6 `submitResult`

```typescript
// ─── submitResult ─────────────────────────────────────────────────────────
// Player submits kills + placement + proof screenshot

export const submitResult = onCall(
  { region: "asia-south1" },
  async (request) => {
    const uid = request.auth?.uid;
    if (!uid) throw new HttpsError("unauthenticated", "Login required");
    await assertNotBanned(uid);

    const { tournamentId, kills, placement, proofStoragePath } = request.data as {
      tournamentId: string;
      kills: number;
      placement: number;
      proofStoragePath: string;
    };

    // Input validation
    if (!tournamentId || !proofStoragePath) {
      throw new HttpsError("invalid-argument", "Missing required fields");
    }
    if (typeof kills !== "number" || kills < 0 || kills > 60) {
      throw new HttpsError("invalid-argument", "Invalid kills value");
    }
    if (typeof placement !== "number" || placement < 1 || placement > 100) {
      throw new HttpsError("invalid-argument", "Invalid placement value");
    }

    const tournamentRef = db.collection("tournaments").doc(tournamentId);
    const resultRef = tournamentRef.collection("results").doc(uid);
    const participantRef = tournamentRef.collection("participants").doc(uid);

    await db.runTransaction(async (t) => {
      const [tournamentSnap, participantSnap, resultSnap] = await Promise.all([
        t.get(tournamentRef),
        t.get(participantRef),
        t.get(resultRef),
      ]);

      if (!tournamentSnap.exists) throw new HttpsError("not-found", "Tournament not found");
      const tournament = tournamentSnap.data()!;

      // Must be LIVE or allow late submission until deadline
      if (!["LIVE", "RESULT_PENDING"].includes(tournament.status)) {
        throw new HttpsError("failed-precondition", `Cannot submit result: tournament is ${tournament.status}`);
      }

      // Check deadline
      if (tournament.resultSubmitDeadline.toMillis() < Date.now()) {
        throw new HttpsError("deadline-exceeded", "Result submission deadline passed");
      }

      if (!participantSnap.exists) {
        throw new HttpsError("permission-denied", "Not a participant");
      }
      const participant = participantSnap.data()!;
      if (!["CHECKED_IN", "COMPLETED"].includes(participant.status)) {
        throw new HttpsError("permission-denied", "Must be checked in to submit result");
      }

      if (resultSnap.exists && resultSnap.data()!.status === "APPROVED") {
        throw new HttpsError("already-exists", "Result already approved");
      }

      // Scoring formula: placement_score + kill_score
      // Higher placement = lower number (1st = best)
      const placementScore = Math.max(0, (100 - placement) * 10);
      const killScore = kills * 5;
      const score = placementScore + killScore;

      // Validate proof file exists in Storage
      const proofFile = storage.bucket().file(proofStoragePath);
      const [exists] = await proofFile.exists();
      if (!exists) {
        throw new HttpsError("failed-precondition", "Proof file not found in storage");
      }

      const userSnap = await t.get(db.collection("users").doc(uid));
      const user = userSnap.data()!;

      t.set(resultRef, {
        uid,
        tournamentId,
        freeFireUid: user.freeFireUid,
        kills,
        placement,
        score,
        proofStoragePath,
        proofUploadedAt: admin.firestore.FieldValue.serverTimestamp(),
        status: "PENDING",
        submittedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      t.update(participantRef, {
        status: "COMPLETED",
      });

      // Move tournament to result_pending if still LIVE
      if (tournament.status === "LIVE") {
        t.update(tournamentRef, {
          status: "RESULT_PENDING",
          updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        });
      }
    });

    return { success: true };
  }
);
```

---

### 3.7 `approveResult` / `rejectResult`

```typescript
// ─── approveResult / rejectResult ─────────────────────────────────────────
// Mod reviews submitted results

export const reviewResult = onCall(
  { region: "asia-south1" },
  async (request) => {
    const modUid = request.auth?.uid;
    if (!modUid) throw new HttpsError("unauthenticated", "Login required");
    await assertRole(modUid, ["admin", "mod"]);

    const { tournamentId, targetUid, action, rejectReason } = request.data as {
      tournamentId: string;
      targetUid: string;
      action: "APPROVED" | "REJECTED";
      rejectReason?: string;
    };

    const resultRef = db
      .collection("tournaments")
      .doc(tournamentId)
      .collection("results")
      .doc(targetUid);

    await db.runTransaction(async (t) => {
      const resultSnap = await t.get(resultRef);
      if (!resultSnap.exists) throw new HttpsError("not-found", "Result not found");
      const result = resultSnap.data()!;

      if (result.status !== "PENDING") {
        throw new HttpsError("failed-precondition", `Result already ${result.status}`);
      }

      t.update(resultRef, {
        status: action,
        reviewedBy: modUid,
        reviewedAt: admin.firestore.FieldValue.serverTimestamp(),
        ...(action === "REJECTED" && { rejectReason: rejectReason || "Not approved" }),
      });

      await writeAuditLog(t, {
        action: action === "APPROVED" ? "APPROVE_RESULT" : "REJECT_RESULT",
        performedBy: modUid,
        targetUid,
        targetCollection: "results",
        targetDocId: `${tournamentId}/${targetUid}`,
        before: { status: "PENDING" },
        after: { status: action },
        metadata: { rejectReason },
      });
    });

    return { success: true };
  }
);
```

---

### 3.8 `settleTournament`

```typescript
// ─── settleTournament ─────────────────────────────────────────────────────
// Admin triggers prize distribution after all results approved
// Unlocks locked coins, distributes prizes, writes ledger entries

export const settleTournament = onCall(
  { region: "asia-south1" },
  async (request) => {
    const adminUid = request.auth?.uid;
    if (!adminUid) throw new HttpsError("unauthenticated", "Login required");
    await assertRole(adminUid, ["admin"]);

    const { tournamentId } = request.data as { tournamentId: string };

    const tournamentRef = db.collection("tournaments").doc(tournamentId);
    const tournamentSnap = await tournamentRef.get();
    if (!tournamentSnap.exists) throw new HttpsError("not-found", "Tournament not found");
    const tournament = tournamentSnap.data()!;

    if (!["RESULT_PENDING", "LIVE"].includes(tournament.status)) {
      throw new HttpsError(
        "failed-precondition",
        `Cannot settle tournament with status: ${tournament.status}`
      );
    }

    // Fetch all approved results, sorted by score descending
    const resultsSnap = await tournamentRef
      .collection("results")
      .where("status", "==", "APPROVED")
      .orderBy("score", "desc")
      .get();

    // Assign ranks by score (handle ties with same rank)
    const rankedResults: Array<{ uid: string; rank: number; kills: number; placement: number; score: number }> = [];
    let currentRank = 1;
    let prevScore = -1;

    for (const doc of resultsSnap.docs) {
      const result = doc.data();
      const rank = result.score === prevScore ? currentRank : currentRank;
      rankedResults.push({ uid: doc.id, rank, kills: result.kills, placement: result.placement, score: result.score });
      prevScore = result.score;
      currentRank++;
    }

    // Map prize tiers
    const prizeMap = new Map<number, number>();
    for (const tier of tournament.prizeDistribution) {
      prizeMap.set(tier.rank, tier.coins);
    }

    // Fetch all participants (for no-shows and non-submitters)
    const participantsSnap = await tournamentRef.collection("participants").get();
    const allParticipants = participantsSnap.docs.map((d) => d.data());

    // Process in batches (Firestore transaction limit: 500 ops)
    // Use multiple transactions for large tournaments
    const BATCH_SIZE = 50;

    // Process prize winners
    for (let i = 0; i < rankedResults.length; i += BATCH_SIZE) {
      const batch = rankedResults.slice(i, i + BATCH_SIZE);

      await db.runTransaction(async (t) => {
        for (const result of batch) {
          const prizeCoins = prizeMap.get(result.rank) || 0;
          const participantRef = tournamentRef.collection("participants").doc(result.uid);
          const balanceRef = db.collection("balances").doc(result.uid);

          const [participantSnap, balanceSnap] = await Promise.all([
            t.get(participantRef),
            t.get(balanceRef),
          ]);

          const participant = participantSnap.data()!;
          const balance = balanceSnap.data() as BalanceDoc;

          const entryFee = participant.coinsLocked;
          const newLocked = Math.max(0, balance.lockedCoins - entryFee);
          const newAvailable = balance.availableCoins + entryFee + prizeCoins;

          // Unlock entry fee + add prize
          t.update(balanceRef, {
            availableCoins: newAvailable,
            lockedCoins: newLocked,
            totalCoins: newAvailable + newLocked,
            version: admin.firestore.FieldValue.increment(1),
            updatedAt: admin.firestore.FieldValue.serverTimestamp(),
          });

          // Ledger: unlock entry fee
          writeLedgerEntry(t, {
            uid: result.uid,
            type: "JOIN_UNLOCK",
            amount: entryFee,
            balanceBefore: balance.availableCoins,
            balanceAfter: balance.availableCoins + entryFee,
            lockedBefore: balance.lockedCoins,
            lockedAfter: newLocked,
            refType: "tournament",
            refId: tournamentId,
            note: `Entry fee unlocked after tournament: ${tournament.title}`,
            createdBy: "system",
            idempotencyKey: `settle:unlock:${tournamentId}:${result.uid}`,
          });

          // Ledger: prize (if any)
          if (prizeCoins > 0) {
            writeLedgerEntry(t, {
              uid: result.uid,
              type: "PRIZE",
              amount: prizeCoins,
              balanceBefore: balance.availableCoins + entryFee,
              balanceAfter: newAvailable,
              lockedBefore: newLocked,
              lockedAfter: newLocked,
              refType: "tournament",
              refId: tournamentId,
              note: `Prize for rank #${result.rank} in ${tournament.title}`,
              createdBy: "system",
              idempotencyKey: `settle:prize:${tournamentId}:${result.uid}`,
            });
          }

          t.update(participantRef, { status: "COMPLETED" });
        }
      });
    }

    // Process no-shows and non-submitters (forfeit entry fee)
    const winnerUids = new Set(rankedResults.map((r) => r.uid));
    const forfeitedParticipants = allParticipants.filter(
      (p) => !winnerUids.has(p.uid) && p.status !== "DISQUALIFIED"
    );

    for (let i = 0; i < forfeitedParticipants.length; i += BATCH_SIZE) {
      const batch = forfeitedParticipants.slice(i, i + BATCH_SIZE);
      await db.runTransaction(async (t) => {
        for (const participant of batch) {
          const balanceRef = db.collection("balances").doc(participant.uid);
          const balanceSnap = await t.get(balanceRef);
          const balance = balanceSnap.data() as BalanceDoc;

          const entryFee = participant.coinsLocked;
          const newLocked = Math.max(0, balance.lockedCoins - entryFee);
          // No refund for no-shows — coins removed from locked (platform keeps)

          t.update(balanceRef, {
            lockedCoins: newLocked,
            totalCoins: balance.availableCoins + newLocked,
            version: admin.firestore.FieldValue.increment(1),
            updatedAt: admin.firestore.FieldValue.serverTimestamp(),
          });

          writeLedgerEntry(t, {
            uid: participant.uid,
            type: "REFUND", // type FORFEIT could be added
            amount: -entryFee,
            balanceBefore: balance.availableCoins,
            balanceAfter: balance.availableCoins,
            lockedBefore: balance.lockedCoins,
            lockedAfter: newLocked,
            refType: "tournament",
            refId: tournamentId,
            note: `Entry fee forfeited (no-show/no-result) for ${tournament.title}`,
            createdBy: "system",
            idempotencyKey: `settle:forfeit:${tournamentId}:${participant.uid}`,
          });
        }
      });
    }

    // Mark tournament as COMPLETED
    await tournamentRef.update({
      status: "COMPLETED",
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    // Write audit log
    const auditRef = db.collection("auditLogs").doc();
    await auditRef.set({
      logId: auditRef.id,
      action: "SETTLE_TOURNAMENT",
      performedBy: adminUid,
      targetCollection: "tournaments",
      targetDocId: tournamentId,
      metadata: {
        winnersCount: rankedResults.length,
        totalPrizeDistributed: rankedResults.reduce(
          (sum, r) => sum + (prizeMap.get(r.rank) || 0), 0
        ),
      },
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    return {
      success: true,
      winnersCount: rankedResults.length,
      forfeitsCount: forfeitedParticipants.length,
    };
  }
);
```

---

### 3.9 `createWithdrawalRequest`

```typescript
// ─── createWithdrawalRequest ──────────────────────────────────────────────
// Validates weekly cap, computes fee, locks coins

export const createWithdrawalRequest = onCall(
  { region: "asia-south1" },
  async (request) => {
    const uid = request.auth?.uid;
    if (!uid) throw new HttpsError("unauthenticated", "Login required");

    await checkRateLimit(withdrawLimiter, uid, "Too many withdrawal requests");
    await assertNotBanned(uid);

    const { requestedCoins, paymentMethod, accountNumber } = request.data as {
      requestedCoins: number;
      paymentMethod: string;
      accountNumber: string;
    };

    if (!requestedCoins || !paymentMethod || !accountNumber) {
      throw new HttpsError("invalid-argument", "Missing required fields");
    }

    const config = await getAppConfig();
    const { minWithdrawCoins, weeklyCapCoins, feePercent, fixedFeeCoins, timezone } =
      config.withdrawalConfig;

    // Validate minimum
    if (requestedCoins < minWithdrawCoins) {
      throw new HttpsError(
        "invalid-argument",
        `Minimum withdrawal is ${minWithdrawCoins} coins`
      );
    }

    // Compute current week window (Monday 00:00 Asia/Karachi)
    const nowKarachi = DateTime.now().setZone(timezone);
    const weekStart = nowKarachi.startOf("week").startOf("day"); // Luxon weeks start Monday
    const weekEnd = weekStart.plus({ weeks: 1 });
    const weekStartTimestamp = admin.firestore.Timestamp.fromMillis(
      weekStart.toMillis()
    );
    const weekEndTimestamp = admin.firestore.Timestamp.fromMillis(weekEnd.toMillis());

    // Generate nonce for idempotency
    const nonce = Math.random().toString(36).substring(2, 8);
    const idempotencyKey = `withdraw:${uid}:${weekStart.toISODate()}:${requestedCoins}:${nonce}`;

    const withdrawalRef = db.collection("withdrawals").doc();
    const balanceRef = db.collection("balances").doc(uid);

    await db.runTransaction(async (t) => {
      // Check weekly usage within transaction
      const weeklyWithdrawalsSnap = await db
        .collection("withdrawals")
        .where("uid", "==", uid)
        .where("status", "in", ["PENDING", "APPROVED", "PAID"])
        .where("createdAt", ">=", weekStartTimestamp)
        .where("createdAt", "<", weekEndTimestamp)
        .get();

      const weeklyUsed = weeklyWithdrawalsSnap.docs.reduce(
        (sum, d) => sum + d.data().requestedCoins, 0
      );

      if (weeklyUsed + requestedCoins > weeklyCapCoins) {
        const remaining = weeklyCapCoins - weeklyUsed;
        throw new HttpsError(
          "resource-exhausted",
          `Weekly withdrawal cap exceeded. You can withdraw ${remaining} more coins this week.`
        );
      }

      // Read balance
      const balanceSnap = await t.get(balanceRef);
      if (!balanceSnap.exists) {
        throw new HttpsError("failed-precondition", "No balance account");
      }
      const balance = balanceSnap.data() as BalanceDoc;

      if (balance.availableCoins < requestedCoins) {
        throw new HttpsError(
          "failed-precondition",
          `Insufficient balance. Available: ${balance.availableCoins}`
        );
      }

      // Compute fee
      const percentFee = Math.ceil((requestedCoins * feePercent) / 100);
      const feeCoins = percentFee + fixedFeeCoins;
      const netCoins = requestedCoins - feeCoins;

      if (netCoins <= 0) {
        throw new HttpsError("invalid-argument", "Fee exceeds withdrawal amount");
      }

      const netPkr = netCoins * config.coinRate;

      // Lock coins (move from available to locked)
      const newAvailable = balance.availableCoins - requestedCoins;
      const newLocked = balance.lockedCoins + requestedCoins;

      t.update(balanceRef, {
        availableCoins: newAvailable,
        lockedCoins: newLocked,
        totalCoins: newAvailable + newLocked,
        version: admin.firestore.FieldValue.increment(1),
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      // Ledger: lock for withdrawal
      writeLedgerEntry(t, {
        uid,
        type: "WITHDRAWAL_LOCK",
        amount: -requestedCoins,
        balanceBefore: balance.availableCoins,
        balanceAfter: newAvailable,
        lockedBefore: balance.lockedCoins,
        lockedAfter: newLocked,
        refType: "withdrawal",
        refId: withdrawalRef.id,
        note: `Withdrawal request locked: ${requestedCoins} coins`,
        createdBy: uid,
        idempotencyKey: `wlock:${withdrawalRef.id}`,
      });

      // Create withdrawal doc
      t.set(withdrawalRef, {
        withdrawalId: withdrawalRef.id,
        uid,
        requestedCoins,
        feeCoins,
        netCoins,
        feePercent,
        fixedFeeCoins,
        coinRate: config.coinRate,
        netPkr,
        paymentMethod,
        accountNumber,
        status: "PENDING",
        weeklyCapSnapshot: weeklyCapCoins,
        weeklyUsedBefore: weeklyUsed,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        idempotencyKey,
      });
    });

    return { success: true, withdrawalId: withdrawalRef.id };
  }
);
```

---

### 3.10 `approveWithdrawal` / `markWithdrawalPaid` / `rejectWithdrawal`

```typescript
// ─── approveWithdrawal ────────────────────────────────────────────────────

export const approveWithdrawal = onCall(
  { region: "asia-south1" },
  async (request) => {
    const adminUid = request.auth?.uid;
    if (!adminUid) throw new HttpsError("unauthenticated", "Login required");
    await assertRole(adminUid, ["admin"]);

    const { withdrawalId } = request.data as { withdrawalId: string };
    const withdrawalRef = db.collection("withdrawals").doc(withdrawalId);

    await db.runTransaction(async (t) => {
      const snap = await t.get(withdrawalRef);
      if (!snap.exists) throw new HttpsError("not-found", "Withdrawal not found");
      const withdrawal = snap.data()!;

      if (withdrawal.status !== "PENDING") {
        throw new HttpsError("failed-precondition", `Status is ${withdrawal.status}`);
      }

      t.update(withdrawalRef, {
        status: "APPROVED",
        approvedBy: adminUid,
        approvedAt: admin.firestore.FieldValue.serverTimestamp(),
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      await writeAuditLog(t, {
        action: "APPROVE_WITHDRAWAL",
        performedBy: adminUid,
        targetUid: withdrawal.uid,
        targetDocId: withdrawalId,
        after: { status: "APPROVED" },
      });
    });

    return { success: true };
  }
);

// ─── markWithdrawalPaid ───────────────────────────────────────────────────

export const markWithdrawalPaid = onCall(
  { region: "asia-south1" },
  async (request) => {
    const adminUid = request.auth?.uid;
    if (!adminUid) throw new HttpsError("unauthenticated", "Login required");
    await assertRole(adminUid, ["admin"]);

    const { withdrawalId, paymentTxnRef } = request.data as {
      withdrawalId: string;
      paymentTxnRef: string;
    };

    const withdrawalRef = db.collection("withdrawals").doc(withdrawalId);

    await db.runTransaction(async (t) => {
      const snap = await t.get(withdrawalRef);
      if (!snap.exists) throw new HttpsError("not-found", "Withdrawal not found");
      const withdrawal = snap.data()!;

      if (withdrawal.status !== "APPROVED") {
        throw new HttpsError("failed-precondition", "Withdrawal must be APPROVED first");
      }

      const idempotencyKey = `wpaid:${withdrawalId}`;
      const existingLedger = await db
        .collection("walletLedger")
        .where("idempotencyKey", "==", idempotencyKey)
        .limit(1)
        .get();
      if (!existingLedger.empty) {
        throw new HttpsError("already-exists", "Already marked as paid");
      }

      const balanceRef = db.collection("balances").doc(withdrawal.uid);
      const balanceSnap = await t.get(balanceRef);
      const balance = balanceSnap.data() as BalanceDoc;

      // Remove from locked (coins are paid out, remove completely)
      const newLocked = Math.max(0, balance.lockedCoins - withdrawal.requestedCoins);

      t.update(balanceRef, {
        lockedCoins: newLocked,
        totalCoins: balance.availableCoins + newLocked,
        version: admin.firestore.FieldValue.increment(1),
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      // Ledger: fee deduction record
      if (withdrawal.feeCoins > 0) {
        writeLedgerEntry(t, {
          uid: withdrawal.uid,
          type: "WITHDRAWAL_FEE",
          amount: -withdrawal.feeCoins,
          balanceBefore: balance.availableCoins,
          balanceAfter: balance.availableCoins,
          lockedBefore: balance.lockedCoins,
          lockedAfter: newLocked,
          refType: "withdrawal",
          refId: withdrawalId,
          note: `Withdrawal fee: ${withdrawal.feeCoins} coins`,
          createdBy: adminUid,
          idempotencyKey: `wfee:${withdrawalId}`,
        });
      }

      // Ledger: payout record
      writeLedgerEntry(t, {
        uid: withdrawal.uid,
        type: "WITHDRAWAL_PAYOUT",
        amount: -withdrawal.netCoins,
        balanceBefore: balance.availableCoins,
        balanceAfter: balance.availableCoins,
        lockedBefore: balance.lockedCoins,
        lockedAfter: newLocked,
        refType: "withdrawal",
        refId: withdrawalId,
        note: `Paid out ${withdrawal.netCoins} coins (PKR ${withdrawal.netPkr}) via ${withdrawal.paymentMethod}`,
        createdBy: adminUid,
        idempotencyKey,
      });

      t.update(withdrawalRef, {
        status: "PAID",
        paidBy: adminUid,
        paidAt: admin.firestore.FieldValue.serverTimestamp(),
        paymentTxnRef,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      await writeAuditLog(t, {
        action: "MARK_WITHDRAWAL_PAID",
        performedBy: adminUid,
        targetUid: withdrawal.uid,
        targetDocId: withdrawalId,
        after: { status: "PAID", paymentTxnRef },
        metadata: { netPkr: withdrawal.netPkr },
      });
    });

    return { success: true };
  }
);

// ─── rejectWithdrawal ─────────────────────────────────────────────────────

export const rejectWithdrawal = onCall(
  { region: "asia-south1" },
  async (request) => {
    const adminUid = request.auth?.uid;
    if (!adminUid) throw new HttpsError("unauthenticated", "Login required");
    await assertRole(adminUid, ["admin"]);

    const { withdrawalId, rejectReason } = request.data as {
      withdrawalId: string;
      rejectReason: string;
    };

    const withdrawalRef = db.collection("withdrawals").doc(withdrawalId);

    await db.runTransaction(async (t) => {
      const snap = await t.get(withdrawalRef);
      if (!snap.exists) throw new HttpsError("not-found", "Withdrawal not found");
      const withdrawal = snap.data()!;

      if (!["PENDING", "APPROVED"].includes(withdrawal.status)) {
        throw new HttpsError("failed-precondition", "Cannot reject in current status");
      }

      // Unlock coins back to available
      const balanceRef = db.collection("balances").doc(withdrawal.uid);
      const balanceSnap = await t.get(balanceRef);
      const balance = balanceSnap.data() as BalanceDoc;

      const newLocked = Math.max(0, balance.lockedCoins - withdrawal.requestedCoins);
      const newAvailable = balance.availableCoins + withdrawal.requestedCoins;

      t.update(balanceRef, {
        availableCoins: newAvailable,
        lockedCoins: newLocked,
        totalCoins: newAvailable + newLocked,
        version: admin.firestore.FieldValue.increment(1),
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      // Ledger: unlock (refund locked coins)
      writeLedgerEntry(t, {
        uid: withdrawal.uid,
        type: "WITHDRAWAL_UNLOCK",
        amount: withdrawal.requestedCoins,
        balanceBefore: balance.availableCoins,
        balanceAfter: newAvailable,
        lockedBefore: balance.lockedCoins,
        lockedAfter: newLocked,
        refType: "withdrawal",
        refId: withdrawalId,
        note: `Withdrawal rejected, coins returned: ${rejectReason}`,
        createdBy: adminUid,
        idempotencyKey: `wunlock:${withdrawalId}`,
      });

      t.update(withdrawalRef, {
        status: "REJECTED",
        rejectReason,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      await writeAuditLog(t, {
        action: "REJECT_WITHDRAWAL",
        performedBy: adminUid,
        targetUid: withdrawal.uid,
        targetDocId: withdrawalId,
        after: { status: "REJECTED", rejectReason },
      });
    });

    return { success: true };
  }
);
```

---

### 3.11 Scheduled Leaderboard Snapshots

```typescript
// ─── scheduledLeaderboardSnapshot ─────────────────────────────────────────
// Runs weekly (Monday 00:05 Karachi) and monthly (1st of month 00:05)

export const scheduledWeeklyLeaderboard = onSchedule(
  {
    schedule: "5 0 * * 1",  // Monday 00:05 UTC+5 equivalent
    region: "asia-south1",
    timeZone: "Asia/Karachi",
  },
  async () => {
    await computeLeaderboardSnapshot("weekly");
  }
);

export const scheduledMonthlyLeaderboard = onSchedule(
  {
    schedule: "5 0 1 * *",  // 1st of every month 00:05
    region: "asia-south1",
    timeZone: "Asia/Karachi",
  },
  async () => {
    await computeLeaderboardSnapshot("monthly");
  }
);

async function computeLeaderboardSnapshot(
  period: "weekly" | "monthly"
): Promise<void> {
  const now = DateTime.now().setZone("Asia/Karachi");

  let periodKey: string;
  let windowStart: DateTime;
  let windowEnd: DateTime;

  if (period === "weekly") {
    // Previous week (we run at start of new week)
    const prevWeek = now.minus({ weeks: 1 });
    periodKey = `${prevWeek.year}-W${String(prevWeek.weekNumber).padStart(2, "0")}`;
    windowStart = prevWeek.startOf("week").startOf("day");
    windowEnd = prevWeek.endOf("week").endOf("day");
  } else {
    const prevMonth = now.minus({ months: 1 });
    periodKey = `${prevMonth.year}-${String(prevMonth.month).padStart(2, "0")}`;
    windowStart = prevMonth.startOf("month");
    windowEnd = prevMonth.endOf("month");
  }

  const windowStartTs = admin.firestore.Timestamp.fromMillis(windowStart.toMillis());
  const windowEndTs = admin.firestore.Timestamp.fromMillis(windowEnd.toMillis());

  // Fetch all completed tournaments in window
  const tournamentsSnap = await db
    .collection("tournaments")
    .where("status", "==", "COMPLETED")
    .where("scheduledAt", ">=", windowStartTs)
    .where("scheduledAt", "<=", windowEndTs)
    .get();

  // Aggregate scores per user
  const userStats = new Map<string, {
    uid: string;
    totalKills: number;
    totalWins: number;
    tournamentsPlayed: number;
    totalPrizesWon: number;
    name: string;
    avatarId: string;
    region: string;
    city: string;
  }>();

  for (const tournamentDoc of tournamentsSnap.docs) {
    const resultsSnap = await tournamentDoc.ref
      .collection("results")
      .where("status", "==", "APPROVED")
      .get();

    for (const resultDoc of resultsSnap.docs) {
      const result = resultDoc.data();
      const uid = result.uid;

      const existing = userStats.get(uid) || {
        uid,
        totalKills: 0,
        totalWins: 0,
        tournamentsPlayed: 0,
        totalPrizesWon: 0,
        name: "",
        avatarId: "",
        region: "",
        city: "",
      };

      existing.totalKills += result.kills || 0;
      existing.totalWins += result.placement === 1 ? 1 : 0;
      existing.tournamentsPlayed += 1;

      if (!existing.name) {
        const userSnap = await db.collection("users").doc(uid).get();
        if (userSnap.exists) {
          const user = userSnap.data()!;
          existing.name = user.name;
          existing.avatarId = user.avatarId;
          existing.region = user.region;
          existing.city = user.city;
        }
      }

      userStats.set(uid, existing);
    }
  }

  // Fetch prize data from ledger
  const prizeEntries = await db
    .collection("walletLedger")
    .where("type", "==", "PRIZE")
    .where("createdAt", ">=", windowStartTs)
    .where("createdAt", "<=", windowEndTs)
    .get();

  for (const entry of prizeEntries.docs) {
    const data = entry.data();
    const existing = userStats.get(data.uid);
    if (existing) {
      existing.totalPrizesWon += data.amount;
      userStats.set(data.uid, existing);
    }
  }

  // Compute composite score and rank
  const scoredUsers = Array.from(userStats.values())
    .map((u) => ({
      ...u,
      score: u.totalKills * 5 + u.totalWins * 200 + u.totalPrizesWon * 0.1,
    }))
    .sort((a, b) => b.score - a.score);

  // Build global leaderboard
  const scopes = ["global", ...new Set(scoredUsers.map((u) => u.region))];

  for (const scope of scopes) {
    const scopeUsers =
      scope === "global"
        ? scoredUsers
        : scoredUsers.filter((u) => u.region === scope);

    if (scopeUsers.length === 0) continue;

    const boardId = `${scope}_${period}_${periodKey}`;
    const boardRef = db.collection("leaderboards").doc(boardId);

    const topEntries = scopeUsers.slice(0, 100);
    const entriesWithRank = topEntries.map((u, i) => ({ ...u, rank: i + 1 }));

    // Write board metadata
    await boardRef.set({
      boardId,
      scope,
      period,
      periodKey,
      weekStart: period === "weekly" ? admin.firestore.Timestamp.fromMillis(windowStart.toMillis()) : null,
      weekEnd: period === "weekly" ? admin.firestore.Timestamp.fromMillis(windowEnd.toMillis()) : null,
      computedAt: admin.firestore.FieldValue.serverTimestamp(),
      totalPlayers: scopeUsers.length,
      topEntries: entriesWithRank,
    });

    // Write individual entries subcollection (for pagination beyond top 100)
    const writeBatch = db.batch();
    for (const [index, user] of scopeUsers.entries()) {
      const entryRef = boardRef.collection("entries").doc(user.uid);
      writeBatch.set(entryRef, {
        ...user,
        rank: index + 1,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      // Firestore batch limit: 500 ops
      if ((index + 1) % 490 === 0) {
        await writeBatch.commit();
      }
    }
    await writeBatch.commit();

    console.log(`Leaderboard snapshot written: ${boardId} (${scopeUsers.length} players)`);
  }
}
```

---

### 3.12 Device Binding & Suspicious Scoring

```typescript
// ─── bindDevice ───────────────────────────────────────────────────────────
// Called on first login / device change attempt

export const bindDevice = onCall(
  { region: "asia-south1" },
  async (request) => {
    const uid = request.auth?.uid;
    if (!uid) throw new HttpsError("unauthenticated", "Login required");

    const { deviceId, deviceModel, osVersion, appVersion } = request.data as {
      deviceId: string;       // SHA-256 of device fingerprint
      deviceModel: string;
      osVersion: string;
      appVersion: string;
    };

    if (!deviceId) throw new HttpsError("invalid-argument", "deviceId required");

    await db.runTransaction(async (t) => {
      const userRef = db.collection("users").doc(uid);
      const deviceBindingRef = db.collection("deviceBindings").doc(deviceId);
      const userDeviceRef = db
        .collection("users")
        .doc(uid)
        .collection("deviceBindings")
        .doc(deviceId);

      const [userSnap, globalBindingSnap] = await Promise.all([
        t.get(userRef),
        t.get(deviceBindingRef),
      ]);

      const user = userSnap.data()!;

      // Device already bound to a DIFFERENT user — suspicious
      if (globalBindingSnap.exists && globalBindingSnap.data()!.uid !== uid) {
        await incrementSuspiciousScore(
          uid,
          30,
          "device_bound_to_other_account"
        );
        throw new HttpsError(
          "permission-denied",
          "This device is bound to another account. Submit a device reset request."
        );
      }

      // User already has a different device bound
      if (user.deviceId && user.deviceId !== deviceId) {
        // Check if there's a pending reset request
        const resetQuery = await db
          .collection("deviceResetRequests")
          .where("uid", "==", uid)
          .where("status", "==", "APPROVED")
          .where("newDeviceId", "==", deviceId)
          .limit(1)
          .get();

        if (resetQuery.empty) {
          await incrementSuspiciousScore(uid, 15, "multiple_device_attempt");
          throw new HttpsError(
            "permission-denied",
            "New device detected. Please submit a device reset request."
          );
        }
        // Approved reset — allow rebinding
      }

      const bindingData = {
        bindingId: deviceId,
        uid,
        deviceModel,
        osVersion,
        appVersion,
        boundAt: user.deviceId ? user.deviceId === deviceId
          ? admin.firestore.FieldValue.serverTimestamp()
          : admin.firestore.FieldValue.serverTimestamp()
          : admin.firestore.FieldValue.serverTimestamp(),
        lastSeenAt: admin.firestore.FieldValue.serverTimestamp(),
        isActive: true,
      };

      t.set(deviceBindingRef, bindingData);
      t.set(userDeviceRef, bindingData);

      t.update(userRef, {
        deviceId,
        lastLoginAt: admin.firestore.FieldValue.serverTimestamp(),
        lastLoginDeviceId: deviceId,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
    });

    return { success: true };
  }
);

// ─── requestDeviceReset ───────────────────────────────────────────────────

export const requestDeviceReset = onCall(
  { region: "asia-south1" },
  async (request) => {
    const uid = request.auth?.uid;
    if (!uid) throw new HttpsError("unauthenticated", "Login required");

    const { newDeviceId, reason } = request.data as {
      newDeviceId: string;
      reason: string;
    };

    // Prevent spam requests
    const existingQuery = await db
      .collection("deviceResetRequests")
      .where("uid", "==", uid)
      .where("status", "==", "PENDING")
      .limit(1)
      .get();

    if (!existingQuery.empty) {
      throw new HttpsError(
        "already-exists",
        "You already have a pending device reset request"
      );
    }

    const userSnap = await db.collection("users").doc(uid).get();
    const user = userSnap.data()!;

    const requestRef = db.collection("deviceResetRequests").doc();
    await requestRef.set({
      requestId: requestRef.id,
      uid,
      currentDeviceId: user.deviceId || "none",
      newDeviceId,
      reason,
      status: "PENDING",
      suspiciousScore: user.suspiciousScore,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    return { success: true, requestId: requestRef.id };
  }
);
```

---

## 4. Concurrency & Balance Safety

### 4.1 The Core Problem

```
DANGER (without transactions):
  Thread A reads balance: 100 coins
  Thread B reads balance: 100 coins
  Thread A writes: 100 - 50 = 50  ✓
  Thread B writes: 100 - 70 = 30  ✗ (should have failed — total spend = 120 > 100)
  Final balance: 30 coins (should be impossible)
```

### 4.2 Strategy: Firestore Transactions + Optimistic Concurrency

```typescript
// ─── BalanceUpdateStrategy ────────────────────────────────────────────────
//
// RULE 1: All balance mutations MUST use db.runTransaction()
// RULE 2: Read balance INSIDE the transaction (not before)
// RULE 3: Validate balance INSIDE the transaction (not before)
// RULE 4: Never use FieldValue.increment() for balance (can't validate minimum)
// RULE 5: version field enables optimistic lock detection
// RULE 6: Retry on contention (Firestore error code 10 = ABORTED)

async function safeDeductCoins(
  t: admin.firestore.Transaction,
  uid: string,
  amount: number,
  reason: string
): Promise<{ before: number; after: number; balance: BalanceDoc }> {
  const balanceRef = db.collection("balances").doc(uid);
  const balanceSnap = await t.get(balanceRef);

  if (!balanceSnap.exists) {
    throw new HttpsError("failed-precondition", "Balance account not found");
  }

  const balance = balanceSnap.data() as BalanceDoc;

  // ── CRITICAL: Validate INSIDE transaction ──────────────────────────────
  if (balance.availableCoins < amount) {
    throw new HttpsError(
      "failed-precondition",
      `Insufficient balance: need ${amount}, have ${balance.availableCoins}. Reason: ${reason}`
    );
  }

  // Guaranteed: no negative balance possible
  const newAvailable = balance.availableCoins - amount;

  // ── Version check (optimistic lock) ───────────────────────────────────
  // Firestore transactions handle this automatically via read-then-write,
  // but we track version explicitly for debugging / admin dashboards
  t.update(balanceRef, {
    availableCoins: newAvailable,
    totalCoins: newAvailable + balance.lockedCoins,
    version: admin.firestore.FieldValue.increment(1),
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  return {
    before: balance.availableCoins,
    after: newAvailable,
    balance,
  };
}
```

### 4.3 Idempotency Patterns

```typescript
// ─── IdempotencyGuard ─────────────────────────────────────────────────────
//
// Every financial operation has a unique idempotencyKey.
// Before writing, check if a ledger entry with that key exists.
// If yes: the operation already succeeded — return cached result.
// This prevents double-crediting from network retries.

async function checkIdempotency(
  t: admin.firestore.Transaction,
  idempotencyKey: string
): Promise<boolean> {
  const existingSnap = await db
    .collection("walletLedger")
    .where("idempotencyKey", "==", idempotencyKey)
    .limit(1)
    .get();

  return !existingSnap.empty;
}

// Usage pattern:
async function creditCoinsIdempotent(
  uid: string,
  amount: number,
  refId: string,
  idempotencyKey: string
): Promise<void> {
  await db.runTransaction(async (t) => {
    const alreadyDone = await checkIdempotency(t, idempotencyKey);
    if (alreadyDone) {
      console.log(`Operation ${idempotencyKey} already processed, skipping`);
      return;
    }

    // ... proceed with balance update and ledger write
  });
}
```

### 4.4 Two-Bucket Design (Available vs Locked)

```
┌─────────────────────────────────────────────────────────────────┐
│                      BALANCE MODEL                              │
│                                                                 │
│  availableCoins  │  lockedCoins   │   totalCoins               │
│  ───────────────   ─────────────   ──────────────              │
│  Freely spendable  Reserved for    Sum (for quick              │
│  Join, Withdraw   pending actions   balance display)           │
│                                                                 │
│  FLOW: Join Tournament                                          │
│  available: 1000 → 950  (-50 entry fee)                        │
│  locked:      200 → 250  (+50)                                  │
│  total:      1200 → 1200 (unchanged!)                           │
│                                                                 │
│  FLOW: Win Prize (settlement)                                   │
│  locked:      250 → 200  (-50 unlocked)                         │
│  available:   950 → 1000 (+50 unlocked) + 2000 prize           │
│  available:  1000 → 3000 (after prize)                          │
│  total:      1200 → 3200                                        │
│                                                                 │
│  ANTI-NEGATIVE GUARANTEE:                                       │
│  availableCoins can never go below 0 (validated in txn)        │
│  lockedCoins always = sum of all pending locks                  │
│  totalCoins = available + locked (invariant maintained)         │
└─────────────────────────────────────────────────────────────────┘
```

### 4.5 Retry Strategy

```typescript
// ─── withRetry ────────────────────────────────────────────────────────────
// Wrap any transaction that may face contention

async function withRetry<T>(
  operation: () => Promise<T>,
  maxRetries: number = 5,
  baseDelayMs: number = 100
): Promise<T> {
  let lastError: Error | null = null;

  for (let attempt = 0; attempt < maxRetries; attempt++) {
    try {
      return await operation();
    } catch (error: any) {
      lastError = error;

      // Firestore contention: ABORTED (10) or UNAVAILABLE (14)
      const isRetryable =
        error?.code === 10 ||        // ABORTED (transaction conflict)
        error?.code === 14 ||        // UNAVAILABLE (transient)
        error?.message?.includes("contention");

      if (!isRetryable) throw error; // Non-retryable: invalid-argument, permission-denied, etc.

      const delay = baseDelayMs * Math.pow(2, attempt) + Math.random() * 50;
      console.warn(`Transaction attempt ${attempt + 1} failed, retrying in ${delay}ms`);
      await new Promise((r) => setTimeout(r, delay));
    }
  }

  throw lastError || new Error("Transaction failed after max retries");
}

// Usage:
// await withRetry(() => db.runTransaction(async (t) => { ... }));
```

### 4.6 Balance Integrity Verification

```typescript
// ─── verifyBalanceIntegrity (Admin Tool) ──────────────────────────────────
// Cross-check balance doc against wallet ledger sum
// Run periodically or on suspicious flag

export const verifyBalanceIntegrity = onCall(
  { region: "asia-south1" },
  async (request) => {
    const adminUid = request.auth?.uid;
    if (!adminUid) throw new HttpsError("unauthenticated", "Login required");
    await assertRole(adminUid, ["admin"]);

    const { targetUid } = request.data as { targetUid: string };

    // Sum all ledger entries for user
    const ledgerSnap = await db
      .collection("walletLedger")
      .where("uid", "==", targetUid)
      .get();

    const ledgerSum = ledgerSnap.docs.reduce(
      (sum, doc) => sum + (doc.data().amount || 0), 0
    );

    const balanceSnap = await db.collection("balances").doc(targetUid).get();
    const balance = balanceSnap.data() as BalanceDoc;

    // Note: locked coins are in-flight, so compare (available + locked) vs ledger sum
    const recordedTotal = balance.totalCoins;
    const discrepancy = recordedTotal - ledgerSum;

    const result = {
      uid: targetUid,
      ledgerSum,
      recordedAvailable: balance.availableCoins,
      recordedLocked: balance.lockedCoins,
      recordedTotal,
      discrepancy,
      isIntact: Math.abs(discrepancy) < 1, // Allow 0 rounding tolerance
    };

    if (!result.isIntact) {
      // Flag for manual review
      await incrementSuspiciousScore(targetUid, 50, "balance_integrity_mismatch");

      await db.collection("auditLogs").doc().set({
        action: "BALANCE_INTEGRITY_FAIL",
        performedBy: adminUid,
        targetUid,
        metadata: result,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
      });
    }

    return result;
  }
);
```

### 4.7 Security Summary Table

```
┌──────────────────────────────┬──────────────────────────────────────────┐
│ Attack Vector                │ Defense                                  │
├──────────────────────────────┼──────────────────────────────────────────┤
│ Double-spend (rapid join)    │ Firestore transaction reads balance       │
│                              │ inside txn; rate limiter 5/60s           │
├──────────────────────────────┼──────────────────────────────────────────┤
│ Negative balance             │ Validate availableCoins >= amount         │
│                              │ INSIDE transaction before write           │
├──────────────────────────────┼──────────────────────────────────────────┤
│ Double topup credit          │ idempotencyKey checked before             │
│                              │ any ledger write in transaction           │
├──────────────────────────────┼──────────────────────────────────────────┤
│ Weekly cap bypass            │ SUM of withdrawals in window              │
│                              │ computed inside transaction               │
├──────────────────────────────┼──────────────────────────────────────────┤
│ Multiple device / accounts   │ deviceBindings global collection;         │
│                              │ suspicious score increments               │
├──────────────────────────────┼──────────────────────────────────────────┤
│ Result spoofing              │ Mod review + proof Storage path required  │
│                              │ Status field locked by security rules     │
├──────────────────────────────┼──────────────────────────────────────────┤
│ Direct balance write         │ Security rules: allow write: if false     │
│                              │ Only Admin SDK (Cloud Functions) writes   │
├──────────────────────────────┼──────────────────────────────────────────┤
│ Room info leakage            │ No direct Firestore read allowed;         │
│                              │ callable Cloud Function checks status     │
├──────────────────────────────┼──────────────────────────────────────────┤
│ Ledger tampering             │ Immutable (no update/delete rules);       │
│                              │ Admin SDK write in txn only               │
└──────────────────────────────┴──────────────────────────────────────────┘
```

---

### Deployment Notes

```bash
# Initialize project
firebase init functions --project goats-arena

# Install dependencies
cd functions
npm install firebase-admin firebase-functions luxon rate-limiter-flexible
npm install -D typescript @types/node

# Deploy all functions
firebase deploy --only functions

# Deploy security rules
firebase deploy --only firestore:rules,storage:rules

# Set custom claims for admins (run once via Admin SDK script)
admin.auth().setCustomUserClaims(uid, { role: 'admin' });

# Environment configuration
firebase functions:config:set app.env="production"
```