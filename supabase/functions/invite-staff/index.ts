// invite-staff: a city/super admin with MFA invites an officer, driver (or, super admin only, a city admin).
// docs/ARCHITECTURE.md Edge Functions. The service key is used only here, on the server.
import { createClient } from "npm:@supabase/supabase-js@2.118.0";
import { checkRequest, claimsOf } from "./rules.ts";

const URL = Deno.env.get("SUPABASE_URL")!;
const ANON = Deno.env.get("SUPABASE_ANON_KEY") ?? "";
const SERVICE = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
// Where the invite email's link lands (the admin web). Set with: supabase secrets set INVITE_REDIRECT_URL=...
const REDIRECT = Deno.env.get("INVITE_REDIRECT_URL") ?? "http://localhost:5173/login";
const ORIGINS = (Deno.env.get("ADMIN_ORIGINS") ?? "http://localhost:5173,https://limpyo-admin.pages.dev").split(",");

function cors(origin: string | null): HeadersInit {
  const allow = origin && ORIGINS.includes(origin) ? origin : ORIGINS[0];
  return {
    "Access-Control-Allow-Origin": allow,
    "Access-Control-Allow-Headers": "authorization, content-type, apikey, x-client-info",
    "Access-Control-Allow-Methods": "POST, OPTIONS",
    Vary: "Origin",
  };
}

function reply(status: number, body: unknown, origin: string | null) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...cors(origin), "Content-Type": "application/json" },
  });
}

Deno.serve(async (req) => {
  const origin = req.headers.get("Origin");
  if (req.method === "OPTIONS") return new Response(null, { status: 204, headers: cors(origin) });
  if (req.method !== "POST") return reply(405, { error: "method_not_allowed" }, origin);

  // 1. Who is calling? Supabase Auth verifies the token; only then are its claims read.
  const token = (req.headers.get("Authorization") ?? "").replace(/^Bearer\s+/i, "");
  if (!token) return reply(401, { error: "unauthorized" }, origin);
  const asCaller = createClient(URL, ANON, { global: { headers: { Authorization: `Bearer ${token}` } } });
  const { data: me, error: meErr } = await asCaller.auth.getUser(token);
  if (meErr || !me?.user) return reply(401, { error: "unauthorized" }, origin);
  const claims = claimsOf(token);
  const callerRole = String(claims.user_role ?? "resident");
  if (claims.aal !== "aal2") return reply(403, { error: "mfa_required" }, origin);

  // 2. Is the request allowed and well-formed?
  let body: unknown;
  try {
    body = await req.json();
  } catch {
    return reply(400, { error: "bad_request" }, origin);
  }
  const checked = checkRequest(body, callerRole);
  if (!checked.ok) return reply(checked.error === "role_not_allowed" ? 403 : 400, { error: checked.error }, origin);
  const invite = checked.value;

  // 3. Invite and assign the role (service key: bypasses RLS, server-side only).
  const admin = createClient(URL, SERVICE, { auth: { persistSession: false } });
  const { data: created, error: invErr } = await admin.auth.admin.inviteUserByEmail(invite.email, {
    redirectTo: REDIRECT,
    data: invite.display_name ? { display_name: invite.display_name } : {},
  });
  if (invErr || !created?.user) {
    const taken = /already|registered|exists/i.test(invErr?.message ?? "");
    return reply(taken ? 409 : 500, { error: taken ? "already_registered" : "invite_failed" }, origin);
  }
  const userId = created.user.id;

  const { error: roleErr } = await admin
    .from("user_roles")
    .update({ role: invite.role, barangay_id: invite.barangay_id, granted_by: me.user.id, granted_at: new Date().toISOString() })
    .eq("user_id", userId);
  if (roleErr) return reply(500, { error: "role_failed" }, origin);

  await admin.from("audit_log").insert({
    actor_id: me.user.id,
    action: "invite_staff",
    target_table: "user_roles",
    target_id: userId,
    after: { email: invite.email, role: invite.role, barangay_id: invite.barangay_id },
  });

  return reply(200, { user_id: userId }, origin);
});
