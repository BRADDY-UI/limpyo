# Limpyo — Database (Postgres + PostGIS on Supabase)

All tables are in schema `public` unless noted. RLS is **enabled and forced on every table**; default is deny. Times are `timestamptz` (UTC) and displayed in Asia/Manila. IDs are `uuid` (gen_random_uuid) unless noted.

## 1. Enums
```
app_role:         resident | driver | barangay_officer | city_admin | super_admin
waste_type:       biodegradable | recyclable | residual | special | ewaste | mixed
report_category:  uncollected | illegal_dumping | overflowing_point | burning | clogged_canal | missed_collection | other
report_status:    submitted | in_review | verified | in_progress | resolved | rejected | merged
reject_reason:    spam | not_waste | outside_area | duplicate | insufficient_info | other
exception_kind:   cancelled | moved | added
collection_state: scheduled | on_the_way | in_area | collected | missed | cancelled | moved
notif_type:       reminder_evening | reminder_morning | truck_on_way | truck_nearby | schedule_change | report_status | announcement | badge | driver_route_change | officer_new_report
```

## 2. Tables

### Identity
| Table | Columns |
|---|---|
| `barangays` | id smallint PK, name text unique, slug, boundary geography(MultiPolygon), centroid geography(Point), population int null, approximate bool. Seeded with all 26 (migration 0003). OSM has no barangay boundaries for GenSan, so boundaries are approximate: nearest-center areas inside the OSM city outline (`supabase/seed/barangays.geojson`, ODbL). |
| `zones` | id, barangay_id FK, name (purok/zone), boundary geography null |
| `profiles` | id uuid PK = auth.users.id, display_name (2–30, filtered), barangay_id FK, home_point geography(Point) null, language ('en','fil','ceb'), theme, leaderboard_visible bool default true, scan_photo_consent bool default false, consent_version int, created_at, deleted_at null |
| `user_roles` | user_id PK FK, role app_role default 'resident', barangay_id null (officers/drivers), granted_by, granted_at |
| `push_tokens` | id, user_id FK, token text unique, platform, app_version, last_seen_at |
| `notification_prefs` | user_id PK, per-type booleans (jsonb with check constraint), quiet_start time, quiet_end time |

### Operations
| Table | Columns |
|---|---|
| `trucks` | id, label ("Truck 2"), active bool, default_driver_id null |
| `routes` | id, barangay_id, zone_id null, name, path geography(LineString), version int |
| `route_stops` | id, route_id FK, seq int, label, point geography(Point), unique(route_id, seq) |
| `schedules` | id, barangay_id, zone_id null, route_id, truck_id null, weekdays smallint[] (1–7), window_start time, window_end time, waste_type, effective_from date, effective_to date null, published bool, created_by |
| `schedule_exceptions` | id, schedule_id FK, on_date date, kind exception_kind, new_date null, new_start null, new_end null, note, created_by, unique(schedule_id, on_date) |
| `collections` | id, schedule_id, on_date, state collection_state, started_at, finished_at, shift_id null. One row per occurrence, materialized 14 days ahead by cron; unique(schedule_id, on_date) |
| `shifts` | id, driver_id, truck_id, route_id, collection_id null, started_at, ended_at null, distance_m int, note |
| `stop_events` | id, shift_id, stop_id, kind ('done','skipped'), reason null, happened_at (device time), received_at, client_id uuid unique (idempotency) |
| `truck_live` | truck_id PK, shift_id, barangay_id, point geography(Point), heading smallint, speed real, updated_at |
| `driver_issues` | id, shift_id, kind, note, happened_at |

### Reports
| Table | Columns |
|---|---|
| `reports` | id, reporter_id null (null after account deletion), barangay_id, category, description (≤280), point geography(Point), geohash text, photo_path null, status, reject_reason null, merged_into null (FK self), flagged bool, metoo_count int, is_system bool, client_id uuid unique, created_at, verified_at, resolved_at |
| `report_events` | id, report_id, actor_id null, from_status, to_status, note, at. Append-only timeline |
| `report_confirmations` | report_id, user_id, at, PK(report_id, user_id). The "Me too" button |
| `report_after_photos` | report_id PK, photo_path, uploaded_by, at |

