-- 0001 foundations (Phase 0)
-- Extensions, default-deny privileges, app_config and the public health() check.
-- Forward-only: never edit this file after it has been applied; add a new migration instead.

-- ---------------------------------------------------------------------------
-- 1. Extensions
-- ---------------------------------------------------------------------------
-- PostGIS: geography types for barangay boundaries, routes, stops, report pins.
create extension if not exists postgis with schema extensions;
-- pg_cron: scheduled jobs (reminders, retention, scores). Lives in its own "cron" schema.
create extension if not exists pg_cron with schema pg_catalog;
-- pg_net: lets the database call Edge Functions over HTTPS (push pipeline). Creates the "net" schema.
create extension if not exists pg_net with schema extensions;

-- ---------------------------------------------------------------------------
-- 2. Default deny
-- ---------------------------------------------------------------------------
-- Supabase normally gives the public API roles (anon = not signed in, authenticated = signed in)
-- full rights on every NEW table and function in "public". We switch that off, so each future
-- table/function must grant exactly what it needs. RLS still guards every row on top of this.
alter default privileges in schema public revoke all on tables from anon, authenticated;
alter default privileges in schema public revoke all on sequences from anon, authenticated;
alter default privileges in schema public revoke execute on functions from public, anon, authenticated;

-- ---------------------------------------------------------------------------
-- 3. app_config: limits, min app version, maintenance switch, feature flags
-- ---------------------------------------------------------------------------
create table public.app_config (
  key         text primary key check (key ~ '^[a-z][a-z0-9_]{1,62}$'),
  value       jsonb not null,
  is_public   boolean not null default false,  -- true = readable by the app without signing in
  description text check (char_length(description) <= 280),
  updated_at  timestamptz not null default now()
);
comment on table public.app_config is 'App-wide settings. Public keys are readable by anyone; writes only by super admin (policy added in Phase 1) or service code.';

alter table public.app_config enable row level security;
alter table public.app_config force row level security;

revoke all on public.app_config from anon, authenticated;
-- Only these columns can ever be read through the API; description/updated_at stay internal.
grant select (key, value, is_public) on public.app_config to anon, authenticated;

create policy "public config keys are readable by everyone"
  on public.app_config
  for select
  to anon, authenticated
  using (is_public);
-- No insert/update/delete policies on purpose: the API can't change config.

-- Convenience view the app reads: only public keys. Runs with the caller's rights (security_invoker),
-- so the RLS policy above still applies.
create view public.app_config_public
  with (security_invoker = true)
  as select key, value from public.app_config where is_public;
revoke all on public.app_config_public from anon, authenticated;
grant select on public.app_config_public to anon, authenticated;

insert into public.app_config (key, value, is_public, description) values
  ('schema_version',   '1',     true,  'Bumped by migrations that the app needs to know about.'),
  ('maintenance',      'false', true,  'true = app shows the maintenance screen.'),
  ('min_version_code', '1',     true,  'Installs below this versionCode must update (security fixes).');

-- ---------------------------------------------------------------------------
-- 4. health(): tiny public check used by the keep-alive workflow and uptime monitor
-- ---------------------------------------------------------------------------
-- Call: POST {SUPABASE_URL}/rest/v1/rpc/health with the anon key.
-- Runs as the caller (security invoker), touches the database, and reveals nothing private.
create function public.health()
returns jsonb
language sql
stable
security invoker
set search_path = ''
as $$
  select jsonb_build_object(
    'ok', true,
    'time', now(),
    'schema_version', (select c.value from public.app_config_public c where c.key = 'schema_version')
  );
$$;
comment on function public.health() is 'Public liveness check. Keeps the free-tier project from pausing.';

revoke all on function public.health() from public, anon, authenticated;
grant execute on function public.health() to anon, authenticated;
