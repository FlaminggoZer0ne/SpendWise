import { ArcElement, BarElement, CategoryScale, Chart, Legend, LinearScale, Tooltip } from "chart.js";

Chart.register(ArcElement, BarElement, CategoryScale, LinearScale, Legend, Tooltip);

export const INCOME_COLOR = "#059669";
export const EXPENSE_COLOR = "#e11d48";

export const PIE_COLORS = [
  "#059669", "#e11d48", "#2563eb", "#d97706", "#7c3aed", "#0891b2",
  "#db2777", "#65a30d", "#ea580c", "#4f46e5", "#0d9488", "#64748b",
];

/** Kompak untuk sumbu grafik: 1.500.000 → "1,5 jt". */
export const compactRupiah = (v) =>
  new Intl.NumberFormat("id-ID", { notation: "compact", maximumFractionDigits: 1 }).format(v);
