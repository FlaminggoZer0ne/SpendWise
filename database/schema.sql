-- =====================================================================
-- SpendWise — schema.sql
-- MySQL 8.0.16+ / MariaDB 10.6+  ·  charset utf8mb4
-- Jalankan: mysql -u root -p < database/schema.sql
-- PERINGATAN: script ini MENGHAPUS database `spendwise` jika sudah ada.
-- =====================================================================

DROP DATABASE IF EXISTS spendwise;
CREATE DATABASE spendwise CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE spendwise;

-- ---------------------------------------------------------------------
-- users  →  User (abstract), Member, Admin   [single-table inheritance: role]
-- ---------------------------------------------------------------------
CREATE TABLE users (
    user_id        INT             NOT NULL AUTO_INCREMENT,
    username       VARCHAR(30)     NOT NULL,
    password_hash  VARCHAR(100)    NOT NULL,              -- format: salt$sha256hex
    full_name      VARCHAR(100)    NOT NULL,
    email          VARCHAR(100)    NOT NULL,
    role           ENUM('MEMBER','ADMIN') NOT NULL DEFAULT 'MEMBER',
    admin_code     VARCHAR(20)     NULL,                  -- hanya untuk ADMIN
    is_active      TINYINT(1)      NOT NULL DEFAULT 1,
    created_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_users_username (username)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- wallets  →  Wallet
-- ---------------------------------------------------------------------
CREATE TABLE wallets (
    wallet_id      INT             NOT NULL AUTO_INCREMENT,
    owner_id       INT             NOT NULL,
    name           VARCHAR(50)     NOT NULL,
    wallet_type    ENUM('CASH','BANK','E_WALLET') NOT NULL DEFAULT 'CASH',
    balance        DECIMAL(15,2)   NOT NULL DEFAULT 0.00,
    created_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (wallet_id),
    UNIQUE KEY uk_wallets_owner_name (owner_id, name),
    CONSTRAINT fk_wallets_owner FOREIGN KEY (owner_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT chk_wallets_balance CHECK (balance >= 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- categories  →  Category   (owner_id NULL = kategori default milik sistem)
-- ---------------------------------------------------------------------
CREATE TABLE categories (
    category_id    INT             NOT NULL AUTO_INCREMENT,
    name           VARCHAR(40)     NOT NULL,
    type           ENUM('INCOME','EXPENSE') NOT NULL,
    is_default     TINYINT(1)      NOT NULL DEFAULT 0,
    owner_id       INT             NULL,
    PRIMARY KEY (category_id),
    KEY idx_categories_owner_type (owner_id, type),
    CONSTRAINT fk_categories_owner FOREIGN KEY (owner_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT chk_categories_default CHECK ((is_default = 1 AND owner_id IS NULL) OR (is_default = 0 AND owner_id IS NOT NULL))
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- transactions  →  Transaction (abstract), Income, Expense   [single-table inheritance: type]
-- ---------------------------------------------------------------------
CREATE TABLE transactions (
    transaction_id INT             NOT NULL AUTO_INCREMENT,
    user_id        INT             NOT NULL,
    wallet_id      INT             NOT NULL,
    category_id    INT             NOT NULL,
    type           ENUM('INCOME','EXPENSE') NOT NULL,
    amount         DECIMAL(15,2)   NOT NULL,
    trx_date       DATE            NOT NULL,
    description    VARCHAR(255)    NULL,
    source         VARCHAR(100)    NULL,                  -- khusus INCOME
    payment_method VARCHAR(30)     NULL,                  -- khusus EXPENSE
    is_recurring   TINYINT(1)      NOT NULL DEFAULT 0,    -- khusus EXPENSE
    created_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (transaction_id),
    KEY idx_trx_user_date (user_id, trx_date),
    KEY idx_trx_category (category_id),
    KEY idx_trx_wallet (wallet_id),
    CONSTRAINT fk_trx_user     FOREIGN KEY (user_id)     REFERENCES users (user_id)          ON DELETE CASCADE,
    CONSTRAINT fk_trx_wallet   FOREIGN KEY (wallet_id)   REFERENCES wallets (wallet_id)      ON DELETE RESTRICT,
    CONSTRAINT fk_trx_category FOREIGN KEY (category_id) REFERENCES categories (category_id) ON DELETE RESTRICT,
    CONSTRAINT chk_trx_amount CHECK (amount > 0 AND amount <= 1000000000)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- budgets  →  FinancialPlan (abstract) → Budget
-- ---------------------------------------------------------------------
CREATE TABLE budgets (
    budget_id      INT             NOT NULL AUTO_INCREMENT,
    user_id        INT             NOT NULL,
    category_id    INT             NOT NULL,
    name           VARCHAR(100)    NOT NULL,              -- "Anggaran <nama kategori>"
    period         CHAR(7)         NOT NULL,              -- YYYY-MM (YearMonth)
    limit_amount   DECIMAL(15,2)   NOT NULL,              -- FinancialPlan.targetAmount
    spent_amount   DECIMAL(15,2)   NOT NULL DEFAULT 0.00,
    created_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (budget_id),
    UNIQUE KEY uk_budgets_user_cat_period (user_id, category_id, period),
    CONSTRAINT fk_budgets_user     FOREIGN KEY (user_id)     REFERENCES users (user_id)          ON DELETE CASCADE,
    CONSTRAINT fk_budgets_category FOREIGN KEY (category_id) REFERENCES categories (category_id) ON DELETE RESTRICT,
    CONSTRAINT chk_budgets_limit CHECK (limit_amount > 0),
    CONSTRAINT chk_budgets_spent CHECK (spent_amount >= 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- saving_goals  →  FinancialPlan (abstract) → SavingGoal
-- ---------------------------------------------------------------------
CREATE TABLE saving_goals (
    goal_id        INT             NOT NULL AUTO_INCREMENT,
    user_id        INT             NOT NULL,
    name           VARCHAR(100)    NOT NULL,
    target_amount  DECIMAL(15,2)   NOT NULL,
    current_amount DECIMAL(15,2)   NOT NULL DEFAULT 0.00,
    deadline       DATE            NOT NULL,
    status         ENUM('ACTIVE','ACHIEVED','CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    created_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (goal_id),
    KEY idx_goals_user_status (user_id, status),
    CONSTRAINT fk_goals_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT chk_goals_target CHECK (target_amount > 0),
    CONSTRAINT chk_goals_current CHECK (current_amount >= 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- notifications  →  Notification
-- ---------------------------------------------------------------------
CREATE TABLE notifications (
    notification_id INT            NOT NULL AUTO_INCREMENT,
    user_id        INT             NOT NULL,
    message        VARCHAR(255)    NOT NULL,
    level          ENUM('WARNING','DANGER') NOT NULL,
    is_read        TINYINT(1)      NOT NULL DEFAULT 0,
    created_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (notification_id),
    KEY idx_notif_user_read (user_id, is_read),
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;
