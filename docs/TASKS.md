# Rencana Kerja & Checklist SpendWise

Centang `[x]` setiap item yang selesai dan commit perubahan file ini bersama kodenya. File ini jadi bahan laporan **Progres Tubes UAS** (apa yang sudah ada hasilnya, apa yang masih on progress).

**Legenda:** A1 = Anggota 1 (Akun & Dompet) · A2 = Anggota 2 (Transaksi & Kategori) · A3 = Anggota 3 (Anggaran & Tabungan) · A4 = Anggota 4 (Laporan & Dashboard)

| Minggu | Tanggal (isi sendiri) | Target utama |
|---|---|---|
| M1 | ____ s.d. ____ | Fondasi proyek & kontrak bersama |
| M2 | ____ s.d. ____ | Model + DAO tiap modul |
| M3 | ____ s.d. ____ | Service + Controller (API) inti |
| M4 | ____ s.d. ____ | Halaman frontend inti terhubung API |
| M5 | ____ s.d. ____ | Fitur lengkap + integrasi antarmodul |
| M6 | ____ s.d. ____ | **Laporan progres UAS** + pengujian |
| M7 | ____ s.d. ____ | Perbaikan SonarQube + polishing |
| M8 | ____ s.d. ____ | Laporan akhir & pengumpulan |

---

## Definition of Done (berlaku untuk setiap fitur)

- [ ] Semua acceptance criteria di `PRD.md` terpenuhi lewat UI **dan** API
- [ ] Nama kelas/method sesuai `CLASS_DESIGN.md`; respons sesuai `API.md`
- [ ] Ada unit test untuk logika model/service terkait (lihat `TESTING.md`)
- [ ] Tidak ada issue SonarQube baru kategori Bug/Vulnerability; tidak ada `System.out.println`, kode mati, atau TODO tertinggal
- [ ] Sudah di-review minimal 1 anggota lain lewat Pull Request dan di-merge ke `develop`
- [ ] Screenshot halaman disimpan ke `docs/screenshots/` (untuk laporan)

---

## M1 — Fondasi (dikerjakan BERSAMA)

- [ ] Buat repo GitHub, branch `main` + `develop`, proteksi `main` (wajib PR) — *ketua*
- [x] Inisialisasi Spring Boot 3 (Spring Web, JDBC, MySQL Driver, Validation) di `backend/` — *A1*
- [x] Inisialisasi React + Vite + Tailwind + Router + Axios di `frontend/`, proxy `/api` — *A4*
- [ ] Import `database/schema.sql` + `seed.sql` di laptop masing-masing — *semua*
- [x] `core/`: `Repository<T>`, `Validatable`, `BaseController`, `ApiResponse`, `BusinessException`, `UnauthorizedException`, `ForbiddenException`, `GlobalExceptionHandler`, `PasswordHasher` — *A1 + A2*
- [x] Semua enum: `WalletType`, `TransactionType`, `GoalStatus`, `PeriodType` — *masing-masing PJ*
- [x] Frontend bersama: `client.js`, `AppLayout`, `Navbar`, `Sidebar`, `ProtectedRoute`, komponen `ui/*`, `utils/format.js` — *A4 + A3*
- [ ] Sepakati & bekukan `API.md` versi 1 (rapat tim) — *semua*
- [ ] Setup SonarQube lokal/SonarCloud dan scan pertama (proyek kosong) — *A2*

---

## Anggota 1 — Akun & Dompet (F01–F04)

**M2**
- [x] `User` (abstract), `Member`, `Admin` + getter, `checkPassword`, `changePassword`, `getRole`, `getMenuAccess`
- [x] `Wallet` (`deposit`, `withdraw`, `transferTo`, `validate`, tanpa `setBalance`)
- [x] `UserDAO` (CRUD, `findByUsername`, `existsByUsername`, `mapRow` → Member/Admin)
- [x] `WalletDAO` (CRUD, `findByOwner`, `findByIdAndOwner`)
- [x] Unit test `Wallet` dan `PasswordHasher`

**M3**
- [x] `AuthService` (register + dompet "Tunai" otomatis, login, `changePassword`, `updateProfile`)
- [x] `WalletService` (CRUD, `deleteWallet` cek transaksi & dompet terakhir, `transfer` @Transactional)
- [x] `AdminService` (`listUsers`, `setUserActive`, kategori default)
- [x] `AuthController`, `WalletController`, `AdminController` — uji semua endpoint dengan Postman/Thunder Client — diuji otomatis: `ApiIntegrationTest`

**M4**
- [x] `AuthContext`, `LoginPage`, `RegisterPage`, logout di Navbar
- [x] `WalletPage`, `WalletFormModal`, `TransferModal`

**M5**
- [x] `ProfilePage` (ubah profil, ganti password)
- [x] `AdminUsersPage`, `AdminCategoriesPage`
- [x] Uji akses: member membuka `/admin/*` → ditolak (UI & API 403)

---

## Anggota 2 — Transaksi & Kategori (F05–F07)

