# PRD — Product Requirements Document SpendWise

Dokumen ini adalah **acuan fitur**: apa yang harus dibuat, siapa penggunanya, dan kapan sebuah fitur dianggap selesai. Detail teknis ada di `ARCHITECTURE.md`, `CLASS_DESIGN.md`, dan `API.md`.

---

## 1. Ringkasan Produk

**SpendWise** adalah aplikasi web pengelolaan keuangan pribadi. Pengguna mencatat pemasukan dan pengeluaran di beberapa dompet, mengatur anggaran bulanan per kategori, menabung untuk target tertentu, dan melihat ringkasan serta laporan keuangannya.

**Target pengguna:** mahasiswa dan pekerja muda yang ingin tahu ke mana uangnya pergi setiap bulan.

**Masalah yang diselesaikan:**
- Pencatatan manual tidak konsisten dan tidak menghitung saldo otomatis.
- Tidak ada peringatan saat pengeluaran sudah mendekati/melewati batas.
- Target tabungan tidak terpantau sehingga sering gagal.

---

## 2. Role Pengguna

| Role | Deskripsi | Menu yang tampil (`getMenuAccess()`) |
|---|---|---|
| **MEMBER** | Pengguna utama. Hanya bisa melihat dan mengubah datanya sendiri. | Dashboard, Transaksi, Dompet, Kategori, Anggaran, Tabungan, Laporan, Profil |
| **ADMIN** | Pengelola sistem. Tidak punya dompet/transaksi. | Kelola Pengguna, Kategori Default, Profil |

Aturan akses:
- Semua halaman kecuali Login dan Daftar wajib login.
- Member yang mengakses data milik member lain → ditolak (`403` atau `404`).
- Endpoint dan halaman admin hanya untuk role ADMIN → selain itu `403`.

---

## 3. Daftar Fitur

| ID | Fitur | Role | Modul | PJ | Prioritas |
|---|---|---|---|---|---|
| F01 | Registrasi & Login | Semua | 1 | Anggota 1 | Wajib |
| F02 | Profil & Ganti Password | Semua | 1 | Anggota 1 | Wajib |
| F03 | Manajemen Dompet + Transfer | Member | 1 | Anggota 1 | Wajib |
| F04 | Panel Admin | Admin | 1 | Anggota 1 | Wajib |
| F05 | Pencatatan Transaksi (CRUD) | Member | 2 | Anggota 2 | Wajib |
| F06 | Kategori Kustom | Member | 2 | Anggota 2 | Wajib |
| F07 | Riwayat, Cari & Filter Transaksi | Member | 2 | Anggota 2 | Wajib |
| F08 | Anggaran Bulanan | Member | 3 | Anggota 3 | Wajib |
| F09 | Notifikasi Anggaran | Member | 3 | Anggota 3 | Wajib |
| F10 | Target Tabungan | Member | 3 | Anggota 3 | Wajib |
| F11 | Dashboard Ringkasan | Member | 4 | Anggota 4 | Wajib |
| F12 | Laporan per Kategori | Member | 4 | Anggota 4 | Wajib |
| F13 | Laporan per Periode | Member | 4 | Anggota 4 | Wajib |
| F14 | Unduh Laporan CSV | Member | 4 | Anggota 4 | Wajib |
| O1 | Transaksi berulang otomatis (`isRecurring`) | Member | 2 | Anggota 2 | Opsional |
| O2 | Ekspor laporan PDF | Member | 4 | Anggota 4 | Opsional |
| O3 | Mode gelap | Semua | Bersama | — | Opsional |

---

## 4. Detail Fitur & Acceptance Criteria

Format: *user story* → kriteria penerimaan (AC). Fitur dianggap **selesai** jika semua AC terpenuhi lewat UI **dan** API.

### F01 — Registrasi & Login

> Sebagai calon pengguna, saya ingin membuat akun dan login agar data keuangan saya tersimpan aman.

