# Kontrak REST API SpendWise

Dokumen ini adalah **kontrak** antara frontend dan backend. Jika ada perubahan (nama field, status code, endpoint), ubah dokumen ini **dulu** lewat PR, baru kodenya.

- Base URL: `/api` (development: `http://localhost:8080/api`, diakses frontend lewat proxy Vite)
- Format: JSON, `Content-Type: application/json; charset=UTF-8`
- Autentikasi: cookie session `JSESSIONID` (Axios: `withCredentials: true`)
- Tanggal: `YYYY-MM-DD` (ISO), bulan: `YYYY-MM`, waktu: `YYYY-MM-DDTHH:mm:ss`
- Nominal: angka desimal (`50000.00`), bukan string

---

## 1. Ringkasan Endpoint

| # | Method | Endpoint | Role | Controller | PJ |
|---|---|---|---|---|---|
| 1 | POST | `/auth/register` | publik | AuthController | A1 |
| 2 | POST | `/auth/login` | publik | AuthController | A1 |
| 3 | POST | `/auth/logout` | login | AuthController | A1 |
| 4 | GET | `/auth/me` | login | AuthController | A1 |
| 5 | PUT | `/auth/profile` | login | AuthController | A1 |
| 6 | PUT | `/auth/password` | login | AuthController | A1 |
| 7 | GET | `/wallets` | MEMBER | WalletController | A1 |
| 8 | POST | `/wallets` | MEMBER | WalletController | A1 |
| 9 | PUT | `/wallets/{id}` | MEMBER | WalletController | A1 |
| 10 | DELETE | `/wallets/{id}` | MEMBER | WalletController | A1 |
| 11 | POST | `/wallets/transfer` | MEMBER | WalletController | A1 |
| 12 | GET | `/admin/users` | ADMIN | AdminController | A1 |
| 13 | PATCH | `/admin/users/{id}/active` | ADMIN | AdminController | A1 |
| 14 | POST | `/admin/categories` | ADMIN | AdminController | A1 |
| 15 | DELETE | `/admin/categories/{id}` | ADMIN | AdminController | A1 |
| 16 | GET | `/transactions` | MEMBER | TransactionController | A2 |
| 17 | POST | `/transactions` | MEMBER | TransactionController | A2 |
| 18 | PUT | `/transactions/{id}` | MEMBER | TransactionController | A2 |
| 19 | DELETE | `/transactions/{id}` | MEMBER | TransactionController | A2 |
| 20 | GET | `/categories` | login | CategoryController | A2 |
| 21 | POST | `/categories` | MEMBER | CategoryController | A2 |
| 22 | DELETE | `/categories/{id}` | MEMBER | CategoryController | A2 |
| 23 | GET | `/budgets?month=` | MEMBER | BudgetController | A3 |
| 24 | POST | `/budgets` | MEMBER | BudgetController | A3 |
| 25 | PUT | `/budgets/{id}` | MEMBER | BudgetController | A3 |
| 26 | DELETE | `/budgets/{id}` | MEMBER | BudgetController | A3 |
| 27 | GET | `/notifications` | MEMBER | NotificationController | A3 |
| 28 | POST | `/notifications/read-all` | MEMBER | NotificationController | A3 |
| 29 | GET | `/goals` | MEMBER | SavingGoalController | A3 |
| 30 | POST | `/goals` | MEMBER | SavingGoalController | A3 |
| 31 | POST | `/goals/{id}/contribute` | MEMBER | SavingGoalController | A3 |
| 32 | POST | `/goals/{id}/cancel` | MEMBER | SavingGoalController | A3 |
| 33 | GET | `/reports/dashboard` | MEMBER | ReportController | A4 |
| 34 | GET | `/reports/category` | MEMBER | ReportController | A4 |
| 35 | GET | `/reports/period` | MEMBER | ReportController | A4 |
| 36 | GET | `/reports/export` | MEMBER | ReportController | A4 |

---

## 2. Format Respons Standar

Semua respons JSON (kecuali unduhan CSV) memakai pembungkus `ApiResponse`:

**Sukses**
```json
{ "success": true, "message": "Transaksi berhasil disimpan", "data": { } }
```

**Gagal**
```json
{
  "success": false,
  "message": "Validasi gagal",
  "errors": { "amount": "Nominal harus lebih dari 0", "date": "Tanggal tidak boleh di masa depan" }
}
```

`message` selalu berisi kalimat Bahasa Indonesia yang siap ditampilkan ke pengguna. `errors` hanya ada untuk kesalahan validasi per field.

