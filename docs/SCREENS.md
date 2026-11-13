# Limpyo — Screen Inventory

Each screen lists purpose, content, actions, and states. States always covered: loading (skeleton), empty, error (retry), offline (cached + banner), no permission.
IDs are used in code (`Route` names) and in tickets.

## Navigation map
```
Launch → (first run) ONB-01..06 → R-HOME
Resident bottom nav: Home (R-HOME) · Schedule (R-SCH) · Report (R-REP) · Scan (R-SCN) · You (R-YOU)
Driver bottom nav:   Today (D-TODAY) · Shift (D-SHIFT, only when active) · Inbox (D-INBOX) · You (R-YOU)
Push deep links: limpyo://report/{id}, limpyo://schedule/{date}, limpyo://announcement/{id}, limpyo://map
Admin web: /login → /mfa → sidebar sections
```

## A. Onboarding and auth (app)
| ID | Screen | Content and actions | Notes |
|---|---|---|---|
| ONB-01 | Welcome | wordmark, one line ("Know when the truck comes."), Continue with Google, Use email, Explore without an account; small disclaimer "Independent project, not an official LGU app" | |
| ONB-02 | Language | English / Filipino / Bisaya, big radio rows | changeable later |
| ONB-03 | Privacy and consent | 5 plain bullets (what we collect, why, who sees it), link to full policy, Agree | required before account creation |
| ONB-04 | Barangay | map with 26 boundaries + searchable list, "Use my location" (coarse) | guests too |
| ONB-05 | Home pin (optional) | drag pin, "Skip", explains it powers truck-nearby alerts and is never shown to others | |
| ONB-06 | Notifications primer | what alerts you'll get, Allow → system dialog | Android 13+ permission |
| AUTH-01 | Email sign in | email, password, show/hide, Forgot password | |
| AUTH-02 | Email sign up | display name, email, password (strength meter), Create | |
| AUTH-03 | Verify email | "Check your inbox", resend (60 s cooldown), open mail app | |
| AUTH-04 | Reset password | email → link → new password screen via deep link | |
| AUTH-05 | Sign-in prompt sheet | shown when a guest taps a gated action; Google / email | |

## B. Resident
| ID | Screen | Content | Actions | States |
|---|---|---|---|---|
| R-HOME | Home (live map) | full map; TopPill barangay switcher; IconPill notifications (badge count); locate-me; MapSheet peek: truck status row + next collection; half: SegmentProgress stops, next 2 stops, today's status chip; full: today's timeline | switch barangay, tap truck → R-TRUCK, open schedule | no truck on shift ("No truck out right now · next: Tomorrow 6:30 AM"), offline (last known + age), location off |
| R-TRUCK | Truck sheet | truck label, status, ETA range, stops done/remaining, last update age | close, "Notify me when near" (sets home pin if none) | stale ping >2 min |
| R-SCH | Schedule | next collection card, DayStrip, waste type legend, upcoming exceptions banner, MonthCalendar | change month, tap day → R-DAY | no schedule set for the barangay ("Not published yet") |
| R-DAY | Day detail | time window, waste type(s) with what-to-put-out tips, route/zone, status if today/past | add reminder toggle | cancelled/moved shown prominently |
| R-REP | My reports | list (status chip, category, date, thumbnail), FAB "New report" | open report, new | empty: "No reports yet" + New report |
| R-REP-NEW-1 | Capture | camera with grid; gallery button; flash | take photo / pick | camera denied |
| R-REP-NEW-2 | Review photo | "Securing image…" (redaction progress), then redacted preview with "Faces and text blurred" note | retake, continue; tap to add extra blur box manually | redaction failed → retake |
| R-REP-NEW-3 | Details | category chips, note (280), location mini-map with adjustable pin + detected barangay | submit | duplicate found sheet ("Add your voice?") |
| R-REP-NEW-4 | Sent | check animation, "Sent. We'll notify you." | done, view report | queued offline: "Will send when online" |
| R-REP-DETAIL | Report detail | redacted photo, category, note, location, Timeline of states with notes, "after" photo if resolved | delete (only while submitted), share link | rejected reason shown |
| R-SCN | Scan | CameraFrame, live hint text, shutter, "Browse guide" | capture, flash, guide | camera denied → guide |
| R-SCN-RES | Scan result | category chip (big), "Likely"/"Not sure", what to do, which day it's collected in your barangay, top-3 alternatives when not sure | Was this right? Yes / No → pick correct; share photo opt-in; scan again | Not waste |
| R-GUIDE | Guide home | search bar, 5 category cards | search, open | works offline |
| R-GUIDE-CAT | Category | what it is, examples, do/don't, how to prepare, collection day link | open item | |
| R-GUIDE-ITEM | Item | names (3 languages), category, prep steps | | |
| R-YOU | You | name, barangay, level + progress to next, points, badges row, my reports count, links: leaderboard, announcements, settings | edit profile | guest: sign-in card |
| R-LB | Leaderboard | segmented: Barangays (city) / People (my barangay); month picker; RankRow list; own row pinned | tap barangay → R-LB-BRGY | |
| R-LB-BRGY | Barangay score | score + 4-part breakdown bars, month trend | | |
| R-BADGES | Badges | grid of Medal, locked/unlocked, how to earn | | |
| R-ANN | Announcements | list (urgent first), detail page | open link | |
| R-INBOX | Notifications | list grouped Today/Earlier, read/unread | tap → deep link, mark all read | |
| R-SET | Settings | language, theme, notifications (per type + quiet hours), home pin, privacy (scan photo sharing, leaderboard visibility, download data, delete account), about | | |
| R-SET-DEL | Delete account | consequences, type DELETE to confirm | | |
| R-ABOUT | About | disclaimer, version, licenses, OSM and dataset credits, demo mode toggle, source code link | | |

