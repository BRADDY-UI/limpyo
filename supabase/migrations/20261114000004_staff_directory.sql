-- 0004 staff directory (Phase 1E)
-- Admin web W-PEOPLE needs staff emails and names, which live in auth.users and profiles
-- (neither is readable by other users). This function returns the minimum, scoped like
-- user_roles: city/super admins see all staff, officers see their own barangay's staff.
-- Everyone needs MFA (aal2). Residents never appear.
create function public.list_staff()
returns table (
  user_id      uuid,
  email        text,
  display_name text,
  role         public.app_role,
  barangay_id  smallint,
  granted_at   timestamptz
)
language sql
stable
security definer
set search_path = ''
as $$
  select ur.user_id, u.email::text, p.display_name, ur.role, ur.barangay_id, ur.granted_at
  from public.user_roles ur
  join auth.users u on u.id = ur.user_id
  left join public.profiles p on p.id = ur.user_id
  where ur.role <> 'resident'
    and public.is_staff_aal2()
    and (public.auth_role() in ('city_admin', 'super_admin') or ur.barangay_id = public.auth_barangay())
  order by ur.role, u.email;
$$;
comment on function public.list_staff() is 'W-PEOPLE: staff list for admins with MFA. Returns nothing for anyone else.';
revoke all on function public.list_staff() from public, anon;
grant execute on function public.list_staff() to authenticated;
