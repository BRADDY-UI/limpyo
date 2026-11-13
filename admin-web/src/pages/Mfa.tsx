import { useEffect, useState, type FormEvent } from "react";
import { Button, Card, Centered, Field, Wordmark } from "../components/ui";
import { supabase } from "../lib/supabase";

/**
 * W-MFA: every staff account needs an authenticator app (TOTP).
 * First login: enroll (scan QR) and confirm. Later logins: enter the 6-digit code.
 * Staff database rules check aal2, so nothing works without this step.
 */
export default function Mfa() {
  const [factorId, setFactorId] = useState<string | null>(null);
  const [qr, setQr] = useState<string | null>(null);
  const [secret, setSecret] = useState<string | null>(null);
  const [code, setCode] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    (async () => {
      const { data } = await supabase.auth.mfa.listFactors();
      const verified = data?.totp?.find((f) => f.status === "verified");
      if (verified) return setFactorId(verified.id);
      // Remove half-finished enrollments so the QR is always fresh.
      for (const f of data?.all ?? []) if (f.status !== "verified") await supabase.auth.mfa.unenroll({ factorId: f.id });
      const { data: en, error } = await supabase.auth.mfa.enroll({ factorType: "totp", friendlyName: "Limpyo admin" });
      if (error || !en) return setError("Couldn't start setup. Reload the page.");
      setFactorId(en.id);
      setQr(en.totp.qr_code);
      setSecret(en.totp.secret);
    })();
  }, []);

  async function verify(e: FormEvent) {
    e.preventDefault();
    if (!factorId) return;
    setBusy(true);
    setError(null);
    const { error } = await supabase.auth.mfa.challengeAndVerify({ factorId, code: code.trim() });
    setBusy(false);
    if (error) setError("That code didn't work. Check the time on your phone and try the newest code.");
    // On success the session becomes aal2 and the app moves on by itself.
  }

  return (
    <Centered>
      <Wordmark />
      <Card>
        <form onSubmit={verify} className="space-y-l">
          <h2 className="font-display text-title">{qr ? "Set up two-step sign-in" : "Enter your code"}</h2>
          {qr ? (
            <>
              <p className="text-body text-muted">Scan this with an authenticator app (Google Authenticator, Microsoft Authenticator, Aegis), then enter the 6-digit code.</p>
              <div className="flex justify-center rounded-medium bg-[#ffffff] p-l">
                <img src={qr} alt="QR code for your authenticator app" width={200} height={200} />
              </div>
              {secret && <p className="break-all text-caption text-muted">Can't scan? Enter this key: <span className="font-mono text-ink">{secret}</span></p>}
            </>
          ) : (
            <p className="text-body text-muted">Open your authenticator app and enter the 6-digit code for Limpyo.</p>
          )}
          <Field label="6-digit code" inputMode="numeric" autoComplete="one-time-code" pattern="[0-9]{6}" maxLength={6} value={code} onChange={(e) => setCode(e.target.value.replace(/\D/g, ""))} required />
          {error && <p className="text-body text-danger">{error}</p>}
          <Button type="submit" disabled={busy || code.length !== 6 || !factorId} className="w-full">Verify</Button>
          <Button type="button" variant="text" onClick={() => supabase.auth.signOut()}>Sign out</Button>
        </form>
      </Card>
    </Centered>
  );
}
