# Limpyo — Architecture

## 1. System overview
```
 Android app (Kotlin/Compose) ──HTTPS──► Supabase
   resident / driver modes                 ├─ Auth (email, Google, MFA for staff, custom JWT claims hook)
   Room cache + outbox                     ├─ Postgres + PostGIS (RLS on every table)
   LiteRT scanner, ML Kit redaction        ├─ Storage (private buckets, signed URLs)
   MapLibre ◄── OpenFreeMap tiles          ├─ Realtime (private broadcast channels per barangay)
   FCM ◄────────────────────────┐          ├─ Edge Functions (validation, push, invites, deletion)
                                │          └─ pg_cron + pg_net (reminders, retention, scores)
 Admin web (React, CF Pages) ───┘ FCM HTTP v1 ◄── send-push function
 GitHub Actions: CI, security scans, keep-alive pings, encrypted weekly backups (private repo)
```

## 2. Android app
**Targets:** minSdk 26 (Android 8), targetSdk/compileSdk = latest stable at Phase 0. Reference device: Realme 6 Pro (Snapdragon 720G, 90 Hz).
**Package:** `com.limpyo.app`. Debug suffix `.dev` → points at `limpyo-dev`.

### Libraries (all free, open source or free Google SDKs)
| Need | Library |
|---|---|
| UI | Jetpack Compose (BOM), Material 3 as base, custom Night Route theme |
| Navigation | Navigation Compose (type-safe routes), SharedTransitionLayout, predictive back |
| DI | Hilt |
| Async | Coroutines + Flow |
| Backend client | supabase-kt (Auth, Postgrest, Storage, Realtime, Functions) on Ktor OkHttp engine |
| Serialization | kotlinx.serialization |
| Local data | Room (cache + outbox), DataStore (settings) |
| Background | WorkManager (sync, uploads), foreground service (driver location) |
| Location | Play services FusedLocationProvider |
| Maps | MapLibre Android SDK |
| Camera | CameraX |
| On-device ML | LiteRT (scanner); ML Kit Face Detection + Text Recognition (Play services versions, smaller APK) |
| Images | Coil 3 |
| Push | Firebase Cloud Messaging (only Firebase product used) |
| Google sign-in | Credential Manager → ID token → Supabase `signInWithIdToken` |
| Secure storage | Supabase session stored in DataStore encrypted with a Keystore AES key (Tink) |
| Logging | Timber (debug only); no third-party analytics |
| Debug | LeakCanary, StrictMode |

### Code structure: one `app` module, feature packages (PLAN.md decision 7)
No Gradle multi-module. Everything lives in `app/`, organized by package under `com.limpyo.app`:
```
(root)                   LimpyoApp (Hilt), MainActivity, MainViewModel
navigation/              routes, bottom nav frame, mode switch (resident/driver)
core/designsystem        theme (LimpyoTokens.kt generated from design/tokens.json), components, icons, map style loader
core/model               pure Kotlin data classes
core/data                repositories, supabase-kt client, Room DB, sync, outbox
core/push                FCM service
core/ml                  scanner (LiteRT), redaction pipeline (ML Kit + bitmap ops); model in app/src/main/assets
core/common              time, i18n helpers, result types, dispatchers
feature/home  feature/schedule  feature/report  feature/scan  feature/you
feature/onboarding  feature/driver  feature/settings
```
Toolchain: AGP 9.1.x, Gradle 9.9, compileSdk/targetSdk 37, Kotlin 2.4. AGP is held at 9.1.x because that's the newest Android Studio 2025.3.1 (Panda) can open; Dependabot ignores AGP ≥ 9.2 until Studio is updated.
Pattern: MVVM + unidirectional data flow. Each screen has `UiState` (immutable), `ViewModel` exposing `StateFlow<UiState>`, and events as functions. Repositories are the only place that touches network or DB.