### Communication
| Table | Columns |
|---|---|
| `announcements` | id, title (≤80), body (≤1000), link_url null (https only, check constraint), barangay_ids smallint[] null (null = city-wide), priority ('normal','urgent'), publish_at, expires_at null, created_by |
| `notifications` | id bigint identity, user_id, type, title, body, deep_link, created_at, sent_at null, read_at null |

### Engagement
| Table | Columns |
|---|---|
| `points_ledger` | id bigint identity, user_id, delta int, reason, ref_table, ref_id, created_at. Append-only |
| `badges` | code PK, names and descriptions in en/fil/ceb, rule jsonb |
| `user_badges` | user_id, badge_code, earned_at, PK(user_id, badge_code) |
| `barangay_scores` | barangay_id, month date, score numeric, ontime_rate, resolution_rate, response_score, participation, frozen bool, PK(barangay_id, month) |
| view `user_points` | user_id, total, month_total, level (computed from the ledger) |

### Content, ML, system
| Table | Columns |
|---|---|
| `guide_categories`, `guide_items` | translated fields (en/fil/ceb), category, tips, content_version |
| `scan_feedback` | id, user_id null, predicted waste_type, confidence real, corrected null, photo_path null (only with consent), model_version, created_at, label_status ('new','confirmed','corrected','discarded') |
| `app_config` | key PK, value jsonb (limits, min_version, maintenance, flags) |
| `audit_log` | id bigint identity, actor_id, action, target_table, target_id, before jsonb, after jsonb, at. Append-only, nobody has UPDATE/DELETE |
| `app_metrics` | day, db_bytes, storage_bytes, mau, notes |

### Indexes
GiST on every geography column. Btree on reports (barangay_id, status, created_at) and (geohash, category, created_at) for duplicate search, collections (barangay via schedule, on_date), points_ledger (user_id, created_at), notifications (user_id, created_at).

## 3. Helper functions
All use `search_path = ''` and fully qualified names. Functions that only read the caller's own JWT (`auth_role`, `auth_barangay`, `is_aal2`, `is_staff_aal2`, `can_manage_barangay`) are SECURITY INVOKER, since they need no extra rights. Functions that write protected tables (`handle_new_user`, `set_user_role`, later `award_points` etc.) are SECURITY DEFINER with EXECUTE granted only where listed. Implemented so far: migration 0002.
- `auth_role()` returns the JWT claim `user_role` (default resident).
- `auth_barangay()` returns the JWT claim `barangay_id`.
- `is_staff_aal2()`: role in (officer, city_admin, super_admin) AND `auth.jwt()->>'aal' = 'aal2'`.
- `can_manage_barangay(b)`: city/super admin with AAL2, or an officer with AAL2 whose barangay = b.
- `award_points(user, delta, reason, ref)`: inserts a ledger row and enforces the daily cap. Callable only from other definer functions and triggers.
- `set_report_status(report_id, to_status, note, reason)`: checks the transition and permission, writes report_events and audit_log, awards points, queues the notification.
- `start_shift`, `record_stop_event`, `end_shift`: check the driver is assigned to that truck/route today; idempotent by client_id.
- `set_user_role(user, role, barangay)`: super_admin + AAL2 only; audited.
- `custom_access_token_hook(event)`: adds `user_role` and `barangay_id` to the JWT.

## 4. RLS policy matrix
S = select, I = insert, U = update, D = delete. "own" = the row belongs to auth.uid(). Staff columns require AAL2.

