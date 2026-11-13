import type { Session } from "@supabase/supabase-js";
import { useEffect, useState, type ReactNode } from "react";
import { AuthContext } from "./auth-context";
import { claimsOf, supabase } from "./supabase";

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    supabase.auth.getSession().then(({ data }) => {
      setSession(data.session);
      setLoading(false);
    });
    const { data } = supabase.auth.onAuthStateChange((_event, s) => setSession(s));
    return () => data.subscription.unsubscribe();
  }, []);

  const c = claimsOf(session?.access_token);
  return <AuthContext.Provider value={{ loading, session, role: c.user_role, barangayId: c.barangay_id, aal: c.aal }}>{children}</AuthContext.Provider>;
}
