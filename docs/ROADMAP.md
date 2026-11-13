# Limpyo — Roadmap

Built in **vertical slices**: each phase ships the database, the app screens and the admin web pieces for one feature, so every phase ends with something you can test on your Realme 6 Pro.

Workflow per step: Claude writes a short spec → you approve → Claude builds → you test on the phone → fix → next.
Estimates assume about 15 focused hours a week. Real speed will differ; treat them as relative sizes.

| Phase | Name | Size | Rough weeks |
|---|---|---|---|
| 0 | Foundations | M | 1 |
| 1 | Identity and onboarding | L | 2 |
| 2 | Schedules | L | 2 |
| 3 | Notifications | M | 1 |
| 4 | Live tracking | L | 2 |
| 5 | Reports | L | 2 |
| 6 | Scanner and guide | L | 2.5 |
| 7 | Points and leaderboard | M | 1 |
| 8 | Hardening | L | 2 |
| 9 | Launch | M | 1 |
| | **Total** | | **~16–18 weeks** |

---

## Phase 0 — Foundations
- [ ] Rename package to `com.limpyo.app`, app name, adaptive icon placeholder, splash.
- [ ] Gradle version catalog, Kotlin/Compose/Hilt setup, module skeleton (see ARCHITECTURE.md).
- [ ] `design/tokens.json` → Compose theme (Night + Day) and Tailwind config; fonts bundled.
- [ ] Core components v0: buttons, card, chips, top pills, bottom nav, sheet.
- [ ] Supabase: create `limpyo-dev` and `limpyo-prod`, link CLI, first migration (extensions: postgis, pg_cron, pg_net).
- [ ] Firebase: add Android app (`com.limpyo.app` + debug SHA-1) to the `limpyo` project, download `google-services.json` (git-ignored).
- [ ] Cloudflare Pages projects: `limpyo` (site) and `limpyo-admin`.
- [ ] GitHub repo `limpyo` (public), branch protection, Dependabot, secret scanning + push protection, CodeQL, gitleaks, CI skeletons, keep-alive workflow.
- [ ] Private repo `limpyo-backups` + backup workflow.
- **Done when:** the app installs on the Realme, shows the Night Route theme and an empty bottom nav; CI is green; both Supabase projects respond to `health`.
- **You provide:** GitHub repo creation approval, Supabase project creation (or let Claude guide), USB-connected phone.

## Phase 1 — Identity and onboarding
- [ ] DB: barangays (26, boundaries seeded), profiles, user_roles, push_tokens, prefs, app_config, audit_log; auth hook; RLS + pgTAP.
- [ ] Auth: Google (Credential Manager), email/password, verify, reset; custom SMTP (Brevo).
- [ ] Screens: ONB-01..06, AUTH-01..05, guest mode, R-YOU (basic), R-SET (language, theme), R-ABOUT.
- [ ] Languages: string resources in en/fil/ceb, per-app language switch.
- [ ] Admin web: W-LOGIN, W-MFA (TOTP enroll/verify), W-PEOPLE (invite staff via `invite-staff`), layout shell.
- **Done when:** a guest can browse; a resident can sign up with Google or email and pick a barangay; you (super admin) can log into the web with MFA and invite an officer and a driver.

## Phase 2 — Schedules
- [ ] DB: trucks, routes, route_stops, schedules, exceptions, collections (+ materializer cron).
- [ ] App: R-SCH, R-DAY, next-collection card on Home sheet, Room offline cache, offline banner.
- [ ] Admin: W-SCHED (recurrence + exceptions), W-ROUTES (click points + stops), W-TRUCKS.
- [ ] Demo dataset for 3–5 barangays, clearly labeled.
- **Done when:** a schedule created on the web appears in the app within seconds and still shows in airplane mode.

## Phase 3 — Notifications
- [ ] FCM in app (token register/refresh), notification channels, dedup, deep links, R-INBOX, notification settings + quiet hours.
- [ ] Backend: notifications table, trigger → pg_net → `send-push`, `dispatch-notifications`, reminders cron.
- [ ] Admin: W-ANN (compose, target, urgent).
- **Done when:** the evening reminder, a schedule-change push and an announcement arrive on the phone and open the right screen; a resend never duplicates.

