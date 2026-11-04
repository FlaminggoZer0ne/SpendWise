export default function Card({ className = "", children, ...props }) {
  return (
    <div className={`glass min-w-0 rounded-2xl p-4 sm:p-5 ${className}`} {...props}>
      {children}
    </div>
  );
}

/** Kartu utama bergradien emerald dengan kilau kaca (saldo total). `children` tampil di bawah angka. */
export function HeroCard({ label, value, children, className = "" }) {
  return (
    <div
      className={`btn-glow relative overflow-hidden rounded-3xl border border-white/40 bg-gradient-to-br from-emerald-500 to-emerald-700 p-6 text-white ${className}`}
    >
      <div aria-hidden="true" className="hero-gloss pointer-events-none absolute -right-10 -top-16 h-52 w-52 rounded-full" />
      <p className="relative text-sm text-white/85">{label}</p>
      <p className="relative mt-1 text-3xl font-bold tabular-nums">{value}</p>
      {children && <div className="relative mt-3">{children}</div>}
    </div>
  );
}

export function StatCard({ label, value, tone = "text-slate-900", icon: Icon, hint }) {
  return (
    <Card className="p-4 sm:p-5">
      <div className="flex items-start justify-between gap-2">
        <div className="min-w-0">
          <p className="text-xs text-slate-500 sm:text-sm">{label}</p>
          <p className={`mt-1 text-lg font-semibold tabular-nums sm:text-2xl ${tone}`}>{value}</p>
          {hint && <p className="mt-1 text-xs text-slate-400">{hint}</p>}
        </div>
        {Icon && (
          <span className="hidden rounded-xl bg-emerald-50/90 p-2 text-emerald-600 ring-1 ring-white/80 sm:block">
            <Icon size={20} />
          </span>
        )}
      </div>
    </Card>
  );
}
