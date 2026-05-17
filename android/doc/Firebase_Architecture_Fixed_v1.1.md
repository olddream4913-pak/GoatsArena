Neeche main aapko **Claude Prompt #2 output ka “patched / fixed complete version”** de raha hoon — sirf wohi issues solve karke jo main ne point kiye thay:

✅ (1) `users` me phone/device leak fix (public vs private)  
✅ (2) Financial collections par **client create/write band** (topup/withdraw/ledger/balance)  
✅ (3) Firestore rules bug fix (`!existingData() == true` etc.)  
✅ (4) Wallet ledger ko **clear & professional** banaya: `deltaAvailable` + `deltaLocked` (double counting khatam)  
✅ (5) Weekly cap ka efficient & txn-safe method (balance doc me weekly counter)

Isko aap repo me as docs + rules + functions ke basis par use kar sakte ho.

---

# GoatsArena Firebase Architecture (Fixed v1.1)

## 0) Final Collection Structure (recommended)
```text
/users/{uid}                       (PUBLIC profile only)
/users/{uid}/private/profile       (PRIVATE: phone, deviceId, payout accounts, kyc)

/balances/{uid}                    (NO client writes)
/walletLedger/{ledgerId}           (append-only; NO client writes)

/topupOrders/{orderId}             (NO client create; Cloud Function only)
/withdrawals/{withdrawalId}        (NO client create; Cloud Function only)

/tournaments/{tournamentId}        (public read)
  /participants/{uid}              (public read optional; write CF only)
  /results/{uid}                   (read: owner/mod; write CF only)
  /roomInfo/{tournamentId}         (read: mod only; user gets via callable)
  /disputes/{disputeId}            (create user allowed OR CF only — below rules show CF-only for max safety)

/deviceBindings/{deviceId}         (admin only read; CF writes)
/deviceResetRequests/{requestId}   (user create via callable; admin approve via callable)

/leaderboards/{boardId}
  /entries/{uid}

/auditLogs/{logId}                 (admin read; CF writes)

/appConfig/main                    (read all users; write admin)
```

---

## 1) Public vs Private User Docs (Fix #1)
### `/users/{uid}` (PUBLIC)
**Yahan phone/deviceId bilkul nahi hoga.**
```ts
interface UserPublic {
  uid: string;
  name: string;
  avatarId: string;
  freeFireUid: string;
  region: string;
  city?: string;
  role: "player" | "mod" | "admin";       // or use custom claims only
  isBanned: boolean;
  suspiciousScore: number;
  createdAt: Timestamp;
  updatedAt: Timestamp;
}
```

### `/users/{uid}/private/profile` (PRIVATE)
```ts
interface UserPrivateProfile {
  fullPhone: string;           // E.164
  deviceId: string;            // bound device hash
  payoutAccounts: {
    easypaisa?: { number: string; name: string };
    jazzcash?: { number: string; name: string };
  };
  kycStatus: "none"|"pending"|"verified"|"rejected";
  updatedAt: Timestamp;
}
```

---

## 2) Balance Model + Ledger (Fix #4)
### `/balances/{uid}`
```ts
interface BalanceDoc {
  uid: string;
  availableCoins: number;     // spendable
  lockedCoins: number;        // reserved for joins/withdrawals
  totalCoins: number;         // available + locked
  weeklyWithdrawnCoins: number;
  weeklyResetAt: Timestamp;   // next Monday 00:00 Asia/Karachi
  version: number;            // increments each mutation
  updatedAt: Timestamp;
}
```

### `/walletLedger/{ledgerId}` (append-only, immutable)
**Important change:** amount confusion khatam.
```ts
type LedgerType =
  | "TOPUP_CREDIT"
  | "JOIN_LOCK"
  | "ENTRY_FEE_SETTLE"
  | "ENTRY_FEE_REFUND"
  | "PRIZE_CREDIT"
  | "WITHDRAW_LOCK"
  | "WITHDRAW_REJECT_UNLOCK"
  | "WITHDRAW_PAID_SETTLE"
  | "ADMIN_ADJUST";

interface WalletLedgerDoc {
  ledgerId: string;
  uid: string;
  type: LedgerType;

  deltaAvailable: number;  // e.g. -50, +2000
  deltaLocked: number;     // e.g. +50, -50

  availableAfter: number;
  lockedAfter: number;

  refType: "topupOrder"|"tournament"|"withdrawal"|"admin"|null;
  refId: string|null;

  note: string;
  createdAt: Timestamp;
  createdBy: string;       // uid or "system"
  idempotencyKey: string;  // must be unique per financial operation
}
```

---

## 3) Storage Paths (more secure)
Use user-scoped paths so random users can’t overwrite:

