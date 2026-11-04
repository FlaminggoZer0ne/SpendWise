# Panduan Frontend SpendWise

React 18 + Vite · Tailwind CSS · React Router v6 · Axios · Chart.js (`react-chartjs-2`) · ikon `lucide-react`

Frontend **tidak** menyimpan aturan bisnis. Semua perhitungan (saldo, progres, rekap) datang dari API. Validasi di form hanya untuk kenyamanan; backend tetap memvalidasi ulang.

---

## 1. Setup

```bash
npm create vite@latest frontend -- --template react
cd frontend
npm install react-router-dom axios chart.js react-chartjs-2 lucide-react
npm install -D tailwindcss@3 postcss autoprefixer
npx tailwindcss init -p
```

`vite.config.js` — proxy ke backend (tidak perlu CORS saat development):

```js
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: { "/api": { target: "http://localhost:8080", changeOrigin: true } },
  },
});
```

---

## 2. Struktur Folder

```
frontend/src/
├── main.jsx                 ← <BrowserRouter><AuthProvider><App/></AuthProvider></BrowserRouter>
├── App.jsx                  ← definisi route
├── index.css                ← @tailwind base/components/utilities
├── api/
│   ├── client.js            ← instance Axios + interceptor 401        [Bersama]
│   ├── authApi.js, walletApi.js, adminApi.js                          [A1]
│   ├── transactionApi.js, categoryApi.js                              [A2]
│   ├── budgetApi.js, notificationApi.js, goalApi.js                   [A3]
│   └── reportApi.js                                                   [A4]
├── context/AuthContext.jsx  ← user aktif, login(), logout(), loading  [A1]
├── components/              ← komponen bersama                        [Bersama]
│   ├── layout/AppLayout.jsx, Navbar.jsx, Sidebar.jsx, BottomNav.jsx (ponsel), AmbientBackground.jsx, menu.js
│   ├── ProtectedRoute.jsx
│   ├── NotificationBell.jsx                                           [A3]
│   └── ui/Button.jsx, Input.jsx, Select.jsx, Modal.jsx, Card.jsx, Table.jsx,
│          ProgressBar.jsx, EmptyState.jsx, ConfirmDialog.jsx, Pagination.jsx, Toast.jsx
├── pages/
│   ├── auth/LoginPage.jsx, RegisterPage.jsx                           [A1]
│   ├── profile/ProfilePage.jsx                                        [A1]
│   ├── wallets/WalletPage.jsx, WalletFormModal.jsx, TransferModal.jsx [A1]
│   ├── admin/AdminUsersPage.jsx, AdminCategoriesPage.jsx              [A1]
│   ├── transactions/TransactionPage.jsx, TransactionFormModal.jsx,
│   │                TransactionFilterBar.jsx                          [A2]
│   ├── categories/CategoryPage.jsx                                    [A2]
│   ├── budgets/BudgetPage.jsx, BudgetFormModal.jsx                    [A3]
│   ├── goals/SavingGoalPage.jsx, GoalFormModal.jsx, ContributeModal.jsx [A3]
│   ├── dashboard/DashboardPage.jsx                                    [A4]
│   ├── reports/ReportPage.jsx, CategoryReportView.jsx, PeriodReportView.jsx [A4]
│   └── NotFoundPage.jsx
└── utils/format.js          ← formatRupiah, formatTanggal, formatBulan [Bersama]
```

---

## 3. Route

| Path | Halaman | Akses | PJ |
|---|---|---|---|
| `/login` | LoginPage | publik (jika sudah login → redirect ke beranda role) | A1 |
| `/register` | RegisterPage | publik | A1 |
| `/` | redirect: MEMBER → `/dashboard`, ADMIN → `/admin/users` | login | — |
| `/dashboard` | DashboardPage | MEMBER | A4 |
| `/transactions` | TransactionPage | MEMBER | A2 |
| `/wallets` | WalletPage | MEMBER | A1 |
| `/categories` | CategoryPage | MEMBER | A2 |
| `/budgets` | BudgetPage | MEMBER | A3 |
| `/goals` | SavingGoalPage | MEMBER | A3 |
| `/reports` | ReportPage | MEMBER | A4 |
| `/profile` | ProfilePage | login | A1 |
| `/admin/users` | AdminUsersPage | ADMIN | A1 |
| `/admin/categories` | AdminCategoriesPage | ADMIN | A1 |
| `*` | NotFoundPage | — | — |

```jsx
<Route element={<ProtectedRoute role="MEMBER" />}>
  <Route element={<AppLayout />}>
    <Route path="/dashboard" element={<DashboardPage />} />
    {/* ... */}
  </Route>
</Route>
```

