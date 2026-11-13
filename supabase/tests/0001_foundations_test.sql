-- pgTAP tests for migration 0001. Run with: supabase test db
-- Each test acts as a real API role and checks that forbidden actions fail.
begin;
create extension if not exists pgtap with schema extensions;
select plan(13);

-- Extensions are installed.
select has_extension('postgis', 'PostGIS installed');
select has_extension('pg_cron', 'pg_cron installed');
select has_extension('pg_net', 'pg_net installed');

-- Every table in public has RLS enabled and forced (CI guard for future tables too).
select is(
  (select count(*)::int from pg_class c join pg_namespace n on n.oid = c.relnamespace
    where n.nspname = 'public' and c.relkind = 'r'
      and not (c.relrowsecurity and c.relforcerowsecurity)
      and c.relname <> 'spatial_ref_sys'),
  0,
  'every public table has RLS enabled and forced'
);

-- Seed a private key to prove it stays hidden.
insert into public.app_config (key, value, is_public) values ('secret_test_key', '"hidden"', false);

-- ---- as anon (not signed in) ----
set local role anon;

select is((public.health() ->> 'ok')::boolean, true, 'anon can call health()');
select is((public.health() ->> 'schema_version'), '1', 'health() reports schema_version');
select is(
  (select count(*)::int from public.app_config_public where key = 'maintenance'),
  1,
  'anon can read a public config key'
);
select is(
  (select count(*)::int from public.app_config where key = 'secret_test_key'),
  0,
  'anon cannot see private config keys'
);
select throws_ok(
  $$ select description from public.app_config $$,
  '42501', null,
  'anon cannot read internal columns'
);
select throws_ok(
  $$ insert into public.app_config (key, value) values ('evil', 'true') $$,
  '42501', null,
  'anon cannot insert config'
);
select throws_ok(
  $$ update public.app_config set value = 'true' where key = 'maintenance' $$,
  '42501', null,
  'anon cannot update config'
);

-- ---- as authenticated (signed-in resident) ----
set local role authenticated;

select throws_ok(
  $$ delete from public.app_config where key = 'maintenance' $$,
  '42501', null,
  'signed-in users cannot delete config'
);
select is(
  (select count(*)::int from public.app_config where key = 'secret_test_key'),
  0,
  'signed-in users cannot see private config keys'
);

reset role;
select * from finish();
rollback;
