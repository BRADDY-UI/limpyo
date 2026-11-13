# Limpyo — Master Plan (index)

**Limpyo** ("clean" in Bisaya) is a free, open-source waste collection app for General Santos City: collection schedules, live truck tracking, photo reports, an on-device waste scanner, and a barangay cleanliness leaderboard. It is a solo rebuild of the eTrakYan thesis concept by Brad Kirby Flores. It is **not** an official LGU system, and that's stated in the app, on the landing page and in the README.

Status: **planning complete, awaiting approval.** No code yet.

## Documents
| File | What it covers |
|---|---|
| [PRODUCT.md](PRODUCT.md) | problem, roles, every feature and its rules, notification catalog, points, key journeys |
| [DESIGN.md](DESIGN.md) | Night Route design system: colors (dark + light), type, spacing, motion, components, map style, copy, accessibility, brand |
| [SCREENS.md](SCREENS.md) | every screen (app resident/driver + admin web) with content, actions and states; navigation map |
| [ARCHITECTURE.md](ARCHITECTURE.md) | system diagram, Android stack and modules, offline/sync, driver location service, Realtime budget, backend functions and crons, push pipeline, maps, repo layout, CI/CD |
| [DATABASE.md](DATABASE.md) | enums, tables, helper functions, RLS matrix, Realtime auth, storage buckets, retention, size budget |
| [SECURITY.md](SECURITY.md) | threats and mitigations, controls per layer, secrets, incident playbook, security tests, Data Privacy Act |
| [AI.md](AI.md) | scanner data plan, model, targets, decision logic; on-device photo redaction |
| [ROADMAP.md](ROADMAP.md) | 10 phases (0–9) with checklists and "done when", risks, later list |
| [QUALITY.md](QUALITY.md) | testing strategy, release process, operations, definition of done |
| Design canvas | https://claude.ai/artifact/TTYUzrBRjfCpndP8i9wBaH (style B chosen) |

## Locked decisions
| Area | Decision |
|---|---|
| Name | Limpyo |
| Platform | Android only, native Kotlin + Jetpack Compose, minSdk 26; reference phone Realme 6 Pro |
| Roles | one app for resident + driver; admin is a web app (officer / city admin / super admin) |
| Backend | Supabase free (Postgres + PostGIS, Auth, Storage, Realtime, Edge Functions); 2 projects: dev + prod |
| Push | Firebase Cloud Messaging only (project `limpyo`, Spark plan, no billing) |
| Admin hosting | Cloudflare Pages (free) |
| Maps | MapLibre + OpenFreeMap vector tiles, custom Night/Day styles; boundaries from OSM |
| Design | Style B "Night Route", dark default, light "Day Route" for sunlight, smooth spring motion |
| Languages | English, Filipino, Bisaya, switchable in Settings |
| AI | on-device only (LiteRT scanner + ML Kit redaction); cloud fallback deferred to v2, opt-in |
| Rewards | points, levels, badges, leaderboards; no redeemable prizes in v1 |
| Security | RLS everywhere, staff MFA, invite-only staff, server-side validation, public repo with no secrets |
| Distribution | signed APK on GitHub Releases + landing page; no Play Store in v1 |
| Data | real barangay boundaries; clearly labeled demo schedules and demo truck |
| Cost | ₱0 |

## Accounts and setup
- [x] GitHub
- [x] Supabase
- [x] Firebase project `limpyo` (Spark plan)
- [x] Cloudflare
- [x] Realme 6 Pro developer options + USB debugging
- [ ] Supabase projects `limpyo-dev` and `limpyo-prod` (Phase 0)
- [ ] GitHub repos `limpyo` (public) and `limpyo-backups` (private) (Phase 0)
- [ ] Brevo account for auth emails (Phase 1)

## Business decisions
1. **License: AGPL-3.0 + commercial license (dual licensing).** The repo is public for the portfolio. Anyone who runs a modified Limpyo as a service must publish their changes, so a vendor can't take it closed. Brad owns all copyright (solo rebuild, no shared code) and can sell an LGU a separate commercial license plus setup, hosting and support. No outside contributions are merged without a Contributor License Agreement, which keeps dual licensing possible.
   - Before selling: check STI's thesis IP policy and agree terms with the eTrakYan teammates about the concept (not legal advice).
2. **Domain:** free `limpyo.pages.dev` for now; buy a domain only when talking to an LGU.
3. **Roles match LGU structure:** super admin (Brad / vendor) → city admin (City ENRO staff) → barangay officer (barangay environment committee / secretary, one barangay) → driver → resident. Built single-city now; the schema keeps a `city` concept in mind so it can become multi-city later.
4. **Guest mode kept, with limits.** Guests can view schedules, the live map, the guide, the scanner, announcements and the barangay leaderboard. Guests can't report, "Me too", earn points, appear on leaderboards, save a home pin, or get personal alerts (only general collection reminders for one barangay on this device, stored locally).

## Final field decisions (Nov 13)
1. **Waste volume:** one tap per collection point (Light / Normal / Heavy / Overflowing) + truck fill level at end of shift (¼ ½ ¾ Full). Gives the city waste-per-barangay data.
2. **Resident check:** after a collection, signed-in residents get one card: "Was your garbage collected today?" Yes / No (+ optional 1–5 rating). "No" answers from many residents flag the collection for the officer. Adds to the barangay score as ground truth for driver data.
3. **Collection points (Brad's model):** officers pin collection points on the admin map (name + landmark, e.g. "Near the chapel", optional photo, time window). A route = ordered points. When saved, the admin web computes the road path once (OpenRouteService free tier) and stores it. The driver app just displays the stored path, so no routing calls or data during the shift. Residents see their nearest point and its ETA, and can request a new point (officer approves).
4. **Segregation:** schedules default to "Mixed"; waste type per day is optional. Drivers can flag a point "Not segregated" with one tap, which feeds a segregation score per barangay. The guide and scanner push education. Each barangay can turn on a "No segregation, no collection" notice (RA 9003).
5. **Driver data cost:** driver mode is offline-first and ultra-low-data (~5–10 MB/day: tiny location pings, route and map preloaded on Wi-Fi). With no data it still logs GPS and stops and syncs later (residents see "last updated"). Pitch: one shared "truck phone" with the cheapest monthly data promo per truck, paid by the LGU. For the pilot, Brad supplies one phone.
6. **Demo barangay:** Calumpang (real collection points and GPS recorded by Brad).
7. **Code structure:** simplified for a Kotlin beginner: one `app` module organized by feature packages (no multi-module), with every step explained.
8. **No deadline.** MVP demo = Phases 0–5 (~10 weeks); scanner, points and hardening after.

## Free tier vs. a real city deployment
The free stack is for development, demo and a small pilot. A city rollout needs paid tiers (paid by the LGU): Supabase Pro (no pausing, daily backups, higher limits), a domain + email sending, and a support agreement. The architecture doesn't change, only the plan level.

## Workflow
1. Before each build step, Claude posts a short spec (what, files touched, how to test).
2. You approve or change it.
3. Claude builds, runs tests, and tells you how to check it on your phone.
4. Claude asks what to build next.
