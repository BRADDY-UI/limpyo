import { createClient } from "@supabase/supabase-js";

const url = import.meta.env.VITE_SUPABASE_URL as string | undefined;
const key = import.meta.env.VITE_SUPABASE_PUBLISHABLE_KEY as string | undefined;

/** False when .env.local is missing: the app shows a setup message instead of failing. */
export const configured = Boolean(url?.startsWith("https://") && key);

// Session lives in memory + sessionStorage only (cleared when the tab closes). docs/SECURITY.md, Admin web.
export const supabase = createClient(url ?? "https://unconfigured.invalid", key ?? "unconfigured", {
  auth: {
    persistSession: true,
    storage: typeof window !== "undefined" ? window.sessionStorage : undefined,
    flowType: "pkce",
    detectSessionInUrl: true,
  },
});

export type Role = "resident" | "driver" | "barangay_officer" | "city_admin" | "super_admin";

export const STAFF: Role[] = ["barangay_officer", "city_admin", "super_admin"];

/** Claims added by the custom access token hook (migration 0002). The server re-checks everything. */
export function claimsOf(accessToken: string | undefined): { user_role: Role; barangay_id?: number; aal?: string } {
  if (!accessToken) return { user_role: "resident" };
  try {
    const part = accessToken.split(".")[1];
    const json = atob(part.replace(/-/g, "+").replace(/_/g, "/"));
    const c = JSON.parse(json);
    return { user_role: (c.user_role as Role) ?? "resident", barangay_id: c.barangay_id, aal: c.aal };
  } catch {
    return { user_role: "resident" };
  }
}

export const ROLE_LABEL: Record<Role, string> = {
  resident: "Resident",
  driver: "Driver",
  barangay_officer: "Barangay officer",
  city_admin: "City admin",
  super_admin: "Super admin",
};
