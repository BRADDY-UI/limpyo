// Run: node supabase/functions/invite-staff/rules.test.ts  (Node 24 strips the types)
import assert from "node:assert/strict";
import { checkRequest, claimsOf } from "./rules.ts";

const ok = (b: unknown, who: string) => checkRequest(b, who);
// allowed
assert.equal(ok({ email: "Officer@Example.com ", role: "barangay_officer", barangay_id: 16 }, "city_admin").ok, true);
assert.deepEqual((ok({ email: "a@b.co", role: "driver", barangay_id: 3, display_name: " Juan " }, "super_admin") as any).value,
  { email: "a@b.co", role: "driver", barangay_id: 3, display_name: "Juan" });
assert.equal(ok({ email: "c@d.ph", role: "city_admin" }, "super_admin").ok, true);
// escalation and bad input
assert.deepEqual(ok({ email: "c@d.ph", role: "city_admin" }, "city_admin"), { ok: false, error: "role_not_allowed" });
assert.deepEqual(ok({ email: "c@d.ph", role: "super_admin" }, "super_admin"), { ok: false, error: "role_not_allowed" });
assert.deepEqual(ok({ email: "c@d.ph", role: "driver", barangay_id: 1 }, "barangay_officer"), { ok: false, error: "role_not_allowed" });
assert.deepEqual(ok({ email: "c@d.ph", role: "driver", barangay_id: 1 }, "resident"), { ok: false, error: "role_not_allowed" });
assert.deepEqual(ok({ email: "c@d.ph", role: "driver" }, "city_admin"), { ok: false, error: "barangay_required" });
assert.deepEqual(ok({ email: "c@d.ph", role: "driver", barangay_id: 99 }, "city_admin"), { ok: false, error: "barangay_required" });
assert.deepEqual(ok({ email: "not-an-email", role: "driver", barangay_id: 1 }, "city_admin"), { ok: false, error: "invalid_email" });
assert.deepEqual(ok({ email: "c@d.ph", role: "driver", barangay_id: 1, is_admin: true }, "city_admin"), { ok: false, error: "unknown_field" });
assert.deepEqual(ok([1, 2], "city_admin"), { ok: false, error: "bad_request" });
assert.deepEqual(ok({ email: "c@d.ph", role: "driver", barangay_id: 1, display_name: "x" }, "city_admin"), { ok: false, error: "invalid_name" });
// claims
const jwt = "h." + Buffer.from(JSON.stringify({ user_role: "city_admin", aal: "aal2" })).toString("base64url") + ".s";
assert.deepEqual(claimsOf(jwt), { user_role: "city_admin", aal: "aal2" });
console.log("invite-staff rules: 14 checks passed");