| Status | Kapan |
|---|---|
| 200 OK | Berhasil (GET, PUT, PATCH, DELETE, aksi POST) |
| 201 Created | Data baru berhasil dibuat |
| 400 Bad Request | Validasi gagal / aturan bisnis dilanggar (saldo kurang, dll.) |
| 401 Unauthorized | Belum login / session habis → frontend mengarahkan ke `/login` |
| 403 Forbidden | Role tidak berhak (member membuka `/admin/**`) |
| 404 Not Found | Data tidak ada **atau bukan milik pengguna** |
| 409 Conflict | Duplikat (username, nama dompet, anggaran kategori-bulan) atau data masih dipakai |
| 500 | Kesalahan server tak terduga |

---

## 3. Objek Data (Schema JSON)

```jsonc
// User (tanpa passwordHash!)
{ "userId": 2, "username": "budi", "fullName": "Budi Santoso", "email": "budi@mail.com",
  "role": "MEMBER", "active": true, "createdAt": "2026-10-01T08:00:00",
  "menuAccess": ["DASHBOARD","TRANSACTIONS","WALLETS","CATEGORIES","BUDGETS","GOALS","REPORTS","PROFILE"] }

// Wallet
{ "walletId": 1, "name": "Tunai", "walletType": "CASH", "balance": 150000.00 }

// Category
{ "categoryId": 6, "name": "Makan & Minum", "type": "EXPENSE", "isDefault": true }

// Transaction
{ "transactionId": 15, "type": "EXPENSE", "amount": 45000.00, "date": "2026-10-28",
  "description": "Makan siang", "category": { "categoryId": 6, "name": "Makan & Minum" },
  "wallet": { "walletId": 2, "name": "BCA" },
  "source": null, "paymentMethod": "QRIS", "isRecurring": false }

// Budget
{ "planId": 1, "name": "Anggaran Makan & Minum", "category": { "categoryId": 6, "name": "Makan & Minum" },
  "period": "2026-10", "targetAmount": 1200000.00, "spentAmount": 1015000.00,
  "remainingAmount": 185000.00, "progressPercentage": 84.58, "status": "WARNING" }   // SAFE | WARNING | EXCEEDED

// SavingGoal
{ "planId": 1, "name": "Laptop Baru", "targetAmount": 8000000.00, "currentAmount": 2500000.00,
  "deadline": "2027-06-30", "status": "ACTIVE", "progressPercentage": 31.25,
  "daysRemaining": 243, "recommendedMonthlySaving": 687500.00 }

// Notification
{ "notificationId": 3, "message": "Anggaran Makan & Minum Oktober 2026 sudah terpakai 85%",
  "level": "WARNING", "isRead": false, "createdAt": "2026-10-28T12:10:00" }
```

---

## 4. Auth & Profil (Anggota 1)

### 4.1 `POST /auth/register`
```json
// Request
{ "username": "sari", "fullName": "Sari Wulandari", "email": "sari@mail.com",
  "password": "member12345", "confirmPassword": "member12345" }
// 201
{ "success": true, "message": "Registrasi berhasil, silakan login", "data": { "userId": 4, "username": "sari", "role": "MEMBER" } }
```
Error: 400 (validasi), 409 `"Username sudah dipakai"`.

### 4.2 `POST /auth/login`
```json
// Request
{ "username": "budi", "password": "member12345" }
// 200 — membuat session, cookie JSESSIONID dikirim di header Set-Cookie
{ "success": true, "message": "Login berhasil", "data": { /* User */ } }
```
Error: 401 `"Username atau password salah"`, 403 `"Akun Anda dinonaktifkan. Hubungi admin."`

### 4.3 `POST /auth/logout` → 200 `{ "success": true, "message": "Logout berhasil" }`

### 4.4 `GET /auth/me` → 200 `data: User` · 401 jika belum login. Dipanggil frontend saat aplikasi dibuka untuk memulihkan status login.

### 4.5 `PUT /auth/profile`
```json
{ "fullName": "Budi Santoso", "email": "budi.s@mail.com" }
```
→ 200 `data: User`

### 4.6 `PUT /auth/password`
```json
{ "oldPassword": "member12345", "newPassword": "rahasia2026", "confirmPassword": "rahasia2026" }
```
→ 200 · 400 `"Password lama tidak sesuai"`

---

## 5. Dompet (Anggota 1)

### 5.1 `GET /wallets`
```json
{ "success": true, "data": { "totalBalance": 1333000.00, "wallets": [ /* Wallet */ ] } }
```

### 5.2 `POST /wallets`
```json
{ "name": "GoPay", "walletType": "E_WALLET", "initialBalance": 100000 }
```
→ 201 `data: Wallet` · 409 `"Nama dompet sudah dipakai"`