- **AC1** Form daftar berisi: username, nama lengkap, email, password, konfirmasi password.
- **AC2** Username 4–30 karakter (huruf, angka, underscore), unik. Duplikat → pesan "Username sudah dipakai".
- **AC3** Password minimal 8 karakter; konfirmasi harus sama.
- **AC4** Email harus berformat valid.
- **AC5** Password disimpan sebagai hash `salt$sha256hex` (lihat §5.1), **tidak pernah** dikirim balik ke frontend.
- **AC6** Registrasi selalu membuat role **MEMBER** dan otomatis membuat satu dompet "Tunai" bersaldo 0.
- **AC7** Login benar → session dibuat, diarahkan ke Dashboard (member) atau Kelola Pengguna (admin).
- **AC8** Login salah → pesan umum "Username atau password salah" (tidak membedakan mana yang salah).
- **AC9** Akun nonaktif → "Akun Anda dinonaktifkan. Hubungi admin."
- **AC10** Logout menghapus session dan kembali ke halaman Login.

### F02 — Profil & Ganti Password

- **AC1** Pengguna dapat melihat username (tidak bisa diubah), nama lengkap, email, tanggal bergabung.
- **AC2** Nama lengkap dan email dapat diubah dengan validasi yang sama seperti F01.
- **AC3** Ganti password wajib mengisi password lama; jika salah → "Password lama tidak sesuai".
- **AC4** Password baru minimal 8 karakter dan tidak boleh sama dengan password lama.

### F03 — Manajemen Dompet + Transfer

> Sebagai member, saya ingin memisahkan uang tunai, rekening bank, dan e-wallet agar saldo tiap tempat jelas.

- **AC1** Daftar dompet menampilkan nama, jenis (Tunai/Bank/E-Wallet), dan saldo; di atasnya ada **total saldo** semua dompet.
- **AC2** Tambah dompet: nama (wajib, maks 50 karakter, unik per pengguna), jenis, saldo awal (≥ 0).
- **AC3** Ubah dompet: hanya nama dan jenis. **Saldo tidak bisa diubah langsung** — hanya lewat transaksi atau transfer.
- **AC4** Hapus dompet ditolak jika dompet masih punya transaksi → "Dompet masih memiliki transaksi". Dompet terakhir tidak boleh dihapus.
- **AC5** Transfer: pilih dompet asal, tujuan, nominal. Asal ≠ tujuan, nominal > 0, saldo asal cukup. Kedua saldo berubah dalam **satu transaksi database** (gagal salah satu → batal semua).
- **AC6** Transfer tidak tercatat sebagai pemasukan/pengeluaran (tidak memengaruhi laporan).

### F04 — Panel Admin

- **AC1** Tabel semua pengguna: username, nama, email, role, status aktif, tanggal bergabung; kotak pencarian username/nama (difilter di frontend).
- **AC2** Admin dapat menonaktifkan/mengaktifkan member. Admin tidak bisa menonaktifkan dirinya sendiri.
- **AC3** Pengguna yang dinonaktifkan tidak bisa login (session yang sedang aktif tetap berlaku hingga logout/timeout).
- **AC4** Admin dapat menambah kategori default (nama + jenis). Nama unik per jenis di antara kategori default.
- **AC5** Admin dapat menghapus kategori default yang **belum dipakai** transaksi/anggaran mana pun; jika sudah dipakai → ditolak.

### F05 — Pencatatan Transaksi

> Sebagai member, saya ingin mencatat setiap pemasukan dan pengeluaran agar saldo dompet selalu sesuai kenyataan.

- **AC1** Form: jenis (Pemasukan/Pengeluaran), nominal, tanggal, kategori (hanya kategori yang jenisnya sesuai), dompet, catatan (opsional, maks 255). Pemasukan punya field opsional **sumber**; pengeluaran punya field opsional **metode bayar**.
- **AC2** Validasi: nominal > 0 dan ≤ 1.000.000.000; tanggal tidak boleh di masa depan; kategori dan dompet milik pengguna (atau kategori default).
- **AC3** Simpan **pemasukan** → saldo dompet bertambah. Simpan **pengeluaran** → saldo dompet berkurang; jika saldo tidak cukup → ditolak "Saldo dompet tidak mencukupi".
- **AC4** Ubah transaksi → efek lama dibatalkan dulu (`revertFromWallet`) lalu efek baru diterapkan (`applyToWallet`). Berlaku juga jika dompet atau jenisnya berubah.
- **AC5** Hapus transaksi → saldo dikembalikan.
- **AC6** Setiap pengeluaran yang ditambah/diubah/dihapus memicu perhitungan ulang anggaran kategori terkait (integrasi F08/F09).
- **AC7** Semua langkah di atas berjalan dalam **satu transaksi database**.

