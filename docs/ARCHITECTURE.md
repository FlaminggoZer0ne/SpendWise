# Arsitektur SpendWise

## 1. Gambaran Besar

```
┌──────────────────────┐      HTTP + JSON       ┌────────────────────────────────────────────┐      JDBC      ┌─────────┐
│  Frontend (React)    │  ───────────────────▶  │  Backend (Spring Boot)                     │  ───────────▶  │  MySQL  │
│  localhost:5173      │  ◀───────────────────  │  localhost:8080                            │  ◀───────────  │         │
│  Pages, Components   │   cookie JSESSIONID    │  Controller → Service → Model → DAO        │                │         │
└──────────────────────┘                        └────────────────────────────────────────────┘                └─────────┘
```

- Frontend **hanya** menampilkan data dan memanggil API. Tidak ada aturan bisnis di frontend selain validasi form ringan (untuk kenyamanan pengguna).
- Seluruh logika bisnis dan desain OOP ada di backend Java. Backend juga yang dinilai untuk konsep OOP dan SonarQube.
- Saat development, Vite meneruskan `/api/**` ke port 8080 (proxy), sehingga browser melihat frontend dan backend sebagai satu origin. Cookie session pun ikut terkirim tanpa konfigurasi CORS.

---

## 2. Lapisan Backend

| Lapisan | Tanggung jawab | Boleh memanggil | Dilarang |
|---|---|---|---|
| **Controller** (`@RestController`) | Terima request, ambil user dari session, panggil service, bungkus respons JSON | Service, DTO, `BaseController` | Akses DAO langsung, logika bisnis, SQL |
| **Service** (`@Service`) | Aturan bisnis, validasi, koordinasi beberapa DAO, `@Transactional` | DAO, Model, Service lain (sesuai `CLASS_DESIGN.md`) | `HttpSession`, `ResponseEntity`, SQL |
| **Model** | State dan perilaku domain (`withdraw()`, `applyToWallet()`, `getProgress()`, `generate()`) | Model lain | Akses DB, anotasi Spring |
| **DAO** (`@Repository`) | SQL via `JdbcTemplate`, mapping `ResultSet` → Model | `JdbcTemplate` | Aturan bisnis |

Alur contoh — **menambah pengeluaran**:

```
TransactionPage (React)
  └─ POST /api/transactions {type:"EXPENSE", amount:50000, ...}
       └─ TransactionController.create(req, session)
            ├─ requireLogin(session)                       ← BaseController
            └─ TransactionService.addTransaction(req, userId)   @Transactional
                 ├─ buildTransaction(req) → new Expense(...)
                 ├─ expense.validate()                       ← Validatable
                 ├─ expense.applyToWallet()  → wallet.withdraw()   ← polymorphism
                 ├─ walletDAO.update(wallet)
                 ├─ transactionDAO.save(expense)
                 └─ budgetService.checkBudget(expense)
                      ├─ budget.addSpending(amount); budgetDAO.update(budget)
                      └─ notifyListeners(budget) → NotificationService.onBudgetWarning(...)   ← Observer
       ◀─ 201 {"success":true,"data":{...transaksi...}}
```

---

## 3. Struktur Package Backend

Diatur **per lapisan** (sesuai class diagram). Nama package dan kelas **harus sama** dengan `CLASS_DESIGN.md`.

```
backend/src/main/java/com/spendwise/
├── SpendWiseApplication.java          ← main class (@SpringBootApplication)
├── core/                              ← BERSAMA (minggu 1)
│   ├── PasswordHasher.java            ← utilitas hash password (salt$sha256hex)
│   ├── BaseController.java            (abstract)
│   ├── Repository.java                (interface, generic <T>)
│   ├── Validatable.java               (interface)
│   ├── ApiResponse.java               ← pembungkus JSON {success, message, data, errors}
│   ├── BusinessException.java         ← error aturan bisnis → 400/404/409
│   ├── UnauthorizedException.java     ← 401
│   ├── ForbiddenException.java        ← 403
│   └── GlobalExceptionHandler.java    (@RestControllerAdvice)
├── model/
│   ├── User.java, Member.java, Admin.java, Wallet.java, WalletType.java                 ← Anggota 1
│   ├── Transaction.java, Income.java, Expense.java, Category.java, TransactionType.java ← Anggota 2
│   ├── FinancialPlan.java, Budget.java, SavingGoal.java, GoalStatus.java,
│   │   Notification.java, BudgetAlertListener.java                                     ← Anggota 3
│   └── Exportable.java, Report.java, CategoryReport.java, PeriodReport.java,
│       PeriodType.java, DashboardSummary.java                                          ← Anggota 4
├── dao/        UserDAO, WalletDAO | TransactionDAO, CategoryDAO | BudgetDAO, NotificationDAO, SavingGoalDAO
├── service/    AuthService, WalletService, AdminService | TransactionService, CategoryService |
│               BudgetService, NotificationService, SavingGoalService | ReportService
├── controller/ AuthController, WalletController, AdminController | TransactionController, CategoryController |
│               BudgetController, NotificationController, SavingGoalController | ReportController
├── dto/        RegisterRequest, LoginRequest, PasswordRequest, ProfileRequest, TransferRequest,
│               TransactionRequest, TransactionFilter, ContributionRequest, PageResult<T>
└── config/     ListenerConfig.java  ← mendaftarkan NotificationService ke BudgetService (Observer)
```

