# Limpyo — Security and Privacy

No system is unhackable. The goal: **no single mistake exposes data, abuse is expensive, and we can detect and recover.** Every control below is tested in CI or in the Phase 8 security pass.

## 1. What we protect
| Asset | Why it matters |
|---|---|
| Residents' accounts, emails, home pins | personal data (Data Privacy Act) |
| Report photos and locations | can reveal people, homes, plates |
| Staff accounts | can change schedules, see all reports, send city-wide pushes |
| Driver live location | safety of drivers |
| Push channel | a hijacked push could spread panic or phishing |
| Data integrity (schedules, scores) | trust; an LGU would judge the app by this |
| Secrets: service-role key, FCM service account, signing keystore, DB password | full compromise if leaked |

## 2. Threats and mitigations
| # | Threat | Mitigation |
|---|---|---|
| T1 | Attacker uses the public anon key to read or write tables directly | RLS forced on every table, default deny, column privileges, pgTAP tests per role |
| T2 | Resident escalates to staff (edits own role) | roles live in `user_roles`, no client UPDATE; JWT claims come from the auth hook; only `set_user_role` (super + AAL2) changes roles |
| T3 | Staff account phishing / password reuse | TOTP MFA required; staff RLS checks `aal2`; invite-only; short sessions; leaked-password check where available |
| T4 | IDOR: reading another user's report or photo | RLS on rows; private buckets; signed URLs (10 min) generated only after RLS check; UUIDs not guessable, and authorization doesn't rely on that |
| T5 | Malicious upload (huge file, polyglot, script, EXIF data) | upload only via signed URL from `create-report`; bucket size + MIME limits; `finalize-report` checks magic bytes and dimensions; client re-encodes to WebP (drops metadata) |
| T6 | Report spam / point farming | daily caps, cooldowns, duplicate merge, points only after staff verification, spam penalty and lockout, ledger audit |
| T7 | Fake or AI-generated photos | can't be fully stopped; points need staff verification; "Me too" corroboration; officers can see report history per user; EXIF stripped so it's not a signal we rely on |
| T8 | Offensive text in notes or display names | word-list filter (en/fil/ceb) → flagged, never auto-published; display names filtered at write; escaping everywhere |
| T9 | XSS in admin web via report text | React escaping, no `dangerouslySetInnerHTML`, strict CSP, Zod validation, Trusted Types where supported |
| T10 | Fake truck positions (broadcast spoofing) | Realtime Authorization: only a driver on an active shift for that barangay can send; server sanity check on upsert (speed < 120 km/h, inside GenSan bbox) |
| T11 | Push abuse | only server code sends pushes (`send-push` requires an internal secret, not callable from clients); announcements require staff AAL2; audit log |
| T12 | Secret leak in the public repo | no secrets in git; gitleaks pre-commit + CI; GitHub secret scanning + push protection; `google-services.json` and keys git-ignored |
| T13 | Supply-chain (malicious dependency) | Dependabot; dependency review on PR; pinned versions + Gradle dependency verification metadata; npm lockfile; minimal dependencies |
| T14 | APK tampering / repackaged fake Limpyo | signed releases, SHA-256 checksums on the release page, the landing page names the only official download |
| T15 | Brute force on login / password reset | Supabase Auth rate limits; CAPTCHA (hCaptcha/Turnstile free) on sign-up and reset if abuse appears |
| T16 | Denial of wallet / free-tier exhaustion | rate limits in Edge Functions; usage-watch alerts; Realtime only while the map is visible; image size caps |
| T17 | Lost data (free tier has no automatic backups) | weekly encrypted dump to a private repo; restore drill once per release |
| T18 | Driver privacy | GPS only during an active shift; driver identity never exposed to residents; shift auto-ends at 12 h |

## 3. Controls by layer

### Database
- `ALTER TABLE ... ENABLE/FORCE ROW LEVEL SECURITY` everywhere; a CI check fails if any table lacks RLS.
- Definer functions with an empty `search_path`, fully qualified names, input validation, and minimal EXECUTE grants.
- CHECK constraints for lengths, enums, URL schemes (https only), coordinates inside a GenSan bounding box.
- `audit_log` is append-only; staff actions always write to it.

### Auth
- Email confirmation on; refresh token rotation with reuse detection; JWT 1 h.
- Staff: MFA enrollment forced on first login; AAL2 needed for every staff policy.
- Google sign-in verifies the ID token server-side (Supabase).

### Edge Functions
- Verify the JWT on every call (except `health`); per-user and per-IP rate limits (counters in Postgres).
- Validate every input with a schema; reject unknown fields; return generic errors (no stack traces).
- Service-role key exists only inside Edge Function secrets.

