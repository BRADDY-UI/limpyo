// Phase 0 placeholder. Login + MFA (W-LOGIN, W-MFA) arrive in Phase 1.
export default function App() {
  return (
    <main className="min-h-dvh grid place-items-center px-xl">
      <div className="max-w-md text-center">
        <h1 className="font-display text-display">limpyo</h1>
        <p className="mt-s text-body text-muted">Admin · staff only</p>
        <div className="mt-x3 rounded-large border border-border bg-surface p-xl text-left">
          <p className="text-overline uppercase text-muted">Status</p>
          <p className="mt-s text-body-strong">Sign-in arrives in Phase 1.</p>
        </div>
        <p className="mt-x3 text-caption text-muted">Independent project, not an official LGU system.</p>
      </div>
    </main>
  )
}
