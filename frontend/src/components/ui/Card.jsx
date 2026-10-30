export default function Card({ className = "", children, ...props }) {
  return (
    <div className={`rounded-xl border border-slate-200 bg-white p-5 shadow-sm ${className}`} {...props}>
      {children}
    </div>
  );
}

export function StatCard({ label, value, tone = "text-slate-900", icon: Icon, hint }) {
  return (
    <Card>
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm text-slate-500">{label}</p>
          <p className={`mt-1 text-2xl font-semibold tabular-nums ${tone}`}>{value}</p>
          {hint && <p className="mt-1 text-xs text-slate-400">{hint}</p>}
        </div>
        {Icon && (
          <span className="rounded-lg bg-emerald-50 p-2 text-emerald-600">
            <Icon size={20} />
          </span>
        )}
      </div>
    </Card>
  );
}
