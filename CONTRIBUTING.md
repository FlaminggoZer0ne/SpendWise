# Panduan Kontribusi SpendWise

Aturan kerja tim agar 4 orang bisa coding paralel tanpa saling menimpa, dan kode tetap lolos SonarQube.

---

## 1. Alur Git

```
main      ← hanya rilis yang sudah stabil (progres UAS, pengumpulan akhir)
 └─ develop   ← integrasi harian; semua PR masuk ke sini
     ├─ feat/m1-auth-login
     ├─ feat/m2-transaksi-crud
     ├─ feat/m3-anggaran-observer
     ├─ feat/m4-laporan-kategori
     └─ fix/m2-saldo-edit-transaksi
```

**Format nama branch:** `<jenis>/<modul>-<deskripsi-singkat>`
- jenis: `feat` (fitur), `fix` (perbaikan bug), `refactor`, `docs`, `test`, `chore` (konfigurasi)
- modul: `core`, `m1`, `m2`, `m3`, `m4`, `fe` (frontend bersama)

**Langkah kerja:**

```bash
git checkout develop && git pull
git checkout -b feat/m2-transaksi-crud
# ... coding, commit kecil-kecil ...
git push -u origin feat/m2-transaksi-crud
# buka Pull Request ke develop di GitHub
```

Sebelum membuat PR, sinkronkan dulu dengan `develop` terbaru (`git pull origin develop`) dan pastikan `./mvnw test` serta `npm run build` berhasil.

---

## 2. Format Commit

`<jenis>(<modul>): <ringkasan dalam bahasa Indonesia, huruf kecil>`

```
feat(m1): tambah endpoint transfer antar dompet
fix(m2): saldo tidak kembali saat transaksi dihapus
test(m3): uji notifikasi ambang 80% hanya terkirim sekali
docs(api): tambah contoh respons laporan periode
refactor(core): pindahkan pesan error ke konstanta
```

Satu commit = satu perubahan logis. Hindari commit "update", "fix bug", "wip final fix".

---

## 3. Pull Request

- Judul mengikuti format commit.
- Deskripsi berisi: fitur/issue terkait (mis. `F05`, `#12`), ringkasan perubahan, cara menguji, screenshot (untuk perubahan UI).
- **Minimal 1 reviewer** dari anggota lain. Reviewer mengecek:
  - [ ] Nama kelas/method sesuai `CLASS_DESIGN.md`
  - [ ] Request/response sesuai `API.md`
  - [ ] Tidak ada logika bisnis di controller atau di frontend
  - [ ] SQL memakai parameter `?`
  - [ ] Ada unit test untuk logika baru
  - [ ] Tidak ada file rahasia (`application.properties`) atau folder build ikut ter-commit
- Merge pakai **Squash and merge**, lalu hapus branch.
- Konflik di file milik modul lain → diskusikan dengan PJ modul tersebut, jangan diselesaikan sepihak.

### Kepemilikan file

Setiap anggota hanya mengubah file modulnya. Perubahan pada file bersama (`core/`, `client.js`, layout, `API.md`, `CLASS_DESIGN.md`, `schema.sql`) wajib lewat PR yang di-review **semua** anggota.

---

## 4. Konvensi Kode Java

- Java 17, indentasi 4 spasi, kurung kurawal di baris yang sama.
- Penamaan: kelas `PascalCase`, method/variabel `camelCase`, konstanta `UPPER_SNAKE_CASE`, package huruf kecil.
- Access modifier **persis** seperti `CLASS_DESIGN.md`. Atribut tidak pernah `public`.
- Constructor injection dengan field `private final`; tanpa `@Autowired` di field.
- Satu kelas publik per file; urutan isi kelas: konstanta → atribut → constructor → method publik → method privat.
- Pesan error untuk pengguna dalam Bahasa Indonesia; simpan sebagai konstanta jika dipakai lebih dari sekali.
- Log pakai SLF4J, bukan `System.out`.
- Javadoc singkat untuk setiap kelas dan method publik di model & service:

```java
/**
 * Mengurangi saldo dompet.
 *
 * @param amount nominal yang ditarik, harus lebih dari 0
 * @return true jika berhasil, false jika saldo tidak mencukupi
 */
public boolean withdraw(double amount) { ... }
```

## 5. Konvensi Kode Frontend

- Komponen fungsi + hooks; nama file komponen `PascalCase.jsx`, utilitas `camelCase.js`.
- Panggilan API hanya dari folder `src/api/`, tidak langsung `axios` di komponen.
- Styling dengan kelas Tailwind; hindari CSS inline kecuali nilai dinamis (mis. lebar progress bar).
- Teks UI Bahasa Indonesia; uang selalu lewat `formatRupiah`, tanggal lewat `formatTanggal`.
- Jalankan `npm run lint` sebelum PR (ESLint bawaan Vite).

---

## 6. Hal yang Tidak Boleh Di-commit

Sudah diatur di `.gitignore`: `backend/target/`, `frontend/node_modules/`, `frontend/dist/`, `application.properties`, file IDE (`.idea/`, `.vscode/`), `.env`, file OS. Jika terlanjur ter-commit: `git rm -r --cached <path>` lalu commit.
