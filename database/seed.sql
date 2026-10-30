-- =====================================================================
-- SpendWise — seed.sql  (jalankan SETELAH schema.sql)
-- Isi: 3 akun demo, 14 kategori default, data contoh member "budi"
--      (Sep–Okt 2026), anggaran Oktober 2026, target tabungan, notifikasi.
-- Saldo dompet & spent_amount anggaran SUDAH konsisten dengan transaksi.
--
-- Akun demo:
--   admin / admin12345   (ADMIN)
--   budi  / member12345  (MEMBER, berisi data contoh)
--   sari  / member12345  (MEMBER, akun kosong)
-- Hash password: salt$sha256hex  dengan sha256(salt + password)
-- =====================================================================

USE spendwise;
SET NAMES utf8mb4;
SET time_zone = '+07:00';

-- users
INSERT INTO users (user_id, username, password_hash, full_name, email, role, admin_code, is_active, created_at) VALUES
  (1, 'admin', 'a1b2c3d4e5f60718$92cc570e94c7b0c85456ceff697f5daf3680a3f27b56908bf91495a5e070b379', 'Administrator SpendWise', 'admin@spendwise.local', 'ADMIN', 'ADM-001', 1, '2026-09-01 08:00:00'),
  (2, 'budi', '9f8e7d6c5b4a3921$2779f47369391e2721f29a81267c58c0dbcd98898a1215d9617df4a8f6efe4db', 'Budi Santoso', 'budi@mail.com', 'MEMBER', NULL, 1, '2026-09-01 09:15:00'),
  (3, 'sari', '0123456789abcdef$ffd2c4c29f0a355fc922cc00299f1871e4d296532d47df4c092333fa723b1069', 'Sari Wulandari', 'sari@mail.com', 'MEMBER', NULL, 1, '2026-10-20 19:30:00');

-- kategori default (owner_id NULL) + kategori kustom milik budi
INSERT INTO categories (category_id, name, type, is_default, owner_id) VALUES
  (1, 'Gaji', 'INCOME', 1, NULL),
  (2, 'Uang Saku', 'INCOME', 1, NULL),
  (3, 'Bonus', 'INCOME', 1, NULL),
  (4, 'Investasi', 'INCOME', 1, NULL),
  (5, 'Pemasukan Lain', 'INCOME', 1, NULL),
  (6, 'Makan & Minum', 'EXPENSE', 1, NULL),
  (7, 'Transportasi', 'EXPENSE', 1, NULL),
  (8, 'Belanja', 'EXPENSE', 1, NULL),
  (9, 'Tagihan & Utilitas', 'EXPENSE', 1, NULL),
  (10, 'Pendidikan', 'EXPENSE', 1, NULL),
  (11, 'Hiburan', 'EXPENSE', 1, NULL),
  (12, 'Kesehatan', 'EXPENSE', 1, NULL),
  (13, 'Kos & Sewa', 'EXPENSE', 1, NULL),
  (14, 'Pengeluaran Lain', 'EXPENSE', 1, NULL),
  (15, 'Ngopi', 'EXPENSE', 0, 2),
  (16, 'Freelance', 'INCOME', 0, 2);

-- dompet (saldo = saldo awal + pemasukan − pengeluaran ± transfer − setoran tabungan)
INSERT INTO wallets (wallet_id, owner_id, name, wallet_type, balance, created_at) VALUES
  (1, 2, 'Tunai', 'CASH', 150000.00, '2026-09-01 09:20:00'),
  (2, 2, 'BCA', 'BANK', 990000.00, '2026-09-01 09:20:00'),
  (3, 2, 'GoPay', 'E_WALLET', 193000.00, '2026-09-01 09:20:00'),
  (4, 3, 'Tunai', 'CASH', 0.00, '2026-10-20 19:30:00');

