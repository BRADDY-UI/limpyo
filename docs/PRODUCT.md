# Limpyo — Product Spec

## 1. Problem (from the eTrakYan study)
- Collection schedules in General Santos are irregular and poorly communicated; some puroks wait weeks.
- Residents can't see where trucks are, so garbage goes out too early and piles up.
- Reporting uncollected garbage or illegal dumping means calling or visiting an office, so most cases go unreported.
- Residents don't know how to segregate (RA 9003), and the barangays have no shared measure of cleanliness.

## 2. Goals and non-goals
**Goals:** residents always know when the truck comes and where it is now; reporting a problem takes under 30 seconds; officials see problems and service quality in one place; segregation help happens on the spot.
**Non-goals (v1):** iOS, redeemable prizes, route optimization, SMS, payments, fleet maintenance or fuel, official LGU integration.

Limpyo is an independent open-source project, not an official LGU service. This is stated on the welcome screen, in About, on the landing page and in the README. All demo data is labeled "Demo".

## 3. Roles
| Role | Where | Created by | Scope |
|---|---|---|---|
| Guest | App | nobody (no account) | View schedules, live map, guide, scanner. Cannot report or earn points. |
| Resident | App | self sign-up | Own barangay by default; can view any barangay. |
| Driver | App (driver mode) | invite by admin | Assigned truck and routes only. |
| Barangay officer | Admin web | invite by admin | One barangay: its reports, schedules, announcements. |
| City admin | Admin web | invite by super admin | All barangays, users, trucks, drivers. |
| Super admin | Admin web | seeded (Brad) | Everything, including role changes and config. |

A person has exactly one role. Staff roles can't be self-assigned. Any role change is written to the audit log.

## 4. Features and rules

### 4.1 Accounts and onboarding
- Sign-in: Google (one tap) or email and password with email verification. No phone OTP, because SMS costs money.
- Guest mode: "Explore without an account" on the welcome screen. Signing up is only asked for when the guest tries to report, earn points, or turn on alerts.
- Onboarding: language, barangay (map or list, or auto-detect from coarse location if allowed), an optional home pin for "truck nearby" alerts, and the notification permission with a primer screen first.
- Profile: display name (shown on leaderboards, editable, profanity-filtered), barangay, home pin, language, theme.
- Account deletion in-app. It removes personal data, anonymizes reports (kept for barangay stats), and runs within 24 h.

### 4.2 Schedules
- A schedule is barangay + optional zone/purok + weekday(s) + time window + waste type + route + truck, with effective dates.
- Exceptions override one date: cancelled, moved (new date and time), or added (extra pickup). Holidays are entered manually.
- Today's status for a barangay is derived from driver events, never set by hand:
  `Scheduled` → `On the way` (shift started for that route) → `In your area` (first stop in the zone done) → `Collected` (last stop done) | `Missed` (window ended + 30 min with no stop events) | `Cancelled` / `Moved`.
- "Missed" automatically opens a system report for the barangay officer.
- Residents see: next collection card, a 7-day strip, a month calendar, and the waste type per day.

### 4.3 Live tracking
- Only trucks on an active shift are visible. Location is never shared off-shift.
- The resident map shows trucks for the selected barangay, the route line (done vs remaining), and the next stops.
- ETA: remaining route distance ÷ the truck's recent average speed, shown as a range ("10–15 min"). If there's no recent ping, it shows "Last seen 3 min ago" instead.
- "Truck nearby" alert: when a driver completes a stop, residents whose home pin is within 400 m of the next 1–2 stops get a push. It's based on stops, not continuous GPS, so it's cheap and private.
- Demo mode (Settings > About > Demo): replays a recorded route locally so anyone can see live tracking. It's labeled "Demo truck".

### 4.4 Reports
- Categories: Uncollected garbage, Illegal dumping, Overflowing collection point, Burning of waste, Clogged canal or drainage, Missed collection, Other.
- Flow: photo (camera or gallery) → on-device redaction (faces blurred, text pixelated, EXIF stripped) → resident reviews the redacted photo → category, short note (max 280 characters), location (GPS pin, adjustable) → submit. Barangay is auto-detected from the pin.
- Lifecycle:
  | State | Set by | Meaning |
  |---|---|---|
  | submitted | resident | received |
  | in_review | officer | someone is looking |
  | verified | officer | real issue confirmed (points awarded here) |
  | in_progress | officer | action scheduled or assigned |
  | resolved | officer | fixed (optional "after" photo) |
  | rejected | officer | not valid; reason required (spam, not waste, outside area, duplicate) |
  | merged | officer/system | folded into another report |
- Duplicate guard: same category within 50 m and 72 h of an open report → "Someone already reported this. Add your voice?" A "Me too" adds a confirmation to the existing report instead of creating a new one.
- Limits: 5 reports per user per day, 2 min between reports, 1 photo per report (max 300 KB after compression).
- Moderation: notes in English, Filipino or Bisaya are checked against word lists. Hits go to the review queue flagged and are never auto-published. Rejecting 3 reports as spam within 30 days locks reporting for 7 days.
- Residents see a status timeline and get a push on every state change. Report photos are visible only to the reporter and staff.