**M2**
- [x] `Transaction` (abstract), `Income`, `Expense` (`applyToWallet`, `revertFromWallet`, `validate`)
- [x] `Category`
- [x] `TransactionDAO` (CRUD, `findByFilter` dinamis + aman, `countByFilter`, `sumByFilter`, `findByPeriod`, `countByWallet`, `sumExpense`)
- [x] `CategoryDAO` (CRUD, `findAvailable`, `isUsed`)
- [x] Unit test polymorphism `Income`/`Expense` dan `validate()`

**M3**
- [x] `TransactionService` (`addTransaction`, `editTransaction`, `deleteTransaction`, `search`, `buildTransaction`) @Transactional
- [x] `CategoryService`
- [x] `TransactionController`, `CategoryController` — diuji otomatis: `ApiIntegrationTest`
- [x] Panggil `BudgetService.checkBudget()` / `recalculate()` (pakai stub dulu jika A3 belum selesai)

**M4**
- [x] `TransactionPage` + `TransactionFilterBar` + `Pagination`
- [x] `TransactionFormModal` (tambah & ubah)

**M5**
- [x] `CategoryPage`
- [x] Uji skenario saldo: tambah/ubah (ganti dompet, ganti jenis)/hapus → saldo selalu benar — `ServiceIntegrationTest` (tambah/ubah ganti dompet & jenis/hapus + rollback)
- [ ] *(Opsional O1)* transaksi berulang

---

## Anggota 3 — Anggaran & Tabungan (F08–F10)

**M2**
- [x] `FinancialPlan` (abstract), `Budget`, `SavingGoal`
- [x] `Notification`, interface `BudgetAlertListener`
- [x] `BudgetDAO`, `NotificationDAO` (`findUnread`, `markAllRead`), `SavingGoalDAO`
- [x] Unit test `getProgress`, `isCompleted`, `getRecommendedMonthlySaving`, `getDaysRemaining`

**M3**
- [x] `BudgetService` (CRUD, `checkBudget` dengan deteksi ambang terlewati, `recalculate`, `addListener`, `notifyListeners`)
- [x] `NotificationService` (implements `BudgetAlertListener`) + `ListenerConfig`
- [x] `SavingGoalService` (`contribute` & `cancelGoal` @Transactional, status ACHIEVED otomatis)
- [x] `BudgetController`, `NotificationController`, `SavingGoalController` — diuji otomatis: `ApiIntegrationTest`

**M4**
- [x] `BudgetPage`, `BudgetFormModal`, `ProgressBar` berwarna
- [x] `NotificationBell` di Navbar

**M5**
- [x] `SavingGoalPage`, `GoalFormModal`, `ContributeModal`
- [x] Uji integrasi: transaksi A2 → anggaran & notifikasi berubah sesuai — `ServiceIntegrationTest.budgetNotificationsFireOnceWhenThresholdsAreCrossed`, `ApiIntegrationTest`

---

## Anggota 4 — Laporan & Dashboard (F11–F14)

**M2**
- [x] Interface `Exportable`, `Report` (abstract), `CategoryReport`, `PeriodReport`, `DashboardSummary`
- [x] Unit test `generate()` dengan data transaksi buatan (tanpa DB), termasuk label minggu ISO dan perbandingan periode

**M3**
- [x] `ReportService` (`createCategoryReport`, `createPeriodReport`, `getDashboardSummary`)
- [x] `ReportController` (+ `exportCsv` dengan header `Content-Disposition`, UTF-8 BOM) — diuji otomatis: `ApiIntegrationTest.exportCsvHasBomSemicolonAndFilename`

**M4**
- [x] `DashboardPage` (kartu ringkasan, bar chart 6 bulan, transaksi terbaru, target aktif)

**M5**
- [x] `ReportPage`: tab Per Kategori (pie chart) & Per Periode (bar chart), filter tanggal, tombol Unduh CSV
- [ ] Cek CSV terbuka rapi di Excel
- [ ] *(Opsional O2)* ekspor PDF

---

## M6–M8 — Bersama

**M6**
- [ ] Laporan **Progres Tubes UAS** per anggota (template di `REPORT_NOTES.md`)
- [x] Uji end-to-end semua skenario di `TESTING.md` §3, catat bug di GitHub Issues — 30/30 lulus (Chromium); bug yang ditemukan sudah diperbaiki
- [ ] Scan SonarQube penuh, bagi issue ke PJ modul

**M7**
- [ ] Perbaiki semua Bug/Vulnerability dan mayoritas Code Smell kritis
- [x] Tampilan responsif dicek di layar ponsel — 390 px: tanpa scroll horizontal, sidebar jadi drawer
- [ ] Perbarui class diagram final (termasuk method tambahan `CLASS_DESIGN.md` §7)

**M8**
- [ ] Export `database/spendwise_export.sql` dan uji import di database kosong
- [ ] Screenshot final setiap halaman + hasil SonarQube
- [ ] Laporan akhir PDF, zip source (tanpa `target/`, `node_modules/`, `dist/`)
- [ ] Upload ke Drive (akses publik via link), isi online text (konfigurasi + akun per role)
