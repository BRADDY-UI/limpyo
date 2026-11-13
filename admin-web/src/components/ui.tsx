import type { ButtonHTMLAttributes, InputHTMLAttributes, ReactNode, SelectHTMLAttributes } from "react";

// Small token-based building blocks (colors/spacing from design/tokens.json via tokens.css).

export function Button({ variant = "primary", className = "", ...rest }: ButtonHTMLAttributes<HTMLButtonElement> & { variant?: "primary" | "secondary" | "text" }) {
  const look = {
    primary: "bg-accent text-on-accent disabled:bg-raised disabled:text-muted",
    secondary: "border border-border text-ink hover:bg-raised",
    text: "text-accent-strong hover:underline",
  }[variant];
  return (
    <button
      className={`h-[52px] rounded-medium px-xxl text-body-strong transition-transform duration-[var(--duration-fast)] active:scale-[0.98] disabled:cursor-not-allowed ${look} ${className}`}
      {...rest}
    />
  );
}

export function Field({ label, error, help, ...rest }: InputHTMLAttributes<HTMLInputElement> & { label: string; error?: string; help?: string }) {
  return (
    <label className="block">
      <span className="text-label text-ink">{label}</span>
      <input
        className={`mt-s block h-14 w-full rounded-medium bg-raised px-l text-body text-ink outline-none ring-1 focus:ring-2 ${error ? "ring-danger" : "ring-border focus:ring-accent-strong"}`}
        {...rest}
      />
      {(error || help) && <span className={`mt-xs block text-caption ${error ? "text-danger" : "text-muted"}`}>{error ?? help}</span>}
    </label>
  );
}

export function Select({ label, children, ...rest }: SelectHTMLAttributes<HTMLSelectElement> & { label: string; children: ReactNode }) {
  return (
    <label className="block">
      <span className="text-label text-ink">{label}</span>
      <select className="mt-s block h-14 w-full rounded-medium bg-raised px-l text-body text-ink outline-none ring-1 ring-border focus:ring-2 focus:ring-accent-strong" {...rest}>
        {children}
      </select>
    </label>
  );
}

export function Card({ children, className = "" }: { children: ReactNode; className?: string }) {
  return <div className={`rounded-large border border-border bg-surface p-xl ${className}`}>{children}</div>;
}

export function Centered({ children }: { children: ReactNode }) {
  return (
    <main className="grid min-h-dvh place-items-center px-xl">
      <div className="w-full max-w-md">{children}</div>
    </main>
  );
}

export function Wordmark() {
  return (
    <div className="mb-x3">
      <h1 className="font-display text-headline">limpyo</h1>
      <p className="text-caption text-muted">Admin · staff only · not an official LGU system</p>
    </div>
  );
}
