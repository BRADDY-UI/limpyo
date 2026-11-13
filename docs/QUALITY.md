# Limpyo — Testing, Release, Operations

## 1. Testing strategy
| Layer | Tool | What |
|---|---|---|
| Kotlin unit | JUnit 5, Turbine, MockK | ViewModels, repositories, state machines (collection state, report transitions), ETA math, dedup |
| Compose UI | Compose UI Test | key flows: onboarding, report submit, driver stop done |
| Screenshots | Roborazzi (JVM, free) | every component and screen in Night + Day, en/fil/ceb, 100% and 200% font |
| Room | in-memory DB tests | migrations, outbox |
| ML | Python + instrumented test | model accuracy on the local test set; on-device latency; redaction removes EXIF and blurs known faces in fixtures |
| Database | pgTAP via Supabase CLI in CI | RLS matrix, functions, constraints, crons (run manually) |
| Edge Functions | Deno test | validation, rate limits, auth checks |
| Admin web | Vitest + Testing Library, Playwright smoke | role-gated routes, report actions, schedule editor |
| Performance | Macrobenchmark + Baseline Profiles | cold start, scroll, map open on the Realme 6 Pro |
| Manual | checklists below | real phone, real streets |

### Manual release checklist (on the Realme 6 Pro)
- Fresh install → guest → sign up (Google and email) → pick barangay.
- Airplane mode: schedule still visible, report queued, sends when back online.
- Driver shift for 30+ min with screen off: truck keeps moving on a second device.
- Push: reminder, schedule change, report status, announcement; tapping opens the right screen; no duplicates.
- Theme switch, language switch (all 3), 200% font, TalkBack on Home and Report.
- Delete account: data gone, reports anonymized.

## 2. Versioning and release
- Semantic versioning `MAJOR.MINOR.PATCH`; `versionCode = MAJOR*10000 + MINOR*100 + PATCH`.
- Branches: `main` (always releasable), short feature branches, PRs with CI.
- Release: tag `vX.Y.Z` → GitHub Actions builds signed APKs (arm64-v8a + armeabi-v7a splits), uploads to GitHub Releases with SHA-256 checksums and a changelog.
- Keystore: generated once, backed up offline in 2 places. **Losing it means users can't update**, so it's the most important file in the project.
- In-app update check: on start (max once a day) reads the latest release tag from the GitHub API; shows "Update available" with the release notes; a forced update when `app_config.min_version` is higher.
- Admin web: every merge to `main` deploys to Cloudflare Pages; PRs get preview URLs.
- DB migrations to prod: run by the release workflow after a manual approval step; always backward-compatible with the previous app version.

## 3. Operations (solo, ₱0)
| Concern | How |
|---|---|
| Uptime | keep-alive workflow every 3 days; free uptime monitor (UptimeRobot free) on `health` and the admin site |
| Usage limits | `usage-watch` cron → admin overview card at 80% of DB/storage; monthly check of Realtime and Edge Function counts in the Supabase dashboard |
| Backups | weekly encrypted `pg_dump` to the private `limpyo-backups` repo, keep 8; storage photos are not backed up (they're short-lived by design) |
| Restore drill | once per minor release: restore the latest dump into `limpyo-dev`, run pgTAP |
| Logs | Supabase logs (short retention on free); Edge Functions log structured JSON without personal data |
| Crash reports | none in v1 (no third-party SDK); a "Send diagnostics" button builds a redacted text report the user can share |
| Dependency updates | Dependabot weekly; merge after CI passes |
| Security reports | `SECURITY.md` at repo root with a contact email; acknowledge within 72 h |

## 4. Definition of done (every task)
- Matches the approved spec and DESIGN.md tokens (no hard-coded colors or sizes).
- Works in Night and Day themes, all 3 languages, 200% font, offline (if relevant).
- RLS tests added for any new table or policy.
- No new lint/detekt/CodeQL warnings; no secrets.
- Tested on the Realme 6 Pro.