## Phase 4 — Live tracking
- [ ] DB: shifts, stop_events, truck_live, driver_issues; definer functions; Realtime Authorization policies.
- [ ] Driver mode: D-TODAY, D-PERM, D-SHIFT, D-ISSUE, D-END; foreground location service; outbox for events.
- [ ] Resident: R-HOME live map (MapLibre + night-route style, boundaries, route, stops, truck glide), R-TRUCK, ETA, collection state machine, "truck on the way" + "nearby" pushes, missed detector.
- [ ] Demo mode (local replay of a recorded GPX).
- [ ] Admin: W-FLEET, W-HOME (today's collections).
- **Done when:** you drive/walk a short route with the driver account on one phone, and a second phone (or emulator) shows the truck moving smoothly with the right ETA and stop progress.

## Phase 5 — Reports
- [ ] DB: reports, events, confirmations, after photos, storage buckets + policies; `create-report`, `finalize-report`, `set_report_status`.
- [ ] App: R-REP list, capture → redaction → details → sent, duplicate "Me too", R-REP-DETAIL timeline, outbox for offline submit.
- [ ] Moderation word lists (en/fil/ceb; you help with Bisaya/Filipino terms).
- [ ] Admin: W-REPORTS (table + map), W-REPORT (all actions).
- **Done when:** a report with a face and a plate in the photo arrives on the web with both blurred and no EXIF; status changes push to the reporter.

## Phase 6 — Scanner and guide
- [ ] Guide content: 5 categories, ~150 items in 3 languages (Claude drafts, you review), bundled + versioned.
- [ ] App: R-GUIDE screens (offline search), R-SCN, R-SCN-RES, feedback flow.
- [ ] ML: data card, local photo collection (you: 600+ photos, 300+ held out as the test set), training notebook, export, model card.
- [ ] Admin: W-GUIDE editor, W-SCANQ labeling queue.
- **Done when:** the scanner meets the targets in AI.md on the local test set, runs in under 150 ms on your phone, and "Not sure" falls back gracefully.

## Phase 7 — Points, badges, leaderboard
- [ ] DB: ledger, badges, user_badges, barangay_scores, user_points view; scoring + month-close crons.
- [ ] App: points/level on R-YOU, R-BADGES, R-LB, R-LB-BRGY, level-up and badge pushes.
- [ ] Admin: W-SCORES.
- **Done when:** verifying a report gives +20 once (never twice on retries), the daily cap holds, and barangay scores show their breakdown.

## Phase 8 — Hardening
- [ ] Security test plan (SECURITY.md §6), fixes, report in `docs/security-reports/`.
- [ ] Performance: Baseline Profiles, Macrobenchmark on the Realme, APK size check, map and list jank pass.
- [ ] Accessibility: TalkBack pass, 200% font, contrast test, one-handed driver mode.
- [ ] Offline/weak-signal tests (network throttling), battery test of an 8 h simulated shift.
- [ ] Translation review (you, Bisaya + Filipino), copy polish.
- [ ] Account deletion, data export, retention jobs verified.
- [ ] W-AUDIT, W-SET, forced update + maintenance screens.
- **Done when:** the security checklist passes with no high findings and the performance budgets in ARCHITECTURE.md hold on your phone.

## Phase 9 — Launch
- [ ] Landing page on `limpyo.pages.dev`: what it is, screenshots, download (GitHub Releases), privacy policy, disclaimer, source link.
- [ ] README: overview, architecture diagram, how to run, security approach, model card link, credits (OSM, datasets, eTrakYan concept).
- [ ] Signed release v1.0.0 with checksums; in-app update check against GitHub Releases.
- [ ] Demo video (1–2 min): resident, driver and admin flows with demo data. You're a video editor and creator, so this is likely the strongest piece for getting LGU attention.
- [ ] One-page LGU brief (what it does, cost ₱0, what a pilot would need).
- **Done when:** a stranger can download, install and use Limpyo from the landing page without your help.

---

## Risks
| Risk | Likelihood | Impact | Plan |
|---|---|---|---|
| Supabase free limits or policy change | low | high | usage-watch alerts; standard Postgres, so migration to self-host or another host is possible; backups weekly |
| Supabase project pause | medium | medium | keep-alive every 3 days, both projects |
| OpenFreeMap outage or change | low | medium | style bundled; tile URL in app_config so it can switch to another free provider without an app update |
| Realme/ColorOS kills driver service | high | high | battery guide on D-PERM, foreground service, resume logic, test early in Phase 4 |
| Scanner misses targets | medium | medium | ship with honest "Not sure" + guide; improve with feedback; detector in v2 |
| Email deliverability (no domain) | medium | medium | Google sign-in as primary; Brevo; optional cheap domain |
| Scope creep | high | high | non-goals list in PRODUCT.md; new ideas go to a "Later" list |
| Bisaya/Filipino translation quality | medium | low | you review every string before release |
| Mistaken for an official LGU app | medium | high | disclaimers everywhere, "Demo" labels, no LGU logos |

## Later (not v1)
iOS, object detector, cloud second opinion, sponsored rewards, route optimization, SMS fallback for barangay officers, offline map packs, public open-data API, multi-city support.