### 5.3 `PUT /wallets/{id}`
```json
{ "name": "GoPay Utama", "walletType": "E_WALLET" }
```
Field `balance` diabaikan jika dikirim (BR-01). → 200 `data: Wallet`

### 5.4 `DELETE /wallets/{id}`
→ 200 · 409 `"Dompet masih memiliki transaksi"` · 409 `"Minimal harus ada satu dompet"`

### 5.5 `POST /wallets/transfer`
```json
{ "fromWalletId": 2, "toWalletId": 1, "amount": 200000 }
```
→ 200 `data: { "from": Wallet, "to": Wallet }` · 400 `"Saldo dompet tidak mencukupi"` · 400 `"Dompet asal dan tujuan tidak boleh sama"`

---

## 6. Admin (Anggota 1)

### 6.1 `GET /admin/users`
→ 200 `data: [ User, ... ]` (pencarian dilakukan di frontend)

### 6.2 `PATCH /admin/users/{id}/active`
```json
{ "active": false }
```
→ 200 · 400 `"Tidak dapat menonaktifkan akun sendiri"`

### 6.3 `POST /admin/categories`
```json
{ "name": "Donasi", "type": "EXPENSE" }
```
→ 201 `data: Category` · 409 `"Kategori default dengan nama ini sudah ada"`

### 6.4 `DELETE /admin/categories/{id}` → 200 · 409 `"Kategori sudah dipakai dan tidak dapat dihapus"`

---

## 7. Transaksi (Anggota 2)

### 7.1 `GET /transactions`

Query (semua opsional):

| Param | Contoh | Default |
|---|---|---|
| `from`, `to` | `2026-10-01`, `2026-10-31` | awal–akhir bulan berjalan |
| `type` | `INCOME` / `EXPENSE` | semua |
| `categoryId` | `6` | semua |
| `walletId` | `2` | semua |
| `q` | `makan` | — (cari di `description`, LIKE) |
| `page` | `1` | 1 |
| `size` | `10` (maks 100) | 10 |

```json
{ "success": true, "data": {
    "items": [ /* Transaction */ ],
    "page": 1, "size": 10, "totalItems": 19, "totalPages": 2,
    "totalIncome": 5500000.00, "totalExpense": 3582000.00 } }
```
`totalIncome`/`totalExpense` dihitung dari **seluruh** data yang cocok filter (bukan hanya halaman ini). Dashboard memakai `?size=5` untuk 5 transaksi terbaru.

### 7.2 `POST /transactions`
```json
{ "type": "EXPENSE", "amount": 45000, "date": "2026-10-28", "categoryId": 6, "walletId": 2,
  "description": "Makan siang", "paymentMethod": "QRIS", "isRecurring": false }
```
Untuk `INCOME` pakai `"source": "Kantor"` (dan abaikan `paymentMethod`/`isRecurring`).
→ 201 `data: Transaction`
Error 400: `"Saldo dompet tidak mencukupi"`, `"Kategori tidak sesuai dengan jenis transaksi"`, `"Tanggal tidak boleh di masa depan"`; 404 kategori/dompet tidak ditemukan.

### 7.3 `PUT /transactions/{id}` — body sama dengan POST → 200 `data: Transaction`
### 7.4 `DELETE /transactions/{id}` → 200

---

## 8. Kategori (Anggota 2)

### 8.1 `GET /categories?type=EXPENSE`
Member: kategori default + milik sendiri. Admin: hanya kategori default.
```json
{ "success": true, "data": [ { "categoryId": 6, "name": "Makan & Minum", "type": "EXPENSE", "isDefault": true },
                             { "categoryId": 15, "name": "Ngopi", "type": "EXPENSE", "isDefault": false } ] }
```

### 8.2 `POST /categories` → `{ "name": "Ngopi", "type": "EXPENSE" }` → 201 · 409 nama duplikat
### 8.3 `DELETE /categories/{id}` → 200 · 400 `"Kategori default tidak dapat dihapus"` · 409 sudah dipakai

---

## 9. Anggaran & Notifikasi (Anggota 3)

### 9.1 `GET /budgets?month=2026-10`
```json
{ "success": true, "data": { "month": "2026-10", "totalLimit": 2200000.00, "totalSpent": 1707000.00,
    "budgets": [ /* Budget */ ] } }
```

### 9.2 `POST /budgets`
```json
{ "categoryId": 6, "month": "2026-10", "targetAmount": 1200000 }
```
→ 201 `data: Budget` · 400 `"Anggaran hanya untuk kategori pengeluaran"` · 409 `"Anggaran untuk kategori ini pada bulan tersebut sudah ada"`

