# GoatsArena — Complete Product Requirements Document

**App Name:** GoatsArena
**Publisher:** Goats Mods
**Platform:** Android (primary), Admin Panel (Web)
**Document Version:** 1.0
**Status:** Implementation-Ready PRD

---

# TABLE OF CONTENTS

1. Executive Summary & Vision
2. MVP vs Phase 2 vs Phase 3 Feature Matrix
3. Screen-by-Screen Map
4. User Journeys
5. Admin & Moderator Panel Requirements
6. Data Entities & Schema
7. Non-Functional Requirements
8. "What Makes GoatsArena Next-Gen" — Ethical Retention Design
9. Technical Architecture Overview
10. Open Questions & Assumptions

---

# SECTION 1: EXECUTIVE SUMMARY & VISION

## 1.1 Product Vision

GoatsArena is a **community-powered tournament platform** for Free Fire enthusiasts in Pakistan and neighboring regions. It enables players to compete in organized tournaments, earn coins, build reputation, and cash out winnings — all within a trustworthy, fraud-resistant environment operated by Goats Mods (an independent community publisher, not affiliated with Garena or official Free Fire operations).

## 1.2 Mission Statement

> "Turn every Free Fire session into a rewarding, competitive experience — where skill earns real value, communities grow together, and every player is treated fairly."

## 1.3 Success Metrics (North Star KPIs)

| Metric | MVP Target (Month 3) | Phase 2 Target (Month 6) |
|---|---|---|
| Registered Users | 5,000 | 25,000 |
| Monthly Active Users | 2,500 | 15,000 |
| Tournaments Completed/Week | 20 | 100 |
| Avg. Session Duration | 8 min | 14 min |
| Withdrawal Fraud Rate | < 0.5% | < 0.2% |
| D7 Retention | 35% | 50% |
| D30 Retention | 18% | 30% |

## 1.4 Core Design Principles

1. **Trust First** — Players must feel their coins are safe, results are fair, disputes are resolved
2. **Skill Should Win** — Anti-smurfing, anti-fraud, verified UID binding
3. **Ethical Engagement** — No dark patterns; missions reward real play not compulsion
4. **Community Feel** — Leaderboards, seasons, streaks create belonging not anxiety
5. **Operator Sustainability** — Platform earns through entry spreads + withdrawal fees, not pay-to-win

---

# SECTION 2: MVP vs PHASE 2 vs PHASE 3 FEATURE MATRIX

## 2.1 MVP Features (Launch — Months 1–3)