### F06 — Kategori Kustom

- **AC1** Daftar kategori menampilkan kategori default (label "Default", tidak bisa dihapus) dan kategori milik sendiri.
- **AC2** Tambah kategori: nama (wajib, maks 40), jenis. Nama tidak boleh sama dengan kategori default atau milik sendiri pada jenis yang sama.
- **AC3** Hapus kategori milik sendiri hanya jika belum dipakai transaksi/anggaran.

### F07 — Riwayat, Cari & Filter

- **AC1** Tabel riwayat: tanggal, kategori, catatan, dompet, nominal (hijau `+` untuk pemasukan, merah `−` untuk pengeluaran). Urut tanggal terbaru.
- **AC2** Filter: rentang tanggal, jenis, kategori, dompet; pencarian kata kunci pada catatan. Filter bisa dikombinasikan.
- **AC3** Paginasi 10 baris per halaman; menampilkan total data dan total pemasukan/pengeluaran sesuai filter.
- **AC4** Default filter: bulan berjalan.

### F08 — Anggaran Bulanan

> Sebagai member, saya ingin membatasi pengeluaran per kategori setiap bulan.

- **AC1** Pilih bulan (default bulan berjalan). Tampil kartu per anggaran: kategori, batas, terpakai, sisa, progress bar.
- **AC2** Tambah anggaran: kategori **pengeluaran**, bulan, batas (> 0). Satu anggaran per kategori per bulan → duplikat ditolak.
- **AC3** Saat dibuat, `terpakai` langsung dihitung dari pengeluaran yang sudah ada di bulan tersebut.
- **AC4** Warna progress: hijau < 80%, kuning 80–99%, merah ≥ 100%.
- **AC5** Ubah batas dan hapus anggaran tersedia.

### F09 — Notifikasi Anggaran

- **AC1** Saat pengeluaran membuat progres anggaran **melewati** 80% (sebelumnya < 80%) → notifikasi WARNING: "Anggaran Makan & Minum Oktober 2026 sudah terpakai 85%".
- **AC2** Saat **melewati** 100% → notifikasi DANGER: "Anggaran Makan & Minum Oktober 2026 terlampaui (Rp 1.250.000 dari Rp 1.200.000)".
- **AC3** Notifikasi hanya dikirim saat ambang dilewati, **tidak berulang** untuk setiap transaksi berikutnya.
- **AC4** Ikon lonceng di navbar menampilkan jumlah notifikasi belum dibaca; klik → daftar notifikasi; tombol "Tandai semua dibaca".
- **AC5** Mekanisme memakai pola **Observer**: `BudgetService` memanggil semua `BudgetAlertListener` yang terdaftar.

### F10 — Target Tabungan

- **AC1** Tambah target: nama, nominal target (> 0), tenggat (harus di masa depan).
- **AC2** Kartu target: progres %, terkumpul/target, sisa hari, **rekomendasi setoran per bulan**, status.
- **AC3** Setor dana: pilih dompet sumber + nominal; saldo dompet berkurang dan `terkumpul` bertambah (satu transaksi database). Nominal tidak boleh melebihi sisa target atau saldo dompet.
- **AC4** Saat terkumpul ≥ target → status otomatis **ACHIEVED**, tombol setor dinonaktifkan.
- **AC5** Batalkan target → status **CANCELLED**; dana yang sudah terkumpul dikembalikan ke **dompet utama** pengguna (dompet tertua, yaitu "Tunai" yang dibuat saat registrasi). Target yang sudah ACHIEVED tidak bisa dibatalkan.
- **AC6** Setoran tabungan tidak dihitung sebagai pengeluaran di laporan.

### F11 — Dashboard Ringkasan

- **AC1** Kartu: Total Saldo, Pemasukan bulan ini, Pengeluaran bulan ini, *Saving rate* bulan ini.
- **AC2** Info anggaran: jumlah anggaran aktif bulan ini dan berapa yang terlampaui.
- **AC3** Daftar target tabungan aktif (maks 3) dengan progres.
- **AC4** 5 transaksi terbaru.
- **AC5** Grafik batang mini pemasukan vs pengeluaran 6 bulan terakhir.