| Table | Guest | Resident | Driver | Officer | City/Super admin |
|---|---|---|---|---|---|
| barangays, zones | S | S | S | S | S; I/U/D super |
| profiles | — | S/U own (no role fields) | S/U own | S display name of own-barangay reporters | S via minimal view |
| user_roles | — | S own | S own | S own barangay | S all; change only via `set_user_role` |
| push_tokens, notification_prefs | — | own | own | own | own |
| trucks, routes, route_stops | S active | S | S | S; I/U own-barangay routes | all |
| schedules, exceptions, collections | S published | S published | S assigned + published | all, own barangay | all |
| shifts, stop_events, driver_issues | — | — | own, via functions only | S own barangay | S all |
| truck_live | S via view (no driver id) | S | write via function only | S | S |
| reports | — | S own; I via `create-report` only | — | S own barangay; U via `set_report_status` | all, via function |
| report_events | — | S for own reports | — | S own barangay | S all |
| report_confirmations | — | I/S own | — | S | S |
| announcements | S published, not expired | S | S | I/U/D own barangay | all |
| notifications | — | S, U(read_at) own | own | own | own |
| points_ledger, user_badges | — | S own | S own | aggregates own barangay | S all; writes via functions only |
| barangay_scores | S | S | S | S | S; recompute via function |
| guide_* | S | S | S | S | I/U/D admin |
| scan_feedback | I (no photo) | I own, S own | I own | — | S/U super (labeling) |
| app_config | S public keys via view | S | S | S | U super |
| audit_log | — | — | — | — | S super; nobody U/D |

Column privileges: `anon` and `authenticated` get no direct UPDATE on status, role, points, flagged, is_system or verified/resolved timestamps. Those change only through definer functions.

## 5. Realtime
- Private broadcast channels `truck:{barangay_id}`, with a Realtime Authorization policy on `realtime.messages`:
  - receive: anyone on `truck:*` topics (it's public info: a truck on a route).
  - send: only a driver with an active shift whose route is in that barangay.
- Residents don't use Postgres-changes subscriptions (cost). The admin fleet map subscribes to `truck_live` changes (staff only, few clients).

## 6. Storage buckets (all private)
| Bucket | Path | Max | MIME | Read | Write |
|---|---|---|---|---|---|
| `report-photos` | `{user_id}/{report_id}.webp` | 400 KB | image/webp, image/jpeg | reporter + staff of that barangay, signed URL (10 min) | reporter, once, via signed upload URL from `create-report` |
| `report-after` | `{report_id}.webp` | 400 KB | same | reporter + staff | staff of that barangay |
| `scan-feedback` | `{yyyy-mm}/{id}.webp` | 300 KB | same | super admin | user with consent, once |

No public buckets. No avatars or announcement images in v1, which keeps the attack surface smaller.

## 7. Retention
| Data | Kept |
|---|---|
| Report photos | until resolved/rejected + 60 days |
| Reports (text, location) | 2 years, then location rounded to ~100 m |
| truck_live | cleared at shift end; shifts and stop events 1 year |
| notifications | 90 days |
| Scan feedback photos | 180 days unless confirmed into the dataset |
| audit_log | 2 years |
| Deleted accounts | profile hard-deleted within 24 h; reports anonymized |

## 8. Size budget (free: 500 MB DB, 1 GB storage)
Rough estimate at 1,000 residents for a year: reports 5k × 1 KB = 5 MB; collections ~4k rows; stop events ~150k × 150 B ≈ 23 MB; notifications (rolling 90 days) ~100k × 300 B ≈ 30 MB; boundaries ~5 MB. Total under 100 MB. Photos: ~500 open reports × 150 KB ≈ 75 MB. `usage-watch` alerts at 80% of either limit.

## 9. Migrations and seeds
- `supabase/migrations/NNNN_name.sql`, forward-only, reviewed in PRs, tested by pgTAP in CI.
- Seeds: barangays + boundaries, waste guide, badges, app_config defaults, demo trucks/routes/schedules (dev only, plus a clearly labeled "Demo" set in prod).
- Every policy has at least one pgTAP test proving a forbidden role is denied.
