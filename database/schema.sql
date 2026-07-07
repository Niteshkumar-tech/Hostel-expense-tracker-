-- ============================================================
--  Hostel Expense Tracker - MySQL Database Schema
--  Run this script in MySQL before launching the Java app
-- ============================================================

CREATE DATABASE IF NOT EXISTS hostel_tracker;
USE hostel_tracker;

-- ---------------------------------------------------------------
-- USER table: stores hostel residents
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS USER (
    user_id   INT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(100) NOT NULL,
    email     VARCHAR(150) UNIQUE NOT NULL,
    room_no   VARCHAR(20)  NOT NULL
);

-- ---------------------------------------------------------------
-- EXPENSE table: records every expense
--   type = 'personal' (only paid_by owes) or 'shared' (split)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS EXPENSE (
    expense_id INT AUTO_INCREMENT PRIMARY KEY,
    title      VARCHAR(200) NOT NULL,
    amount     DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    date       DATE          NOT NULL,
    paid_by    INT           NOT NULL,
    type       ENUM('personal','shared') NOT NULL DEFAULT 'shared',
    FOREIGN KEY (paid_by) REFERENCES USER(user_id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------
-- EXPENSE_SPLIT table: each row = one user's share of an expense
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS EXPENSE_SPLIT (
    split_id     INT AUTO_INCREMENT PRIMARY KEY,
    expense_id   INT           NOT NULL,
    user_id      INT           NOT NULL,
    share_amount DECIMAL(10,2) NOT NULL CHECK (share_amount >= 0),
    FOREIGN KEY (expense_id) REFERENCES EXPENSE(expense_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)    REFERENCES USER(user_id)       ON DELETE CASCADE
);

-- ---------------------------------------------------------------
-- PAYMENT table: settlement / repayment records
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS PAYMENT (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    from_user  INT           NOT NULL,
    to_user    INT           NOT NULL,
    amount     DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    date       DATE          NOT NULL,
    FOREIGN KEY (from_user) REFERENCES USER(user_id) ON DELETE CASCADE,
    FOREIGN KEY (to_user)   REFERENCES USER(user_id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------
-- Handy view: net balance per user
--   positive  => user is owed money
--   negative  => user owes money
-- ---------------------------------------------------------------
CREATE OR REPLACE VIEW USER_BALANCE AS
SELECT
    u.user_id,
    u.name,
    u.room_no,
    COALESCE(paid.total_paid,   0)
    - COALESCE(owed.total_owed,  0)
    + COALESCE(recv.total_recv,  0)
    - COALESCE(sent.total_sent,  0) AS balance
FROM USER u
LEFT JOIN (
    SELECT paid_by AS uid, SUM(amount) AS total_paid
    FROM EXPENSE GROUP BY paid_by
) paid  ON paid.uid  = u.user_id
LEFT JOIN (
    SELECT user_id  AS uid, SUM(share_amount) AS total_owed
    FROM EXPENSE_SPLIT GROUP BY user_id
) owed  ON owed.uid  = u.user_id
LEFT JOIN (
    SELECT to_user   AS uid, SUM(amount) AS total_recv
    FROM PAYMENT GROUP BY to_user
) recv  ON recv.uid  = u.user_id
LEFT JOIN (
    SELECT from_user AS uid, SUM(amount) AS total_sent
    FROM PAYMENT GROUP BY from_user
) sent  ON sent.uid  = u.user_id;

-- ---------------------------------------------------------------
-- Optional: seed data so the app feels alive on first run
-- ---------------------------------------------------------------
INSERT IGNORE INTO USER (name, email, room_no) VALUES
  ('Alice Khan',   'alice@hostel.com',   '101'),
  ('Bob Raza',     'bob@hostel.com',     '102'),
  ('Carol Iqbal',  'carol@hostel.com',   '103');