### Data strategy
| Data | Source of truth on device | Refresh |
|---|---|---|
| Barangays, guide content | bundled JSON + Room, versioned | on app start if `content_version` changed |
| Schedules + exceptions | Room (offline-first) | on open, pull-to-refresh, push "schedule change" triggers sync, daily WorkManager |
| Live trucks | memory only | Realtime while Home/Truck visible; unsubscribed in background |
| My reports | Room | on open + on push |
| Outbox (reports, driver events, scan feedback) | Room table, WorkManager with network constraint, exponential backoff | idempotency key (UUID) per item so retries never duplicate |
| Settings | DataStore | — |

### Driver location service
- Foreground service type `location`, persistent notification "Sharing location · Shift active", stops at End shift or 12 h max.
- Fused location: high accuracy, 5 s interval while moving; 30 s when stationary (speed < 1 m/s for 60 s).
- Sends a Realtime **broadcast** on `truck:{barangay_id}` every 15 s (`{truck_id, lat, lng, heading, speed, ts}`), and upserts `truck_live` every 30 s (for late joiners and the admin fleet map).
- No signal: keep the latest point only (not a backlog) for live; stop events are queued in the outbox with original timestamps.
- Realme/ColorOS battery killing: D-PERM guides the driver to "Allow background activity" and to exclude Limpyo from battery optimization.

### Realtime budget (free tier ≈ 2M messages/month, ~200 concurrent; re-verify at Phase 0)
- Residents subscribe only while the map is on screen, only to their barangay channel.
- Estimate at 1,000 active residents: trucks send ~430k/month, residents receive ~360k/month, ≈ 0.8M total. If usage grows, the broadcast interval goes to 20–30 s (marker interpolation hides it).

### Background jobs (WorkManager)
`SyncSchedulesWorker` (daily + on push), `OutboxWorker` (network required), `ContentUpdateWorker` (weekly), `PushTokenWorker` (on token refresh).

### Performance budgets
Cold start < 1.5 s to first frame on Realme 6 Pro; home map interactive < 2.5 s; 90 fps scroll; release APK (arm64 split) < 30 MB; scanner inference < 150 ms; redaction < 1.2 s. Baseline Profiles + R8 full mode. Checked with Macrobenchmark in Phase 8.

## 3. Admin web
- React + Vite + TypeScript, React Router, TanStack Query, supabase-js, Tailwind (tokens from `design/tokens.json`), MapLibre GL JS, Recharts, React Hook Form + Zod.
- Dark (Night Route) by default, light toggle.
- Hosted on Cloudflare Pages: `limpyo-admin.pages.dev` (prod), preview deploys per PR. `_headers` file sets security headers (see SECURITY.md).
- Staff-only. There's no sign-up page; accounts come from invites. Every route checks session, AAL2 (MFA) and role before rendering. The database enforces the same checks regardless.

## 4. Backend (Supabase)
**Projects:** `limpyo-dev` (testing, demo data) and `limpyo-prod`. Free tier allows 2. Migrations are applied by CI with the Supabase CLI.

### Auth
- Providers: Email (confirm on, secure password change, min length 10, leaked-password check if available on free), Google.
- Custom SMTP for auth emails (built-in SMTP is heavily rate-limited): Brevo free tier (~300/day) with a verified sender. Open question: a cheap domain improves deliverability (see PLAN.md).
- Custom Access Token hook adds `user_role` and `barangay_id` claims from `user_roles`.
- MFA (TOTP) required for all staff; RLS for staff actions checks `aal = 'aal2'`.
- JWT expiry 1 h, refresh token rotation on, reuse detection on.