### Android app
- No secrets in the APK (the anon key and URL are public by design).
- R8 full mode, `android:allowBackup="false"`, `usesCleartextTraffic="false"` + network security config, exported=false on everything except the launcher and deep-link activity.
- Deep links validated (allowed hosts and paths only).
- Session tokens encrypted at rest with a Keystore-backed key.
- `FLAG_SECURE` on driver mode screens (map with live location) and on account deletion.
- Certificate pinning: not used (rotation risk can brick installs); TLS + RLS carry the load. Re-evaluate if an LGU adopts it.
- Play Integrity API: not used (requires Play distribution).

### Admin web (Cloudflare Pages `_headers`)
```
Content-Security-Policy: default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data: blob: https://*.supabase.co; connect-src 'self' https://*.supabase.co wss://*.supabase.co https://tiles.openfreemap.org; worker-src blob:; frame-ancestors 'none'; base-uri 'none'; form-action 'self'
Strict-Transport-Security: max-age=63072000; includeSubDomains; preload
X-Content-Type-Options: nosniff
Referrer-Policy: strict-origin-when-cross-origin
Permissions-Policy: camera=(), microphone=(), geolocation=(self)
Cross-Origin-Opener-Policy: same-origin
```
Plus: no third-party scripts, Subresource Integrity is unnecessary (self-hosted only), session in memory + refresh token in an httpOnly-equivalent flow where supabase-js allows, auto sign-out after 30 min idle.

### Repo
- Branch protection on `main`: PR required, CI green, no force-push.
- Signed commits (recommended), CODEOWNERS, `SECURITY.md` at repo root with a contact for vulnerability reports.

## 4. Secrets inventory
| Secret | Lives in | Rotate how |
|---|---|---|
| Supabase service-role key | Edge Function secrets, GitHub Actions secret (migrations only) | Supabase dashboard → new key → update secrets |
| DB password | password manager, backup workflow secret | dashboard reset |
| FCM service account JSON | Edge Function secret | Google Cloud console → new key, delete old |
| Android upload/signing keystore + passwords | offline (2 copies: PC + encrypted cloud drive), GitHub secret (base64) | can't rotate without breaking updates; guard carefully |
| Backup encryption key (age) | password manager | regenerate; re-encrypt future backups |
| SMTP credentials | Supabase Auth settings | provider dashboard |
| internal push secret | Postgres vault + Edge secret | regenerate both |

## 5. Incident playbook (solo)
1. Contain: set `app_config.maintenance = true`; disable sign-ups; revoke the leaked key.
2. Rotate affected secrets (table above); force sign-out all sessions if auth is involved.
3. Investigate with `audit_log`, Supabase logs (7-day retention on free; export what you need immediately).
4. Fix and add a test that would have caught it.
5. Tell affected users in-app and on the repo if personal data was exposed; if an LGU is involved, they handle the NPC notification.

## 6. Security test plan (Phase 8, repeated each release)
- pgTAP: every table × every role × S/I/U/D, expected allow/deny.
- Manual abuse script (curl + anon key): read others' reports, upload a 10 MB file, upload a `.html` as `.webp`, set own role, award own points, send a broadcast as a resident, call `send-push` directly.
- MobSF static scan of the release APK; fix high findings.
- OWASP ZAP baseline scan against the admin web preview deploy.
- Dependency audit (Gradle + npm), CodeQL results reviewed.
- Results written to `docs/security-reports/YYYY-MM.md`.

## 7. Privacy (Data Privacy Act of 2012, RA 10173)
Not legal advice. These follow the Act's principles of transparency, legitimate purpose and proportionality.
- **Privacy notice** (in-app + landing page): what is collected, why, how long, who can see it, user rights, contact.
- **Consent**: explicit at sign-up (ONB-03), separate opt-ins for scanner photo sharing and leaderboard visibility. Withdrawing is as easy as giving.
- **Minimization**: no phone number, no birthday, no contact list, no continuous resident location. The home pin is optional, never shown to others, and used only for nearby alerts.
- **Photos**: faces and text blurred on device, EXIF stripped, private storage, deleted 60 days after closure.
- **Rights**: view and download my data (`export-my-data`), correct profile, delete account (`delete-account`).
- **Drivers**: location only on shift; told clearly in D-PERM.
- **Children**: the app is general-audience; no features target minors; no ads or tracking.
- **No analytics SDKs, no ads, no selling data.** Crash reports (if added later) are opt-in and contain no personal data.
- If an LGU ever adopts Limpyo, they become the personal information controller and must handle their own DPA obligations (e.g., DPO, NPC registration where required).
