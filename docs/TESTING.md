# Rencana Pengujian SpendWise

Tiga lapis pengujian: **unit test** (JUnit, logika model & service), **uji API** (Postman/Thunder Client), dan **uji fungsional end-to-end** lewat browser. Ditambah analisis kualitas kode dengan **SonarQube**.

---

## 1. Unit Test (JUnit 5)

Lokasi: `backend/src/test/java/com/spendwise/...` (mengikuti package kelas yang diuji). Jalankan: `./mvnw test`.

Model diuji **tanpa** database (cukup `new`). Service diuji dengan DAO tiruan (Mockito, sudah termasuk di `spring-boot-starter-test`).

| Kelas | Kasus minimal | PJ |
|---|---|---|
| `PasswordHasher` | hash → matches benar; password salah → false; dua hash password sama berbeda (salt) | A1 |
| `Wallet` | deposit menambah; withdraw > saldo → false & saldo tetap; nominal ≤ 0 → exception; transferTo ke dompet sama → exception; transfer sukses mengubah dua saldo | A1 |
| `Member` / `Admin` | `getRole()` dan `getMenuAccess()` berbeda (polymorphism) | A1 |
| `AuthService` | register username duplikat → 409; login akun nonaktif → 403; login sukses mengembalikan `Member` | A1 |
| `Income` / `Expense` | `applyToWallet` menambah/mengurangi; `revertFromWallet` kebalikannya; expense saldo kurang → false | A2 |
| `Transaction.validate()` | nominal 0, tanggal besok, kategori beda jenis → exception | A2 |
| `TransactionService` | edit pengeluaran pindah dompet → saldo dompet lama kembali, dompet baru berkurang; hapus memanggil `recalculate` | A2 |
| `Budget` | progress 0,85 → `isNearLimit(0.8)` true; > 1 → `isOverBudget` true | A3 |
| `BudgetService.checkBudget` | 70%→85% memicu `onBudgetWarning` **sekali**; 85%→90% tidak memicu lagi; 95%→105% memicu `onBudgetExceeded` | A3 |
| `SavingGoal` | `addContribution` sampai target → status ACHIEVED; `getRecommendedMonthlySaving` sesuai BR-07 | A3 |
| `CategoryReport` | persentase total = 100%; urut terbesar; `getTopExpenseCategory` | A4 |
| `PeriodReport` | label WEEKLY ISO benar; `compareWithPreviousPeriod` sebelumnya 0 → null/NaN ditangani | A4 |
| `Report.toCSV()` | baris header + pemisah `;` + desimal koma | A4 |

Contoh:

```java
class WalletTest {
    @Test
    void withdrawMoreThanBalanceShouldFailAndKeepBalance() {
        Wallet w = new Wallet(1, 2, "Tunai", WalletType.CASH, 100_000);
        assertFalse(w.withdraw(150_000));
        assertEquals(100_000, w.getBalance(), 0.001);
    }
}
```

Target cakupan (*coverage*) untuk paket `model` dan `service`: **≥ 60%** (diukur JaCoCo, terlihat di SonarQube).

### 1.1 Tes integrasi (butuh database)

Tes `*IntegrationTest` (DAO, service, API lewat MockMvc) hanya aktif bila `-Dspendwise.it=true`, sehingga `./mvnw test` biasa tetap hijau tanpa MySQL. Database harus sudah berisi `schema.sql` + `seed.sql`. Tiap tes di-rollback, jadi data seed tidak berubah (satu tes rollback sengaja berjalan tanpa transaksi tes dan membersihkan datanya sendiri).

```bash
cd backend
./mvnw test -Dspendwise.it=true \
  "-Dspring.datasource.url=jdbc:mysql://localhost:3306/spendwise?useSSL=false&serverTimezone=Asia/Jakarta&allowPublicKeyRetrieval=true" \
  -Dspring.datasource.username=root -Dspring.datasource.password=
```

> Jalankan hanya pada database lokal/uji. `schema.sql` melakukan `DROP DATABASE spendwise`.