`ProtectedRoute`: selama `loading` tampilkan spinner; jika belum login → `/login`; jika role tidak cocok → beranda role-nya.

Sidebar dibangun dari `user.menuAccess` (dikirim backend dari `getMenuAccess()`), bukan di-hardcode per role:

| Kode menu | Label | Ikon | Path |
|---|---|---|---|
| DASHBOARD | Dashboard | LayoutDashboard | /dashboard |
| TRANSACTIONS | Transaksi | ArrowLeftRight | /transactions |
| WALLETS | Dompet | Wallet | /wallets |
| CATEGORIES | Kategori | Tags | /categories |
| BUDGETS | Anggaran | PieChart | /budgets |
| GOALS | Tabungan | Target | /goals |
| REPORTS | Laporan | BarChart3 | /reports |
| USERS | Kelola Pengguna | Users | /admin/users |
| DEFAULT_CATEGORIES | Kategori Default | FolderCog | /admin/categories |
| PROFILE | Profil | UserCircle | /profile |

---

## 4. Pola Pemanggilan API

`api/client.js`:

```js
import axios from "axios";

const client = axios.create({ baseURL: "/api", withCredentials: true });

client.interceptors.response.use(
  (res) => res.data,                                   // langsung { success, message, data }
  (err) => {
    const status = err.response?.status;
    if (status === 401 && !window.location.pathname.startsWith("/login")) {
      window.location.href = "/login?expired=1";
    }
    return Promise.reject(err.response?.data ?? { success: false, message: "Tidak dapat terhubung ke server" });
  }
);
export default client;
```

Contoh modul API:

```js
// api/transactionApi.js
import client from "./client";
export const getTransactions = (params) => client.get("/transactions", { params });
export const createTransaction = (body) => client.post("/transactions", body);
export const updateTransaction = (id, body) => client.put(`/transactions/${id}`, body);
export const deleteTransaction = (id) => client.delete(`/transactions/${id}`);
```

Pola di halaman: state `loading`, `error`, `data`; tampilkan pesan `err.message` dari backend apa adanya, dan pesan per field dari `err.errors` di bawah input terkait.

**Unduh CSV:**

```js
const res = await axios.get("/api/reports/export", { params, responseType: "blob", withCredentials: true });
const name = res.headers["content-disposition"]?.match(/filename="(.+)"/)?.[1] ?? "laporan.csv";
const url = URL.createObjectURL(res.data);
Object.assign(document.createElement("a"), { href: url, download: name }).click();
URL.revokeObjectURL(url);
```

---

## 5. Isi Tiap Halaman

