import { useEffect, useState, type FormEvent } from "react";
import { Button, Card, Field, Select } from "../components/ui";
import { useAuth } from "../lib/auth-context";
import { ROLE_LABEL, supabase, type Role } from "../lib/supabase";

interface Staff {
  user_id: string;
  email: string;
  display_name: string | null;
  role: Role;
  barangay_id: number | null;
  granted_at: string;
}
interface Barangay {
  id: number;
  name: string;
}

const INVITABLE: Partial<Record<Role, Role[]>> = {
  super_admin: ["barangay_officer", "driver", "city_admin"],
  city_admin: ["barangay_officer", "driver"],
};

const ERRORS: Record<string, string> = {
  already_registered: "That email already has an account. Role changes for existing accounts come later.",
  role_not_allowed: "You can't invite that role.",
  barangay_required: "Pick a barangay for officers and drivers.",
  invalid_email: "Enter a valid email.",
  mfa_required: "Your session needs two-step sign-in. Sign out and back in.",
};

/** W-PEOPLE (Phase 1): staff list + invite. Residents are not listed here. */
export default function People() {
  const { role } = useAuth();
  const [staff, setStaff] = useState<Staff[]>([]);
  const [barangays, setBarangays] = useState<Barangay[]>([]);
  const [loadError, setLoadError] = useState<string | null>(null);

  const [refresh, setRefresh] = useState(0);

  useEffect(() => {
    let alive = true;
    Promise.all([supabase.rpc("list_staff"), supabase.from("barangays").select("id,name").order("id")]).then(([s, b]) => {
      if (!alive) return;
      if (s.error) setLoadError("Couldn't load staff.");
      else setStaff(s.data as Staff[]);
      if (!b.error) setBarangays(b.data as Barangay[]);
    });
    return () => {
      alive = false;
    };
  }, [refresh]);

  const name = (id: number | null) => barangays.find((b) => b.id === id)?.name ?? "—";
  const canInvite = INVITABLE[role] ?? [];

  return (
    <div className="space-y-xxl">
      <h1 className="font-display text-headline">People</h1>
      {canInvite.length > 0 && <InviteForm roles={canInvite} barangays={barangays} onInvited={() => setRefresh((n) => n + 1)} />}
      <Card className="overflow-x-auto p-0">
        <table className="w-full text-left text-body">
          <thead className="text-overline uppercase text-muted">
            <tr className="border-b border-divider">
              <th className="p-l">Name</th>
              <th className="p-l">Email</th>
              <th className="p-l">Role</th>
              <th className="p-l">Barangay</th>
            </tr>
          </thead>
          <tbody>
            {staff.map((p) => (
              <tr key={p.user_id} className="border-b border-divider last:border-0">
                {/* All text is rendered by React (escaped); no HTML from the database. */}
                <td className="p-l">{p.display_name ?? "—"}</td>
                <td className="p-l text-muted">{p.email}</td>
                <td className="p-l">{ROLE_LABEL[p.role]}</td>
                <td className="p-l">{name(p.barangay_id)}</td>
              </tr>
            ))}
            {staff.length === 0 && (
              <tr>
                <td colSpan={4} className="p-l text-muted">{loadError ?? "No staff yet."}</td>
              </tr>
            )}
          </tbody>
        </table>
      </Card>
    </div>
  );
}

function InviteForm({ roles, barangays, onInvited }: { roles: Role[]; barangays: Barangay[]; onInvited: () => void }) {
  const [email, setEmail] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [role, setRole] = useState<Role>(roles[0]);
  const [barangayId, setBarangayId] = useState<number | "">("");
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState<{ ok: boolean; text: string } | null>(null);
  const needsBarangay = role === "barangay_officer" || role === "driver";

  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setMessage(null);
    const { data, error } = await supabase.functions.invoke("invite-staff", {
      body: {
        email: email.trim(),
        role,
        barangay_id: needsBarangay ? barangayId || null : null,
        display_name: displayName.trim() || null,
      },
    });
    setBusy(false);
    if (error) {
      let code = "";
      try {
        code = (await (error as { context?: Response }).context?.json())?.error ?? "";
      } catch {
        /* not JSON */
      }
      return setMessage({ ok: false, text: ERRORS[code] ?? "The invite didn't go through. Try again." });
    }
    if (data?.user_id) {
      setMessage({ ok: true, text: `Invite sent to ${email.trim()}.` });
      setEmail("");
      setDisplayName("");
      onInvited();
    }
  }

  return (
    <Card>
      <form onSubmit={submit} className="grid gap-l md:grid-cols-2">
        <h2 className="font-display text-title md:col-span-2">Invite staff</h2>
        <Field label="Email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        <Field label="Display name (optional)" maxLength={30} value={displayName} onChange={(e) => setDisplayName(e.target.value)} />
        <Select label="Role" value={role} onChange={(e) => setRole(e.target.value as Role)}>
          {roles.map((r) => (
            <option key={r} value={r}>{ROLE_LABEL[r]}</option>
          ))}
        </Select>
        <Select label="Barangay" value={barangayId} disabled={!needsBarangay} onChange={(e) => setBarangayId(e.target.value ? Number(e.target.value) : "")} required={needsBarangay}>
          <option value="">{needsBarangay ? "Choose…" : "Not needed"}</option>
          {barangays.map((b) => (
            <option key={b.id} value={b.id}>{b.name}</option>
          ))}
        </Select>
        {message && <p className={`text-body md:col-span-2 ${message.ok ? "text-accent-strong" : "text-danger"}`}>{message.text}</p>}
        <div className="md:col-span-2">
          <Button type="submit" disabled={busy}>Send invite</Button>
        </div>
      </form>
    </Card>
  );
}