### Edge Functions (Deno, TypeScript)
| Function | Purpose | Auth |
|---|---|---|
| `create-report` | validates input, rate limits, duplicate check, text moderation, inserts report, returns signed upload URL | resident JWT |
| `finalize-report` | checks uploaded file (type, size, magic bytes, re-encode check), marks report submitted | resident JWT |
| `send-push` | sends FCM HTTP v1 messages in batches; drops dead tokens | internal only (secret header from pg_net) |
| `dispatch-notifications` | builds reminders and alerts from due rows, calls send-push | internal (pg_cron) |
| `invite-staff` | creates staff user, sets role, sends invite email | admin JWT + AAL2 |
| `delete-account` | deletes auth user, anonymizes reports, removes photos and tokens | own JWT (re-auth within 5 min) |
| `export-my-data` | JSON of the user's data | own JWT |
| `health` | trivial DB read for keep-alive | public, rate limited |

### Scheduled jobs (pg_cron)
| Job | Every | Does |
|---|---|---|
| reminders | 5 min | finds collections whose reminder time just passed, queues notifications |
| missed-detector | 10 min | marks collections Missed, opens system report |
| retention | daily 03:00 | deletes resolved-report photos > 60 days, scan photos > 180 days, old notifications > 90 days, stale truck_live |
| scores | daily 02:00 | recomputes current-month barangay scores |
| month-close | 1st of month | freezes last month, awards Barangay Hero badges |
| usage-watch | daily | records DB/storage size into `app_metrics`; flags >80% of free limits on the admin overview |

### Push pipeline
`notifications` row inserted → AFTER INSERT trigger → `pg_net` POST to `send-push` with an internal secret → FCM. Each message uses the notification id as `collapse_key`/tag, and the app dedups the last 200 ids, so it's at-most-once on screen.

## 5. Maps and geodata
- Tiles: OpenFreeMap public vector tiles, no key. Style JSON (`night-route.json`, `day-route.json`) is hosted on the Cloudflare Pages landing site and also bundled in the app as a fallback.
- Barangay boundaries: exported once from OpenStreetMap (admin_level 10 in General Santos), cleaned, stored as GeoJSON in `supabase/seed/barangays.geojson` and PostGIS `geography`. If a boundary is missing, it's drawn manually and marked approximate. ODbL attribution is shown.
- Point-in-polygon in PostGIS assigns a report's barangay. Distance queries use `geography` with GiST indexes.
- No bulk offline tile download (respects OpenFreeMap fair use); MapLibre's ambient cache covers revisits.

## 6. Repo and environments
```
D:\GenTrack  (monorepo, public on GitHub as "limpyo")
├─ app/ core/ feature/          Android
├─ admin-web/                   React
├─ supabase/                    config.toml, migrations/, functions/, seed/, tests/ (pgTAP)
├─ ml/                          notebooks, data cards, export scripts (no raw images in git)
├─ design/                      tokens.json, map styles, icon sources
├─ site/                        landing page + privacy policy (Cloudflare Pages)
├─ docs/                        these specs
└─ .github/                     workflows, dependabot.yml, CODEOWNERS, issue templates
```
Secrets: none in git. `local.properties` / `.env.local` (git-ignored) hold the Supabase URL and anon key for dev. CI holds keystore, service-role key and FCM service account as GitHub encrypted secrets. `google-services.json` is git-ignored and injected in CI.

## 7. CI/CD (GitHub Actions, free on public repos)
| Workflow | On | Steps |
|---|---|---|
| android-ci | PR, main | ktlint/detekt, unit tests, Compose screenshot tests (Roborazzi), assembleDebug, Android Lint |
| web-ci | PR, main | typecheck, lint, unit tests, Playwright smoke, build |
| db-ci | PR touching supabase/ | start local Supabase, apply migrations, run pgTAP RLS tests |
| security | PR, weekly | CodeQL (Kotlin, JS/TS), gitleaks, dependency review |
| release | tag v* | build signed release APKs (arm64 + armeabi-v7a splits), checksums, GitHub Release, deploy web to Pages, apply prod migrations (manual approval) |
| keepalive | every 3 days | calls `health` on dev and prod |
| backup | weekly (in a separate private repo) | `supabase db dump` prod → encrypt with age → store as private release asset; keep 8 |