> **Kelas pendukung** (`ApiResponse`, `PasswordHasher`, exception, DTO, config) tidak ada di class diagram karena bukan bagian dari desain domain. Tetap dibuat agar kode rapi; cantumkan di laporan akhir sebagai kelas pendukung.

---

## 4. Autentikasi & Otorisasi

- Login berhasil → `session.setAttribute(BaseController.SESSION_USER, user)` (objek `Member` atau `Admin`).
- `BaseController` menyediakan:
  - `getCurrentUser(session)` → `User` atau `null`
  - `requireLogin(session)` → `User`, lempar `UnauthorizedException` (401) jika belum login
  - `requireRole(session, "ADMIN")` → `User`, lempar `ForbiddenException` (403) jika role tidak cocok
- **Kepemilikan data** dicek di service: setiap query DAO untuk data member menyertakan `user_id`/`owner_id`. Jika data tidak ditemukan untuk user tersebut → 404 (jangan bocorkan bahwa datanya ada milik orang lain).
- Objek `User` di session tidak boleh diserialisasi bersama `passwordHash`. Respons `/api/auth/me` memakai map/DTO tanpa field itu (atau `@JsonIgnore` pada getter `passwordHash`).

---

## 5. Transaksi Database

Method service yang mengubah lebih dari satu tabel **wajib** `@Transactional`:

| Method | Tabel yang berubah |
|---|---|
| `AuthService.register` | users, wallets |
| `WalletService.transfer` | wallets (2 baris) |
| `TransactionService.addTransaction / editTransaction / deleteTransaction` | transactions, wallets, budgets, notifications |
| `SavingGoalService.contribute / cancelGoal` | saving_goals, wallets |

---

## 6. Pola Desain yang Dipakai

| Pola | Di mana | Keterangan |
|---|---|---|
| Layered / MVC | Controller–Service–DAO | Pemisahan tanggung jawab |
| Template / Inheritance | `BaseController`, `User`, `Transaction`, `FinancialPlan`, `Report` | Perilaku bersama di superclass, detail di subclass |
| Strategy via polymorphism | `Transaction.applyToWallet()` | `Income` menambah, `Expense` mengurangi saldo |
| Repository / DAO | `Repository<T>` + 7 DAO | Kontrak CRUD generik |
| Observer | `BudgetService` ↔ `BudgetAlertListener` | Notifikasi anggaran tanpa coupling langsung |
| Singleton + Dependency Injection | Semua `@Service`, `@Repository`, `@RestController` | Satu instance dikelola Spring, di-inject lewat constructor |

**Aturan injeksi:** gunakan *constructor injection* dengan field `private final` (bukan `@Autowired` di field). SonarQube menandai field injection sebagai code smell.

```java
@Service
public class WalletService {
    private final WalletDAO walletDAO;
    private final TransactionDAO transactionDAO;

    public WalletService(WalletDAO walletDAO, TransactionDAO transactionDAO) {
        this.walletDAO = walletDAO;
        this.transactionDAO = transactionDAO;
    }
}
```

---

## 7. Penanganan Error

- Service melempar `BusinessException(status, message)` untuk pelanggaran aturan (saldo kurang, duplikat, dll.).
- `GlobalExceptionHandler` mengubah semua exception menjadi format JSON standar (lihat `API.md` §2):

| Exception | HTTP |
|---|---|
| `BusinessException` | sesuai field `status` (400/404/409) |
| `UnauthorizedException` | 401 |
| `ForbiddenException` | 403 |
| `MethodArgumentNotValidException` / `HttpMessageNotReadableException` | 400 |
| `Exception` lain | 500 dengan pesan umum "Terjadi kesalahan pada server" (detail hanya di log) |

---

## 8. Struktur Frontend (ringkas)

Detail di `FRONTEND.md`.

```
frontend/src/
├── api/          client.js (Axios, baseURL "/api", withCredentials) + satu file per modul
├── context/      AuthContext.jsx (user aktif, login/logout)
├── components/   Navbar, Sidebar, ProtectedRoute, NotificationBell, Modal, Table, ...
├── pages/        auth/, dashboard/, wallets/, transactions/, categories/, budgets/, goals/, reports/, admin/, profile/
└── utils/        format.js (formatRupiah, formatTanggal)
```