-- transaksi member budi (user_id 2)
INSERT INTO transactions (transaction_id, user_id, wallet_id, category_id, type, amount, trx_date, description, source, payment_method, is_recurring, created_at) VALUES
  (1, 2, 2, 1, 'INCOME', 5000000.00, '2026-09-01', 'Gaji September', 'PT Maju Jaya', NULL, 0, '2026-09-01 12:00:00'),
  (2, 2, 2, 13, 'EXPENSE', 1300000.00, '2026-09-02', 'Bayar kos September', NULL, 'Transfer', 1, '2026-09-02 12:00:00'),
  (3, 2, 1, 6, 'EXPENSE', 35000.00, '2026-09-03', 'Sarapan nasi uduk', NULL, 'Tunai', 0, '2026-09-03 12:00:00'),
  (4, 2, 3, 7, 'EXPENSE', 60000.00, '2026-09-05', 'Ojek online minggu ini', NULL, 'GoPay', 0, '2026-09-05 12:00:00'),
  (5, 2, 2, 8, 'EXPENSE', 275000.00, '2026-09-07', 'Belanja bulanan', NULL, 'Debit', 0, '2026-09-07 12:00:00'),
  (6, 2, 3, 15, 'EXPENSE', 48000.00, '2026-09-10', 'Kopi susu x2', NULL, 'GoPay', 0, '2026-09-10 12:00:00'),
  (7, 2, 1, 6, 'EXPENSE', 320000.00, '2026-09-12', 'Makan siang seminggu', NULL, 'Tunai', 0, '2026-09-12 12:00:00'),
  (8, 2, 2, 9, 'EXPENSE', 180000.00, '2026-09-15', 'Listrik token', NULL, 'Transfer', 1, '2026-09-15 12:00:00'),
  (9, 2, 3, 11, 'EXPENSE', 100000.00, '2026-09-18', 'Nonton bioskop', NULL, 'GoPay', 0, '2026-09-18 12:00:00'),
  (10, 2, 2, 16, 'INCOME', 750000.00, '2026-09-20', 'Desain logo klien', 'Klien UMKM', NULL, 0, '2026-09-20 12:00:00'),
  (11, 2, 1, 6, 'EXPENSE', 410000.00, '2026-09-22', 'Makan siang & malam', NULL, 'Tunai', 0, '2026-09-22 12:00:00'),
  (12, 2, 3, 7, 'EXPENSE', 85000.00, '2026-09-25', 'Ojek online', NULL, 'GoPay', 0, '2026-09-25 12:00:00'),
  (13, 2, 2, 10, 'EXPENSE', 150000.00, '2026-09-28', 'Buku kuliah', NULL, 'Debit', 0, '2026-09-28 12:00:00'),
  (14, 2, 3, 15, 'EXPENSE', 72000.00, '2026-09-30', 'Ngopi sambil nugas', NULL, 'GoPay', 0, '2026-09-30 12:00:00'),
  (15, 2, 2, 1, 'INCOME', 5000000.00, '2026-10-01', 'Gaji Oktober', 'PT Maju Jaya', NULL, 0, '2026-10-01 12:00:00'),
  (16, 2, 2, 13, 'EXPENSE', 1300000.00, '2026-10-01', 'Bayar kos Oktober', NULL, 'Transfer', 1, '2026-10-01 12:00:00'),
  (17, 2, 1, 6, 'EXPENSE', 42000.00, '2026-10-02', 'Sarapan & kopi', NULL, 'Tunai', 0, '2026-10-02 12:00:00'),
  (18, 2, 3, 7, 'EXPENSE', 95000.00, '2026-10-04', 'Ojek online', NULL, 'GoPay', 0, '2026-10-04 12:00:00'),
  (19, 2, 2, 8, 'EXPENSE', 310000.00, '2026-10-05', 'Belanja bulanan', NULL, 'Debit', 0, '2026-10-05 12:00:00'),
  (20, 2, 1, 6, 'EXPENSE', 285000.00, '2026-10-06', 'Makan siang seminggu', NULL, 'Tunai', 0, '2026-10-06 12:00:00'),
  (21, 2, 3, 15, 'EXPENSE', 56000.00, '2026-10-08', 'Kopi susu x2', NULL, 'GoPay', 0, '2026-10-08 12:00:00'),
  (22, 2, 2, 9, 'EXPENSE', 180000.00, '2026-10-10', 'Listrik token', NULL, 'Transfer', 1, '2026-10-10 12:00:00'),
  (23, 2, 3, 7, 'EXPENSE', 120000.00, '2026-10-12', 'Ojek online', NULL, 'GoPay', 0, '2026-10-12 12:00:00'),
  (24, 2, 1, 6, 'EXPENSE', 260000.00, '2026-10-13', 'Makan siang', NULL, 'Tunai', 0, '2026-10-13 12:00:00'),
  (25, 2, 3, 15, 'EXPENSE', 64000.00, '2026-10-15', 'Ngopi bareng teman', NULL, 'GoPay', 0, '2026-10-15 12:00:00'),
  (26, 2, 3, 11, 'EXPENSE', 150000.00, '2026-10-17', 'Langganan musik & nonton', NULL, 'GoPay', 0, '2026-10-17 12:00:00'),
  (27, 2, 2, 16, 'INCOME', 500000.00, '2026-10-18', 'Edit video klien', 'Klien UMKM', NULL, 0, '2026-10-18 12:00:00'),
  (28, 2, 1, 6, 'EXPENSE', 248000.00, '2026-10-20', 'Makan siang & malam', NULL, 'Tunai', 0, '2026-10-20 12:00:00'),
  (29, 2, 3, 15, 'EXPENSE', 52000.00, '2026-10-21', 'Kopi susu', NULL, 'GoPay', 0, '2026-10-21 12:00:00'),
  (30, 2, 3, 7, 'EXPENSE', 110000.00, '2026-10-23', 'Ojek online', NULL, 'GoPay', 0, '2026-10-23 12:00:00'),
  (31, 2, 2, 12, 'EXPENSE', 85000.00, '2026-10-25', 'Obat & vitamin', NULL, 'Debit', 0, '2026-10-25 12:00:00'),
  (32, 2, 3, 15, 'EXPENSE', 45000.00, '2026-10-27', 'Ngopi sore', NULL, 'GoPay', 0, '2026-10-27 12:00:00'),
  (33, 2, 2, 6, 'EXPENSE', 180000.00, '2026-10-28', 'Makan siang kantor', NULL, 'QRIS', 0, '2026-10-28 12:00:00');