### 9.3 `PUT /budgets/{id}` → `{ "targetAmount": 1500000 }` → 200 `data: Budget`
### 9.4 `DELETE /budgets/{id}` → 200

### 9.5 `GET /notifications`
```json
{ "success": true, "data": { "unreadCount": 2, "items": [ /* Notification, maks 20 terbaru */ ] } }
```
### 9.6 `POST /notifications/read-all` → 200

---

## 10. Target Tabungan (Anggota 3)

### 10.1 `GET /goals` → 200 `data: [ SavingGoal ]` (semua status, urut: ACTIVE dulu, lalu tenggat terdekat)

### 10.2 `POST /goals`
```json
{ "name": "Laptop Baru", "targetAmount": 8000000, "deadline": "2027-06-30" }
```
→ 201 `data: SavingGoal` · 400 `"Tenggat harus di masa depan"`

### 10.3 `POST /goals/{id}/contribute`
```json
{ "walletId": 2, "amount": 500000 }
```
→ 200 `data: { "goal": SavingGoal, "wallet": Wallet }` · 400 `"Saldo dompet tidak mencukupi"` · 400 `"Nominal melebihi sisa target"` · 400 `"Target sudah tercapai"`

### 10.4 `POST /goals/{id}/cancel` → 200 `data: SavingGoal` (dana dikembalikan ke dompet utama, BR-14)

---

## 11. Laporan & Dashboard (Anggota 4)

### 11.1 `GET /reports/dashboard`
```json
{ "success": true, "data": {
    "totalBalance": 1333000.00, "monthIncome": 5500000.00, "monthExpense": 3582000.00,
    "savingRate": 34.87, "activeBudgets": 4, "overBudgetCount": 1, "activeGoals": 1 } }
```
`savingRate` bernilai `null` jika `monthIncome` = 0. Komponen lain dashboard memakai endpoint yang sudah ada: transaksi terbaru (`GET /transactions?size=5`), target aktif (`GET /goals`, ambil yang `ACTIVE`), tren 6 bulan (`GET /reports/period?type=MONTHLY&from=...&to=...`).

### 11.2 `GET /reports/category?from=2026-10-01&to=2026-10-31`
```json
{ "success": true, "data": {
    "title": "Laporan Pengeluaran per Kategori", "startDate": "2026-10-01", "endDate": "2026-10-31",
    "totalIncome": 5500000.00, "totalExpense": 3582000.00, "netBalance": 1918000.00,
    "topExpenseCategory": "Kos & Sewa",
    "rows": [ { "category": "Kos & Sewa", "total": 1300000.00, "percentage": 36.29 },
              { "category": "Makan & Minum", "total": 1015000.00, "percentage": 28.34 } ] } }
```

### 11.3 `GET /reports/period?type=WEEKLY&from=2026-10-01&to=2026-10-31`
`type`: `DAILY` | `WEEKLY` | `MONTHLY` | `YEARLY`
```json
{ "success": true, "data": {
    "title": "Laporan per Periode", "periodType": "WEEKLY", "startDate": "2026-10-01", "endDate": "2026-10-31",
    "totalIncome": 5500000.00, "totalExpense": 3582000.00, "netBalance": 1918000.00,
    "averageDailyExpense": 115548.39, "comparisonWithPrevious": 18.02,
    "rows": [ { "label": "2026-W40", "income": 5000000.00, "expense": 1437000.00 },
              { "label": "2026-W41", "income": 0.00, "expense": 831000.00 } ] } }
```
Label: DAILY `2026-10-28`, WEEKLY `2026-W44` (ISO week), MONTHLY `2026-10`, YEARLY `2026`. `comparisonWithPrevious` bernilai `null` bila periode sebelumnya kosong.

### 11.4 `GET /reports/export?kind=category&from=2026-10-01&to=2026-10-31`
`kind`: `category` | `period`. Untuk `period` tambahkan `periodType=WEEKLY` (opsional, default `MONTHLY`).
Respons **bukan JSON**:
```
HTTP/1.1 200 OK
Content-Type: text/csv; charset=UTF-8
Content-Disposition: attachment; filename="spendwise_laporan-kategori_2026-10-01_2026-10-31.csv"

﻿Kategori;Total;Persentase
Kos & Sewa;1300000;36,29
Makan & Minum;1015000;28,34
...
Total Pemasukan;5500000;
Total Pengeluaran;3582000;
```
Frontend mengunduh dengan Axios `responseType: "blob"` lalu membuat link unduhan.
