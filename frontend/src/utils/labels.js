export const WALLET_TYPES = [
  { value: "CASH", label: "Tunai" },
  { value: "BANK", label: "Bank" },
  { value: "E_WALLET", label: "E-Wallet" },
];

export const walletTypeLabel = (v) => WALLET_TYPES.find((t) => t.value === v)?.label ?? v;

export const TRANSACTION_TYPES = [
  { value: "EXPENSE", label: "Pengeluaran" },
  { value: "INCOME", label: "Pemasukan" },
];

export const PERIOD_TYPES = [
  { value: "DAILY", label: "Harian" },
  { value: "WEEKLY", label: "Mingguan" },
  { value: "MONTHLY", label: "Bulanan" },
  { value: "YEARLY", label: "Tahunan" },
];

export const GOAL_STATUS = {
  ACTIVE: { label: "Aktif", style: "bg-emerald-50 text-emerald-700" },
  ACHIEVED: { label: "Tercapai", style: "bg-blue-50 text-blue-700" },
  CANCELLED: { label: "Dibatalkan", style: "bg-slate-100 text-slate-500" },
};

/** Memetakan status anggaran dari API (SAFE | WARNING | EXCEEDED) ke warna ProgressBar. */
export const budgetTone = (status) => ({ SAFE: "safe", WARNING: "warning", EXCEEDED: "danger" })[status] ?? "safe";
