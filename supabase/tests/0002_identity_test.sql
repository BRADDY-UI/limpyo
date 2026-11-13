-- pgTAP tests for migrations 0002 (identity) and 0003 (barangays). Run with: supabase test db
-- Every rule is tested from the side of someone who must NOT be allowed.
begin;
create extension if not exists pgtap with schema extensions;
select plan(42);

-- ---------------------------------------------------------------------------
-- Fixtures (as postgres): 5 accounts. Signing up runs the on_auth_user_created trigger.
-- ---------------------------------------------------------------------------
insert into auth.users (id, email, aud, role, raw_user_meta_data) values
  ('00000000-0000-0000-0000-00000000000a', 'a@test.invalid', 'authenticated', 'authenticated',
   '{"display_name": "  Ana  ", "language": "ceb", "role": "super_admin", "user_role": "super_admin"}'),
  ('00000000-0000-0000-0000-00000000000b', 'b@test.invalid', 'authenticated', 'authenticated', '{"display_name": "B"}'),
  ('00000000-0000-0000-0000-00000000000c', 'c@test.invalid', 'authenticated', 'authenticated', '{"language": "xx"}'),
  ('00000000-0000-0000-0000-00000000000d', 'd@test.invalid', 'authenticated', 'authenticated', '{}'),
  ('00000000-0000-0000-0000-00000000000e', 'e@test.invalid', 'authenticated', 'authenticated', '{}');

-- c = barangay officer of Lagao (16), d = super admin, e = city admin (set directly as postgres for the fixture)
update public.user_roles set role = 'barangay_officer', barangay_id = 16 where user_id = '00000000-0000-0000-0000-00000000000c';
update public.user_roles set role = 'super_admin' where user_id = '00000000-0000-0000-0000-00000000000d';
update public.user_roles set role = 'city_admin' where user_id = '00000000-0000-0000-0000-00000000000e';
update public.profiles set home_point = extensions.st_setsrid(extensions.st_makepoint(125.19, 6.128), 4326)::extensions.geography,
  barangay_id = 16 where id = '00000000-0000-0000-0000-00000000000b';

-- Acts as a signed-in user with the given JWT claims (role/aal as the hook and MFA would set them).
create function pg_temp.as_user(uid text, user_role text, aal text default 'aal1', brgy int default null)
returns void language sql as $$
  select set_config('request.jwt.claims', jsonb_strip_nulls(jsonb_build_object(
    'sub', uid, 'role', 'authenticated', 'user_role', user_role, 'aal', aal, 'barangay_id', brgy))::text, true);
$$;

-- ---------------------------------------------------------------------------
-- Barangays (0003)
-- ---------------------------------------------------------------------------
select is((select count(*)::int from public.barangays), 26, '26 barangays loaded');
select is((select count(*)::int from public.barangays where boundary is null), 0, 'every barangay has a boundary');
select ok((select bool_and(approximate) from public.barangays), 'derived boundaries are marked approximate');
select is(
  (select b.name from public.barangays b
    where extensions.st_covers(b.boundary, extensions.st_setsrid(extensions.st_makepoint(125.1904, 6.1281), 4326)::extensions.geography)),
  'Lagao', 'the Lagao center point falls inside Lagao');
select ok((select bool_and(public.in_gensan(centroid)) from public.barangays), 'all centers are inside General Santos');

-- ---------------------------------------------------------------------------
-- Sign-up trigger
-- ---------------------------------------------------------------------------
select is((select role::text from public.user_roles where user_id = '00000000-0000-0000-0000-00000000000a'), 'resident',
  'a role in sign-up metadata is ignored: new accounts are residents');
select is((select display_name from public.profiles where id = '00000000-0000-0000-0000-00000000000a'), 'Ana', 'display name is trimmed');
select is((select language from public.profiles where id = '00000000-0000-0000-0000-00000000000a'), 'ceb', 'valid language is kept');
select is((select display_name from public.profiles where id = '00000000-0000-0000-0000-00000000000b'), null, 'a 1-letter name is dropped, not rejected');
select is((select language from public.profiles where id = '00000000-0000-0000-0000-00000000000c'), 'en', 'unknown language falls back to English');
select is((select count(*)::int from public.notification_prefs), 5, 'everyone gets default notification prefs');

-- Constraints
select throws_ok($$ update public.profiles set home_point = extensions.st_setsrid(extensions.st_makepoint(121.0, 14.6), 4326)::extensions.geography
  where id = '00000000-0000-0000-0000-00000000000a' $$, '23514', null, 'a home pin outside General Santos is rejected');
select throws_ok($$ update public.user_roles set role = 'barangay_officer', barangay_id = null
  where user_id = '00000000-0000-0000-0000-00000000000a' $$, '23514', null, 'an officer must have a barangay');

-- ---------------------------------------------------------------------------
-- Guest (anon)
-- ---------------------------------------------------------------------------
set local role anon;
select is((select count(*)::int from public.barangays), 26, 'guests can list barangays');
select throws_ok($$ select * from public.profiles $$, '42501', null, 'guests cannot read profiles');
select throws_ok($$ select * from public.user_roles $$, '42501', null, 'guests cannot read roles');
select throws_ok($$ insert into public.barangays (id, name, slug, centroid) values (99, 'Fake', 'fake',
  extensions.st_setsrid(extensions.st_makepoint(125.1, 6.1), 4326)::extensions.geography) $$, '42501', null, 'guests cannot add barangays');

