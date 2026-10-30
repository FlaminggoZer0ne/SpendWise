export function Table({ children }) {
  return (
    <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white shadow-sm">
      <table className="min-w-full divide-y divide-slate-200 text-sm">{children}</table>
    </div>
  );
}

export function Th({ children, right = false, className = "" }) {
  return (
    <th
      scope="col"
      className={`bg-slate-50 px-4 py-3 text-xs font-semibold uppercase tracking-wide text-slate-500 ${right ? "text-right" : "text-left"} ${className}`}
    >
      {children}
    </th>
  );
}

export function Td({ children, right = false, className = "" }) {
  return <td className={`px-4 py-3 text-slate-700 ${right ? "text-right tabular-nums" : ""} ${className}`}>{children}</td>;
}
