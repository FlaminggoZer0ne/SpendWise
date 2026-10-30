# Changelog

Semua perubahan penting pada proyek dicatat di sini. Format mengikuti [Keep a Changelog](https://keepachangelog.com/id-ID/1.1.0/).

Tambahkan entri di bagian **Unreleased** setiap kali PR di-merge ke `develop`. Saat rilis (progres UAS, pengumpulan akhir), pindahkan isi Unreleased menjadi versi baru.

Kategori: **Ditambahkan**, **Diubah**, **Diperbaiki**, **Dihapus**.

## [Unreleased]

### Ditambahkan
- Backend Spring Boot 3 / Java 17 (`backend/`): `pom.xml` (Web, JDBC, Validation, MySQL, JaCoCo) dan Maven Wrapper.
- `core/`: `Repository<T>`, `Validatable`, `BaseController` (+ `ok(msg, body)`, `created(...)`), `ApiResponse`, `BusinessException`, `UnauthorizedException`, `ForbiddenException`, `GlobalExceptionHandler`, `PasswordHasher`, `JdbcSupport`.
- Seluruh model (Modul 1–4), enum, 7 DAO (`JdbcTemplate`, placeholder `?`), 8 service, 9 controller, DTO, dan `ListenerConfig` (Observer).
- 138 tes JUnit 5: unit model/service (78, selalu jalan) dan integrasi DAO/service/API (60, aktif dengan `-Dspendwise.it=true`, lihat `docs/TESTING.md`).

- Frontend React 18 + Vite + Tailwind (`frontend/`): seluruh halaman (login, registrasi, dashboard, transaksi, dompet, kategori, anggaran, tabungan, laporan + unduh CSV, profil, admin), lonceng notifikasi, layout responsif; `npm run build` dan `npm run lint` bersih.

- Koleksi Postman `docs/api-tests/spendwise.postman_collection.json` (56 request) dan 26 screenshot di `docs/screenshots/`.

### Diperbaiki
- `ReportPage`: pindah tab laporan sempat merender data tab sebelumnya (peringatan React key); kini menampilkan spinner sampai data baru tiba.

### Diubah
- Properti sensitif pada model yang dipakai sebagai request body (`balance`, `spentAmount`, `currentAmount`, `status`, `isDefault`, `ownerId`, `userId`, id) ditandai `READ_ONLY` agar klien tidak bisa mengisinya lewat JSON (BR-01).

## [0.1.0] — 2026-10-30

### Ditambahkan
- Dokumentasi awal proyek: README, CONTRIBUTING, CLAUDE.md, PRD, ARCHITECTURE, CLASS_DESIGN, API, DATABASE, FRONTEND, TASKS, TESTING, REPORT_NOTES.
- `database/schema.sql` (7 tabel) dan `database/seed.sql` (akun demo, kategori default, data contoh Sep–Okt 2026).

<!--
Contoh entri berikutnya:

## [0.5.0] — 2026-12-05 (Progres UAS)
### Ditambahkan
- F01–F03: registrasi, login, dompet, transfer (A1)
- F05, F07: CRUD transaksi + filter (A2)
### Diperbaiki
- Saldo tidak kembali saat transaksi pengeluaran dihapus (#14)
-->
