# Limpyo

**Limpyo** ("clean" in Bisaya) is a free, open-source waste collection app for General Santos City: collection schedules, live truck tracking, photo reports, an on-device waste scanner, and a barangay cleanliness leaderboard.

> **Independent project. Limpyo is not an official LGU (City of General Santos or barangay) app or service.** All demo data is labeled "Demo".

Status: **Phase 0 (foundations)**. Nothing is usable yet. See [docs/ROADMAP.md](docs/ROADMAP.md).

## Repository layout
| Folder | What |
|---|---|
| `app/` | Android app (Kotlin, Jetpack Compose) |
| `admin-web/` | Staff dashboard (React, Vite, TypeScript, Tailwind) |
| `supabase/` | Database migrations, seeds, Edge Functions, tests |
| `design/` | Design tokens (single source for app and web) |
| `site/` | Landing page and privacy policy |
| `docs/` | Product, design, architecture, security plans |

## Build the Android app
1. Install Android Studio (JDK 21 is bundled).
2. Put your own Firebase `google-services.json` in `app/` (it is git-ignored).
3. `./gradlew :app:installDebug`

## License
Limpyo is dual-licensed:
- **GNU AGPL-3.0** ([LICENSE](LICENSE)) for everyone. If you run a modified version as a service, you must publish your changes.
- A **commercial license** is available from the author for organizations that can't use the AGPL.

Contributions require signing a Contributor License Agreement (CLA) so dual licensing stays possible. Please open an issue before sending a pull request.

## Security
See [SECURITY.md](SECURITY.md) to report a vulnerability.

Concept based on the eTrakYan thesis study. Map data © OpenStreetMap contributors (ODbL).