| Kelas tes | Isi |
|---|---|
| `DaoIntegrationTest` | SQL tiap DAO, filter dinamis & paginasi, kebal injeksi, constraint DB |
| `ServiceIntegrationTest` | Saldo tambah/ubah (ganti dompet & jenis)/hapus, rollback, Observer anggaran 80%/100%, tabungan, laporan |
| `ApiIntegrationTest` | Kontrak `API.md`: status code, format `ApiResponse`, 401/403/404, CSV (BOM, `;`, nama file) |

---

## 2. Uji API

Simpan koleksi Postman/Thunder Client di `docs/api-tests/spendwise.postman_collection.json` (export dari aplikasi). Set environment `baseUrl = http://localhost:8080/api`. Postman otomatis menyimpan cookie `JSESSIONID` setelah login.

Urutan uji cepat (pakai data seed):

1. `POST /auth/login` budi → 200
2. `GET /wallets` → total saldo Rp 1.333.000
3. `POST /transactions` pengeluaran Rp 50.000 kategori 6 (Makan & Minum) dompet 1 → 201; `GET /budgets?month=2026-10` → Makan & Minum terpakai 1.065.000
4. `POST /transactions` pengeluaran Rp 10.000.000 → 400 "Saldo dompet tidak mencukupi"
5. `GET /admin/users` sebagai budi → 403
6. `POST /auth/logout` lalu `GET /wallets` → 401

---

## 3. Skenario Uji Fungsional (Browser)

Catat hasil di tabel ini saat M6 dan M7 (kolom Hasil: ✅ / ❌ + nomor issue).

> **Terakhir dijalankan 30 Okt 2026** di Chromium 149 (Playwright) terhadap MariaDB 10.4 berisi `seed.sql`, viewport desktop 1366×850 dan ponsel 390×844: 30 skenario TC + 10 pemeriksaan tambahan (logout, 7 halaman tanpa scroll horizontal di ponsel, drawer sidebar) = **40/40 lulus**. Aksi dilakukan lewat klik UI; saldo diperiksa lewat API. Screenshot ada di `docs/screenshots/`.

| ID | Fitur | Langkah | Hasil yang diharapkan | Hasil |
|---|---|---|---|---|
| TC-01 | F01 | Daftar akun baru "tes01" | Berhasil, diarahkan ke login; punya dompet "Tunai" Rp 0 | ✅ |
| TC-02 | F01 | Daftar dengan username "budi" | Pesan "Username sudah dipakai" | ✅ |
| TC-03 | F01 | Login budi password salah | "Username atau password salah" | ✅ |
| TC-04 | F04/F01 | Admin nonaktifkan sari → login sari | "Akun Anda dinonaktifkan…" | ✅ |
| TC-05 | F02 | Ganti password dengan password lama salah | "Password lama tidak sesuai" | ✅ |
| TC-06 | F03 | Tambah dompet "OVO" saldo awal 50.000 | Muncul di daftar, total saldo bertambah 50.000 | ✅ |
| TC-07 | F03 | Transfer BCA → Tunai 100.000 | BCA −100.000, Tunai +100.000, laporan tidak berubah | ✅ |
| TC-08 | F03 | Hapus dompet BCA (punya transaksi) | Ditolak "Dompet masih memiliki transaksi" | ✅ |
| TC-09 | F05 | Tambah pengeluaran 20.000 dari GoPay | Saldo GoPay −20.000 | ✅ |
| TC-10 | F05 | Ubah transaksi TC-09 jadi 30.000 dari Tunai | GoPay kembali +20.000, Tunai −30.000 | ✅ |
| TC-11 | F05 | Hapus transaksi TC-10 | Tunai +30.000 | ✅ |
| TC-12 | F05 | Pengeluaran melebihi saldo dompet | Ditolak, saldo tidak berubah | ✅ |
| TC-13 | F05 | Pilih tanggal besok | Tidak bisa dipilih / ditolak | ✅ |
| TC-14 | F06 | Tambah kategori "Ngopi" (sudah ada) | Ditolak nama duplikat | ✅ |
| TC-15 | F06 | Hapus kategori default | Tidak ada tombol hapus / ditolak | ✅ |
| TC-16 | F07 | Filter Okt 2026 + kategori Ngopi | 4 transaksi, total pengeluaran Rp 217.000 | ✅ |
| TC-17 | F08 | Buat anggaran Belanja Okt 2026 Rp 500.000 | Terpakai langsung Rp 310.000 (62%) | ✅ |
| TC-18 | F08 | Buat anggaran Makan & Minum Okt 2026 lagi | Ditolak duplikat | ✅ |
| TC-19 | F09 | Tambah pengeluaran Belanja 100.000 (→82%) | Notifikasi WARNING muncul, badge lonceng +1 | ✅ |
| TC-20 | F09 | Tambah lagi Belanja 10.000 (→84%) | **Tidak** ada notifikasi baru | ✅ |
| TC-21 | F09 | Tambah Belanja 100.000 (→104%) | Notifikasi DANGER, kartu merah | ✅ |
| TC-22 | F10 | Setor 500.000 ke "Laptop Baru" dari BCA | Progres 37,5%, BCA −500.000 | ✅ |
| TC-23 | F10 | Setor melebihi sisa target | Ditolak | ✅ |
| TC-24 | F10 | Batalkan target | Status CANCELLED, dana kembali ke dompet utama | ✅ |
| TC-25 | F11 | Buka dashboard budi | Angka kartu = jumlah dari halaman Transaksi & Dompet | ✅ |
| TC-26 | F12 | Laporan kategori Okt 2026 | Persentase total 100%, terbesar "Kos & Sewa" | ✅ |
| TC-27 | F13 | Laporan periode mingguan Okt 2026 | Bar per minggu ISO, rata-rata harian tampil | ✅ |
| TC-28 | F14 | Unduh CSV | File terbuka rapi di Excel, nama file sesuai | ✅ unduh, nama file, BOM & pemisah `;` terverifikasi; tampilan di Excel belum dicek |
| TC-29 | Akses | Member membuka `/admin/users` | Diarahkan ke dashboard; API 403 | ✅ |
| TC-30 | Akses | Ubah URL `/api/transactions/{id}` milik user lain | 404 | ✅ |

