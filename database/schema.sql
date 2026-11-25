-- =======================================================
-- SMART LOST-AND-FOUND MANAGEMENT SYSTEM
-- Database Schema Script  (SAFE / IDEMPOTENT VERSION)
--
-- Safety guarantees:
--   * No DROP DATABASE -- never destroys existing data
--   * CREATE DATABASE IF NOT EXISTS -- safe to run again
--   * CREATE TABLE IF NOT EXISTS   -- safe to run again
--   * All indexes defined INLINE inside CREATE TABLE
--     so they are only created once with the table
-- =======================================================

-- Create database only if it does not already exist
CREATE DATABASE IF NOT EXISTS smart_lost_found
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_lost_found;

-- -------------------------------------------------------
-- Table 1: USERS
-- Stores student and administrator accounts.
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS USERS (
    user_id         INT          PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(100) NOT NULL,
    register_number VARCHAR(30)  NOT NULL,
    email           VARCHAR(120) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL,
    account_status  VARCHAR(20)  DEFAULT 'ACTIVE',
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,

    -- Uniqueness constraints (also create unique indexes automatically)
    CONSTRAINT uq_users_register_number UNIQUE (register_number),
    CONSTRAINT uq_users_email           UNIQUE (email),

    -- Value-domain checks
    CONSTRAINT chk_user_role       CHECK (role           IN ('STUDENT', 'ADMIN')),
    CONSTRAINT chk_account_status  CHECK (account_status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),

    -- Performance indexes (inline = created once with the table)
    INDEX idx_users_role   (role)
) ENGINE=InnoDB;

-- -------------------------------------------------------
-- Table 2: LOST_ITEMS
-- Reports of items lost on campus.
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS LOST_ITEMS (
    lost_item_id  INT          PRIMARY KEY AUTO_INCREMENT,
    user_id       INT          NOT NULL,
    item_name     VARCHAR(100) NOT NULL,
    category      VARCHAR(50)  NOT NULL,
    description   TEXT,
    lost_location VARCHAR(150) NOT NULL,
    lost_date     DATE         NOT NULL,
    status        VARCHAR(30)  DEFAULT 'OPEN',
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_lost_items_user FOREIGN KEY (user_id)
        REFERENCES USERS(user_id) ON DELETE CASCADE,

    CONSTRAINT chk_lost_item_status CHECK (status IN ('OPEN', 'CLAIMED', 'CLOSED')),

    INDEX idx_lost_items_category (category),
    INDEX idx_lost_items_location (lost_location),
    INDEX idx_lost_items_status   (status),
    INDEX idx_lost_items_date     (lost_date),
    INDEX idx_lost_items_user     (user_id)
) ENGINE=InnoDB;

-- -------------------------------------------------------
-- Table 3: FOUND_ITEMS
-- Reports of items found on campus.
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS FOUND_ITEMS (
    found_item_id  INT          PRIMARY KEY AUTO_INCREMENT,
    user_id        INT          NOT NULL,
    item_name      VARCHAR(100) NOT NULL,
    category       VARCHAR(50)  NOT NULL,
    description    TEXT,
    found_location VARCHAR(150) NOT NULL,
    found_date     DATE         NOT NULL,
    status         VARCHAR(30)  DEFAULT 'AVAILABLE',
    created_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_found_items_user FOREIGN KEY (user_id)
        REFERENCES USERS(user_id) ON DELETE CASCADE,

    CONSTRAINT chk_found_item_status CHECK (status IN ('AVAILABLE', 'CLAIMED', 'CLOSED')),

    INDEX idx_found_items_category (category),
    INDEX idx_found_items_location (found_location),
    INDEX idx_found_items_status   (status),
    INDEX idx_found_items_date     (found_date),
    INDEX idx_found_items_user     (user_id)
) ENGINE=InnoDB;

-- -------------------------------------------------------
-- Table 4: CLAIMS
-- Ownership claim requests submitted by students.
--
-- Business rule: each claim must reference EXACTLY ONE
-- item -- either a lost item OR a found item, never both.
-- The CHECK constraint enforces this at the database level.
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS CLAIMS (
    claim_id          INT       PRIMARY KEY AUTO_INCREMENT,
    user_id           INT       NOT NULL,
    lost_item_id      INT       NULL,
    found_item_id     INT       NULL,
    claim_description TEXT      NOT NULL,
    claim_date        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    claim_status      VARCHAR(30) DEFAULT 'PENDING',
    admin_remarks     TEXT,

    CONSTRAINT fk_claims_user       FOREIGN KEY (user_id)
        REFERENCES USERS(user_id)       ON DELETE CASCADE,
    CONSTRAINT fk_claims_lost_item  FOREIGN KEY (lost_item_id)
        REFERENCES LOST_ITEMS(lost_item_id)  ON DELETE CASCADE,
    CONSTRAINT fk_claims_found_item FOREIGN KEY (found_item_id)
        REFERENCES FOUND_ITEMS(found_item_id) ON DELETE CASCADE,

    -- Exactly one item ID must be non-NULL
    CONSTRAINT chk_claim_single_item CHECK (
        (lost_item_id IS NOT NULL AND found_item_id IS NULL) OR
        (lost_item_id IS NULL     AND found_item_id IS NOT NULL)
    ),

    CONSTRAINT chk_claim_status CHECK (claim_status IN ('PENDING', 'APPROVED', 'REJECTED')),

    INDEX idx_claims_status   (claim_status),
    INDEX idx_claims_user     (user_id),
    INDEX idx_claims_lost     (lost_item_id),
    INDEX idx_claims_found    (found_item_id)
) ENGINE=InnoDB;

-- -------------------------------------------------------
-- Table 5: ADMIN_ACTIVITY_LOG
-- Tracks all administrative actions for audit purposes.
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS ADMIN_ACTIVITY_LOG (
    log_id             INT           PRIMARY KEY AUTO_INCREMENT,
    admin_id           INT           NOT NULL,
    action_type        VARCHAR(60)   NOT NULL,
    target_description VARCHAR(255)  NOT NULL,
    details            TEXT,
    performed_at       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_activity_log_admin FOREIGN KEY (admin_id)
        REFERENCES USERS(user_id) ON DELETE CASCADE,
    INDEX idx_activity_log_admin    (admin_id),
    INDEX idx_activity_log_type     (action_type),
    INDEX idx_activity_log_time     (performed_at)
) ENGINE=InnoDB;
