import { useId } from "react";

const FIELD =
  "block w-full rounded-xl border bg-white/70 px-3 py-2.5 text-sm text-slate-900 placeholder-slate-400 backdrop-blur-md focus:bg-white/90 focus:outline-none focus:ring-2 focus:ring-emerald-500 disabled:bg-slate-100/70 disabled:text-slate-500";

export function Field({ label, error, hint, htmlFor, children }) {
  return (
    <div>
      {label && (
        <label htmlFor={htmlFor} className="mb-1 block text-sm font-medium text-slate-700">
          {label}
        </label>
      )}
      {children}
      {hint && !error && <p className="mt-1 text-xs text-slate-400">{hint}</p>}
      {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
    </div>
  );
}

export default function Input({ label, error, hint, className = "", ...props }) {
  const id = useId();
  return (
    <Field label={label} error={error} hint={hint} htmlFor={id}>
      <input id={id} className={`${FIELD} ${error ? "border-red-400" : "border-slate-300"} ${className}`} {...props} />
    </Field>
  );
}

export { FIELD };