### Authentication & Onboarding
- [x] Phone OTP login via Firebase Auth
- [x] Profile setup: name, avatar (preset selection), Free Fire UID, region/city
- [x] One-device-one-account binding (device fingerprint stored on first login)
- Note: One-device binding 100% foolproof nahi; rooted/spoof devices bypass kar sakte hain, isliye suspicious cases manual review + cooldown me jayenge.
- [x] Manual device-reset request flow (user submits request → admin approves)
- [x] Guest preview (browse tournaments, can't join without login)

### Coins Wallet
- [x] Coin balance display + locked coins display
- [x] Buy coins flow (manual payment → admin confirms → coins credited)
Bonus topup tiers (coin packages): 600/800/1000 coin packages (PKR = coins × 4).
- [x] Wallet ledger (all transactions: credit/debit/lock/unlock/reserve)
- [x] Transaction history screen (filterable)

### Tournaments — Custom Room Type
- [x] Tournament listing (upcoming/live/completed)
- [x] Tournament detail page (rules, prize pool, slots, entry fee)
- [x] Join tournament with coin reservation (coins locked, not deducted until result)
- [x] Check-in system (player confirms attendance 15 min before)
- [x] Room ID + password reveal at scheduled time
- [x] Result submission form (kills, rank, screenshot upload)
- [x] Basic result review by moderator
- [x] Prize distribution in coins upon approval
- [x] Dispute submission by player

### Withdrawals
- [x] Withdrawal request form (amount, Easypaisa/JazzCash number)
- [x] Minimum 100 coins, weekly cap 1200 coins
- [x] Configurable withdrawal fee (percent + fixed fee)
- UI wording: “Tax” ka word app me Processing Fee / Platform Fee use hoga, aur fee breakdown (percent + fixed coins) user ko pehle show hoga.
- Weekly cap timezone: Asia/Karachi, week starts Monday 00:00.
- [x] Status tracking: pending → approved → paid / rejected
- [x] Rejection reason visible to user

### Basic Engagement
- [x] Player profile page with stats (matches, wins, earnings)
- [x] Global leaderboard (weekly, coins earned)
- [x] Basic rank tiers (Bronze → Silver → Gold → Platinum → Diamond)
- [x] Push notifications (tournament reminders, result approved, withdrawal status)

### Admin Panel (MVP)
- [x] Tournament CRUD
- [x] Topup order confirmation/rejection
- [x] Withdrawal request management
- [x] User management (view, ban, device reset)
- [x] Result review + dispute resolution
- [x] Basic audit log viewer

---

## 2.2 Phase 2 Features (Months 4–6)

### Authentication Enhancements
- [ ] Free Fire UID verification (screenshot-based, semi-automated OCR check)
- [ ] Profile completeness score (nudge to complete profile)
- [ ] Account linking (link second phone number with cooldown)

### Tournament Expansion
- [ ] Scrims tournament type (multi-squad practice, kill leaderboard-based prizes)
- [ ] Leagues/Seasons (multi-week, points-based, final prize pool)
- [ ] Tournament bracket visualizer
- [ ] Squad/Team registration (up to 4 players per squad entry)
- [ ] Tournament waiting list (auto-fill if slot opens)
- [ ] Scheduled recurring tournaments (daily/weekly auto-creation by templates)

### Engagement — Missions & Streaks
- [ ] Daily missions (e.g., "Join 1 tournament today → +5 coins")
- [ ] Weekly missions (e.g., "Win 3 matches this week → +50 coins")
- [ ] Victory streak system (3/5/10 consecutive top-3 finishes → streak badges + bonuses)
- [ ] Season ranks with reset every 8 weeks
- [ ] Regional leaderboards (Pakistan → per-city)
- [ ] Mode-specific leaderboards (scrims/custom/league separate)

### Referral System
- [ ] Unique referral code per user
- [ ] Referrer earns coins when referred user completes first topup
- [ ] Anti-fraud: device fingerprint must differ, no self-referral, KYC required for payout
- [ ] Referral dashboard (who referred, status, earnings)

### Wallet Enhancements
- [ ] Automatic payment gateway integration (JazzCash/Easypaisa API, if available)
- [ ] Coin gifting between verified friends (with daily limit + audit)
- [ ] Topup voucher codes (admin generates batch codes for promotions)

### Trust & Safety
- [ ] Suspicious activity score (computed daily, auto-flags accounts)
- [ ] Rate limiting UI feedback (user sees cooldown timer, not just error)
- [ ] Photo ID verification for withdrawals above 500 coins (manual review)

### Admin Panel Phase 2
- [ ] Analytics dashboard (revenue, MAU, tournament activity)
- [ ] Batch withdrawal processing
- [ ] Moderator role with limited permissions
- [ ] Scheduled announcement push notifications
- [ ] Config panel (change coin value, bonus tiers, fees from UI)

---

## 2.3 Phase 3 Features (Months 7–12)

### Platform Expansion
- [ ] iOS app (React Native shared codebase)
- [ ] Web portal (player profile public pages, bracket viewer)
- [ ] Organizer accounts (verified community organizers can create their own tournaments under GoatsArena brand guidelines)
- [ ] Spectator/viewer mode for live scrims

### Advanced Engagement
- [ ] GoatsArena Ranked Mode (dedicated ranked queue with matchmaking)
- [ ] Tournament chat (in-app per-tournament chat room)
- [ ] Achievement badges (100+ distinct achievements, shareable to social)
- [ ] Monthly "Season Finals" event with special prize pool
- [ ] Clan/Guild system (5–20 members, clan leaderboard, clan wars)
- [ ] Avatar store (earn/buy cosmetic avatars with coins)

### Monetization Expansion
- [ ] Ad placements (Google AdMob) — rewarded ads for bonus coins (ethically capped at 10 coins/day)
- [ ] Sponsored tournaments (brand-sponsored prize pools)
- [ ] Premium membership (no-ads, priority dispute resolution, +5% bonus on topup)

### Intelligence & Automation
- [ ] ML-based fraud detection (anomaly in result submissions, kill counts)
- [ ] Auto-moderation: obvious fake screenshot detection
- [ ] Churn prediction + personalized retention nudges (ethical)
- [ ] Automated season management (leaderboard freeze → prize distribution pipeline)

---

# SECTION 3: SCREEN-BY-SCREEN MAP

## Naming Convention
Each screen has an ID (e.g., `SCR-001`), a name, its role (User/Admin/Both), and complete field/action/state documentation.

---

## MODULE A: ONBOARDING & AUTH

### SCR-001 — Splash / App Entry
**Role:** User
**Duration:** 1.5 seconds, then route based on auth state

**Logic:**
- If first launch + no auth → `SCR-002` (Welcome)
- If auth exists + device bound + profile complete → `SCR-010` (Home)
- If auth exists + profile incomplete → `SCR-005` (Profile Setup)
- If device mismatch detected → `SCR-008` (Device Mismatch Warning)

**States:**
- Loading animation: GoatsArena logo + "by Goats Mods" tagline
- Offline state: Show cached home after 3s timeout with offline banner

---

### SCR-002 — Welcome Screen
**Role:** User

**Elements:**
- App logo + tagline: "Compete. Win. Earn."
- Hero illustration (tournament scene, non-branded)
- Carousel of 3 value propositions:
  1. "Join tournaments for coins"
  2. "Fair results, trusted platform"
  3. "Cash out your winnings"
- CTA Button: "Get Started" → `SCR-003`
- Secondary link: "Browse Tournaments (Guest)" → `SCR-010` (limited)

**Empty State:** N/A (always shows)
**Error State:** N/A

---

### SCR-003 — Phone Number Entry
**Role:** User

**Fields:**
- Country code selector (default: +92 Pakistan, supports other South Asian codes)
- Phone number input (10 digits for PK, validated format)
- "Continue" button

**Actions:**
- Tap "Continue" → validate format → call Firebase Auth `verifyPhoneNumber()`
- Rate limit: Max 3 OTP requests per phone per hour (Firebase + server-side)

**States:**
- Default: empty input
- Typing: real-time format validation (red border if invalid)
- Loading: spinner on button, disabled input
- Error — Invalid format: inline error "Enter a valid 10-digit number"
- Error — Rate limited: "Too many attempts. Try again in X minutes" + countdown timer
- Error — Network: "No internet connection. Check your connection."
- Success: Navigate to `SCR-004`

---

### SCR-004 — OTP Verification
**Role:** User

**Fields:**
- 6-digit OTP input (auto-filled from SMS if permission granted)
- "Verify" button
- "Resend OTP" (disabled 60s countdown)
- "Wrong number? Go back" link

**Actions:**
- Auto-submit on 6th digit entry
- Resend: calls Firebase again, resets 60s timer, max 3 resends
- Verify: calls Firebase `signInWithCredential()`

**States:**
- Default: empty 6 boxes
- Filling: boxes fill progressively, auto-advance
- Loading: spinner, boxes disabled
- Error — Wrong OTP: "Incorrect code. X attempts remaining" (max 5)
- Error — Expired OTP: "Code expired. Tap Resend."
- Error — Too many attempts: lock for 30 minutes with countdown
- Success: 
  - New user → `SCR-005` (Profile Setup)
  - Returning user + device matches → `SCR-010` (Home)
  - Returning user + new device → `SCR-008` (Device Mismatch)

---

### SCR-005 — Profile Setup (Step 1: Basic Info)
**Role:** User (new only; returning users use Edit Profile)

**Fields:**
- Display name (3–20 chars, alphanumeric + underscores, profanity filtered)
- Avatar picker: grid of 16 preset avatars (illustrated, no real photos required at MVP)
- Free Fire UID (numeric, 8–12 digits, validated format)
- Region selector: Pakistan (default) + optional city dropdown (Karachi, Lahore, Islamabad, Rawalpindi, Peshawar, Quetta, other)

**Actions:**
- "Next" button → validates all fields
- Avatar tap → opens avatar picker modal
- UID info icon → tooltip: "Find your UID in Free Fire → Profile → tap UID"

**Validation Rules:**
- Name: required, no special chars except underscore, not already taken
- UID: required, numeric only, 8–12 digits
- Region: required (Pakistan pre-selected)
- City: optional

**States:**
- Default: empty form
- Validation errors: inline red messages per field
- Loading (name availability check): spinner next to name field
- Name taken: "This name is already taken. Try another."
- UID format error: "UID must be 8–12 digits"
- Success: Navigate to `SCR-006`

---

### SCR-006 — Profile Setup (Step 2: Terms & Permissions)
**Role:** User

**Elements:**
- Summary card: "You're setting up as [Name]"
- Terms of Service checkbox (links to in-app ToS)
- "Platform not affiliated with Garena/Free Fire" disclaimer (prominent, required read)
- Age confirmation: "I am 13 years or older"
- Notification permission request (system dialog trigger)

**Actions:**
- "Create Account" button (disabled until both checkboxes checked)
- Triggers: device fingerprint capture, account creation call, device binding record creation

**States:**
- Button disabled: greyed with tooltip "Accept terms to continue"
- Loading: "Creating your profile..."
- Error — Username conflict (race condition): "Name taken, please go back and choose another"
- Error — Server: "Something went wrong. Please try again."
- Success: → `SCR-007` (Welcome to GoatsArena)

---

### SCR-007 — Welcome / Onboarding Complete
**Role:** User

**Elements:**
- Celebration animation (confetti)
- "Welcome, [Name]! You're in the Arena."
- Starting bonus display: "🎁 You've received 50 Welcome Coins!" (configurable)
- Quick action cards:
  - "Browse Tournaments" → `SCR-020`
  - "Buy Coins" → `SCR-040`
  - "Complete Profile" → (Phase 2 completeness feature)
- "Go to Home" button → `SCR-010`

---

### SCR-008 — Device Mismatch Warning
**Role:** User

**Elements:**
- Warning icon (not alarming, just informational)
- Message: "Your account is linked to a different device. For security, only one device can be active at a time."
- Options:
  - "Use this device" → triggers device-reset request creation → `SCR-009`
  - "Log out" → clears session, returns to `SCR-002`
- Note: "Using this device will log out your previous device within 24 hours (after admin review)"

**States:**
- Default: shows options
- After tapping "Use this device": "Reset request submitted. You'll be notified within 24 hours."

---

### SCR-009 — Device Reset Request Status
**Role:** User

**Elements:**
- Status card: Pending / Approved / Rejected
- If Pending: "Our team is reviewing your request. This usually takes up to 24 hours."
- If Approved: "Your device has been updated. Tap below to continue." → Button → `SCR-010`
- If Rejected: reason shown + "Contact support" link
- Request submission timestamp

---

## MODULE B: HOME & NAVIGATION

### SCR-010 — Home Screen
**Role:** User

**Layout:** Bottom navigation bar (persistent):
1. 🏠 Home
2. 🏆 Tournaments
3. 📋 Missions (Phase 2)
4. 🏅 Leaderboard
5. 👤 Profile

**Home Screen Content (Scrollable):**

**Header:**
- GoatsArena logo (left)
- Notification bell (right) with unread badge
- Coin balance chip (right): "💰 [balance]" → taps open wallet

**Banner Section:**
- Horizontal scrollable banners: active promotions, bonus topup offers, featured tournament
- Auto-scroll with dots indicator

**Active Tournaments (Upcoming in 24h):**
- Horizontal scroll card list
- Card shows: tournament name, game mode, entry fee, prize pool, slots remaining, time to start
- "Join" quick-action on card
- "See All" link → `SCR-020`

**My Active Joins:**
- Cards for tournaments user has joined (check-in pending / room info available / result pending)
- Quick status indicator + CTA button per card

**Daily Mission Teaser (Phase 2; MVP shows placeholder):**
- "Complete daily missions for bonus coins" → `SCR-080`

**Leaderboard Teaser:**
- Top 3 players this week + "See Full Leaderboard" → `SCR-070`

**Referral Banner:**
- "Invite friends, earn coins" → `SCR-090` (Phase 2)

**Empty States:**
- No active tournaments: "No tournaments live right now. Check back soon!" with "Browse All" button
- No active joins: "You haven't joined any tournaments yet." with "Browse" button

**Error State:**
- Network error: cached content shown + "Offline — showing cached data" banner + retry button

---

### SCR-011 — Notifications Center
**Role:** User

**Sections (tabbed):** All | Tournaments | Wallet | System

**Notification Card shows:**
- Icon (type-specific)
- Title + body (max 2 lines, truncated)
- Timestamp (relative: "2 min ago")
- Unread dot indicator
- Tap → deep link to relevant screen

**Actions:**
- "Mark all as read"
- Swipe to dismiss individual notification

**Empty State:** "No notifications yet. We'll let you know about tournaments and results here."

**Notification Types:**
- Tournament starting in 15 min (check-in reminder)
- Room ID now available
- Result approved / rejected
- Withdrawal status changed
- Coins credited/debited
- Mission completed (Phase 2)
- Streak milestone (Phase 2)

---

## MODULE C: TOURNAMENT SCREENS

### SCR-020 — Tournament Listing
**Role:** User

**Header:**
- Search bar (search by name)
- Filter button → `SCR-021` (Filter Sheet)

**Tabs:**
1. Upcoming
2. Live
3. My Entries
4. Completed

**Tournament Card (list item):**
- Tournament name
- Type badge: Custom Room / Scrims / League
- Mode: Solo / Duo / Squad
- Entry fee: "X coins" (or "FREE")
- Prize pool: "X coins total"
- Slots: "12/32 joined"
- Start time: formatted + countdown if < 24h
- Status badge: Open / Full / Live / Completed / Cancelled
- "Join" / "View" / "Check-in" CTA (context-aware)

**Filter Options (SCR-021 — Bottom Sheet):**
- Type: All / Custom Room / Scrims / League
- Mode: All / Solo / Duo / Squad
- Entry fee: Free / 0–50 / 50–200 / 200+
- Status: Upcoming / Live / Completed
- Reset filters button

**States:**
- Loading: skeleton cards (3 visible)
- Empty (no results): "No tournaments match your filters." + Reset filters button
- Empty (no upcoming): "No upcoming tournaments right now. Check back later!"
- Error: "Failed to load. Tap to retry."
- Pull-to-refresh supported

---

### SCR-022 — Tournament Detail
**Role:** User

**Header:**
- Back button
- Tournament name (bold)
- Share button (share link to tournament — Phase 2)

**Hero Section:**
- Tournament banner image (admin-uploaded or default by type)
- Type + Mode badges
- Status chip (Open / Full / Live / Results Pending / Completed)

**Info Grid:**
- 📅 Date & Time
- 💰 Entry Fee
- 🏆 Prize Pool (total coins)
- 👥 Slots: Filled / Total
- ⏱ Duration (estimated)
- 🌍 Region

**Prize Distribution Table:**
- Rank 1: X coins
- Rank 2: X coins
- Rank 3: X coins
- (configurable per tournament)
- Per-kill bonus: X coins/kill (if applicable)

**Rules Section (Expandable):**
- Text field with tournament-specific rules
- Fair play pledge reminder

**Participants Section:**
- "X players joined" with avatars of first 8 + "+Y more" overflow
- Tap → `SCR-023` (Participants List)

**Action Button Area:**
- **State: Open + not joined + enough coins:** "Join Tournament (X coins)" → confirmation dialog
- **State: Open + not joined + insufficient coins:** "Join Tournament" → disabled with "Insufficient coins — Top Up" link
- **State: Open + already joined + not check-in time:** "Joined ✓" badge + "Cancel" link (if cancellation window open)
- **State: Check-in window open:** "Check In Now" CTA (highlighted, countdown)
- **State: Checked in + room not revealed:** "Waiting for Room Info..." with countdown
- **State: Room revealed:** "View Room Info" → `SCR-025`
- **State: Room info seen + result not submitted:** "Submit Result" → `SCR-026`
- **State: Result submitted:** "Result Under Review"
- **State: Completed:** "View Results" → `SCR-028`
- **State: Cancelled:** "Tournament Cancelled — Coins Refunded"
- **State: Full:** "Tournament Full — Join Waitlist" (Phase 2) or "Full"

**States:**
- Loading: skeleton layout
- Error: retry prompt

---

### SCR-023 — Participants List
**Role:** User

**Elements:**
- Search bar (filter by name)
- List of joined players: avatar, name, rank badge, check-in status (✓ / -)
- Total count header

---

### SCR-024 — Join Confirmation Dialog
**Role:** User (Modal over SCR-022)

**Elements:**
- "Confirm Entry"
- Tournament name
- Entry fee: "X coins will be locked (not deducted until results)"
- Current balance: "Your balance: Y coins"
- After join: "Remaining available: Z coins"
- "By joining, you agree to the tournament rules and fair play pledge."
- Buttons: "Confirm Join" (primary) | "Cancel"

**States:**
- Tapping Confirm: loading spinner
- Success: brief success toast "You're in! Good luck." → SCR-022 updates
- Error — Concurrent full: "This tournament just filled up. Sorry!"
- Error — Insufficient: edge case warning (recheck balance)

---

### SCR-025 — Room Info Screen
**Role:** User (only for checked-in, registered players of that tournament)

**Elements:**
- Tournament name
- ⏰ Match starts in: countdown timer
- Room ID: `XXXXXXXX` (large, copyable, tap-to-copy)
- Password: `XXXX` (large, copyable, tap-to-copy)
- "Copy Room ID" button
- "Copy Password" button
- Instructions: "Join the room in Free Fire using above credentials. Do NOT share room info."
- Report room issue button (if room info is wrong)

**Security:**
- Screen recording blocked (FLAG_SECURE)
- Room info only accessible to verified check-in users
- Expires / greys out 30 min after match start

**States:**
- Before reveal time: "Room info will be revealed at [time]" + countdown
- Revealed: shows room ID + password
- Expired: "Match has started. Room info no longer shown."

---

### SCR-026 — Result Submission
**Role:** User

**Header:** "Submit Your Result — [Tournament Name]"

**Fields:**
- Placement rank: number input (1 to total-slots)
- Kills: number input (0–30)
- Screenshot upload: tap to upload from gallery or camera
  - Required: clearly shows in-game result screen with UID visible
  - Max 2 screenshots (result screen + optional rank screen)
  - File size: max 5MB each, JPEG/PNG
- "Any issues to report?" checkbox → if checked, expands dispute note field
- Submit button

**Rules reminder (inline):**
- "Submit within 30 minutes of match end."
- "Fake screenshots = permanent ban."

**States:**
- Image picker loading: progress bar
- Image too large: "Image too large. Max 5MB."
- Upload error: "Upload failed. Try again."
- Submission loading: "Submitting your result..."
- Success: → `SCR-027`
- Error — Window closed: "Submission window has closed."
- Already submitted: shows previously submitted result (read-only)

---

### SCR-027 — Result Submitted Confirmation
**Role:** User

**Elements:**
- Success checkmark animation
- "Result submitted! Our moderators will review within 2 hours."
- Submitted data summary (rank, kills, screenshots attached)
- "Track Status" → back to tournament detail (SCR-022) with "Under Review" status
- "Go Home" button

---

### SCR-028 — Tournament Results / Leaderboard
**Role:** User (public)

**Elements:**
- Tournament name + completion date
- Final results table:
  - Rank | Player name | Kills | Placement | Coins Won
  - Winner row highlighted (gold background)
  - Top 3 with medal icons
- "My Result" section (if user participated): their row highlighted blue
- Prize distribution note ("Prizes distributed to winners within 24 hours")

**States:**
- Results pending: "Results being finalized. Check back soon."
- Disputed results: "Some results are under dispute. Final results pending."

---

## MODULE D: DISPUTES

### SCR-029 — Dispute Submission
**Role:** User

**Elements:**
- Tournament name + my submitted result
- Dispute reason (radio buttons):
  - Wrong result recorded for me
  - Opponent cheated / used hack
  - Room host issue
  - Other
- Description text area (50–500 chars, required)
- Evidence upload (additional screenshots, max 3)
- "I confirm this dispute is genuine. False disputes may result in penalties."
- Submit button

**States:**
- Already disputed: shows submitted dispute (read-only)
- Loading / success / error states (standard)

---

### SCR-030 — Dispute Status
**Role:** User

**Elements:**
- Dispute ID
- Submitted date
- Status: Pending / Under Review / Resolved — Upheld / Resolved — Rejected
- Moderator note (if resolved)
- Timeline of status changes

---

## MODULE E: WALLET

### SCR-040 — Wallet Home
**Role:** User

**Header:** "My Wallet"

**Balance Card:**
- Available coins: large number
- Locked coins: smaller, with info icon ("Coins reserved for active tournament entries")
- Total coins: available + locked

**Action Buttons Row:**
- "Buy Coins" → `SCR-041`
- "Withdraw" → `SCR-045`
- "History" → `SCR-049`

**Recent Transactions (last 5):**
- Each row: icon | description | amount (+ green / - red) | date
- "See All" → `SCR-049`

**Active Topup Orders (if any pending):**
- Shows pending manual topup with payment instructions

---

### SCR-041 — Buy Coins — Tier Selection
**Role:** User

**Header:** "Top Up Coins"

**Tier Cards (3 visible, scrollable):**

| Tier | Pay | Get | Bonus | Badge |
|---|---|---|---|---|
| Starter | 600 PKR | 610 coins | +10 | — |
| Popular | 800 PKR | 820 coins | +20 | 🔥 Popular |
| Best Value | 1000 PKR | 1050 coins | +50 | ⭐ Best Value |

**Custom amount input** (Phase 2 with gateway): For MVP, only above tiers

**Notes:**
- "1 coin = 4 PKR"
- Bonus coins displayed prominently
- "Bonuses are configurable by GoatsArena admin"

**Actions:**
- Tap tier → "Proceed" button activates → `SCR-042`

---

### SCR-042 — Buy Coins — Payment Instructions
**Role:** User

**Header:** "Complete Payment"

**Elements:**
- Selected amount: "Pay 600 PKR → Get 610 coins"
- Payment methods tabs: Easypaisa | JazzCash | Bank Transfer
- Per method:
  - Account number / title to send to (admin-configured)
  - Amount to send: `600 PKR`
  - Reference note to include: "Your phone number last 4 digits"
- "I have made the payment" button → opens `SCR-043`
- Timer: "This order expires in 30 minutes"

**States:**
- After time expires: "Order expired. Please start again."

---

### SCR-043 — Topup Confirmation Form
**Role:** User

**Header:** "Confirm Your Payment"

**Fields:**
- Transaction ID / TID (from Easypaisa/JazzCash receipt): text input
- Payment method (auto-selected from previous step)
- Screenshot of payment receipt: upload (required)

**Actions:**
- "Submit for Review" → `SCR-044`

**Validation:**
- TID: required, alphanumeric
- Screenshot: required

---

### SCR-044 — Topup Order Status
**Role:** User

**Elements:**
- Order ID
- Amount submitted
- Coins to receive: X coins
- Status: Pending Review / Approved / Rejected / Coins Credited
- Timeline
- If rejected: reason + "Contact Support"
- If approved: animated coin credit notification

**Auto-refresh:** Every 30 seconds while on screen
**Push notification:** User notified when status changes

---

### SCR-045 — Withdraw — Request Form
**Role:** User

**Header:** "Withdraw Coins"

**Balance Info:**
- Available for withdrawal: X coins
- Weekly used: Y / 1200 coins
- Weekly remaining: Z coins

**Fields:**
- Withdrawal amount: numeric input
  - Minimum: 100 coins
  - Maximum: min(available balance, weekly remaining)
- Payment method: Easypaisa / JazzCash radio
- Account number: numeric input (verified against saved)
- Account name: text input
- "Save this account for future" checkbox

**Fee Preview (dynamic, updates as amount changes):**
- Example: Amount: 500 coins → Fee: X coins → You receive: Y coins
- Fee breakdown: "Platform fee: Z% + X fixed coins"
- Converted to PKR: "≈ [Y × 4] PKR"

**Submit button:** "Request Withdrawal"

**Validation:**
- Amount < 100: "Minimum withdrawal is 100 coins"
- Amount > weekly cap remaining: "Weekly limit reached. Resets on [date]."
- Amount > available: "Insufficient available balance"
- Account number format: validated per method

---

### SCR-046 — Withdraw Confirmation Dialog
**Role:** User (Modal)

**Elements:**
- Summary: amount, fee, net payout, account
- "Confirm Withdrawal" | "Cancel"
- "Once submitted, requests are processed manually and cannot be auto-cancelled."

---

### SCR-047 — Withdrawal Request Status (List)
**Role:** User

**List of all withdrawal requests:**
- Each item: date, amount, net payout, status chip (Pending/Approved/Paid/Rejected)
- Tap → `SCR-048`

---

### SCR-048 — Withdrawal Request Detail
**Role:** User

**Elements:**
- Request ID
- Amount requested / fee / net payout
- Account info (masked: last 4 digits)
- Status timeline with dates
- If paid: payment confirmation (TID if admin provided)
- If rejected: reason
- "Contact Support" link (all states)

---

### SCR-049 — Transaction History
**Role:** User

**Filter Tabs:** All | Credits | Debits | Locked | Tournaments

**Each Transaction Row:**
- Icon (type-specific)
- Description: "Tournament entry — [name]" / "Topup — Order #" / "Prize — [tournament]" / etc.
- Amount: +X / -X coins (colored)
- Date + time
- Status if applicable

**Empty State:** "No transactions yet. Start by buying coins or joining a tournament."

---

## MODULE F: LEADERBOARD

### SCR-070 — Leaderboard Hub
**Role:** User

**Tabs (top):**
- Global | Regional | My Rank

**Time Period Selector (segmented):**
- Weekly | Monthly | Season

**Mode Selector (Phase 2):**
- All | Custom Room | Scrims | League

**Leaderboard Table:**
- Rank | Player (avatar + name) | Score (coins won / points) | Badge

**My Rank Card (sticky at bottom if not in top 20):**
- "Your Rank: #234 | 450 coins this week"

**Top 3 Special Display:**
- Gold / Silver / Bronze podium visual at top

**States:**
- Loading: skeleton
- Empty: "Leaderboard populates after first week of season"
- My rank unavailable: "Complete at least 1 match to appear on leaderboard"

---

### SCR-071 — Regional Leaderboard
**Role:** User

**Selector:** Pakistan → City dropdown

**Same layout as SCR-070 but filtered to region/city.**

---

## MODULE G: PROFILE & SETTINGS

### SCR-060 — My Profile
**Role:** User

**Header:**
- Avatar (large)
- Display name + edit icon
- Rank badge (tier name + icon)
- "GoatsArena ID: #XXXXX"

**Stats Grid:**
- Total Matches
- Wins
- Top 3 Finishes
- Total Kills
- Total Earned (coins)
- Win Rate %

**Streak Section (Phase 2):**
- Current streak: "🔥 3 consecutive Top-3 finishes"
- Best streak ever

**Season Progress (Phase 2):**
- Current rank points
- Progress bar to next tier
- Season ends in: X days

**Recent Tournament History:**
- Last 5 tournaments: name, placement, coins won/lost
- "See all" → `SCR-062`

**Buttons:**
- "Edit Profile" → `SCR-061`
- "Share Profile" (Phase 2)

---

### SCR-061 — Edit Profile
**Role:** User

**Fields:**
- Display name (editable, 30-day cooldown on change)
- Avatar (change from presets)
- City (optional, changeable)

**Non-editable (shown greyed with info):**
- Phone number: "Contact support to change"
- Free Fire UID: "Contact support to change" (to prevent UID swapping fraud)

---

### SCR-062 — Match History (Full)
**Role:** User

**List with filters:** All / Wins / Custom Room / Scrims / League

**Each match row:** Tournament name, date, placement, kills, coins won/lost

---

### SCR-063 — Settings
**Role:** User

**Sections:**

**Account:**
- Linked phone number (masked)
- Device info + binding status
- "Request device reset" → `SCR-009`

**Notifications:**
- Toggle per notification type

**Privacy:**
- Profile visibility: Public / Friends Only (Phase 2) / Private

**About:**
- App version
- Terms of Service (in-app viewer)
- Privacy Policy
- "GoatsArena is not affiliated with Garena or Free Fire" (prominent disclaimer)
- Contact/Support link

**Danger Zone:**
- "Delete Account" (with confirmation: requires OTP re-auth)
- "Log Out"

---

## MODULE H: MISSIONS (Phase 2)

### SCR-080 — Missions Hub
**Role:** User

**Tabs:** Daily | Weekly | Achievements

**Mission Card:**
- Icon + title: "Win 1 match today"
- Progress bar: "0/1 completed"
- Reward: "+10 coins"
- Claim button (activates when completed, expires at reset)
- Streak indicator if applicable

**Daily Reset Countdown:** "Resets in X hours"
**Weekly Reset Countdown:** "Resets in X days"

**States:**
- Mission completed + unclaimed: highlighted + "Claim!" CTA
- Mission claimed: greyed, "Claimed ✓"
- Mission in progress: progress bar active
- All claimed: "Come back tomorrow!" celebration

---

## MODULE I: REFERRAL (Phase 2)

### SCR-090 — Referral Dashboard
**Role:** User

**Elements:**
- "Your referral code: GOAT-XXXX" + copy button + share button
- Reward info: "Earn 50 coins when your friend makes their first topup"
- Referred friends list: name (masked), join date, status (Joined/Topup Pending/Reward Earned)
- Total earned from referrals
- Anti-fraud note: "Referral rewards require unique devices and phone numbers"

---

# SECTION 4: USER JOURNEYS

## 4.1 Journey 1: New User Complete Onboarding

```
Open App (SCR-001)
    └─► Welcome Screen (SCR-002)
            └─► "Get Started"
                    └─► Phone Entry (SCR-003)
                            └─► Enter +92XXXXXXXXXX
                                    └─► OTP Sent (Firebase)
                                            └─► OTP Screen (SCR-004)
                                                    └─► Auto-fill / Manual entry
                                                            └─► Verified
                                                                    ├─► [New User] Profile Setup (SCR-005)
                                                                    │       └─► Name + Avatar + UID + Region
                                                                    │               └─► Terms & Permissions (SCR-006)
                                                                    │                       └─► Accept + Create Account
                                                                    │                               └─► Device fingerprint captured
                                                                    │                               └─► deviceBindings record created
                                                                    │                               └─► Welcome Screen (SCR-007)
                                                                    │                               └─► 50 welcome coins credited
                                                                    │                               └─► Home (SCR-010)
                                                                    │
                                                                    └─► [Returning User] Home (SCR-010)
```

---

## 4.2 Journey 2: Buy Coins (Manual Topup)

```
Home (SCR-010) → Coin balance tap → Wallet (SCR-040)
    └─► "Buy Coins" → Tier Selection (SCR-041)
            └─► Select "Best Value — 1000 coins package (Pay 4000 PKR) → Get 1050 coins"
                    └─► Proceed → Payment Instructions (SCR-042)
                            └─► Send 1000 PKR via Easypaisa to [admin account]
                                    └─► "I have made the payment"
                                            └─► Topup Form (SCR-043)
                                                    └─► Enter TID + Upload screenshot
                                                            └─► Submit → Topup Status (SCR-044)
                                                                    └─► Status: "Pending Review"
                                                                            └─► [Admin reviews in panel]
                                                                                    ├─► APPROVED
                                                                                    │    └─► 1050 coins credited to balance
                                                                                    │    └─► walletLedger: CREDIT 1050 coins
                                                                                    │    └─► Push notification sent
                                                                                    │    └─► User sees "Coins Credited! ✓"
                                                                                    │
                                                                                    └─► REJECTED (fraud/wrong amount)
                                                                                         └─► Rejection reason shown
                                                                                         └─► No coins credited
                                                                                         └─► Push notification sent
```

---

## 4.3 Journey 3: Join Tournament → Room Info → Submit Result

```
Home (SCR-010)
    └─► Browse Tournaments → Listing (SCR-020)
            └─► Tap tournament card → Detail (SCR-022)
                    └─► Check balance sufficient (100 coins entry fee)
                            └─► "Join Tournament (100 coins)"
                                    └─► Join Confirmation Dialog (SCR-024)
                                            └─► "Confirm Join"
                                                    └─► API: reserve 100 coins
                                                    └─► walletLedger: LOCK 100 coins
                                                    └─► balance.lockedCoins += 100
                                                    └─► balance.availableCoins -= 100
                                                    └─► Tournament slot count +1
                                                    └─► Success toast: "You're in!"
                                                    └─► Tournament detail refreshes

[T-15 minutes: Check-in window opens]
Push notification: "Check-in now for [Tournament]!"
    └─► Notification → SCR-022
            └─► CTA: "Check In Now"
                    └─► API: mark player checked-in
                    └─► Confirmation: "Checked in ✓"

[T-0: Scheduled reveal time]
Push notification: "Room info is ready!"
    └─► Notification → Room Info (SCR-025)
            └─► Room ID: 12345678 (tap to copy)
            └─► Password: abcd
            └─► Player joins room in Free Fire

[Match plays — approximately 25 minutes]

[T+30 min: Result submission window]
Push notification: "Submit your result now! Window closes in 30 min."
    └─► Notification → Result Submission (SCR-026)
            └─► Enter: Placement = 1, Kills = 8
            └─► Upload screenshot of result screen
            └─► Submit
                    └─► API: create result record (PENDING_REVIEW)
                    └─► SCR-027: "Result submitted!"

[Admin/Moderator reviews in panel]
    ├─► APPROVED
    │    └─► API: distribute prizes
    │    └─► walletLedger: UNLOCK 100 (entry fee) → DEBIT 100 coins
    │    └─► walletLedger: CREDIT prize amount (e.g., 500 coins for rank 1)
    │    └─► balance.availableCoins += (prize - entry fee) net
    │    └─► Tournament result published
    │    └─► Push: "You won 500 coins! 🏆"
    │    └─► Victory streak counter incremented
    │
    └─► REJECTED (fake screenshot)
         └─► Entry fee forfeited (DEBIT 100 coins)
         └─► Push: "Result rejected — reason shown"
         └─► Dispute window opens for player
```

---

## 4.4 Journey 4: Win → Withdraw Request

```
Home (SCR-010) — Coin balance: 620 coins
    └─► Wallet (SCR-040)
            └─► "Withdraw" → Withdrawal Form (SCR-045)
                    └─► Enter amount: 500 coins
                            └─► Fee preview: 5% + 10 fixed = 35 coins fee
                            └─► Net payout: 465 coins = ~1860 PKR
                    └─► Payment method: Easypaisa
                    └─► Account: 03XX-XXXXXXX
                    └─► "Request Withdrawal"
                            └─► Confirmation Dialog (SCR-046)
                                    └─► "Confirm"
                                            └─► API: create withdrawalRequest (PENDING)
                                            └─► walletLedger: LOCK 500 coins
                                            └─► balance.availableCoins -= 500
                                            └─► balance.lockedCoins += 500
                                            └─► Push: "Withdrawal request submitted"

[Finance Admin reviews in admin panel]
    ├─► APPROVED
    │    └─► Admin processes Easypaisa transfer manually
    │    └─► Admin marks: PAID + enters TID
    │    └─► walletLedger: DEBIT 500 coins (locked → deducted)
    │    └─► walletLedger: DEBIT 35 coins fee (already factored)
    │    └─► balance.lockedCoins -= 500
    │    └─► weeklyWithdrawal.used += 500
    │    └─► Push: "Payment sent! TID: XXXX. You'll receive ~1860 PKR shortly."
    │
    └─► REJECTED
         └─► walletLedger: UNLOCK 500 coins
         └─► balance.lockedCoins -= 500
         └─► balance.availableCoins += 500
         └─► Push: "Withdrawal rejected — [reason]"
```

---

## 4.5 Journey 5: Dispute Flow

```
Tournament result rejected → Player disagrees
    └─► SCR-022 (Tournament Detail) → "Dispute Result" button
            └─► SCR-029 (Dispute Submission)
                    └─► Select reason: "Wrong result recorded for me"
                    └─► Description: "I placed rank 2 but system shows rank 5"
                    └─► Upload additional evidence screenshot
                    └─► Submit
                            └─► disputes record created (PENDING)
                            └─► Assigned to next available moderator
                            └─► Push: "Dispute submitted. ID: #DIS-XXX"

[Moderator reviews in admin panel]
    ├─► UPHELD (player was right)
    │    └─► Result corrected
    │    └─► Prizes redistributed
    │    └─► Push: "Your dispute was upheld. Result corrected."
    │
    └─► REJECTED (screenshot verified correct)
         └─► Moderator note explains reasoning
         └─► Push: "Dispute reviewed. Original result stands."
```

---

# SECTION 5: ADMIN & MODERATOR PANEL

## 5.1 Role Definitions & Permissions Matrix

| Feature | Super Admin | Admin | Moderator | Finance |
|---|---|---|---|---|
| View all data | ✅ | ✅ | Partial | Partial |
| Create/Edit/Delete tournaments | ✅ | ✅ | ❌ | ❌ |
| Confirm topup orders | ✅ | ✅ | ❌ | ✅ |
| Process withdrawals | ✅ | ✅ | ❌ | ✅ |
| Review results | ✅ | ✅ | ✅ | ❌ |
| Resolve disputes | ✅ | ✅ | ✅ | ❌ |
| Ban/unban users | ✅ | ✅ | Temp ban only | ❌ |
| Device reset approval | ✅ | ✅ | ❌ | ❌ |
| Edit config (fees, bonuses) | ✅ | ❌ | ❌ | ❌ |
| View audit logs | ✅ | ✅ | Own actions | ❌ |
| Create admin accounts | ✅ | ❌ | ❌ | ❌ |
| Manage room IDs | ✅ | ✅ | ✅ | ❌ |
| View user financials | ✅ | ✅ | ❌ | ✅ |
| Announcements/push | ✅ | ✅ | ❌ | ❌ |

---

## 5.2 Admin Panel Pages

### AP-001 — Dashboard (Home)
**Role:** Admin, Super Admin, Finance

**Widgets:**
- Today's revenue (coins bought - coins withdrawn in PKR equivalent)
- Active users today / this week
- Open topup orders (count + urgent badge if > 10)
- Pending withdrawals (count + total coins)
- Active tournaments (live now)
- Pending result reviews
- Open disputes
- Flagged accounts (suspicious score > threshold)
- Recent audit log (last 10 entries)

**Charts:**
- New registrations (7-day line chart)
- Topup revenue (7-day bar chart)
- Tournament activity (7-day bar chart)

---

### AP-002 — User Management
**Role:** Admin, Super Admin

**User List:**
- Search by phone / name / UID / GoatsArena ID
- Filters: status (active/banned/suspended), region, joined date, suspicious score range
- Sort by: join date, earnings, suspicious score

**User List Columns:**
- Avatar | Name | Phone (masked) | Free Fire UID | Region | Balance | Joined Date | Status | Suspicious Score | Actions

**User Actions (per row):**
- View full profile
- Temporary suspend (1h / 24h / 7d)
- Permanent ban
- Reset device binding
- Manual coin adjustment (with mandatory reason — audited)
- View wallet ledger
- View tournament history

**Bulk Actions:**
- Export selected users (CSV)
- Bulk suspend

**User Detail Page:**
- All profile fields
- Device binding history
- Login history (device, IP, timestamp)
- Full wallet ledger
- Tournament history
- Disputes filed
- Active flags / suspicious activity log
- Audit trail of admin actions on this account

---

### AP-003 — Topup Order Management
**Role:** Admin, Finance

**Queue View:**
- Tabs: Pending | Approved | Rejected | All
- Filter by: date range, amount range, payment method
- Sort by: submission time (oldest first default)

**Order List Columns:**
- Order ID | User (name + phone) | Amount (PKR) | Coins | Method | TID | Submitted | Status | Actions

**Actions per order:**
- View full details
- View screenshot (payment receipt)
- Approve: confirm coins to credit (editable in case of bonus override) → credits coins → audit log
- Reject: mandatory rejection reason (dropdown + optional text) → no coins credited

**Safeguards:**
- Cannot approve same TID twice (TID uniqueness check across all orders)
- Cannot approve orders older than 48h without super admin override
- Suspicious order flag if TID not provided or duplicate

---

### AP-004 — Withdrawal Management
**Role:** Admin, Finance

**Queue View:**
- Tabs: Pending | Approved | Paid | Rejected
- Filter by: date range, amount, method, user
- Weekly total view: shows total coins/PKR in queue

**Withdrawal List Columns:**
- Request ID | User | Amount Coins | Fee | Net Coins | Net PKR | Method | Account (masked) | Requested | Status | Actions

**Actions per request:**
- View full user profile
- Approve request (unlocks for payment processing)
- Mark as Paid: enter TID (optional), confirmation required
- Reject: mandatory reason

**Bulk Processing (Phase 2):**
- Select multiple approved → Export payment CSV (for batch Easypaisa transfer)
- Bulk mark as paid after transfer

---

### AP-005 — Tournament Management
**Role:** Admin, Moderator (view + manage results), Super Admin

**Tournament List:**
- Filter by type, status, date
- Create new tournament button

**Tournament Create/Edit Form:**
- Name
- Type: Custom Room / Scrims / League
- Game mode: Solo / Duo / Squad
- Entry fee (coins)
- Total slots (must be multiple of appropriate squad size)
- Start date/time
- Check-in opens: minutes before start (configurable, default 30)
- Room info reveal time: minutes before start (configurable, default 5)
- Result submission window: minutes after start (configurable, default 60)
- Prize structure:
  - Per-rank prizes (dynamic rows: add/remove ranks)
  - Per-kill bonus (coins per kill, 0 = disabled)
- Banner image upload (optional)
- Rules text (rich text editor)
- Region (All / Pakistan / specific city)
- Status: Draft / Published / Cancelled

**Tournament Detail (Admin View):**
- All info above
- Room ID + Password (admin enters when ready to reveal)
- Participant list with check-in status
- Result submission status per player
- "Release Room Info" button (manual trigger or auto at set time)
- Prize distribution trigger (manual button after results approved)

---

### AP-006 — Result Review Queue
**Role:** Admin, Moderator

**Queue:**
- Pending result submissions
- Prioritized by: flagged, disputed, time waiting

**Result Review Card:**
- Tournament name + match ID
- Player name + Free Fire UID
- Submitted: rank, kills
- Screenshots (tap to enlarge, zoom supported)
- Other submissions for same tournament (comparison panel)
- Previous submission history for this player (fraud context)

**Actions:**
- Approve result
- Reject result with reason (dropdown: fake screenshot / incorrect data / duplicate / other)
- Flag as suspicious (escalates to admin)
- Add internal note (visible to all admins, not user)

**Dispute Indicator:**
- If player has disputed, shows dispute note + evidence alongside

---

### AP-007 — Dispute Queue
**Role:** Admin, Moderator

**List:**
- Pending | Under Review | Resolved
- Assigned to: me / all

**Dispute Detail:**
- All player-submitted info + evidence
- Original result + all other results for that match
- Ability to update result directly
- Resolve: Upheld / Rejected + mandatory moderator note
- Internal notes
- Escalate to admin (moderator → admin)

**Prize Redistribution:**
- When dispute upheld + result corrected: auto-recalculate prize distribution
- Confirm button triggers re-distribution

---

### AP-008 — Device Reset Queue
**Role:** Admin, Super Admin

**List:**
- Pending reset requests
- User info + old device fingerprint + new device fingerprint

**Actions:**
- Approve (updates device binding, logs old device as inactive)
- Reject with reason

**Anti-fraud indicators:**
- Time since last device reset (flag if < 30 days)
- Count of previous resets
- Suspicious activity score

---

### AP-009 — Config Panel
**Role:** Super Admin only

**Configurable values (with audit trail on every change):**

**Coins:**
- 1 coin = X PKR (default: 4)
- Welcome bonus coins for new users

**Topup Bonuses:**
- Tier 1: Min amount, bonus coins
- Tier 2: Min amount, bonus coins
- Tier 3: Min amount, bonus coins
- (Add/remove tiers)

**Withdrawal:**
- Minimum withdrawal coins
- Weekly cap coins per user
- Withdrawal fee: percent (%) + fixed coins
- Processing time SLA (displayed to users)

**Tournaments:**
- Default check-in window (minutes)
- Default result submission window (minutes)
- Default room reveal time (minutes)

**Anti-spam:**
- OTP requests per phone per hour
- Join requests per user per minute
- Withdrawal requests per user per day

**Suspicious Score:**
- Thresholds for auto-flag levels

**Payment Info:**
- Easypaisa account number, account name
- JazzCash account number, account name
- Bank transfer details

---

### AP-010 — Leaderboard Management
**Role:** Admin, Super Admin

**Actions:**
- View current season leaderboard (all modes + global)
- Start new season (resets points, archives previous season)
- View previous season archives
- Manual adjustment (rare — with mandatory reason + audit)
- Freeze leaderboard for prize calculation

---

### AP-011 — Audit Logs
**Role:** Admin, Super Admin (own actions also visible to Moderator)

**Columns:** Timestamp | Actor (admin name + role) | Action | Target (user/entity ID) | Details | IP Address

**Filter by:**
- Actor
- Action type
- Date range
- Target entity

**Actions logged (exhaustive list):**
- User ban/unban/suspend
- Coin manual adjustment
- Topup approve/reject
- Withdrawal approve/reject/mark paid
- Result approve/reject
- Dispute resolve
- Device reset approve/reject
- Config change (with old + new values)
- Tournament create/edit/cancel
- Admin account create/role change
- Prize distribution trigger
- Room ID set/reveal
- Season start/end

---

### AP-012 — Analytics (Phase 2)
**Role:** Admin, Super Admin

**Sections:**
- Revenue: daily/weekly/monthly topup volume, withdrawal volume, net platform revenue
- Users: registrations, MAU, DAU, retention cohorts, region breakdown
- Tournaments: tournaments/week, avg fill rate, avg prize pool, most popular types
- Engagement: mission completion rates, leaderboard participation, referral conversions
- Fraud: flagged accounts per week, disputed results %, fraud attempts blocked

---

### AP-013 — Announcements (Phase 2)
**Role:** Admin, Super Admin

**Actions:**
- Create push notification (title, body, target: all / region / specific user)
- Schedule for specific time
- View send history + open rates

---

# SECTION 6: DATA ENTITIES & SCHEMA

## 6.1 Complete Entity Definitions

### Entity: `users`
```
{
  userId: String (UUID, primary key),
  phoneNumber: String (E.164 format, unique, indexed),
  firebaseUID: String (unique),
  displayName: String (unique, indexed),
  avatarId: String (references preset avatar list),
  freefireUID: String (indexed),
  region: String (enum: Pakistan, ...),
  city: String (optional),
  status: Enum [active, suspended, banned],
  suspensionUntil: Timestamp (nullable),
  banReason: String (nullable),
  termsAcceptedAt: Timestamp,
  termsVersion: String,
  createdAt: Timestamp,
  updatedAt: Timestamp,
  lastLoginAt: Timestamp,
  lastLoginDevice: String (fingerprint ref),
  profileCompleteness: Integer (0-100, Phase 2),
  suspiciousScore: Float (0-100, default 0),
  referralCode: String (unique, generated on create),
  referredByUserId: String (nullable, FK users),
  totalMatchesPlayed: Integer,
  totalWins: Integer,
  totalKills: Integer,
  totalEarned: Integer (coins, lifetime),
  currentStreakCount: Integer,
  bestStreak: Integer,
  rankTier: Enum [Bronze, Silver, Gold, Platinum, Diamond],
  rankPoints: Integer,
  isProfileVerified: Boolean (UID screenshot verified, Phase 2),
  notificationTokens: Array<String> (FCM tokens, multi-device awareness),
  isAdmin: Boolean,
  adminRole: Enum [null, moderator, finance, admin, superadmin]
}
```

### Entity: `deviceBindings`
```
{
  bindingId: String (UUID),
  userId: String (FK users, indexed),
  deviceFingerprint: String (composite hash of: device model + Android ID + IMEI hash if available),
  androidId: String (hashed),
  deviceModel: String,
  osVersion: String,
  appVersion: String,
  ipAddressOnBind: String (hashed),
  status: Enum [active, superseded, admin_reset],
  boundAt: Timestamp,
  supersededAt: Timestamp (nullable),
  supersededByBindingId: String (nullable),
  resetRequestId: String (nullable, FK deviceResetRequests)
}
```

### Entity: `deviceResetRequests`
```
{
  requestId: String (UUID),
  userId: String (FK users),
  oldDeviceFingerprint: String,
  newDeviceFingerprint: String,
  reason: String (user-provided),
  status: Enum [pending, approved, rejected],
  adminNote: String (nullable),
  reviewedBy: String (FK users.adminId, nullable),
  requestedAt: Timestamp,
  reviewedAt: Timestamp (nullable)
}
```

### Entity: `balances`
```
{
  userId: String (FK users, primary key, 1:1 with user),
  availableCoins: Integer (>= 0),
  lockedCoins: Integer (>= 0),
  totalCoins: Integer (computed: available + locked),
  weeklyWithdrawnCoins: Integer (resets every Monday 00:00 PKT),
  weeklyResetAt: Timestamp (next Monday 00:00 PKT),
  lifetimeEarned: Integer,
  lifetimeWithdrawn: Integer,
  updatedAt: Timestamp
}
```
**Constraint:** `availableCoins + lockedCoins` must always equal sum of all non-cancelled `walletLedger` entries for this user (enforced by transaction atomicity).

### Entity: `walletLedger`
```
{
  ledgerId: String (UUID, primary key),
  userId: String (FK users, indexed),
  type: Enum [
    CREDIT_TOPUP,
    CREDIT_PRIZE,
    CREDIT_WELCOME_BONUS,
    CREDIT_REFERRAL,
    CREDIT_MISSION_REWARD,
    CREDIT_MANUAL_ADJUSTMENT,
    CREDIT_DISPUTE_REFUND,
    CREDIT_ENTRY_REFUND,
    DEBIT_ENTRY_FEE,
    DEBIT_WITHDRAWAL,
    DEBIT_WITHDRAWAL_FEE,
    DEBIT_MANUAL_ADJUSTMENT,
    LOCK_ENTRY_FEE,
    LOCK_WITHDRAWAL,
    UNLOCK_ENTRY_FEE,
    UNLOCK_WITHDRAWAL_REJECTED
  ],
  amount: Integer (always positive; type determines direction),
  balanceAfter: Integer (available balance after this transaction),
  lockedAfter: Integer (locked balance after this transaction),
  referenceType: Enum [topupOrder, withdrawalRequest, tournament, mission, referral, manual],
  referenceId: String (ID of related entity),
  description: String (human-readable),
  performedBy: String (userId or 'system' or adminId),
  createdAt: Timestamp (immutable),
  isReversed: Boolean (default false — only true if manually reversed by super admin),
  reversedAt: Timestamp (nullable),
  reversalLedgerId: String (nullable, FK ledger entry that reversed this)
}
```
**Immutability Rule:** No ledger entry is ever deleted or updated. Corrections create new ledger entries referencing the original. Reversals require super admin + mandatory reason + audit log.

### Entity: `topupOrders`
```
{
  orderId: String (UUID),
  userId: String (FK users, indexed),
  amountPKR: Integer,
  coinsRequested: Integer,
  bonusCoins: Integer,
  totalCoinsToCredit: Integer,
  paymentMethod: Enum [easypaisa, jazzcash, bank_transfer],
  transactionId: String (TID provided by user, indexed for duplicate check),
  screenshotUrl: String (Firebase Storage URL),
  status: Enum [pending, approved, rejected, expired],
  rejectionReason: String (nullable),
  reviewedBy: String (FK admin userId, nullable),
  createdAt: Timestamp,
  expiresAt: Timestamp (createdAt + 30 min for MVP),
  reviewedAt: Timestamp (nullable),
  coinsLedgerId: String (FK walletLedger, set on approval)
}
```

### Entity: `tournaments`
```
{
  tournamentId: String (UUID),
  name: String,
  type: Enum [custom_room, scrims, league],
  gameMode: Enum [solo, duo, squad],
  status: Enum [draft, published, registration_open, check_in, live, result_pending, completed, cancelled],
  entryFeeCoins: Integer,
  totalSlots: Integer,
  filledSlots: Integer,
  region: String,
  city: String (nullable),
  startTime: Timestamp,
  checkInOpensAt: Timestamp (computed: startTime - checkInWindowMinutes),
  roomRevealAt: Timestamp (computed: startTime - roomRevealMinutes),
  resultSubmissionDeadline: Timestamp (computed: startTime + resultWindowMinutes),
  checkInWindowMinutes: Integer,
  roomRevealMinutes: Integer,
  resultWindowMinutes: Integer,
  prizePool: Array<{rank: Integer, coinsAwarded: Integer}>,
  perKillBonus: Integer (0 = disabled),
  totalPrizeCoins: Integer (sum of prizePool),
  rules: String (markdown),
  bannerImageUrl: String (nullable),
  roomId: String (nullable, set by admin, revealed at roomRevealAt),
  roomPassword: String (nullable),
  createdBy: String (FK admin userId),
  createdAt: Timestamp,
  updatedAt: Timestamp,
  cancelledAt: Timestamp (nullable),
  cancellationReason: String (nullable),
  prizeDistributedAt: Timestamp (nullable),
  leagueSeasonId: String (nullable, FK leagueSeasons if type=league)
}
```

### Entity: `tournamentEntries`
```
{
  entryId: String (UUID),
  tournamentId: String (FK tournaments, indexed),
  userId: String (FK users, indexed),
  squadId: String (nullable, FK squads, Phase 2),
  status: Enum [registered, checked_in, no_show, disqualified, completed],
  registeredAt: Timestamp,
  checkedInAt: Timestamp (nullable),
  entryFeeLockLedgerId: String (FK walletLedger),
  entryFeeCoins: Integer (snapshot of fee at time of join),
  roomInfoViewedAt: Timestamp (nullable),
  isWaitlisted: Boolean (default false, Phase 2)
}
```

### Entity: `results`
```
{
  resultId: String (UUID),
  tournamentId: String (FK tournaments, indexed),
  entryId: String (FK tournamentEntries, indexed),
  userId: String (FK users, indexed),
  submittedPlacement: Integer,
  submittedKills: Integer,
  screenshotUrls: Array<String>,
  submittedAt: Timestamp,
  status: Enum [pending_review, approved, rejected, disputed],
  reviewedBy: String (FK admin userId, nullable),
  reviewedAt: Timestamp (nullable),
  rejectionReason: String (nullable),
  approvedPlacement: Integer (nullable, may differ from submitted),
  approvedKills: Integer (nullable),
  coinsAwarded: Integer (nullable, set on approval),
  prizeDistributionLedgerId: String (FK walletLedger, nullable),
  internalNote: String (nullable, admin-only),
  isFlagged: Boolean (default false)
}
```

### Entity: `disputes`
```
{
  disputeId: String (UUID),
  tournamentId: String (FK tournaments),
  resultId: String (FK results),
  userId: String (FK users, indexed),
  reason: Enum [wrong_result, cheating, room_host_issue, other],
  description: String,
  evidenceUrls: Array<String>,
  status: Enum [pending, under_review, resolved_upheld, resolved_rejected],
  assignedTo: String (FK admin userId, nullable),
  moderatorNote: String (nullable),
  resolution: String (nullable),
  submittedAt: Timestamp,
  resolvedAt: Timestamp (nullable),
  prizeRedistributed: Boolean (default false)
}
```

### Entity: `withdrawalRequests`
```
{
  requestId: String (UUID),
  userId: String (FK users, indexed),
  amountCoins: Integer,
  feeCoins: Integer,
  netCoins: Integer,
  feePercent: Float (snapshot of config at time of request),
  feeFixed: Integer (snapshot),
  netPKR: Integer (computed: netCoins × coinValuePKR),
  paymentMethod: Enum [easypaisa, jazzcash, bank_transfer],
  accountNumber: String (encrypted at rest),
  accountName: String,
  status: Enum [pending, approved, paid, rejected],
  rejectionReason: String (nullable),
  paymentTid: String (nullable, entered by finance on paid),
  reviewedBy: String (FK admin userId, nullable),
  reviewedAt: Timestamp (nullable),
  paidAt: Timestamp (nullable),
  lockLedgerId: String (FK walletLedger),
  debitLedgerId: String (FK walletLedger, set on paid),
  requestedAt: Timestamp,
  weeklyWithdrawalBucket: String (e.g., "2024-W03" for weekly cap tracking)
}
```

### Entity: `leaderboardEntries`
```
{
  entryId: String (UUID),
  userId: String (FK users, indexed),
  period: Enum [weekly, monthly, season],
  periodKey: String (e.g., "2024-W03", "2024-01", "S2"),
  scope: Enum [global, region, city],
  scopeValue: String (e.g., "Pakistan", "Karachi"),
  mode: Enum [all, custom_room, scrims, league],
  coinsEarned: Integer,
  matchesPlayed: Integer,
  wins: Integer,
  totalKills: Integer,
  rank: Integer (computed on leaderboard snapshot),
  rankTier: String (snapshot),
  updatedAt: Timestamp
}
```

### Entity: `missions` (Phase 2)
```
{
  missionId: String (UUID),
  type: Enum [daily, weekly, achievement],
  title: String,
  description: String,
  rewardCoins: Integer,
  requirement: {
    type: Enum [join_tournament, win_match, top3_finish, submit_result, topup, referral],
    count: Integer
  },
  isActive: Boolean,
  createdAt: Timestamp
}
```

### Entity: `userMissionProgress` (Phase 2)
```
{
  progressId: String (UUID),
  userId: String (FK users),
  missionId: String (FK missions),
  periodKey: String (day "2024-01-15" or week "2024-W03"),
  currentCount: Integer,
  targetCount: Integer,
  isCompleted: Boolean,
  isClaimed: Boolean,
  completedAt: Timestamp (nullable),
  claimedAt: Timestamp (nullable),
  rewardLedgerId: String (nullable, FK walletLedger)
}
```

### Entity: `referrals` (Phase 2)
```
{
  referralId: String (UUID),
  referrerUserId: String (FK users),
  referredUserId: String (FK users),
  referredDeviceFingerprint: String,
  status: Enum [pending, qualified, rewarded, fraud_flagged],
  qualificationEvent: String (e.g., "first_topup"),
  qualifiedAt: Timestamp (nullable),
  rewardCoins: Integer,
  rewardedAt: Timestamp (nullable),
  rewardLedgerId: String (nullable, FK walletLedger)
}
```

### Entity: `auditLogs`
```
{
  logId: String (UUID),
  actorId: String (admin userId or 'system'),
  actorRole: String,
  actionType: String (from exhaustive enum list in AP-011),
  targetEntityType: String (e.g., 'user', 'withdrawalRequest'),
  targetEntityId: String,
  previousValue: JSON (nullable, snapshot before change),
  newValue: JSON (nullable, snapshot after change),
  ipAddress: String (hashed),
  userAgent: String,
  timestamp: Timestamp (immutable),
  sessionId: String,
  note: String (mandatory for sensitive actions)
}
```

### Entity: `config`
```
{
  configId: String (always single record, or keyed by configKey),
  coinValuePKR: Integer (default 4),
  welcomeBonusCoins: Integer,
  topupBonusTiers: Array<{packageCoins: Integer, bonusCoins: Integer}>,
  withdrawalMinCoins: Integer,
  withdrawalWeeklyCap: Integer,
  withdrawalFeePercent: Float,
  withdrawalFeeFixed: Integer,
  checkInWindowMinutes: Integer,
  roomRevealMinutes: Integer,
  resultWindowMinutes: Integer,
  otpRateLimitPerHour: Integer,
  joinRateLimitPerMinute: Integer,
  withdrawalDailyRequestLimit: Integer,
  suspiciousScoreThreshold: Float,
  paymentAccounts: {
    easypaisa: {number: String, name: String},
    jazzcash: {number: String, name: String},
    bankTransfer: {details: String}
  },
  updatedAt: Timestamp,
  updatedBy: String (FK admin userId)
}
```

---

# SECTION 7: NON-FUNCTIONAL REQUIREMENTS

## 7.1 Security Requirements

### Authentication Security
- Firebase Auth for OTP — never store raw OTP in database
- All API endpoints require valid Firebase ID token (JWT, verified server-side)
- Token refresh handled automatically; expired tokens result in re-auth prompt
- Admin panel uses separate Firebase project/tenant with MFA enforced for all admin accounts

### Device Binding
- Device fingerprint = SHA-256(Android ID + device model + manufacturer + build fingerprint)
- Do NOT use IMEI (deprecated and requires dangerous permission)
- Fingerprint checked on every login session initiation
- Mismatch creates automatic device reset request; does not automatically allow login

### Data Security
- All sensitive fields encrypted at rest (AES-256): account numbers, phone numbers, IP addresses
- Firebase Storage rules: result screenshots and topup receipts readable only by uploader + admin service account
- HTTPS enforced on all network calls
- Certificate pinning on Android app (pin to GoatsArena backend + Firebase certificates)
- No PII in push notification payloads (only notification IDs for deep linking)
- Wallet ledger: append-only, no UPDATE/DELETE permissions for application service accounts

### API Security
- Rate limiting per endpoint per user:
  - OTP request: 3/hour per phone
  - Join tournament: 10/minute per user
  - Withdrawal request: 3/day per user
  - Result submission: 1 per tournament per user
  - Dispute submission: 1 per result per user
- Input validation server-side (never trust client)
- SQL injection prevention: Firestore rules + parameterized queries for any SQL layer
- File upload validation: type (JPEG/PNG only), size (5MB max), malware scan (Cloud Storage + VirusTotal API, Phase 2)

### Admin Panel Security
- Hosted separately from app backend (separate domain)
- IP allowlist for admin panel access (Phase 2 hardening)
- Session timeout: 30 minutes inactivity
- MFA required for all admin roles
- All sensitive actions require confirmation dialog + reason
- Read-only mode available for audit/review roles

## 7.2 Performance Requirements

| Operation | Target P50 | Target P95 | Notes |
|---|---|---|---|
| App cold start | < 2s | < 3s | Cached home |
| Tournament list load | < 1s | < 2s | Paginated, 20/page |
| Tournament detail load | < 800ms | < 1.5s | Cached 30s |
| Coin balance fetch | < 500ms | < 1s | Real-time Firestore listener |
| OTP send | < 3s | < 5s | Firebase dependent |
| Result submission (with photo upload) | < 5s | < 10s | Depends on image size + connection |
| Leaderboard load | < 1s | < 2s | Pre-computed snapshots |
| Push notification delivery | < 5s | < 30s | FCM |

**Optimization Strategies:**
- Firestore offline persistence enabled (home screen cached)
- Leaderboards pre-computed by scheduled Cloud Function (not computed on-the-fly)
- Image compression before upload (client-side, reduce to max 1080p)
- Pagination on all list screens (20 items per page)
- Skeleton loading states on all network-dependent screens

## 7.3 Availability & Reliability
- Target uptime: 99.5% (MVP) → 99.9% (Phase 2)
- Graceful degradation: app shows cached data if backend unreachable
- Firebase as primary backend: leverages Google's global infrastructure
- Critical operations (coin transactions) use Firestore transactions (atomic reads + writes)
- No critical operation depends on a single point of failure

## 7.4 Abuse Prevention

### Anti-Fraud Framework

**Layer 1 — Device-Level:**
- One device = one account enforced via device binding
- Device fingerprint stored server-side; client-provided fingerprint cross-checked
- Jailbreak/root detection: warn user and flag account (not instant ban — reduces false positives)
- Emulator detection: flag and review (emulators should not participate in prize tournaments)

**Layer 2 — Behavioral:**
- Suspicious Score (0–100) computed nightly by Cloud Function:
  - Multiple accounts same device fingerprint: +40
  - OTP from same phone recycled (SIM-swap attempt): +20
  - Result submission in < 5 minutes of match start: +15
  - Kill count outlier (> 2 standard deviations from tournament average): +20
  - Multiple disputes filed in short period: +10
  - Withdrawal right after joining (no matches played): +15
  - Referred accounts all from same device: +25
- Score > 50: auto-flag for manual review
- Score > 80: auto-temporary suspend (24h) + alert admin

**Layer 3 — Result Verification:**
- Screenshots stored immutably in Firebase Storage
- Moderator reviews each result with contextual comparison (other submissions in same match)
- Kill count plausibility: if submitted kills > tournament-mode maximum realistic kills → auto-flag
- Screenshot metadata checked (if available): timestamp must be within match window

**Layer 4 — Financial:**
- TID uniqueness: same transaction ID cannot be used twice (server-side check)
- Withdrawal to account number cross-referenced: single account number used by multiple users → flag
- Large withdrawal (> 500 coins) → require manual finance review regardless of auto-approval
- Referral reward: only pays out if referred user's device fingerprint differs from referrer's

**Layer 5 — Rate Limits (see Security section)**

---

## 7.5 Content Moderation
- Display names: profanity filter (server-side, regex + wordlist)
- Screenshots: stored but not publicly visible; admin-only access
- Dispute descriptions: length limits + profanity filter
- No public chat in MVP; when added (Phase 3), full content moderation required

## 7.6 Compliance & Legal
- Disclaimer on all app screens' settings: "GoatsArena is an independent community platform. Not affiliated with Garena, Free Fire, or any official entity."
- ToS and Privacy Policy accessible in-app without login
- Minimum age: 13 (self-declared on signup) — no collection of data known to be from under-13
- Data retention: wallet ledger retained permanently (financial record); user data retained 7 years post-account deletion per financial compliance; screenshots retained 90 days after match
- GDPR-inspired controls even for Pakistan market: account deletion removes PII, anonymizes financial records (replace name/phone with "deleted_user_[id]")

---

# SECTION 8: WHAT MAKES GOATSARENA NEXT-GEN

## 8.1 Ethical Retention Framework

### The Core Principle
GoatsArena retains users by **making the platform genuinely rewarding**, not by exploiting psychological weaknesses. Every retention mechanic passes the "Would I be comfortable explaining this mechanism to the user?" test.

---

### 8.2 Ethical Engagement Systems

#### Victory Streaks — Celebrating Real Achievement
```
Streak Logic:
- Streak increments: any Top-3 finish in any tournament
- Streak breaks: if player joins tournament and finishes outside Top-3 (OR misses check-in)
- Streak does NOT break if player simply doesn't play (no punishment for taking breaks)

Streak Milestones & Rewards:
- 3-streak: "Hot Streak 🔥" badge + 15 bonus coins
- 5-streak: "On Fire 🌟" badge + 30 bonus coins  
- 10-streak: "Unstoppable 💎" badge + 75 bonus coins + profile frame unlock
- 25-streak: "GoatsArena Legend 🐐" badge + 200 bonus coins + exclusive avatar

Ethical Design Choices:
✅ Rewards milestone, not continuous — no "you're about to lose your streak" anxiety
✅ Taking a day off doesn't punish you
✅ Reward is coin-based (real value) not just cosmetic pressure
```

#### Rank Tiers — Progress That Feels Earned
```
Tiers: Bronze (0-99 pts) → Silver (100-299) → Gold (300-599) → Platinum (600-999) → Diamond (1000+)

Points earned per:
- Top-1 finish: 20 points
- Top-3 finish: 10 points
- Top-10 finish: 3 points
- Participation (check-in + result submitted): 1 point

Season: 8 weeks → reset to calibration (retain 20% of points as "legacy" — doesn't start from 0)

Ethical Design:
✅ Soft reset: players don't lose everything — reduces frustration
✅ Rank shown only on profile/leaderboard — not used to gatekeep tournaments
✅ Diamond players displayed with prestige but can still compete in any bracket
✅ Season end: all Diamond players get "Diamond Season X" badge permanently
```

#### Daily & Weekly Missions — Reward What Players Do Anyway
```
Mission Design Rules:
1. Missions should match natural play behavior — never require unnatural grinding
2. All missions completable in <= 1 hour of play
3. Missions never expire mid-stream (daily resets at midnight PKT, weekly on Monday)
4. Never punish for missing a day — no "streak lost" messaging on missions

Sample Daily Missions:
- "Check in to any tournament today" → +5 coins
- "Submit your match result" → +5 coins  
- "Visit the leaderboard" → +2 coins (engagement, not grind)

Sample Weekly Missions:
- "Place Top-3 in 2 matches this week" → +50 coins
- "Join 3 different tournaments" → +30 coins
- "Refer a friend who registers" → +25 coins (Phase 2)

Ethical Guardrail:
- Max 15 coins/day from missions (prevents mission farming exploits)
- Missions cannot require purchases ("Buy X coins → earn Y") — that's predatory
- Mission rewards clearly shown upfront, no surprise reductions
```

#### Leaderboards — Competition Without Toxicity
```
Design Choices:
✅ Weekly reset: fresh competition every week (prevents permanent "untouchable" top players)
✅ Regional boards: local heroes visible (motivates city-level competition)
✅ "My Rank" always visible — not just top 100 (everyone knows where they stand)
✅ Mode-separated boards: scrims players compete with scrims players
✅ No "you dropped X ranks" anxiety notifications — only positive milestone notifications
✅ Privacy option: players can opt out of public leaderboard (shows anonymized entry)

Leaderboard-based Rewards (Season End):
- Top 1 each region: 1000 coins + exclusive "Season Champion" badge
- Top 10 global: 500 coins + podium badge
- Everyone who played ≥ 5 matches: participation certificate (in-app)
```

---

## 8.3 Trust Architecture — Why Players Stay

### Transparent Economics
```
Every financial interaction shows:
- Exact coins locked (not hidden)
- Exact fee before withdrawal (not revealed at payment)
- Exchange rate always visible (1 coin = 4 PKR, shown in wallet)
- Topup bonus shown before payment (not after)
```

### Result Integrity System
```
Multi-layer verification creates trust:
1. Screenshot mandatory → cheaters can't lie without evidence
2. Moderator review → human verification, not just algorithmic
3. Dispute window → players have recourse
4. Audit trail → every decision logged, appealable
5. Public results → tournament results visible to all (accountability)

Trust Signal displayed to users:
- "X results reviewed by moderators this week"
- "X disputes resolved this month"
- Response time SLA displayed: "Results reviewed within 2 hours"
```

### Community Accountability
```
- Player's dispute history visible to admins (serial false disputants flagged)
- Result rejection reason explained to player (not a black box)
- Appeals: one re-appeal per result per player (escalates to Admin from Moderator)
- Fake screenshot = permanent ban (enforced consistently, no exceptions for high-spenders)
```

---

## 8.4 Platform Sustainability = Player Trust

### Revenue Model That Doesn't Exploit
```
GoatsArena earns from:
1. Withdrawal fee (5% + 10 coins on cashouts) — players who win and withdraw pay a small fee
2. Entry spread (not applicable for MVP — may add organizer fee in Phase 3)
3. Future: Rewarded ads (ethically capped), sponsored tournaments, premium membership

GoatsArena does NOT earn from:
❌ Pay-to-win mechanics (coins don't improve tournament performance)
❌ Loot boxes or gambling mechanics
❌ Artificial scarcity of tournament slots for paying users
❌ Selling user data
```

### The Compounding Trust Loop
```
Player wins tournament
    → Prize credited transparently
    → Withdrawal processed with clear fee
    → Payment arrives as promised
    → Player tells friends → referral
    → Community grows
    → More tournaments → more prize pools
    → Better experience for everyone
```

---

## 8.5 What Makes GoatsArena Different From Generic Tournament Apps

| Feature | Generic Tournament App | GoatsArena |
|---|---|---|
| Entry fee lock | Deducted immediately | Locked, returned if cancelled |
| Dispute resolution | Email + wait | In-app + SLA + status tracking |
| Withdrawal transparency | Hidden fees | Fee shown before request |
| Device security | IP check only | Device fingerprint binding |
| Streak system | Break if you miss a day | Only breaks on bad performance |
| Leaderboard reset | Never (discourages new players) | Weekly + Season (stays competitive) |
| Result verification | Screenshot + auto-approve | Screenshot + human review |
| Fraud prevention | Basic rate limits | Multi-layer behavioral scoring |
| Admin accountability | No logs | Immutable audit logs for all actions |
| Config transparency | Hardcoded | Configurable + logged every change |

---

# SECTION 9: TECHNICAL ARCHITECTURE OVERVIEW

## 9.1 Technology Stack

```
ANDROID APP:
- Language: Kotlin
- Architecture: MVVM + Clean Architecture (Domain/Data/Presentation layers)
- UI: Jetpack Compose (Material 3)
- Navigation: Compose Navigation with typed routes
- DI: Hilt
- Async: Coroutines + Flow
- Network: Retrofit + OkHttp (for backend APIs)
- Firebase SDK: Auth, Firestore, Storage, Messaging (FCM), Crashlytics
- Image: Coil
- Security: EncryptedSharedPreferences, BiometricPrompt (Phase 2)
- Analytics: Firebase Analytics (privacy-first config)

BACKEND:
- Firebase Firestore (primary database)
- Firebase Cloud Functions (Node.js/TypeScript) — business logic, scheduled jobs
- Firebase Auth (phone OTP)
- Firebase Storage (screenshots, receipts)
- Firebase Cloud Messaging (push notifications)
- Optional Phase 2: Express.js API layer on Cloud Run for complex operations

ADMIN PANEL:
- React.js + TypeScript
- Firebase Admin SDK
- Hosted: Firebase Hosting or Vercel
- Auth: Firebase Auth with admin tenant + MFA

SECURITY:
- Cloud Armor (DDoS protection, Phase 2)
- Firebase App Check (blocks non-app traffic to Firestore)
- Secret Manager (API keys, payment credentials)
```

## 9.2 Key Cloud Functions

```typescript
// Scheduled: runs every minute — handles auto-state transitions
tournamentStateManager(): 
  registration_open → check_in (at checkInOpensAt)
  check_in → live (at startTime)
  live → result_pending (at resultSubmissionDeadline)

// Triggered: on result approval — distributes prizes atomically
distributePrizes(tournamentId):
  Firestore transaction:
    - Calculate prizes per approved result
    - Create CREDIT_PRIZE ledger entries
    - Create DEBIT_ENTRY_FEE ledger entries  
    - Update balance documents
    - Update tournament status to completed

// Scheduled: nightly — computes suspicious scores
computeSuspiciousScores():
  For each user with activity in last 7 days:
    - Check device binding anomalies
    - Check result submission patterns
    - Check withdrawal patterns
    - Update users.suspiciousScore

// Scheduled: Monday 00:00 PKT — resets weekly withdrawal limits
resetWeeklyWithdrawalLimits():
  Update all balances: weeklyWithdrawnCoins = 0, weeklyResetAt = next Monday

// Scheduled: nightly — computes leaderboard snapshots
computeLeaderboardSnapshots():
  Aggregate tournament results by period/scope/mode
  Write to leaderboardEntries collection
  Compute ranks within each group
```

## 9.3 Firestore Security Rules (Key Rules)

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Users can read their own data only
    match /users/{userId} {
      allow read: if request.auth.uid == userId || isAdmin();
      allow write: if false; // Only Cloud Functions write to users
    }
    
    // Wallet ledger: append-only, no client writes
    match /walletLedger/{ledgerId} {
      allow read: if request.auth != null && 
                     resource.data.userId == request.auth.uid;
      allow write: if false; // Cloud Functions only
    }
    
    // Balances: read own, no client writes
    match /balances/{userId} {
      allow read: if request.auth.uid == userId;
      allow write: if false;
    }
    
    // Tournaments: public read, admin write
    match /tournaments/{tournamentId} {
      allow read: if true; // Public browsing
      allow write: if isAdmin();
    }
    
    // Room info: only checked-in participants, only after reveal time
    match /tournaments/{tournamentId}/roomInfo/{doc} {
      allow read: if isCheckedInParticipant(tournamentId) && 
                     isAfterRevealTime(tournamentId);
      allow write: if isAdmin();
    }
    
    // Results: write own, read after tournament complete
    match /results/{resultId} {
      allow create: if request.auth.uid == request.resource.data.userId &&
                       isWithinSubmissionWindow(request.resource.data.tournamentId);
      allow read: if request.auth.uid == resource.data.userId || isAdmin();
      allow update: if false; // Cloud Functions only
    }
    
    // Withdrawal requests: create own, read own
    match /withdrawalRequests/{requestId} {
      allow create: if request.auth.uid == request.resource.data.userId;
      allow read: if request.auth.uid == resource.data.userId || isAdmin();
      allow update: if false;
    }
    
    function isAdmin() {
      return request.auth.token.admin == true;
    }
    
    function isCheckedInParticipant(tournamentId) {
      return exists(/databases/$(database)/documents/tournamentEntries/
        $(tournamentId + '_' + request.auth.uid)) &&
        get(/databases/$(database)/documents/tournamentEntries/
        $(tournamentId + '_' + request.auth.uid)).data.status == 'checked_in';
    }
  }
}
```

---

# SECTION 10: OPEN QUESTIONS & ASSUMPTIONS

## 10.1 Assumptions Made
1. **Manual payment processing** is acceptable for MVP (no payment gateway integration required immediately)
2. **Pakistan** is the primary market; multi-region is Phase 2+ with Pakistan-city granularity sufficient for MVP
3. **Admin team size:** 1–3 people managing tournaments and reviews initially
4. **Tournament frequency:** 3–5 tournaments per day at launch
5. **Average prize pool:** 500–2000 coins per tournament
6. **Firebase** is the chosen backend (no self-hosted database for MVP)
7. **Free Fire UID** is self-declared in MVP; screenshot verification is Phase 2
8. **Squad tournaments** (Phase 2) — MVP handles solo tournaments primarily

## 10.2 Open Questions for Stakeholder Resolution

| # | Question | Options | Default Assumption |
|---|---|---|---|
| 1 | How quickly must topup orders be reviewed? | 1hr SLA / 4hr / 24hr | 4 hours |
| 2 | What withdrawal fee structure? | % only / % + fixed / tiered | 5% + 10 coins fixed |
| 3 | Welcome bonus coins for new users? | 0 / 25 / 50 / 100 | 50 coins |
| 4 | Can a user cancel a tournament entry? | No / Yes with full refund / Yes with partial refund | Yes, if > 1 hour before start |
| 5 | What happens to entry fees of no-shows? | Returned / Forfeited to prize pool | Forfeited to prize pool |
| 6 | Moderator dispute resolution SLA? | 1hr / 2hr / 4hr / 24hr | 2 hours (shown to users) |
| 7 | Should coin gift feature be in MVP? | Yes / Phase 2 | Phase 2 |
| 8 | Public API for tournament results? | Yes / No | No (MVP) |
| 9 | Support channel? | In-app chat / WhatsApp link / Email | WhatsApp link in settings (MVP) |
| 10 | Screenshot watermarking to prevent reuse? | No / Client-side / Server-side | Server-side Phase 2 |

---

## Document Sign-off Checklist

Before implementation begins, confirm:

- [ ] Coin economics reviewed and approved (entry fees, prize pools, withdrawal fees)
- [ ] Payment account details configured (Easypaisa/JazzCash admin account)
- [ ] Admin team roles assigned and accounts created
- [ ] Firebase project provisioned and security rules reviewed
- [ ] ToS and Privacy Policy drafted by legal/stakeholders
- [ ] "Not affiliated with Garena/Free Fire" disclaimer approved
- [ ] Initial tournament schedule planned for launch week
- [ ] Anti-fraud thresholds reviewed and calibrated
- [ ] Withdrawal SLA communicated to community before launch
- [ ] Beta test group identified (50–100 trusted community members for soft launch)

---

*Document prepared for GoatsArena by product design & architecture review.*
*Version 1.0 — Ready for engineering handoff.*
*All entity schemas subject to Firebase data modeling review before implementation.*