| Halaman | Komponen utama | Endpoint |
|---|---|---|
| **Login** | Form username + password, link ke Daftar, pesan "Sesi berakhir" jika `?expired=1` | POST /auth/login |
| **Register** | Form 5 field + validasi ringan | POST /auth/register |
| **Dashboard** | 4 kartu (Total Saldo, Pemasukan, Pengeluaran, Saving rate), info anggaran, bar chart 6 bulan, 3 target aktif, 5 transaksi terbaru | GET /reports/dashboard, /reports/period?type=MONTHLY, /goals, /transactions?size=5 |
| **Transaksi** | Filter bar, ringkasan total, tabel + paginasi, tombol "+ Transaksi", aksi ubah/hapus per baris | /transactions, /categories, /wallets |
| **Form transaksi** | Toggle Pemasukan/Pengeluaran (mengganti daftar kategori dan field sumber/metode), nominal, tanggal (maks hari ini), kategori, dompet + saldonya, catatan | POST/PUT /transactions |
| **Dompet** | Kartu total saldo, kartu per dompet (ikon per jenis), tombol Tambah dan Transfer | /wallets |
| **Kategori** | Dua tab Pengeluaran/Pemasukan; badge "Default"; tambah/hapus kustom | /categories |
| **Anggaran** | Pemilih bulan, ringkasan total batas vs terpakai, kartu per anggaran dengan ProgressBar berwarna | /budgets |
| **Tabungan** | Kartu per target: progres, terkumpul/target, sisa hari, rekomendasi/bulan, badge status; tombol Setor dan Batalkan | /goals |
| **Laporan** | Tab "Per Kategori" (pie + tabel) dan "Per Periode" (bar + tabel + rata-rata harian + perbandingan); filter tanggal; tombol Unduh CSV | /reports/* |
| **Notifikasi** | Lonceng + badge angka di Navbar; dropdown daftar; "Tandai semua dibaca"; polling setiap 60 detik dan setelah menyimpan transaksi | /notifications |
| **Profil** | Data akun, form ubah profil, form ganti password | /auth/me, /auth/profile, /auth/password |
| **Admin Pengguna** | Kotak cari, tabel pengguna, toggle aktif | /admin/users |
| **Admin Kategori** | Tabel kategori default per jenis, tambah/hapus | /categories, /admin/categories |

Setiap halaman daftar wajib punya 3 keadaan: **loading** (skeleton/spinner), **kosong** (`EmptyState` + ajakan aksi, mis. "Belum ada transaksi. Catat transaksi pertamamu"), dan **error** (pesan + tombol coba lagi).

---

## 6. Gaya Visual

| Token | Nilai (Tailwind) | Pemakaian |
|---|---|---|
| Primer | `emerald-600` / hover `emerald-700` | tombol utama, link aktif sidebar |
| Pemasukan | `emerald-600` | nominal `+`, grafik pemasukan |
| Pengeluaran | `rose-600` | nominal `−`, grafik pengeluaran |
| Peringatan | `amber-500` | anggaran 80–99% |
| Bahaya | `red-600` | anggaran ≥ 100%, tombol hapus |
| Latar | `slate-50`; kartu `white` + `border-slate-200` + `rounded-xl` + `shadow-sm` | |
| Teks | `slate-900` judul, `slate-600` isi, `slate-400` keterangan | |
| Font | Inter / sistem (`font-sans`) | |

- Layout: sidebar kiri 240 px (desktop), menjadi drawer di layar < 768 px; navbar atas berisi judul halaman, lonceng notifikasi, menu pengguna.
- Angka uang rata kanan di tabel dan memakai `tabular-nums`.
- Konfirmasi wajib sebelum hapus (`ConfirmDialog`), setelah aksi berhasil tampilkan `Toast`.

### 6.1 Efek kaca (glass) — mengikuti desain Figma "SpendWise Mobile App"

Tampilan memakai efek ala iOS: isi putih semi-transparan + blur latar + tepi terang + bayangan lembut. Semua gaya terpusat di `src/index.css` dan komponen `ui/*`; **halaman tidak boleh menulis ulang `bg-white border shadow`** — pakai `Card`, `Table`, `Modal`, atau kelas di bawah.

| Kelas / komponen | Pemakaian |
|---|---|
| `.glass` | Kartu, tabel, daftar (isi putih 60%, blur 24 px) |
| `.glass-strong` | Navbar, sidebar, modal, dropdown notifikasi, bottom nav (putih 70%, blur 40 px) |
| `.glass-field` | Tombol sekunder, chip tidak aktif |
| `.btn-glow` | Tombol utama / chip aktif / kartu saldo: bayangan hijau + sorot tepi atas |
| `HeroCard` (`ui/Card.jsx`) | Kartu saldo bergradien emerald dengan kilau (Dashboard, Dompet) |
| `AmbientBackground` | Tiga blob warna (hijau/biru/ungu) diburamkan di belakang halaman — tanpa ini blur tidak terlihat |
| `BottomNav` | Pil kaca melayang di bawah layar, hanya < 768 px; tab utama per role di `layout/menu.js` (`BOTTOM_TABS`), menu lain lewat drawer |

- Konten halaman diberi `pb-28` di ponsel agar tidak tertutup bottom nav.
- **Aksesibilitas:** `@media (prefers-reduced-transparency: reduce)` dan browser tanpa `backdrop-filter` otomatis memakai isi putih hampir solid. Teks tetap `slate-600`+ di atas kaca agar kontras terjaga.
- Gradien dan blob memakai nilai Tailwind langsung (bukan token variabel); warna dasar tetap emerald/slate dari tabel di atas.

---

## 7. Utilitas Format

```js
// utils/format.js
export const formatRupiah = (n) =>
  new Intl.NumberFormat("id-ID", { style: "currency", currency: "IDR", maximumFractionDigits: 0 }).format(n ?? 0);
// 1250000 → "Rp 1.250.000"

export const formatTanggal = (iso) =>
  new Date(iso + "T00:00:00").toLocaleDateString("id-ID", { day: "numeric", month: "short", year: "numeric" });
// "2026-10-30" → "30 Okt 2026"

export const formatBulan = (ym) =>
  new Date(ym + "-01T00:00:00").toLocaleDateString("id-ID", { month: "long", year: "numeric" });
// "2026-10" → "Oktober 2026"

export const todayISO = () => new Date().toLocaleDateString("sv-SE", { timeZone: "Asia/Jakarta" }); // "2026-10-30"
```