-- anggaran Oktober 2026 (spent_amount = total pengeluaran kategori tsb di Oktober 2026)
INSERT INTO budgets (budget_id, user_id, category_id, name, period, limit_amount, spent_amount, created_at) VALUES
  (1, 2, 6, 'Anggaran Makan & Minum', '2026-10', 1200000.00, 1015000.00, '2026-10-01 07:00:00'),
  (2, 2, 7, 'Anggaran Transportasi', '2026-10', 500000.00, 325000.00, '2026-10-01 07:00:00'),
  (3, 2, 15, 'Anggaran Ngopi', '2026-10', 200000.00, 217000.00, '2026-10-01 07:00:00'),
  (4, 2, 11, 'Anggaran Hiburan', '2026-10', 300000.00, 150000.00, '2026-10-01 07:00:00');

-- target tabungan
INSERT INTO saving_goals (goal_id, user_id, name, target_amount, current_amount, deadline, status, created_at) VALUES
  (1, 2, 'Laptop Baru', 8000000.00, 2500000.00, '2027-06-30', 'ACTIVE', '2026-09-05 10:00:00'),
  (2, 2, 'Dana Darurat', 3000000.00, 3000000.00, '2026-12-31', 'ACHIEVED', '2026-09-02 20:00:00');

-- notifikasi anggaran (dihasilkan sesuai aturan ambang 80%/100%)
INSERT INTO notifications (notification_id, user_id, message, level, is_read, created_at) VALUES
  (1, 2, 'Anggaran Ngopi Oktober 2026 sudah terpakai 86%', 'WARNING', 1, '2026-10-21 12:00:01'),
  (2, 2, 'Anggaran Ngopi Oktober 2026 terlampaui (Rp 217.000 dari Rp 200.000)', 'DANGER', 0, '2026-10-27 12:00:01'),
  (3, 2, 'Anggaran Makan & Minum Oktober 2026 sudah terpakai 85%', 'WARNING', 0, '2026-10-28 12:00:01');

-- set ulang AUTO_INCREMENT agar data baru tidak bentrok dengan ID di atas
ALTER TABLE users AUTO_INCREMENT = 100;
ALTER TABLE categories AUTO_INCREMENT = 100;
ALTER TABLE wallets AUTO_INCREMENT = 100;
ALTER TABLE transactions AUTO_INCREMENT = 1000;
ALTER TABLE budgets AUTO_INCREMENT = 100;
ALTER TABLE saving_goals AUTO_INCREMENT = 100;
ALTER TABLE notifications AUTO_INCREMENT = 100;
