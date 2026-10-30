import { AlertTriangle, Loader2 } from "lucide-react";
import Button from "./Button";

export function Spinner({ label = "Memuat..." }) {
  return (
    <div className="flex items-center justify-center gap-2 py-12 text-sm text-slate-500" role="status">
      <Loader2 className="animate-spin" size={18} />
      {label}
    </div>
  );
}

export function ErrorState({ error, onRetry }) {
  return (
    <div className="flex flex-col items-center rounded-xl border border-red-200 bg-red-50 px-6 py-10 text-center" role="alert">
      <AlertTriangle className="text-red-500" size={24} />
      <p className="mt-2 text-sm text-red-700">{error?.message ?? "Terjadi kesalahan"}</p>
      {onRetry && (
        <Button variant="secondary" size="sm" className="mt-4" onClick={onRetry}>
          Coba lagi
        </Button>
      )}
    </div>
  );
}

/** Pesan galat umum di atas form (untuk galat tanpa field, mis. "Saldo dompet tidak mencukupi"). */
export function FormError({ error }) {
  if (!error?.message) return null;
  return (
    <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700" role="alert">
      {error.message}
    </p>
  );
}
