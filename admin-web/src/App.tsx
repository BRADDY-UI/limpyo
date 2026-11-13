import { BrowserRouter, Navigate, NavLink, Outlet, Route, Routes } from "react-router";
import { Button, Card, Centered, Wordmark } from "./components/ui";
import { AuthProvider } from "./lib/auth";
import { useAuth, useIdleSignOut } from "./lib/auth-context";
import { configured, ROLE_LABEL, STAFF, supabase } from "./lib/supabase";
import Login from "./pages/Login";
import Mfa from "./pages/Mfa";
import People from "./pages/People";

export default function App() {
  if (!configured) {
    return (
      <Centered>
        <Wordmark />
        <Card>
          <p className="text-body">Copy <code>admin-web/.env.example</code> to <code>.env.local</code> and fill in the Supabase URL and publishable key.</p>
        </Card>
      </Centered>
    );
  }
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Gate />} />
          <Route element={<Gate />}>
            <Route element={<Shell />}>
              <Route index element={<Overview />} />
              <Route path="people" element={<People />} />
            </Route>
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

/**
 * Every page passes three checks before rendering: signed in, MFA done (aal2), staff role.
 * The database enforces the same checks on every query, so skipping this in the browser gains nothing.
 */
function Gate() {
  const { loading, session, aal, role } = useAuth();
  if (loading) return null;
  if (!session) return <Login />;
  if (aal !== "aal2") return <Mfa />;
  if (!STAFF.includes(role)) {
    return (
      <Centered>
        <Wordmark />
        <Card>
          <p className="text-body">This account isn't a staff account. Staff accounts are created by invitation.</p>
          <Button variant="secondary" className="mt-l" onClick={() => supabase.auth.signOut()}>Sign out</Button>
        </Card>
      </Centered>
    );
  }
  return <Outlet />;
}

function Shell() {
  useIdleSignOut(30);
  const { session, role } = useAuth();
  const link = ({ isActive }: { isActive: boolean }) =>
    `block rounded-medium px-l py-m text-label ${isActive ? "bg-raised text-accent-strong" : "text-ink hover:bg-raised"}`;
  return (
    <div className="flex min-h-dvh">
      <aside className="w-[248px] shrink-0 border-r border-divider bg-ground p-l">
        <p className="mb-xxl px-l font-display text-title">limpyo</p>
        <nav className="space-y-xs">
          <NavLink to="/" end className={link}>Overview</NavLink>
          <NavLink to="/people" className={link}>People</NavLink>
        </nav>
        <div className="mt-x4 px-l text-caption text-muted">
          <p className="truncate">{session?.user.email}</p>
          <p>{ROLE_LABEL[role]}</p>
          <button className="mt-s text-accent-strong hover:underline" onClick={() => supabase.auth.signOut()}>Sign out</button>
        </div>
      </aside>
      <main className="min-w-0 flex-1 p-x3">
        <Outlet />
      </main>
    </div>
  );
}

/** W-HOME placeholder until schedules and reports exist (Phases 2 and 5). */
function Overview() {
  return (
    <div className="space-y-l">
      <h1 className="font-display text-headline">Overview</h1>
      <Card>
        <p className="text-body text-muted">Today's collections, open reports and the fleet map appear here from Phase 2.</p>
      </Card>
    </div>
  );
}
