# CLAUDE.md — Panduan untuk AI Coding Agent

> File ini dibaca otomatis oleh Claude Code. Untuk agent lain (Cursor, Copilot, Codex, dll.), salin isinya ke `AGENTS.md` atau file aturan yang dipakai agent tersebut.

## Proyek

**SpendWise**: aplikasi web pengelolaan keuangan pribadi, Tugas Besar mata kuliah Pemrograman Berorientasi Objek. Desain OOP di backend Java adalah bagian yang **dinilai**, jadi kepatuhan pada desain kelas lebih penting daripada "cara yang lebih singkat".

- Backend: Java 17, Spring Boot 3, Spring Web, Spring JDBC (`JdbcTemplate`), MySQL — folder `backend/`, package `com.spendwise`
- Frontend: React 18 + Vite, Tailwind CSS, React Router, Axios, Chart.js — folder `frontend/`
- Database: `database/schema.sql`, `database/seed.sql`

## Wajib dibaca sebelum menulis kode

1. `docs/CLASS_DESIGN.md` — **sumber kebenaran** nama kelas, atribut, method, tipe, access modifier
2. `docs/API.md` — kontrak endpoint, format JSON, status code
3. `docs/PRD.md` — acceptance criteria & aturan bisnis (BR-xx)
4. `docs/ARCHITECTURE.md` — lapisan, package, auth, transaksi DB
5. `docs/DATABASE.md` — skema & query referensi

Jika permintaan pengguna bertentangan dengan dokumen di atas, **tanyakan dulu** sebelum menyimpang, lalu perbarui dokumen terkait di commit yang sama.

## Perintah

```bash
# backend
cd backend && ./mvnw spring-boot:run          # jalankan (port 8080)
cd backend && ./mvnw test                     # unit test
cd backend && ./mvnw clean verify             # build + test + laporan JaCoCo

# frontend
cd frontend && npm install && npm run dev     # jalankan (port 5173, proxy /api → 8080)
cd frontend && npm run build && npm run lint

# database (reset ke data awal)
mysql -u root -p < database/schema.sql && mysql -u root -p < database/seed.sql
```

Akun uji: `admin`/`admin12345`, `budi`/`member12345` (berisi data), `sari`/`member12345` (kosong).

## Aturan Keras

- **Jangan** mengganti nama/signature kelas & method yang ada di `CLASS_DESIGN.md`. Method tambahan publik hanya yang tercantum di §7 dokumen itu; method `private` pembantu boleh.
- **Jangan** mengganti `JdbcTemplate` dengan JPA/Hibernate, dan jangan memakai Lombok (getter/setter ditulis eksplisit agar terlihat di penilaian OOP).
- Hierarki wajib dipertahankan: `BaseController` → semua controller; `User` → `Member`/`Admin`; `Transaction` → `Income`/`Expense`; `FinancialPlan` → `Budget`/`SavingGoal`; `Report` → `CategoryReport`/`PeriodReport`. Interface: `Repository<T>`, `Validatable`, `BudgetAlertListener`, `Exportable`.
- Saldo dompet hanya berubah lewat `Wallet.deposit/withdraw/transferTo`. **Tidak ada `setBalance()`.**
- Gunakan polymorphism, bukan `instanceof`/`if (type == ...)`, untuk perilaku yang berbeda antar subclass (mis. `t.applyToWallet()`).
- Controller tidak mengakses DAO; service tidak menyentuh `HttpSession`/`ResponseEntity`; model tidak mengakses DB.
- Semua SQL memakai placeholder `?`. Tidak ada penggabungan string berisi input pengguna.
- Method service yang mengubah > 1 tabel diberi `@Transactional`.
- Constructor injection (`private final`), tanpa field `@Autowired`. Log pakai SLF4J, tanpa `System.out`.
- Respons JSON selalu lewat `ApiResponse` (`{success, message, data, errors}`), pesan untuk pengguna dalam Bahasa Indonesia.
- Frontend: panggilan API hanya lewat `src/api/*`; tidak ada perhitungan bisnis di frontend.
- Setiap logika baru di model/service disertai unit test JUnit 5.

## Kepemilikan Modul

| Modul | PJ | Kelas |
|---|---|---|
| Core | Semua | `BaseController`, `Repository<T>`, `Validatable`, kelas pendukung |
| 1 Akun & Dompet | Anggota 1 | User, Member, Admin, Wallet, WalletType, Auth/Wallet/Admin Controller+Service, UserDAO, WalletDAO |
| 2 Transaksi & Kategori | Anggota 2 | Transaction, Income, Expense, Category, TransactionType, Transaction/Category Controller+Service+DAO |
| 3 Anggaran & Tabungan | Anggota 3 | FinancialPlan, Budget, SavingGoal, GoalStatus, Notification, BudgetAlertListener, Budget/Notification/SavingGoal Controller+Service+DAO |
| 4 Laporan & Dashboard | Anggota 4 | Exportable, Report, CategoryReport, PeriodReport, PeriodType, DashboardSummary, ReportService, ReportController |

Saat diminta mengerjakan satu modul, jangan mengubah file modul lain kecuali benar-benar perlu; sebutkan perubahan lintas modul secara eksplisit di ringkasan.

## Alur Kerja yang Diharapkan

1. Baca bagian dokumen yang relevan.
2. Buat/ubah kode mengikuti lapisan: model → DAO → service → controller → frontend.
3. Tulis/jalankan unit test; untuk API, uji dengan contoh request di `API.md`.
4. Centang item terkait di `docs/TASKS.md` dan tambahkan entri di `CHANGELOG.md` bagian *Unreleased*.
5. Ringkas perubahan: file yang diubah, acceptance criteria yang terpenuhi, hal yang belum.
