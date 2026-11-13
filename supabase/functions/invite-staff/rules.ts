// Pure rules for invite-staff, kept separate so they're easy to read and test.

export type Role = "resident" | "driver" | "barangay_officer" | "city_admin" | "super_admin";

/** Who may invite which roles (docs/PRODUCT.md §3). Officers and drivers can't invite anyone. */
export const CAN_INVITE: Record<string, Role[]> = {
  super_admin: ["city_admin", "barangay_officer", "driver"],
  city_admin: ["barangay_officer", "driver"],
};

export const NEEDS_BARANGAY: Role[] = ["barangay_officer", "driver"];

export interface InviteRequest {
  email: string;
  role: Role;
  barangay_id: number | null;
  display_name: string | null;
}

export type Checked = { ok: true; value: InviteRequest } | { ok: false; error: string };

const EMAIL = /^[^\s@]{1,64}@[^\s@]{1,190}\.[^\s@]{2,63}$/;

/** Validates untrusted JSON. Unknown fields are rejected (docs/SECURITY.md, Edge Functions). */
export function checkRequest(body: unknown, callerRole: string): Checked {
  if (typeof body !== "object" || body === null || Array.isArray(body)) return { ok: false, error: "bad_request" };
  const b = body as Record<string, unknown>;
  const allowed = new Set(["email", "role", "barangay_id", "display_name"]);
  if (Object.keys(b).some((k) => !allowed.has(k))) return { ok: false, error: "unknown_field" };

  const email = typeof b.email === "string" ? b.email.trim().toLowerCase() : "";
  if (!EMAIL.test(email) || email.length > 254) return { ok: false, error: "invalid_email" };

  const role = b.role as Role;
  if (!(CAN_INVITE[callerRole] ?? []).includes(role)) return { ok: false, error: "role_not_allowed" };

  let barangay: number | null = null;
  if (NEEDS_BARANGAY.includes(role)) {
    if (!Number.isInteger(b.barangay_id) || (b.barangay_id as number) < 1 || (b.barangay_id as number) > 26) {
      return { ok: false, error: "barangay_required" };
    }
    barangay = b.barangay_id as number;
  }

  let name: string | null = null;
  if (b.display_name !== undefined && b.display_name !== null) {
    if (typeof b.display_name !== "string") return { ok: false, error: "invalid_name" };
    const n = b.display_name.trim();
    if (n.length < 2 || n.length > 30) return { ok: false, error: "invalid_name" };
    name = n;
  }
  return { ok: true, value: { email, role, barangay_id: barangay, display_name: name } };
}

/** Reads claims from a JWT that Supabase Auth has ALREADY verified (never trust an unverified token). */
export function claimsOf(jwt: string): Record<string, unknown> {
  const part = jwt.split(".")[1] ?? "";
  const json = atob(part.replace(/-/g, "+").replace(/_/g, "/").padEnd(Math.ceil(part.length / 4) * 4, "="));
  return JSON.parse(json);
}