### 4.5 Waste scanner and segregation guide
- Scan: the camera has a center frame ("Fit one item in the box") and runs on-device in under 0.3 s. Classes: Biodegradable, Recyclable, Residual, Special/Hazardous, E-Waste, plus a "Not waste" gate.
- The result shows the category, how to dispose of it in GenSan, which collection day accepts it in the user's barangay, and a confidence word ("Likely" / "Not sure"). Numbers are never shown.
- Low confidence shows "Not sure". The user picks from the 3 most likely categories or searches the guide.
- "Was this right?" Yes or No + correct category. Opt-in to share the photo (redacted) to improve the model. Off by default.
- Guide: 5 categories, about 150 common items with English, Filipino and Bisaya names, searchable offline, with tips and do/don't lists.

### 4.6 Announcements
- City-wide or for specific barangays, normal or urgent, optional schedule and expiry, plain text and optional link. No images in v1.
- Urgent announcements push immediately and bypass quiet hours. Normal ones respect quiet hours.

### 4.7 Notifications (catalog)
| Type | Trigger | Audience | Default |
|---|---|---|---|
| Collection reminder (evening) | 7:00 PM the day before | residents of the barangay | on |
| Collection reminder (morning) | 30 min before the window | residents of the barangay | off |
| Truck on the way | shift started on the barangay's route | residents of the barangay | on |
| Truck nearby | stop completed near the home pin | residents with a pin | on |
| Schedule change | exception created | residents of the barangay | on (urgent) |
| Report status | state change | the reporter | on |
| Announcement | published | target barangays | on |
| Badge / level up | earned | the user | on |
| Driver: route change | schedule or route edited for today | assigned driver | always |
| Officer: new report | submitted in the barangay | barangay officers (web + email digest) | on |
- Quiet hours are 9:00 PM–6:00 AM (editable). Each push carries a stable id, so a resend replaces the notification instead of duplicating it (kept from the thesis design).

### 4.8 Points, levels, badges, leaderboard
- Points are only earned for actions that help, and only after verification:
  | Action | Points | Rule |
  |---|---|---|
  | Report verified | +20 | +10 more when resolved |
  | "Me too" on a report that gets verified | +5 | max 3 per day |
  | Scanner feedback given | +1 | max 5 per day |
  | First report in a new month | +10 | streak bonus |
  | Report rejected as spam | −20 | |
  Daily cap is 50 points. Every change is a row in an append-only ledger.
- Levels (names in all 3 languages): 1 Newcomer 0, 2 Helper 100, 3 Neighbor 300, 4 Guardian 700, 5 Champion 1500.
- Badges (v1): First Report, Five Verified, Sharp Eye (3 reports resolved in 1 week), Sorter (25 scans with feedback), Early Bird (report before 7 AM, verified), Barangay Hero (top 3 in the barangay for a month).
- Barangay leaderboard (monthly, city-wide), built only from real data:
  `score = 40% on-time collection rate + 25% report resolution rate + 15% median response-time score + 20% verified participation per 1,000 residents`.
  Each barangay shows its breakdown, so the number is explainable.
- Resident leaderboard: inside one barangay only, monthly, display names only. Anyone can hide themselves.
- Prizes: none in v1. An admin config flag reserves a "Rewards" area for future sponsored rewards.

### 4.9 Driver mode
- Same app. Driver accounts open in driver mode (separate bottom nav).
- Today: assigned truck, route(s), start time, stop count. "Start shift" asks for location (foreground service with a persistent notification).
- Active shift: full-screen map, next stop card with big "Done" and "Skip" buttons (52 dp+), stop list, "Report issue" (truck breakdown, road blocked, area full, other).
- End shift: summary (stops done/skipped, distance, time), optional note. Location sharing stops immediately.
- Works on weak signal: events queue locally and sync later with their original timestamps.

### 4.10 Admin web (officer / city admin / super admin)
Overview, live fleet, reports queue, schedules and exceptions, routes and stops editor, trucks, people (invite, role, deactivate), announcements, leaderboard and scores, guide content, scanner feedback review, audit log, settings. Officers see only their barangay. Details are in SCREENS.md.

### 4.11 Settings (app)
Language (English / Filipino / Bisaya), theme (Dark default / Light / System), notifications (per type + quiet hours), home pin, privacy (scanner photo sharing, leaderboard visibility, download my data, delete account), About (disclaimer, version, open-source licenses, OSM attribution, demo mode).

## 5. Key journeys (acceptance tests)
1. A guest opens the app, picks Lagao, and sees the next collection and live truck in under 10 s.
2. A resident reports illegal dumping with a photo in under 30 s, on 3G, offline-tolerant.
3. An officer verifies the report on the web; the resident gets a push and +20 points within 10 s.
4. A driver starts a shift; residents of the barangay get "Truck on the way" and see the truck move smoothly.
5. A resident scans a plastic bottle and gets "Recyclable" with the right GenSan disposal note.
6. An admin cancels tomorrow's collection; residents get an urgent schedule-change push.