## C. Driver (app)
| ID | Screen | Content | Actions |
|---|---|---|---|
| D-TODAY | Today | truck, route(s), time window, stop count, schedule changes banner | Start shift (56 dp, accent) |
| D-PERM | Location primer | why, only during shift, battery-optimization guide for Realme/Oppo/Xiaomi | Allow |
| D-SHIFT | Active shift | full map following the truck, next stop card (name, distance), Done / Skip (56 dp), stop list sheet, connection indicator | done, skip (reason picker), report issue, end shift |
| D-ISSUE | Report issue | breakdown / road blocked / area full / other + note | send |
| D-END | End shift | summary: stops done/skipped, distance, duration, unsynced count | note, confirm |
| D-INBOX | Inbox | route changes, admin messages | |

## D. Admin web
| ID | Page | Content and actions | Who |
|---|---|---|---|
| W-LOGIN | Login | email + password (staff only), forgot password | all staff |
| W-MFA | MFA | enroll TOTP (QR) on first login, verify 6-digit code each session | all staff |
| W-HOME | Overview | today: collections scheduled / on the way / done / missed; open reports by status; response time; mini fleet map; alerts list | officer (own brgy), admin |
| W-FLEET | Live fleet | map with all on-shift trucks, routes, stop progress; side list; truck detail drawer | admin; officer sees own |
| W-REPORTS | Reports queue | table + map toggle; filters (status, category, barangay, date, flagged); bulk actions | officer, admin |
| W-REPORT | Report detail | photo, note, location, reporter (display name only), "Me too" count, timeline; actions: review, verify, reject (reason), in progress, resolve (after photo), merge | officer, admin |
| W-SCHED | Schedules | weekly calendar per barangay; create/edit recurrence (weekdays, window, waste type, route, truck, effective dates); exceptions per date; publish | officer (own), admin |
| W-ROUTES | Routes | draw route on map (snap to roads not in v1: click points), add/reorder stops, assign barangay/zone | admin |
| W-TRUCKS | Trucks | list, add, label, assign default driver, active/inactive | admin |
| W-PEOPLE | People | staff + residents search; invite (email, role, barangay); change role (super admin); deactivate; residents shown with minimal data | admin, super admin |
| W-ANN | Announcements | compose (target, priority, schedule, expiry), preview, list with delivery counts | officer (own), admin |
| W-SCORES | Leaderboard | monthly scores and components per barangay, recompute, freeze month | admin |
| W-GUIDE | Guide content | edit categories and items (3 languages), publish versions to app | admin |
| W-SCANQ | Scanner feedback | labeling queue of opted-in photos: confirm/correct label, export dataset | super admin |
| W-AUDIT | Audit log | who did what, when, filterable, read-only | super admin |
| W-SET | Settings | app config (limits, quiet hours, feature flags, demo mode), staff own profile + MFA reset | super admin |

## E. System states (shared)
- Offline banner; sync status in You > "Pending uploads (2)".
- Forced update screen if `app_config.min_version` > installed (security fixes).
- Maintenance screen when `app_config.maintenance = true`.
- Error boundary screen with "Send diagnostics" (no personal data).
