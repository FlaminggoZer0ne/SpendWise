# SpendWise — Aplikasi Web Pengelolaan Keuangan Pribadi

Tugas Besar Pemrograman Berorientasi Objek (PBO) · School of Computing, Telkom University · 2026

SpendWise membantu pengguna mencatat pemasukan dan pengeluaran, mengelola beberapa dompet, mengatur anggaran bulanan per kategori (dengan notifikasi di 80% dan 100%), menetapkan target tabungan, serta melihat dashboard dan laporan per kategori/periode yang bisa diunduh sebagai CSV.

| Bagian | Teknologi |
|---|---|
| Backend | Java 17, Spring Boot 3 (Spring Web, Spring JDBC/`JdbcTemplate`), Maven |
| Frontend | React 18 + Vite, Tailwind CSS, React Router, Axios, Chart.js (`react-chartjs-2`) |
| Database | MySQL 8 / MariaDB 10.6+ |
| Kualitas | SonarQube, JUnit 5 |

---

## Anggota Kelompok

| No | Nama | NIM | Modul | Fitur |
|---|---|---|---|---|
| 1 | [Nama Anggota 1] | [NIM] | Akun & Dompet | F01–F04 |
| 2 | [Nama Anggota 2] | [NIM] | Transaksi & Kategori | F05–F07 |
| 3 | [Nama Anggota 3] | [NIM] | Anggaran & Tabungan | F08–F10 |
| 4 | [Nama Anggota 4] | [NIM] | Laporan & Dashboard | F11–F14 |

---

## Struktur Repository

```
spendwise/
├── README.md               ← dokumen ini
├── CLAUDE.md               ← panduan untuk AI coding agent (opsional)
├── CONTRIBUTING.md         ← alur Git, konvensi kode, review
├── CHANGELOG.md
├── backend/                ← Spring Boot (Maven)
│   ├── pom.xml
│   └── src/main/java/com/spendwise/...
├── frontend/               ← React + Vite
│   ├── package.json
│   └── src/...
├── database/
│   ├── schema.sql          ← struktur tabel
│   └── seed.sql            ← kategori default + akun demo + data contoh
└── docs/
    ├── PRD.md              ← fitur, acceptance criteria, aturan bisnis
    ├── ARCHITECTURE.md     ← lapisan, package, alur request, auth
    ├── CLASS_DESIGN.md     ← spesifikasi kelas (sumber kebenaran OOP)
    ├── API.md              ← kontrak REST API + contoh JSON
    ├── DATABASE.md         ← skema, relasi, constraint
    ├── FRONTEND.md         ← halaman, route, komponen, gaya
    ├── TASKS.md            ← checklist per anggota per minggu
    ├── TESTING.md          ← skenario uji, unit test, SonarQube
    └── REPORT_NOTES.md     ← bahan laporan progres & laporan akhir
```

---

## Prasyarat

- JDK 17 (cek: `java -version`)
- Maven 3.9+ (atau gunakan `./mvnw` yang ikut di proyek)
- Node.js 20 LTS + npm (cek: `node -v`)
- MySQL 8 atau MariaDB 10.6+ (XAMPP/Laragon juga bisa)

---

## Cara Menjalankan (Lokal)

### 1. Siapkan database

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
```

Atau lewat phpMyAdmin: buat database `spendwise`, lalu **Import** `schema.sql` kemudian `seed.sql`.

### 2. Konfigurasi backend

Salin `backend/src/main/resources/application.properties.example` menjadi `application.properties`, lalu sesuaikan:

```properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/spendwise?useSSL=false&serverTimezone=Asia/Jakarta&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Session login berlaku 2 jam
server.servlet.session.timeout=2h
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=lax
```

> `application.properties` berisi password database, jadi **tidak di-commit**. Yang di-commit hanya `application.properties.example`.

### 3. Jalankan backend

```bash
cd backend
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

Backend berjalan di `http://localhost:8080`. Uji cepat: buka `http://localhost:8080/api/auth/me` di browser → harus muncul `{"success":false,"message":"Silakan login terlebih dahulu",...}` dengan status 401. Artinya backend dan penanganan session sudah jalan.

### Menjalankan tes

```bash
cd backend && ./mvnw test            # unit test (tanpa database)
cd backend && ./mvnw clean verify    # + laporan JaCoCo di backend/target/site/jacoco/index.html
```

Tes integrasi (DAO, service, API) butuh database berisi `schema.sql` + `seed.sql` dan hanya aktif dengan `-Dspendwise.it=true` — lihat `docs/TESTING.md` §1.1. Koleksi Postman ada di `docs/api-tests/`.

### 4. Jalankan frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend berjalan di `http://localhost:5173`. Vite meneruskan semua request `/api/**` ke `http://localhost:8080` (lihat `vite.config.js`), jadi tidak perlu mengatur CORS saat development.

---

## Akun Demo

Akun berikut dibuat oleh `database/seed.sql`:

| Role | Username | Password | Keterangan |
|---|---|---|---|
| Admin | `admin` | `admin12345` | Kelola pengguna & kategori default |
| Member | `budi` | `member12345` | Sudah berisi dompet, transaksi Oktober 2026, anggaran, dan target tabungan |
| Member | `sari` | `member12345` | Akun kosong untuk mencoba alur dari awal |

---

## Build untuk Pengumpulan

```bash
# Backend → backend/target/spendwise-backend.jar
cd backend && ./mvnw clean package -DskipTests

# Frontend → frontend/dist/
cd frontend && npm run build
```

Checklist pengumpulan (lihat juga `docs/REPORT_NOTES.md`):

- [ ] Source code **tanpa** folder `backend/target/`, `frontend/node_modules/`, `frontend/dist/`
- [ ] `database/spendwise_export.sql` hasil export terbaru dari DBMS (dicek bisa di-import ke database kosong tanpa error)
- [ ] Laporan akhir PDF: fitur + pembuat, class diagram, screenshot, hasil SonarQube
- [ ] Kolom online text: konfigurasi (bagian **Cara Menjalankan**) + satu akun per role (tabel **Akun Demo**)
- [ ] Link Google Drive/OneDrive dibuka dalam mode "Siapa saja yang memiliki link"

---

## Dokumentasi

Mulai dari [`docs/PRD.md`](docs/PRD.md) untuk memahami fitur, lalu [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) dan [`docs/CLASS_DESIGN.md`](docs/CLASS_DESIGN.md) sebelum menulis kode. Kontrak antara frontend dan backend ada di [`docs/API.md`](docs/API.md).
