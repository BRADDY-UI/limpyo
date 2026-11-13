-- 0002 identity (Phase 1A)
-- Barangays and zones, profiles, roles, push tokens, notification prefs, audit log,
-- JWT claims hook, role-change function, and RLS for all of them (docs/DATABASE.md).
-- Forward-only: never edit after it has been applied; add a new migration instead.

-- ---------------------------------------------------------------------------
-- 1. Types
-- ---------------------------------------------------------------------------
create type public.app_role as enum ('resident', 'driver', 'barangay_officer', 'city_admin', 'super_admin');

-- ---------------------------------------------------------------------------
-- 2. Helper: is a point inside the General Santos area? (bounding box incl. municipal waters)
-- ---------------------------------------------------------------------------
create function public.in_gensan(p extensions.geography)
returns boolean
language sql
immutable
set search_path = ''
as $$
  select p is null or (
    extensions.st_x(p::extensions.geometry) between 124.90 and 125.40
    and extensions.st_y(p::extensions.geometry) between 5.90 and 6.40
  );
$$;
comment on function public.in_gensan(extensions.geography) is 'CHECK helper: point (or null) lies inside the General Santos bounding box.';

-- ---------------------------------------------------------------------------
-- 3. Places
-- ---------------------------------------------------------------------------
create table public.barangays (
  id          smallint primary key,
  name        text not null unique check (char_length(name) between 2 and 60),
  slug        text not null unique check (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$'),
  boundary    extensions.geography(MultiPolygon, 4326),
  centroid    extensions.geography(Point, 4326) not null check (public.in_gensan(centroid)),
  population  integer check (population >= 0),
  approximate boolean not null default false
);
comment on table public.barangays is 'The 26 barangays of General Santos. approximate = boundary was derived, not surveyed.';
create index barangays_boundary_gix on public.barangays using gist (boundary);

create table public.zones (
  id          integer generated always as identity primary key,
  barangay_id smallint not null references public.barangays (id) on delete cascade,
  name        text not null check (char_length(name) between 1 and 60),
  boundary    extensions.geography(MultiPolygon, 4326),
  unique (barangay_id, name)
);
comment on table public.zones is 'Puroks / zones inside a barangay.';
create index zones_boundary_gix on public.zones using gist (boundary);

-- ---------------------------------------------------------------------------
-- 4. People
-- ---------------------------------------------------------------------------
create table public.profiles (
  id                  uuid primary key references auth.users (id) on delete cascade,
  display_name        text check (display_name is null or char_length(btrim(display_name)) between 2 and 30),
  barangay_id         smallint references public.barangays (id),
  home_point          extensions.geography(Point, 4326) check (public.in_gensan(home_point)),
  language            text not null default 'en' check (language in ('en', 'fil', 'ceb')),
  theme               text not null default 'dark' check (theme in ('dark', 'light', 'system')),
  leaderboard_visible boolean not null default true,
  scan_photo_consent  boolean not null default false,
  consent_version     integer not null default 0 check (consent_version >= 0),
  created_at          timestamptz not null default now(),
  updated_at          timestamptz not null default now(),
  deleted_at          timestamptz
);
comment on table public.profiles is 'One row per account. Home pin is private: never readable by anyone but the owner.';
comment on column public.profiles.home_point is 'Optional; powers truck-nearby alerts. Owner-only.';

create table public.user_roles (
  user_id     uuid primary key references auth.users (id) on delete cascade,
  role        public.app_role not null default 'resident',
  barangay_id smallint references public.barangays (id),
  granted_by  uuid references auth.users (id) on delete set null,
  granted_at  timestamptz not null default now(),
  constraint officer_needs_barangay check (role <> 'barangay_officer' or barangay_id is not null)
);
comment on table public.user_roles is 'Exactly one role per person. Changed only through public.set_user_role().';

create table public.push_tokens (
  id           uuid primary key default gen_random_uuid(),
  user_id      uuid not null references auth.users (id) on delete cascade,
  token        text not null unique check (char_length(token) between 20 and 4096),
  platform     text not null default 'android' check (platform in ('android', 'web')),
  app_version  text check (char_length(app_version) <= 32),
  last_seen_at timestamptz not null default now()
);
create index push_tokens_user_idx on public.push_tokens (user_id);

create table public.notification_prefs (
  user_id     uuid primary key references auth.users (id) on delete cascade,
  prefs       jsonb not null default '{}'::jsonb check (jsonb_typeof(prefs) = 'object' and pg_column_size(prefs) < 2048),
  quiet_start time not null default '21:00',
  quiet_end   time not null default '06:00'
);

-- ---------------------------------------------------------------------------
-- 5. Audit log (append-only, nobody updates or deletes)
-- ---------------------------------------------------------------------------
create table public.audit_log (
  id           bigint generated always as identity primary key,
  actor_id     uuid,
  action       text not null check (char_length(action) between 2 and 64),
  target_table text,
  target_id    text,
  before       jsonb,
  after        jsonb,
  at           timestamptz not null default now()
);
create index audit_log_at_idx on public.audit_log (at desc);

create function public.audit_log_is_append_only()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
  raise exception 'audit_log is append-only' using errcode = '42501';
end;
$$;
create trigger audit_log_no_update before update or delete on public.audit_log
  for each row execute function public.audit_log_is_append_only();
create trigger audit_log_no_truncate before truncate on public.audit_log
  for each statement execute function public.audit_log_is_append_only();

-- ---------------------------------------------------------------------------
-- 6. JWT helpers (read the claims added by the access-token hook)
-- ---------------------------------------------------------------------------
create function public.auth_role()
returns public.app_role
language sql
stable
set search_path = ''
as $$
  select case
    when auth.jwt() ->> 'user_role' in ('resident', 'driver', 'barangay_officer', 'city_admin', 'super_admin')
      then (auth.jwt() ->> 'user_role')::public.app_role
    else 'resident'::public.app_role
  end;
$$;

create function public.auth_barangay()
returns smallint
language sql
stable
set search_path = ''
as $$
  select case when auth.jwt() ->> 'barangay_id' ~ '^[0-9]{1,4}$' then (auth.jwt() ->> 'barangay_id')::smallint end;
$$;

create function public.is_aal2()
returns boolean
language sql
stable
set search_path = ''
as $$
  select coalesce(auth.jwt() ->> 'aal', '') = 'aal2';
$$;

create function public.is_staff_aal2()
returns boolean
language sql
stable
set search_path = ''
as $$
  select public.auth_role() in ('barangay_officer', 'city_admin', 'super_admin') and public.is_aal2();
$$;

create function public.can_manage_barangay(b smallint)
returns boolean
language sql
stable
set search_path = ''
as $$
  select public.is_aal2() and (
    public.auth_role() in ('city_admin', 'super_admin')
    or (public.auth_role() = 'barangay_officer' and public.auth_barangay() = b)
  );
$$;

-- ---------------------------------------------------------------------------
-- 7. New account -> profile + resident role + default prefs
-- ---------------------------------------------------------------------------
-- raw_user_meta_data is user-controlled: only display_name and language are read,
-- both sanitized. A role in metadata is ignored on purpose.
create function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
  name text := left(btrim(coalesce(new.raw_user_meta_data ->> 'display_name', new.raw_user_meta_data ->> 'full_name', '')), 30);
  lang text := new.raw_user_meta_data ->> 'language';
begin
  insert into public.profiles (id, display_name, language)
  values (
    new.id,
    case when char_length(name) >= 2 then name end,
    case when lang in ('en', 'fil', 'ceb') then lang else 'en' end
  );
  insert into public.user_roles (user_id) values (new.id);
  insert into public.notification_prefs (user_id) values (new.id);
  return new;
end;
$$;
revoke all on function public.handle_new_user() from public, anon, authenticated;
create trigger on_auth_user_created after insert on auth.users
  for each row execute function public.handle_new_user();

create function public.touch_updated_at()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
  new.updated_at := now();
  return new;
end;
$$;
create trigger profiles_touch before update on public.profiles
  for each row execute function public.touch_updated_at();

-- ---------------------------------------------------------------------------
-- 8. Custom access token hook: adds user_role and barangay_id claims
--    Enable in Auth settings: Hooks -> Customize Access Token -> public.custom_access_token_hook
-- ---------------------------------------------------------------------------
create function public.custom_access_token_hook(event jsonb)
returns jsonb
language plpgsql
stable
set search_path = ''
as $$
declare
  claims jsonb := coalesce(event -> 'claims', '{}'::jsonb);
  r public.app_role;
  b smallint;
begin
  select ur.role, ur.barangay_id into r, b
  from public.user_roles ur
  where ur.user_id = (event ->> 'user_id')::uuid;

  claims := jsonb_set(claims, '{user_role}', to_jsonb(coalesce(r, 'resident')::text));
  if b is not null then
    claims := jsonb_set(claims, '{barangay_id}', to_jsonb(b));
  else
    claims := claims - 'barangay_id';
  end if;
  return jsonb_set(event, '{claims}', claims);
end;
$$;
grant usage on schema public to supabase_auth_admin;
grant execute on function public.custom_access_token_hook(jsonb) to supabase_auth_admin;
revoke execute on function public.custom_access_token_hook(jsonb) from public, anon, authenticated;

-- ---------------------------------------------------------------------------
-- 9. Role changes: super admin with MFA only, always audited
-- ---------------------------------------------------------------------------
create function public.set_user_role(target uuid, new_role public.app_role, new_barangay smallint default null)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
  old_row public.user_roles;
begin
  if not (public.auth_role() = 'super_admin' and public.is_aal2()) then
    raise exception 'only a super admin with MFA can change roles' using errcode = '42501';
  end if;
  if target = auth.uid() then
    raise exception 'you cannot change your own role' using errcode = '42501';
  end if;
  if new_role in ('barangay_officer', 'driver') and new_barangay is null then
    raise exception 'this role needs a barangay' using errcode = '22023';
  end if;

  select * into old_row from public.user_roles where user_id = target for update;
  if not found then
    raise exception 'no such user' using errcode = 'P0002';
  end if;

  update public.user_roles
     set role = new_role,
         barangay_id = case when new_role in ('barangay_officer', 'driver') then new_barangay end,
         granted_by = auth.uid(),
         granted_at = now()
   where user_id = target;

  insert into public.audit_log (actor_id, action, target_table, target_id, before, after)
  values (
    auth.uid(), 'set_user_role', 'user_roles', target::text,
    jsonb_build_object('role', old_row.role, 'barangay_id', old_row.barangay_id),
    jsonb_build_object('role', new_role, 'barangay_id', case when new_role in ('barangay_officer', 'driver') then new_barangay end)
  );
end;
$$;
revoke all on function public.set_user_role(uuid, public.app_role, smallint) from public, anon;
grant execute on function public.set_user_role(uuid, public.app_role, smallint) to authenticated;

-- ---------------------------------------------------------------------------
-- 10. Row Level Security (enabled + forced on every table, default deny)
-- ---------------------------------------------------------------------------
alter table public.barangays enable row level security;
alter table public.barangays force row level security;
alter table public.zones enable row level security;
alter table public.zones force row level security;
alter table public.profiles enable row level security;
alter table public.profiles force row level security;
alter table public.user_roles enable row level security;
alter table public.user_roles force row level security;
alter table public.push_tokens enable row level security;
alter table public.push_tokens force row level security;
alter table public.notification_prefs enable row level security;
alter table public.notification_prefs force row level security;
alter table public.audit_log enable row level security;
alter table public.audit_log force row level security;

-- Places: public read (guests too). Writes only through migrations/service code for now.
grant select on public.barangays, public.zones to anon, authenticated;
create policy "barangays are public" on public.barangays for select to anon, authenticated using (true);
create policy "zones are public" on public.zones for select to anon, authenticated using (true);

-- Profiles: owner reads and edits their own row. No insert (trigger) and no delete (delete-account function).
grant select on public.profiles to authenticated;
grant update (display_name, barangay_id, home_point, language, theme, leaderboard_visible, scan_photo_consent, consent_version)
  on public.profiles to authenticated;
create policy "read own profile" on public.profiles for select to authenticated using (id = (select auth.uid()));
create policy "update own profile" on public.profiles for update to authenticated
  using (id = (select auth.uid())) with check (id = (select auth.uid()));

-- Roles: read your own; staff with MFA read their scope. Writes only via set_user_role().
grant select on public.user_roles to authenticated;
create policy "read own role or staff scope" on public.user_roles for select to authenticated using (
  user_id = (select auth.uid())
  or (public.is_staff_aal2() and (
        public.auth_role() in ('city_admin', 'super_admin')
        or barangay_id = public.auth_barangay()))
);
grant select on public.user_roles to supabase_auth_admin;
create policy "auth hook reads roles" on public.user_roles for select to supabase_auth_admin using (true);

-- Push tokens: owner only.
grant select, delete on public.push_tokens to authenticated;
grant insert (token, platform, app_version) on public.push_tokens to authenticated;
grant update (app_version, last_seen_at) on public.push_tokens to authenticated;
alter table public.push_tokens alter column user_id set default auth.uid();
create policy "own push tokens" on public.push_tokens for all to authenticated
  using (user_id = (select auth.uid())) with check (user_id = (select auth.uid()));

-- Notification prefs: owner reads and edits.
grant select on public.notification_prefs to authenticated;
grant update (prefs, quiet_start, quiet_end) on public.notification_prefs to authenticated;
create policy "read own prefs" on public.notification_prefs for select to authenticated using (user_id = (select auth.uid()));
create policy "update own prefs" on public.notification_prefs for update to authenticated
  using (user_id = (select auth.uid())) with check (user_id = (select auth.uid()));

-- Audit log: super admin with MFA reads. Nobody writes through the API.
grant select on public.audit_log to authenticated;
create policy "super admin reads audit log" on public.audit_log for select to authenticated
  using (public.auth_role() = 'super_admin' and public.is_aal2());

-- Helpers callable by the API roles (they only read the caller's own JWT).
grant execute on function public.in_gensan(extensions.geography) to anon, authenticated;
grant execute on function public.auth_role(), public.auth_barangay(), public.is_aal2(),
  public.is_staff_aal2(), public.can_manage_barangay(smallint) to anon, authenticated;
