-- 0005 lock platform helper functions (Phase 1E)
-- Found by `supabase db advisors` on limpyo-dev: hosted projects created with
-- "enable RLS automatically" get public.rls_auto_enable(), a SECURITY DEFINER
-- event-trigger helper that the API roles could call via /rest/v1/rpc.
-- It isn't meant to be called by anyone directly, so remove that access.
-- (The function doesn't exist on a local database; the guard makes this a no-op there.)
do $$
begin
  if to_regprocedure('public.rls_auto_enable()') is not null then
    revoke execute on function public.rls_auto_enable() from public, anon, authenticated;
  end if;
end;
$$;