---

## 4. SonarQube

### 4.1 Menjalankan

**Opsi A — SonarQube lokal (Docker):**

```bash
docker run -d --name sonarqube -p 9000:9000 sonarqube:lts-community
# buka http://localhost:9000 (admin/admin), buat project "spendwise", buat token
cd backend
./mvnw clean verify sonar:sonar \
  -Dsonar.projectKey=spendwise \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=TOKEN_ANDA
```

**Opsi B — SonarCloud** (gratis untuk repo publik): hubungkan repo GitHub di sonarcloud.io dan ikuti wizard Maven.

Tambahkan plugin JaCoCo di `pom.xml` agar coverage terbaca:

```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.12</version>
  <executions>
    <execution><goals><goal>prepare-agent</goal></goals></execution>
    <execution><id>report</id><phase>verify</phase><goals><goal>report</goal></goals></execution>
  </executions>
</plugin>
```

Frontend boleh ikut di-scan (opsional) dengan `sonar-scanner` dan `sonar.sources=frontend/src`, tetapi fokus penilaian OOP ada di backend.

### 4.2 Target Quality Gate

| Metrik | Target |
|---|---|
| Bugs | 0 |
| Vulnerabilities | 0 |
| Security Hotspots | semua di-review |
| Code Smells | rating **A** |
| Coverage (`model` + `service`) | ≥ 60% |
| Duplications | < 3% |

### 4.3 Penyebab Code Smell yang Sering Muncul (hindari sejak awal)

- Field injection `@Autowired` → pakai constructor injection
- `System.out.println` / `e.printStackTrace()` → pakai logger SLF4J (`private static final Logger log = LoggerFactory.getLogger(...)`)
- Menangkap `Exception` umum lalu diam saja
- String literal berulang ≥ 3 kali (mis. `"ADMIN"`, pesan error) → jadikan konstanta
- Method terlalu panjang/kompleks (Cognitive Complexity > 15) → pecah jadi method `private`
- Nama variabel satu huruf, import tak terpakai, kode yang di-comment
- SQL dari penggabungan string → Security Hotspot (wajib pakai `?`)
- Field `public` non-final

Simpan screenshot dashboard SonarQube (overview + daftar issue) sebelum dan sesudah perbaikan untuk laporan akhir.