- Topup proof: `topup_proofs/{uid}/{orderId}.jpg`
- Result proof: `results/{tournamentId}/{uid}.jpg`
- Dispute: `disputes/{tournamentId}/{uid}/{disputeId}_{n}.jpg`

---

# 4) Firestore Rules (Fixed & Secure)
**Principle:** Coins/ledger/balance/topup/withdraw/results = Cloud Functions only.

Create file: `firestore.rules`

```js
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    function isAuth() { return request.auth != null; }
    function uid() { return request.auth.uid; }

    // Prefer custom claims for roles:
    function isAdmin() { return isAuth() && request.auth.token.role == 'admin'; }
    function isMod() { return isAuth() && (request.auth.token.role == 'mod' || request.auth.token.role == 'admin'); }

    // ---------------- USERS ----------------
    match /users/{userId} {
      allow read: if isAuth(); // public profiles
      allow create: if false;  // create via callable function
      allow update: if false;  // update via callable function
      allow delete: if false;

      match /private/{docId} {
        allow read: if isAuth() && (uid() == userId || isAdmin());
        allow write: if false; // CF only
      }
    }

    // ---------------- BALANCES + LEDGER ----------------
    match /balances/{userId} {
      allow read: if isAuth() && (uid() == userId || isAdmin());
      allow write: if false; // CF only
    }

    match /walletLedger/{ledgerId} {
      allow read: if isAuth() && (resource.data.uid == uid() || isAdmin());
      allow write: if false; // CF only (append-only)
    }

    // ---------------- CONFIG ----------------
    match /appConfig/{configId} {
      allow read: if isAuth();
      allow write: if isAdmin();
    }

    // ---------------- TOURNAMENTS ----------------
    match /tournaments/{tournamentId} {
      allow read: if true;
      allow create, update, delete: if isAdmin(); // admin panel / CF

      match /participants/{pUid} {
        allow read: if true;        // optional public
        allow write: if false;      // join/checkin via CF only
      }

      match /roomInfo/{roomDocId} {
        allow read, write: if isMod();  // users get via callable getRoomInfo()
      }

      match /results/{rUid} {
        allow read: if isAuth() && (uid() == rUid || isMod());
        allow write: if false;          // submit/review via CF only
      }

      match /disputes/{disputeId} {
        allow read: if isAuth() && (resource.data.raisedBy == uid() || isMod());
        allow write: if false;          // create/update via CF only (max safety)
      }
    }

    // ---------------- TOPUP + WITHDRAW ----------------
    match /topupOrders/{orderId} {
      allow read: if isAuth() && (resource.data.uid == uid() || isAdmin() || isMod());
      allow write: if false; // CF only
    }

    match /withdrawals/{withdrawalId} {
      allow read: if isAuth() && (resource.data.uid == uid() || isAdmin());
      allow write: if false; // CF only
    }

    // ---------------- DEVICE SECURITY ----------------
    match /deviceBindings/{bindingId} {
      allow read: if isAdmin();
      allow write: if false; // CF only
    }

    match /deviceResetRequests/{requestId} {
      allow read: if isAuth() && (resource.data.uid == uid() || isAdmin());
      allow write: if false; // CF only
    }

    // ---------------- AUDIT LOGS ----------------
    match /auditLogs/{logId} {
      allow read: if isAdmin();
      allow write: if false; // CF only
    }

    // ---------------- LEADERBOARDS ----------------
    match /leaderboards/{boardId} {
      allow read: if true;
      match /entries/{entryId} {
        allow read: if true;
        allow write: if false; // CF only
      }
      allow write: if false;
    }
  }
}
```

---

# 5) Storage Rules (Fixed)
Create file: `storage.rules`

```js
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {

    function isAuth() { return request.auth != null; }
    function uid() { return request.auth.uid; }
    function isAdmin() { return isAuth() && request.auth.token.role == 'admin'; }
    function isMod() { return isAuth() && (request.auth.token.role == 'mod' || request.auth.token.role == 'admin'); }
    function isValidImage(maxMb) {
      return request.resource.contentType.matches('image/.*')
        && request.resource.size < maxMb * 1024 * 1024;
    }

    // Topup proofs: topup_proofs/{uid}/{orderId}.jpg
    match /topup_proofs/{userId}/{fileName} {
      allow write: if isAuth() && uid() == userId && isValidImage(5);
      allow read: if isMod() || isAdmin();
    }

    // Result proofs: results/{tournamentId}/{uid}.jpg
    match /results/{tournamentId}/{userId} {
      allow write: if isAuth() && uid() == userId && isValidImage(5);
      allow read: if isMod() || isAdmin();
    }

    // Dispute proofs: disputes/{tournamentId}/{uid}/{fileName}
    match /disputes/{tournamentId}/{userId}/{fileName} {
      allow write: if isAuth() && uid() == userId && isValidImage(10);
      allow read: if isMod() || isAdmin();
    }

    match /{allPaths=**} {
      allow read, write: if false;
    }
  }
}
```

