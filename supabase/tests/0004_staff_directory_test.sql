-- pgTAP tests for migration 0004 (list_staff). Run with: supabase test db
begin;
create extension if not exists pgtap with schema extensions;
select plan(6);

insert into auth.users (id, email, aud, role, raw_user_meta_data) values
  ('00000000-0000-0000-0000-0000000000a1', 'res@test.invalid', 'authenticated', 'authenticated', '{}'),
  ('00000000-0000-0000-0000-0000000000a2', 'off16@test.invalid', 'authenticated', 'authenticated', '{"display_name":"Officer Lagao"}'),
  ('00000000-0000-0000-0000-0000000000a3', 'off6@test.invalid', 'authenticated', 'authenticated', '{}'),
  ('00000000-0000-0000-0000-0000000000a4', 'admin@test.invalid', 'authenticated', 'authenticated', '{}');
update public.user_roles set role = 'barangay_officer', barangay_id = 16 where user_id = '00000000-0000-0000-0000-0000000000a2';
update public.user_roles set role = 'barangay_officer', barangay_id = 6  where user_id = '00000000-0000-0000-0000-0000000000a3';
update public.user_roles set role = 'city_admin' where user_id = '00000000-0000-0000-0000-0000000000a4';

create function pg_temp.as_user(uid text, user_role text, aal text default 'aal1', brgy int default null)
returns void language sql as $$
  select set_config('request.jwt.claims', jsonb_strip_nulls(jsonb_build_object(
    'sub', uid, 'role', 'authenticated', 'user_role', user_role, 'aal', aal, 'barangay_id', brgy))::text, true);
$$;

set local role anon;
select throws_ok($$ select * from public.list_staff() $$, '42501', null, 'guests cannot call list_staff');
reset role;

select pg_temp.as_user('00000000-0000-0000-0000-0000000000a1', 'resident', 'aal2');
set local role authenticated;
select is((select count(*)::int from public.list_staff()), 0, 'a resident gets an empty staff list');
reset role;

select pg_temp.as_user('00000000-0000-0000-0000-0000000000a4', 'city_admin', 'aal1');
set local role authenticated;
select is((select count(*)::int from public.list_staff()), 0, 'a city admin without MFA gets nothing');
reset role;

select pg_temp.as_user('00000000-0000-0000-0000-0000000000a4', 'city_admin', 'aal2');
set local role authenticated;
select is((select count(*)::int from public.list_staff() where email like '%@test.invalid'), 3, 'a city admin with MFA sees all 3 staff');
select is((select count(*)::int from public.list_staff() where email = 'res@test.invalid'), 0, 'residents never appear');
reset role;

select pg_temp.as_user('00000000-0000-0000-0000-0000000000a2', 'barangay_officer', 'aal2', 16);
set local role authenticated;
select is((select array_agg(email order by email) from public.list_staff() where email like '%@test.invalid'),
  array['off16@test.invalid'], 'an officer with MFA sees only their own barangay staff');
reset role;

select * from finish();
rollback;
