# Catatan Laporan (Progres UAS & Laporan Akhir)

Kumpulkan bahan laporan di sini sejak awal supaya tidak kelabakan di minggu terakhir. Screenshot disimpan di `docs/screenshots/` dengan nama `<modul>_<halaman>_<keterangan>.png`, mis. `m2_transaksi_filter-oktober.png`.

---

## 1. Template Laporan Progres Tubes UAS (per anggota)

> Diisi masing-masing anggota, sesuai instruksi: pekerjaan yang menjadi tanggung jawab, yang sudah ada hasilnya, yang masih on progress, beserta screenshot aplikasi/kode.

```markdown
### [Nama] — [NIM] — Modul [n]: [nama modul]

**Tanggung jawab:** F0x–F0y (lihat PRD.md)
**Kelas yang dibuat:** (salin dari CLASS_DESIGN.md §1)

| Item | Status | Bukti |
|---|---|---|
| Kelas model (User, Member, ...) | ✅ Selesai | screenshot kode `Wallet.java` |
| DAO (UserDAO, WalletDAO) | ✅ Selesai | screenshot kode + hasil unit test |
| Service & Controller | 🟡 On progress (transfer belum) | screenshot Postman `POST /wallets` |
| Halaman frontend | 🟡 On progress | screenshot halaman Dompet |
| Unit test | ✅ 12 test lulus | screenshot `./mvnw test` |

**Kendala & rencana minggu depan:**
- ...
```

Screenshot yang disarankan untuk progres:
- Struktur folder/package di IDE yang menunjukkan kelas-kelas modul
- Potongan kode yang menunjukkan konsep OOP (subclass `extends`, `implements`, method `@Override`)
- Hasil `./mvnw test` (hijau)
- Request–response di Postman
- Halaman frontend yang sudah jadi

---

## 2. Kerangka Laporan Akhir

1. **Cover** — judul, anggota (nama, NIM), kelas, dosen
2. **Deskripsi Aplikasi** — ringkas dari `PRD.md` §1
3. **Daftar Fitur & Pembuatnya** — tabel F01–F14 dengan kolom "Dikerjakan oleh" (wajib)
4. **Teknologi & Arsitektur** — dari `ARCHITECTURE.md` (diagram lapisan)
5. **Class Diagram Final** — overview + per modul, sudah memuat method tambahan (`CLASS_DESIGN.md` §7) dan disebutkan kelas pendukung
6. **Penerapan Konsep OOP** — inheritance, abstract class, interface, polymorphism, encapsulation; sertakan potongan kode singkat per konsep
7. **Rancangan Basis Data** — ERD dari `DATABASE.md`
8. **Screenshot Aplikasi** — setiap halaman, kelompokkan per modul/anggota
9. **Hasil Pengujian** — tabel skenario `TESTING.md` §3 yang sudah terisi + ringkasan unit test
10. **Hasil SonarQube** — screenshot overview (Bugs, Vulnerabilities, Code Smells, Coverage, Duplications); sebelum & sesudah perbaikan jika ada
11. **Panduan Instalasi & Akun** — dari `README.md`
12. **Kesimpulan & Pembagian Kerja**

---

## 3. Checklist Screenshot

| Modul | Halaman / bukti | File | ✔ |
|---|---|---|---|
| 1 | Login, Register (termasuk pesan error) | m1_login_pesan-error.png, m1_register_username-duplikat.png | ✔ |
| 1 | Dompet + modal transfer | m1_dompet_daftar.png, m1_dompet_modal-transfer.png, m1_dompet_modal-tambah.png | ✔ |
| 1 | Profil (ubah profil & password) | m1_profil.png | ✔ |
| 1 | Admin: kelola pengguna, kategori default | m1_admin_kelola-pengguna.png, m1_admin_kategori-default.png | ✔ |
| 2 | Riwayat transaksi + filter aktif | m2_transaksi_riwayat.png, m2_transaksi_filter-ngopi-oktober.png | ✔ |
| 2 | Form tambah pemasukan & pengeluaran | m2_transaksi_form-pengeluaran.png, m2_transaksi_form-pemasukan.png | ✔ |
| 2 | Kategori (tab pengeluaran/pemasukan) | m2_kategori_pengeluaran.png, m2_kategori_pemasukan.png | ✔ |
| 3 | Anggaran (aman / peringatan / terlampaui) | m3_anggaran_aman-peringatan-terlampaui.png | ✔ |
| 3 | Notifikasi lonceng terbuka | m3_notifikasi_lonceng-terbuka.png | ✔ |
| 3 | Target tabungan (aktif, tercapai) + modal setor | m3_tabungan_aktif-dan-tercapai.png, m3_tabungan_modal-setor.png | ✔ |
| 4 | Dashboard | m4_dashboard.png | ✔ |
| 4 | Laporan per kategori (pie) & per periode (bar) | m4_laporan_per-kategori-pie.png, m4_laporan_per-periode-mingguan.png | ✔ |
| 4 | File CSV terbuka di Excel | | |
| — | SonarQube overview | | |
| — | Hasil unit test | | |

Data `seed.sql` (akun `budi`) sudah memunculkan semua status anggaran dan target, jadi cocok dipakai untuk screenshot.

Screenshot di `docs/screenshots/` diambil otomatis dari data seed (Chromium, 1366×900 dan ponsel 390×844, file `mobile_*.png`). Belum ada: file CSV di Excel, SonarQube, dan hasil `./mvnw test` (ambil dari terminal masing-masing).

### Ringkasan angka untuk laporan (diukur 30 Okt 2026)

| Ukuran | Nilai |
|---|---|
| Tes otomatis backend | 138 (78 unit + 60 integrasi), semua lulus |
| Cakupan JaCoCo (unit + integrasi) | model 95%, service 92%, controller 96%, dao 88%, core 93% |
| Skenario browser `TESTING.md` §3 | 30/30 TC + 10 pemeriksaan tambahan lulus |
| Koleksi Postman `docs/api-tests/` | 56 request, 56 sesuai pada DB seed |


---

## 4. Isi Kolom Online Text Saat Pengumpulan (draf)

```
Link artefak (Google Drive, akses publik): https://drive.google.com/...

Konfigurasi:
1. Import database/spendwise_export.sql ke MySQL 8 / MariaDB (membuat database "spendwise").
2. Backend (Java 17 + Maven): salin backend/src/main/resources/application.properties.example
   menjadi application.properties, isi username/password MySQL, lalu jalankan:
   cd backend && ./mvnw spring-boot:run   (port 8080)
3. Frontend (Node.js 20): cd frontend && npm install && npm run dev   (buka http://localhost:5173)

Akun per role:
- Admin  : admin / admin12345
- Member : budi / member12345
```
