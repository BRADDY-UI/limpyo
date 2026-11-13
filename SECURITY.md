# Security policy

## Reporting a vulnerability
Please **do not** open a public issue for security problems.

Report privately through GitHub: **Security → Report a vulnerability** on this repository (private vulnerability reporting). Include what you found, how to reproduce it, and the impact you expect.

You'll get an acknowledgement within **72 hours**. Fixes for confirmed issues are released as soon as practical, and reporters are credited unless they prefer not to be.

## Scope
- Android app (`app/`)
- Admin web (`admin-web/`)
- Database rules, storage policies and Edge Functions (`supabase/`)

Out of scope: denial-of-service volume tests, social engineering, and third-party services (Supabase, Firebase, Cloudflare, OpenFreeMap) themselves.

## Supported versions
Only the latest release receives security fixes.

The full threat model and controls are in [docs/SECURITY.md](docs/SECURITY.md).