---

# 6) Cloud Functions (Core fixes applied)

## 6.1 Golden Rules
- **All** coin mutations: **Firestore transaction** inside CF only  
- **Idempotency**: recommend a doc lock:

`/idempotency/{key}` create in transaction. If exists → abort.

### Idempotency helper (recommended)
```ts
async function claimIdempotency(t, key) {
  const ref = db.collection("idempotency").doc(key);
  const snap = await t.get(ref);
  if (snap.exists) throw new HttpsError("already-exists", "Already processed");
  t.set(ref, { key, createdAt: FieldValue.serverTimestamp() });
}
```

---

## 6.2 joinTournament (fixed ledger model)
Transaction steps:
1) read tournament + balance + user public + user private (deviceId)
2) check open slots, not banned, balance.available >= entryFee
3) update balance:
- available -= entryFee
- locked += entryFee
4) ledger:
- type JOIN_LOCK
- deltaAvailable = -entryFee
- deltaLocked = +entryFee
5) create participant doc
6) tournament filledSlots++

---

## 6.3 settleTournament (entry fee settle vs refund)
For each participant:
- **If tournament cancelled:** refund:
  - balance.available += fee
  - balance.locked -= fee
  - ledger ENTRY_FEE_REFUND (deltaAvailable +fee, deltaLocked -fee)

- **If valid participant (played / result submitted):** charge entry fee:
  - balance.locked -= fee
  - ledger ENTRY_FEE_SETTLE (deltaAvailable 0, deltaLocked -fee)
  - (total coins decrease by fee automatically)

- **If no-show / forfeit:** also charge:
  - same as settle (locked -= fee)

- **Prize:** (winners only)
  - balance.available += prize
  - ledger PRIZE_CREDIT (deltaAvailable +prize, deltaLocked 0)

> Is model me “unlock then debit” wali confusion khatam.

---

## 6.4 createWithdrawalRequest (weekly cap fix)
**Old output** weekly cap query scan karta tha. Best txn-safe:

- `balances/{uid}.weeklyWithdrawnCoins` maintain karo  
- Scheduled function weekly reset kare.

Transaction:
1) read config + balance
2) if requestedCoins < min → reject
3) if weeklyWithdrawn + requestedCoins > cap → reject
4) compute feeCoins = ceil(req*percent/100) + fixed
5) netCoins = req - feeCoins (must be >0)
6) update balance:
- available -= requestedCoins
- locked += requestedCoins
- weeklyWithdrawnCoins += requestedCoins
7) ledger WITHDRAW_LOCK:
- deltaAvailable = -requestedCoins
- deltaLocked = +requestedCoins
8) create withdrawal doc with fee/net snapshots

---

## 6.5 markWithdrawalPaid (final settle)
Transaction:
1) read withdrawal + balance
2) only if status APPROVED
3) update balance:
- locked -= requestedCoins
4) ledger WITHDRAW_PAID_SETTLE:
- deltaAvailable = 0
- deltaLocked = -requestedCoins
5) update withdrawal status PAID + paymentTxnRef
6) audit log

> Fee already included in requestedCoins; for transparency withdrawal doc me `feeCoins/netCoins/netPkr` show karo.

---

## 6.6 weekly reset scheduled function
Every Monday 00:00 Asia/Karachi:
- set `weeklyWithdrawnCoins = 0`
- set `weeklyResetAt = next Monday`

---

# 7) What you do next (step-by-step)
1) Repo me files add karo:
   - `firestore.rules`
   - `storage.rules`
   - `docs/Firebase_Architecture_Fixed_v1.1.md` (is message ko save)

2) Firebase deploy:
```bash
firebase deploy --only firestore:rules,storage:rules
firebase deploy --only functions
```

3) Roles/claims set karo (admin/mod):
- Admin SDK script / Cloud Function to set custom claims:
  - `{ role: "admin" }` or `{ role: "mod" }`

---

## Ab aap se 2 confirmations (taake main Prompt #3 Codex perfect bana doon)
1) Aap client ko **direct Firestore writes bilkul band** rakhna chahte ho (recommended) aur sab callable CF se? (Yes/No)  
2) Withdraw fee aap final kya rakhna chahte ho: **percent + fixed coins** (e.g., 5% + 10) ya sirf percent?

Confirm kar do, phir main aapko Codex ke liye **next prompt (Android app code)** aapke isi fixed backend ke exactly according de dunga (OTP + profile + wallet + tournaments + withdrawals + leaderboard).