-- ---------------------------------------------------------------------------
-- Resident A
-- ---------------------------------------------------------------------------
reset role;
select pg_temp.as_user('00000000-0000-0000-0000-00000000000a', 'resident');
set local role authenticated;

select is((select count(*)::int from public.profiles), 1, 'a resident sees exactly one profile: their own');
select is((select count(*)::int from public.profiles where id = '00000000-0000-0000-0000-00000000000b'), 0,
  'a resident cannot see another person''s profile or home pin');
select lives_ok($$ update public.profiles set display_name = 'Ana Cruz', barangay_id = 6
  where id = '00000000-0000-0000-0000-00000000000a' $$, 'a resident can edit their own profile');
update public.profiles set display_name = 'hacked' where id = '00000000-0000-0000-0000-00000000000b';
select throws_ok($$ update public.profiles set deleted_at = now() where id = '00000000-0000-0000-0000-00000000000a' $$,
  '42501', null, 'a resident cannot set protected profile columns');
select throws_ok($$ update public.user_roles set role = 'super_admin' where user_id = '00000000-0000-0000-0000-00000000000a' $$,
  '42501', null, 'a resident cannot make themselves admin');
select throws_ok($$ insert into public.user_roles (user_id, role) values ('00000000-0000-0000-0000-00000000000a', 'super_admin') $$,
  '42501', null, 'a resident cannot insert a role row');
select throws_ok($$ select public.set_user_role('00000000-0000-0000-0000-00000000000b', 'city_admin') $$,
  '42501', null, 'a resident cannot call set_user_role');
select is((select count(*)::int from public.user_roles), 1, 'a resident sees only their own role');
select is((select count(*)::int from public.audit_log), 0, 'a resident cannot read the audit log');
select throws_ok($$ insert into public.audit_log (action) values ('forged') $$, '42501', null, 'a resident cannot write the audit log');

-- Push tokens
select lives_ok($$ insert into public.push_tokens (token) values ('resident-a-token-0123456789') $$, 'a resident can register a push token');
select throws_ok($$ insert into public.push_tokens (token, user_id) values ('stolen-token-0123456789xx', '00000000-0000-0000-0000-00000000000b') $$,
  '42501', null, 'a resident cannot register a token for someone else');

-- ---------------------------------------------------------------------------
-- Back as postgres: check the resident's writes, then officer and admin cases
-- ---------------------------------------------------------------------------
reset role;
select is((select display_name from public.profiles where id = '00000000-0000-0000-0000-00000000000b'), null,
  'the resident''s attempt to rename B changed nothing');
select is((select user_id::text from public.push_tokens where token = 'resident-a-token-0123456789'),
  '00000000-0000-0000-0000-00000000000a', 'a new push token is owned by the caller');

-- Officer of Lagao without MFA
select pg_temp.as_user('00000000-0000-0000-0000-00000000000c', 'barangay_officer', 'aal1', 16);
set local role authenticated;
select is((select count(*)::int from public.user_roles), 1, 'an officer without MFA sees only their own role');
reset role;

-- Officer with MFA: own barangay only
select pg_temp.as_user('00000000-0000-0000-0000-00000000000c', 'barangay_officer', 'aal2', 16);
set local role authenticated;
select is((select count(*)::int from public.user_roles where barangay_id is distinct from 16), 0, 'an officer with MFA sees no roles outside their barangay');
select ok(public.can_manage_barangay(16::smallint) and not public.can_manage_barangay(1::smallint), 'an officer manages only their own barangay');
reset role;

-- Super admin without MFA
select pg_temp.as_user('00000000-0000-0000-0000-00000000000d', 'super_admin', 'aal1');
set local role authenticated;
select throws_ok($$ select public.set_user_role('00000000-0000-0000-0000-00000000000b', 'city_admin') $$,
  '42501', null, 'a super admin without MFA cannot change roles');
reset role;

-- Super admin with MFA
select pg_temp.as_user('00000000-0000-0000-0000-00000000000d', 'super_admin', 'aal2');
set local role authenticated;
select lives_ok($$ select public.set_user_role('00000000-0000-0000-0000-00000000000b', 'barangay_officer', 16::smallint) $$,
  'a super admin with MFA can change a role');
select throws_ok($$ select public.set_user_role('00000000-0000-0000-0000-00000000000d', 'resident') $$,
  '42501', null, 'nobody can change their own role');
select is((select count(*)::int from public.audit_log where action = 'set_user_role'), 1, 'the role change is in the audit log');
reset role;

-- City admin with MFA can read roles but not change them
select pg_temp.as_user('00000000-0000-0000-0000-00000000000e', 'city_admin', 'aal2');
set local role authenticated;
select throws_ok($$ select public.set_user_role('00000000-0000-0000-0000-00000000000a', 'driver', 16::smallint) $$,
  '42501', null, 'a city admin cannot change roles');
reset role;

-- Audit log is append-only even for the database owner
select throws_ok($$ delete from public.audit_log $$, '42501', null, 'audit log rows cannot be deleted, even by postgres');

-- Access-token hook
select is(
  (public.custom_access_token_hook('{"user_id": "00000000-0000-0000-0000-00000000000b", "claims": {"sub": "x"}}'::jsonb)
    -> 'claims' ->> 'user_role'), 'barangay_officer', 'the hook adds user_role to the token');
select is(
  (public.custom_access_token_hook('{"user_id": "00000000-0000-0000-0000-00000000000b", "claims": {}}'::jsonb)
    -> 'claims' ->> 'barangay_id'), '16', 'the hook adds barangay_id for officers');

select * from finish();
rollback;
