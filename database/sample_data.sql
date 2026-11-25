-- =======================================================
-- SMART LOST-AND-FOUND MANAGEMENT SYSTEM
-- Sample Test Data Script  (SAFE / IDEMPOTENT VERSION)
--
-- Safety guarantees:
--   * NO DROP, TRUNCATE, or DELETE statements
--   * INSERT IGNORE skips a row if its primary key
--     already exists -- existing data is NEVER modified
--   * Safe to run multiple times without side effects
--   * SET FOREIGN_KEY_CHECKS removed (not needed with
--     INSERT IGNORE + correct insertion order)
--
-- Test accounts:
--   Admin   : admin@college.edu        / Admin@123
--   Student : rahul.sharma@college.edu / Student@123
--   Student : priya.patel@college.edu  / Student@123
--   Student : ananya.singh@college.edu / Student@123
-- =======================================================

USE smart_lost_found;

-- -------------------------------------------------------
-- 1. USERS
-- Passwords are PBKDF2WithHmacSHA256 (65536 iterations).
-- INSERT IGNORE skips rows whose user_id already exists.
-- -------------------------------------------------------
INSERT IGNORE INTO USERS
    (user_id, name, register_number, email, password_hash, role, account_status)
VALUES
-- Administrator account
(1,
 'System Administrator',
 'ADMIN-001',
 'admin@college.edu',
 '65536:5ed4948cadb4918c613e559344873162:fb0769980b30cda4dd3fd653b07f2e963d1671d93165d0e7239dc3cb4c7a25bf',
 'ADMIN', 'ACTIVE'),

-- Student: Rahul Sharma
(2,
 'Rahul Sharma',
 '2023BCSE0101',
 'rahul.sharma@college.edu',
 '65536:3594cdafe712f7fb3d3c8a25371df8ec:3f929406dae5f19a0787588b5bd22d9b1c4679917769e16184860f0314ca5b4b',
 'STUDENT', 'ACTIVE'),

-- Student: Priya Patel
(3,
 'Priya Patel',
 '2023BECE0205',
 'priya.patel@college.edu',
 '65536:3594cdafe712f7fb3d3c8a25371df8ec:3f929406dae5f19a0787588b5bd22d9b1c4679917769e16184860f0314ca5b4b',
 'STUDENT', 'ACTIVE'),

-- Student: Ananya Singh
(4,
 'Ananya Singh',
 '2023BIT0312',
 'ananya.singh@college.edu',
 '65536:3594cdafe712f7fb3d3c8a25371df8ec:3f929406dae5f19a0787588b5bd22d9b1c4679917769e16184860f0314ca5b4b',
 'STUDENT', 'ACTIVE');

-- -------------------------------------------------------
-- 2. LOST ITEMS
-- -------------------------------------------------------
INSERT IGNORE INTO LOST_ITEMS
    (lost_item_id, user_id, item_name, category, description,
     lost_location, lost_date, status)
VALUES
(1, 2,
 'Brown Leather Wallet', 'Wallet',
 'Tommy Hilfiger brown leather bi-fold wallet containing student ID card and Metro travel pass.',
 'Main Campus Cafeteria, Table 14', '2026-11-01', 'OPEN'),

(2, 3,
 'Samsung Galaxy Buds 2', 'Electronics',
 'White charging case with purple silicone protective cover. Lost during evening study session.',
 'Central Library 2nd Floor Reading Room', '2026-11-02', 'OPEN'),

(3, 4,
 'Database System Concepts (Korth)', 'Books',
 'Hardcover 7th Edition textbook with yellow sticky notes on chapters 6 and 14.',
 'Computer Science Lab 3', '2026-10-30', 'CLAIMED'),

(4, 2,
 'Silver Keychain with Bike Key', 'Keys',
 'Honda bike key attached to a metallic Avengers keychain and a tiny USB flash drive.',
 'Student Vehicle Parking Lot B', '2026-11-03', 'OPEN');

-- -------------------------------------------------------
-- 3. FOUND ITEMS
-- -------------------------------------------------------
INSERT IGNORE INTO FOUND_ITEMS
    (found_item_id, user_id, item_name, category, description,
     found_location, found_date, status)
VALUES
(1, 3,
 'Brown Wallet with Cards', 'Wallet',
 'Found a gents brown wallet near the cafeteria cash counter during lunch break.',
 'Main Campus Cafeteria', '2026-11-01', 'AVAILABLE'),

(2, 4,
 'Wireless Earbuds in Purple Cover', 'Electronics',
 'Found a wireless earbud case on desk 42 in the quiet reading zone.',
 'Central Library 2nd Floor', '2026-11-02', 'AVAILABLE'),

(3, 2,
 'College ID Card', 'ID Card',
 'Department of Mechanical Engineering identity card found on a playground bench.',
 'Sports Complex Ground', '2026-11-03', 'AVAILABLE'),

(4, 3,
 'Database Management Textbook', 'Books',
 'Found on the laboratory bench after a practical session ended.',
 'Computer Science Lab 3', '2026-10-30', 'CLAIMED');

-- -------------------------------------------------------
-- 4. CLAIMS
-- -------------------------------------------------------
INSERT IGNORE INTO CLAIMS
    (claim_id, user_id, lost_item_id, found_item_id,
     claim_description, claim_date, claim_status, admin_remarks)
VALUES
-- Pending: Rahul claiming found wallet
(1, 2, NULL, 1,
 'The wallet contains my Student ID card (Rahul Sharma, 2023BCSE0101) and a blue Bengaluru Namma Metro card. There is also an emergency note in the hidden inner compartment.',
 '2026-11-02 10:15:00', 'PENDING', NULL),

-- Approved: Ananya claiming found Korth textbook
(2, 4, NULL, 4,
 'My name "Ananya S." is written inside the front cover in blue gel pen, and there are sticky notes on the ER diagrams chapter.',
 '2026-10-31 14:30:00', 'APPROVED',
 'Verified student name written on page 1 and sticky-note annotations. Handed over to student.'),

-- Rejected: Invalid third-party claim for ID card
(3, 3, NULL, 3,
 'I think this ID card belongs to my cousin who attends this college.',
 '2026-11-03 16:45:00', 'REJECTED',
 'Claims for student identification must be submitted directly by the rightful cardholder.');

-- -------------------------------------------------------
-- 5. Reserve auto_increment counters above sample IDs
--    so new real records start at ID 10 and never
--    collide with the fixed sample data above.
--    ALTER TABLE ... AUTO_INCREMENT only raises the value;
--    it never lowers it, so this is safe to run again.
-- -------------------------------------------------------
ALTER TABLE USERS        AUTO_INCREMENT = 10;
ALTER TABLE LOST_ITEMS   AUTO_INCREMENT = 10;
ALTER TABLE FOUND_ITEMS  AUTO_INCREMENT = 10;
ALTER TABLE CLAIMS       AUTO_INCREMENT = 10;