### F12 — Laporan per Kategori

- **AC1** Input: rentang tanggal.
- **AC2** Output: tabel **pengeluaran** per kategori, total, persentase (urut terbesar) + pie chart + kategori pengeluaran terbesar.
- **AC3** Ringkasan: total pemasukan, total pengeluaran, saldo bersih periode.

### F13 — Laporan per Periode

- **AC1** Input: jenis periode (Harian/Mingguan/Bulanan/Tahunan) dan rentang tanggal.
- **AC2** Output: bar chart pemasukan vs pengeluaran per periode + tabel.
- **AC3** Rata-rata pengeluaran harian dan perbandingan total pengeluaran dengan periode sebelumnya (panjang sama) dalam %.

### F14 — Unduh Laporan CSV

- **AC1** Tombol "Unduh CSV" di halaman Laporan sesuai filter yang sedang aktif.
- **AC2** Nama file: `spendwise_laporan-kategori_2026-10-01_2026-10-31.csv`.
- **AC3** Pemisah `;`, encoding UTF-8 dengan BOM agar langsung terbaca benar di Excel versi Indonesia.

---

## 5. Aturan Bisnis & Rumus

| Kode | Aturan |
|---|---|
| BR-01 | Saldo dompet **hanya** berubah lewat `deposit()`, `withdraw()`, `transferTo()`. Tidak ada setter saldo. |
| BR-02 | Saldo dompet tidak boleh negatif. |
| BR-03 | Semua nominal dibulatkan 2 desimal; ditampilkan sebagai Rupiah tanpa desimal (`Rp 1.250.000`). |
| BR-04 | `Budget.getProgress()` = `spentAmount ÷ targetAmount` (0,0–∞). |
| BR-05 | Ambang notifikasi: WARNING ≥ 0,80; DANGER ≥ 1,00; hanya saat ambang **dilewati**. |
| BR-06 | `SavingGoal.getProgress()` = `currentAmount ÷ targetAmount`, maksimal 1,0. |
| BR-07 | Rekomendasi setoran per bulan = `sisa target ÷ max(1, sisa bulan hingga tenggat)` (sisa bulan dibulatkan ke atas). |
| BR-08 | Saldo bersih = total pemasukan − total pengeluaran (periode laporan). |
| BR-09 | *Saving rate* = `(pemasukan − pengeluaran) ÷ pemasukan × 100%`; jika pemasukan 0 → tampil "—". |
| BR-10 | Perbandingan periode = `(total ini − total sebelumnya) ÷ total sebelumnya × 100%`; jika sebelumnya 0 → "—". |
| BR-11 | Rata-rata pengeluaran harian = total pengeluaran ÷ jumlah hari dalam rentang. |
| BR-12 | Transfer antar dompet dan setoran tabungan **tidak** masuk laporan pemasukan/pengeluaran. |
| BR-13 | Kategori default (`is_default = 1`, `owner_id = NULL`) terlihat oleh semua member. |
| BR-14 | Dompet utama = dompet dengan `wallet_id` terkecil milik pengguna; dipakai untuk pengembalian dana target yang dibatalkan. |

### 5.1 Format Password

`passwordHash = salt + "$" + hex(SHA-256(salt + password))`, dengan `salt` = 16 karakter heksadesimal acak (`SecureRandom`). Contoh ada di `database/seed.sql`.

---

## 6. Kebutuhan Non-Fungsional

- **Keamanan:** password ter-hash; session cookie `HttpOnly`; semua query memakai parameter (`?`) — **dilarang** menyusun SQL dengan penggabungan string (mencegah SQL injection, juga dicek SonarQube).
- **Kinerja:** halaman utama dimuat < 2 detik untuk 1.000 transaksi.
- **Responsif:** tampilan nyaman di layar 360 px (ponsel) hingga 1440 px.
- **Bahasa UI:** Bahasa Indonesia; format tanggal `30 Okt 2026`; zona waktu Asia/Jakarta.
- **Kualitas kode:** lolos quality gate SonarQube (lihat `TESTING.md`).

---

## 7. Di Luar Lingkup

- Integrasi bank, payment gateway, atau OCR struk.
- Multi-mata uang.
- Berbagi dompet antar pengguna.
- Aplikasi mobile native.
