const VARIANTS = {
  primary:
    "btn-glow bg-gradient-to-br from-emerald-500 to-emerald-600 text-white hover:from-emerald-600 hover:to-emerald-700 disabled:opacity-50 disabled:shadow-none",
  secondary: "glass-field text-slate-700 hover:bg-white/90 disabled:text-slate-400",
  danger:
    "bg-gradient-to-br from-rose-500 to-red-600 text-white shadow-[0_8px_20px_rgba(220,38,38,0.3),inset_0_1px_0_rgba(255,255,255,0.35)] hover:from-rose-600 hover:to-red-700 disabled:opacity-50 disabled:shadow-none",
  ghost: "text-slate-600 hover:bg-white/60 disabled:text-slate-300",
};

const SIZES = { sm: "px-2.5 py-1.5 text-xs", md: "px-4 py-2.5 text-sm" };

export default function Button({ variant = "primary", size = "md", className = "", type = "button", ...props }) {
  return (
    <button
      type={type}
      className={`inline-flex items-center justify-center gap-2 rounded-xl font-medium transition-all active:scale-[0.98] focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:ring-offset-1 disabled:cursor-not-allowed ${VARIANTS[variant]} ${SIZES[size]} ${className}`}
      {...props}
    />
  );
}
