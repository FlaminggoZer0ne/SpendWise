const TONES = {
  primary: "bg-gradient-to-r from-emerald-400 to-emerald-600",
  safe: "bg-gradient-to-r from-emerald-400 to-emerald-600",
  warning: "bg-gradient-to-r from-amber-400 to-amber-500",
  danger: "bg-gradient-to-r from-rose-500 to-red-600",
};

/** `percent` hanya untuk tampilan panjang bar (dibatasi 0–100); nilai dan status berasal dari API. */
export default function ProgressBar({ percent = 0, tone = "primary", label }) {
  const width = Math.max(0, Math.min(100, percent));
  return (
    <div
      role="progressbar"
      aria-valuenow={Math.round(percent)}
      aria-valuemin={0}
      aria-valuemax={100}
      aria-label={label}
      className="h-2.5 w-full overflow-hidden rounded-full bg-slate-200/70 shadow-inner"
    >
      <div className={`h-full rounded-full transition-all ${TONES[tone]}`} style={{ width: `${width}%` }} />
    </div>
  );
}
