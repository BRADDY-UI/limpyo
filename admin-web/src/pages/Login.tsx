import { useState, type FormEvent } from "react";
import { Button, Card, Centered, Field, Wordmark } from "../components/ui";
import { supabase } from "../lib/supabase";

/** W-LOGIN: staff only. There's no sign-up here; accounts come from invites. */
export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [resetSent, setResetSent] = useState(false);

  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    const { error } = await supabase.auth.signInWithPassword({ email: email.trim(), password });
    setBusy(false);
    // Generic message on purpose: don't reveal which part was wrong.
    if (error) setError(error.status === 429 ? "Too many tries. Wait a minute." : "Wrong email or password.");
  }

  async function forgot() {
    if (!email.includes("@")) return setError("Enter your email first.");
    await supabase.auth.resetPasswordForEmail(email.trim(), { redirectTo: `${window.location.origin}/login` });
    setResetSent(true);
  }

  return (
    <Centered>
      <Wordmark />
      <Card>
        <form onSubmit={submit} className="space-y-l">
          <h2 className="font-display text-title">Sign in</h2>
          <Field label="Email" type="email" autoComplete="username" value={email} onChange={(e) => setEmail(e.target.value)} required />
          <Field label="Password" type="password" autoComplete="current-password" value={password} onChange={(e) => setPassword(e.target.value)} required />
          {error && <p className="text-body text-danger">{error}</p>}
          {resetSent && <p className="text-body text-accent-strong">If that email has an account, a reset link is on its way.</p>}
          <Button type="submit" disabled={busy} className="w-full">Sign in</Button>
          <Button type="button" variant="text" onClick={forgot}>Forgot password?</Button>
        </form>
      </Card>
    </Centered>
  );
}
