export function Table({ children }) {
  return (
    <div className="glass overflow-x-auto rounded-2xl">
      <table className="min-w-full divide-y divide-slate-200/70 text-sm">{children}</table>
    </div>
  );
}

export function Th({ children, right = false, className = "" }) {
  return (
    <th
      scope="col"
      className={`bg-white/40 px-4 py-3 text-xs font-semibold uppercase tracking-wide text-slate-500 ${right ? "text-right" : "text-left"} ${className}`}
    >
      {children}
    </th>
  );
}

export function Td({ children, right = false, className = "" }) {
  return <td className={`px-4 py-3 text-slate-700 ${right ? "text-right tabular-nums" : ""} ${className}`}>{children}</td>;
}
