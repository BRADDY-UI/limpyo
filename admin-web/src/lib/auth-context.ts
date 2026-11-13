import type { Session } from "@supabase/supabase-js";
import { createContext, useContext, useEffect } from "react";
import { supabase, type Role } from "./supabase";

export interface AuthValue {
  loading: boolean;
  session: Session | null;
  role: Role;
  barangayId?: number;
  /** "aal2" once the TOTP code was verified in this session. */
  aal: string | undefined;
}

export const AuthContext = createContext<AuthValue>({ loading: true, session: null, role: "resident", aal: undefined });

export const useAuth = () => useContext(AuthContext);
/** Signs staff out after 30 minutes without any interaction (docs/SECURITY.md). */
export function useIdleSignOut(minutes = 30) {
  useEffect(() => {
    let timer = window.setTimeout(signOut, minutes * 60_000);
    const reset = () => {
      window.clearTimeout(timer);
      timer = window.setTimeout(signOut, minutes * 60_000);
    };
    function signOut() {
      supabase.auth.signOut();
    }
    const events = ["pointerdown", "keydown", "scroll"] as const;
    events.forEach((e) => window.addEventListener(e, reset, { passive: true }));
    return () => {
      window.clearTimeout(timer);
      events.forEach((e) => window.removeEventListener(e, reset));
    };
  }, [minutes]);
}
