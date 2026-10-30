export const formatRupiah = (n) =>
  new Intl.NumberFormat("id-ID", { style: "currency", currency: "IDR", maximumFractionDigits: 0 }).format(n ?? 0);

export const formatTanggal = (iso) =>
  iso
    ? new Date(iso + "T00:00:00").toLocaleDateString("id-ID", { day: "numeric", month: "short", year: "numeric" })
    : "-";

export const formatBulan = (ym) =>
  new Date(ym + "-01T00:00:00").toLocaleDateString("id-ID", { month: "long", year: "numeric" });

export const formatWaktu = (iso) =>
  iso
    ? new Date(iso).toLocaleString("id-ID", { day: "numeric", month: "short", hour: "2-digit", minute: "2-digit" })
    : "-";

export const todayISO = () => new Date().toLocaleDateString("sv-SE", { timeZone: "Asia/Jakarta" });

export const currentMonth = () => todayISO().slice(0, 7);

/** Format persen untuk tampilan; nilai null (mis. saving rate tanpa pemasukan) tampil "—". */
export const formatPersen = (n) =>
  n == null ? "—" : `${Number(n).toLocaleString("id-ID", { maximumFractionDigits: 2 })}%`;

export const awalBulan = (ym) => `${ym}-01`;

export const akhirBulan = (ym) => {
  const [y, m] = ym.split("-").map(Number);
  const last = new Date(y, m, 0).getDate();
  return `${ym}-${String(last).padStart(2, "0")}`;
};

/** Menggeser "YYYY-MM" sejumlah bulan; hanya untuk navigasi tampilan, bukan aturan bisnis. */
export const geserBulan = (ym, delta) => {
  const [y, m] = ym.split("-").map(Number);
  const d = new Date(y, m - 1 + delta, 1);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`;
};
