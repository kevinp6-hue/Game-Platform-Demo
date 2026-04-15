-- 1. Cleanup (Optional: for testing)
DROP TABLE IF EXISTS library, purchases, user_roles, roles, game_genres, genres, games, addresses, user_emails, users, developers CASCADE;

-- 2. Core Entities
CREATE TABLE users (
    user_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    birth_date DATE,
    password_hash TEXT NOT NULL,
    date_joined TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMPTZ
);

CREATE TABLE developers (
    developer_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    dev_name TEXT NOT NULL,
    country VARCHAR(50),
    owner_user_id INT REFERENCES users(user_id)
);

-- 3. User Sub-Tables
CREATE TABLE user_emails (
    email_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id INT REFERENCES users(user_id) ON DELETE CASCADE,
    email TEXT NOT NULL UNIQUE,
    is_primary BOOLEAN DEFAULT FALSE
);

-- At most one primary email per user
CREATE UNIQUE INDEX ux_user_emails_user_primary
ON user_emails (user_id)
WHERE is_primary = TRUE;

CREATE TABLE addresses (
    address_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id INT REFERENCES users(user_id) ON DELETE CASCADE,
    street TEXT,
    city TEXT,
    state TEXT,
    zip_code VARCHAR(20),
    country TEXT
);

-- 4. Game Catalog
CREATE TABLE genres (
    genre_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    genre_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE games (
    game_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    developer_id INT REFERENCES developers(developer_id),
    title TEXT NOT NULL,
    release_date DATE,
    size_gb NUMERIC(10, 2),
    current_price NUMERIC(10, 2) CHECK (current_price >= 0)
);

CREATE TABLE game_genres (
    game_id INT REFERENCES games(game_id) ON DELETE CASCADE,
    genre_id INT REFERENCES genres(genre_id) ON DELETE CASCADE,
    PRIMARY KEY (game_id, genre_id)
);

-- 5. Transactions & Ownership
CREATE TABLE purchases (
    purchase_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    game_id INT REFERENCES games(game_id),
    user_id INT REFERENCES users(user_id),
    price_paid NUMERIC(10, 2) NOT NULL,
    purchase_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_refunded BOOLEAN DEFAULT FALSE,
    refunded_at TIMESTAMPTZ
);

CREATE TABLE library (
    user_id INT REFERENCES users(user_id) ON DELETE CASCADE,
    game_id INT REFERENCES games(game_id) ON DELETE CASCADE,
    acquisition_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    total_playtime_minutes INT DEFAULT 0,
    last_played TIMESTAMPTZ,
    is_installed BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (user_id, game_id)
);

-- Role Definition Table
CREATE TABLE roles (
    role_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE -- 'admin', 'developer', 'moderator', 'player'
);

-- User-Role Mapping Table
CREATE TABLE user_roles (
    user_id INT REFERENCES users(user_id) ON DELETE CASCADE,
    role_id INT REFERENCES roles(role_id) ON DELETE CASCADE,
    assigned_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);

-- Seed basic roles
INSERT INTO roles (role_name) VALUES ('admin'), ('developer'), ('moderator'), ('player');

-- Function: Manage Library Entry based on Purchase/Refund
CREATE OR REPLACE FUNCTION fn_manage_library_access()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO library (user_id, game_id, acquisition_date)
        VALUES (NEW.user_id, NEW.game_id, NEW.purchase_date)
        ON CONFLICT (user_id, game_id) DO NOTHING;
    
    ELSIF (TG_OP = 'UPDATE' AND NEW.is_refunded = TRUE AND OLD.is_refunded = FALSE) THEN
        DELETE FROM library WHERE user_id = NEW.user_id AND game_id = NEW.game_id;
        NEW.refunded_at = CURRENT_TIMESTAMP;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function: Prevent Duplicate Active Purchases
CREATE OR REPLACE FUNCTION fn_prevent_duplicate_purchase()
RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM purchases 
        WHERE user_id = NEW.user_id AND game_id = NEW.game_id AND is_refunded = FALSE
    ) THEN
        RAISE EXCEPTION 'User already owns this game.';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function: Auto-update Last Played timestamp
CREATE OR REPLACE FUNCTION fn_update_last_played()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.total_playtime_minutes <> OLD.total_playtime_minutes THEN
        NEW.last_played = CURRENT_TIMESTAMP;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION has_role(check_user_id INT, target_role TEXT)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 
        FROM user_roles ur
        JOIN roles r ON ur.role_id = r.role_id
        WHERE ur.user_id = check_user_id AND r.role_name = target_role
    );
END;
$$ LANGUAGE plpgsql;

-- BINDING THE TRIGGERS
-- Purchases -> Library sync
-- - INSERT: add to library (AFTER INSERT)
-- - REFUND: remove from library and stamp refunded_at (BEFORE UPDATE so NEW.refunded_at persists)
DROP TRIGGER IF EXISTS tr_purchase_sync ON purchases;
DROP TRIGGER IF EXISTS tr_purchase_refund ON purchases;

CREATE TRIGGER tr_purchase_sync
AFTER INSERT ON purchases
FOR EACH ROW EXECUTE FUNCTION fn_manage_library_access();

CREATE TRIGGER tr_purchase_refund
BEFORE UPDATE ON purchases
FOR EACH ROW EXECUTE FUNCTION fn_manage_library_access();

CREATE TRIGGER tr_check_dupes BEFORE INSERT ON purchases 
    FOR EACH ROW EXECUTE FUNCTION fn_prevent_duplicate_purchase();

CREATE TRIGGER tr_update_time BEFORE UPDATE ON library 
    FOR EACH ROW EXECUTE FUNCTION fn_update_last_played();

-- ---------------------------------------------------------------------------
-- Seed data (repeatable: init.sql is intended to run on fresh DB init)
-- ---------------------------------------------------------------------------

-- Users
INSERT INTO users (username, birth_date, password_hash, date_joined, is_active, last_login)
SELECT
    format('user%02s', i) AS username,
    (DATE '1980-01-01' + ((i * 37) % 12000))::date AS birth_date,
    format('dev_hash_%s', i) AS password_hash,
    CURRENT_TIMESTAMP - ((i * 2) || ' days')::interval AS date_joined,
    (i % 17) <> 0 AS is_active,
    CASE WHEN (i % 5) = 0 THEN NULL ELSE CURRENT_TIMESTAMP - ((i % 30) || ' days')::interval END AS last_login
FROM generate_series(1, 50) AS s(i);

-- Primary emails for every user; plus a few secondary emails
INSERT INTO user_emails (user_id, email, is_primary)
SELECT u.user_id, format('%s@example.com', u.username), TRUE
FROM users u;

INSERT INTO user_emails (user_id, email, is_primary)
SELECT u.user_id, format('%s.alt@example.com', u.username), FALSE
FROM users u
WHERE (u.user_id % 10) = 0;

-- Addresses (1 per user)
INSERT INTO addresses (user_id, street, city, state, zip_code, country)
SELECT
    u.user_id,
    format('%s Main St', 100 + u.user_id) AS street,
    CASE (u.user_id % 8)
        WHEN 0 THEN 'Boston'
        WHEN 1 THEN 'New York'
        WHEN 2 THEN 'Philadelphia'
        WHEN 3 THEN 'Baltimore'
        WHEN 4 THEN 'Raleigh'
        WHEN 5 THEN 'Miami'
        WHEN 6 THEN 'Chicago'
        ELSE 'Seattle'
    END AS city,
    CASE (u.user_id % 8)
        WHEN 0 THEN 'MA'
        WHEN 1 THEN 'NY'
        WHEN 2 THEN 'PA'
        WHEN 3 THEN 'MD'
        WHEN 4 THEN 'NC'
        WHEN 5 THEN 'FL'
        WHEN 6 THEN 'IL'
        ELSE 'WA'
    END AS state,
    format('%05s', (10000 + (u.user_id * 97) % 89999)) AS zip_code,
    'USA' AS country
FROM users u;

-- Roles: everyone is a player; some are admins/moderators/developers
INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.role_id
FROM users u
JOIN roles r ON r.role_name = 'player'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.role_id
FROM users u
JOIN roles r ON r.role_name = 'admin'
WHERE u.user_id IN (1, 2)
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.role_id
FROM users u
JOIN roles r ON r.role_name = 'moderator'
WHERE u.user_id IN (3, 4, 5)
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.role_id
FROM users u
JOIN roles r ON r.role_name = 'developer'
WHERE u.user_id BETWEEN 1 AND 10
ON CONFLICT DO NOTHING;

-- Developers (owned by first 10 users)
INSERT INTO developers (dev_name, country, owner_user_id)
SELECT
    format('Studio %s', i) AS dev_name,
    CASE (i % 6)
        WHEN 0 THEN 'USA'
        WHEN 1 THEN 'Canada'
        WHEN 2 THEN 'UK'
        WHEN 3 THEN 'Japan'
        WHEN 4 THEN 'Germany'
        ELSE 'Sweden'
    END AS country,
    i AS owner_user_id
FROM generate_series(1, 10) AS s(i);

-- Genres
INSERT INTO genres (genre_name) VALUES
('Action'),
('Adventure'),
('RPG'),
('Strategy'),
('Simulation'),
('Puzzle'),
('Platformer'),
('Shooter'),
('Sports'),
('Racing'),
('Horror'),
('Indie')
ON CONFLICT DO NOTHING;

-- Games (80)
INSERT INTO games (developer_id, title, release_date, size_gb, current_price)
SELECT
    ((i - 1) % 10) + 1 AS developer_id,
    format('Game %03s', i) AS title,
    (DATE '2012-01-01' + ((i * 19) % 4000))::date AS release_date,
    (((i * 13) % 2400) / 10.0)::numeric(10, 2) AS size_gb,
    CASE WHEN (i % 15) = 0 THEN 0::numeric(10, 2)
         ELSE (((i * 7) % 6000) / 100.0)::numeric(10, 2)
    END AS current_price
FROM generate_series(1, 80) AS s(i);

-- Game -> genres (1..3 deterministic tags each)
INSERT INTO game_genres (game_id, genre_id)
SELECT game_id, ((game_id - 1) % 12) + 1
FROM games
UNION ALL
SELECT game_id, ((game_id + 3) % 12) + 1
FROM games
WHERE (game_id % 2) = 0
UNION ALL
SELECT game_id, ((game_id + 7) % 12) + 1
FROM games
WHERE (game_id % 5) = 0
ON CONFLICT DO NOTHING;

-- Purchases (~350). Library rows are created by trigger on insert.
INSERT INTO purchases (game_id, user_id, price_paid, purchase_date, is_refunded)
SELECT
    g.game_id,
    u.user_id,
    (g.current_price * CASE
        WHEN (u.user_id % 7) = 0 THEN 0.50
        WHEN (u.user_id % 5) = 0 THEN 0.75
        ELSE 1.00
    END)::numeric(10, 2) AS price_paid,
    CURRENT_TIMESTAMP - (((u.user_id * 3 + g.game_id) % 365) || ' days')::interval AS purchase_date,
    FALSE AS is_refunded
FROM users u
JOIN games g ON ((u.user_id * 31 + g.game_id * 17) % 11) = 0;

-- Refund a handful of purchases (exercise refund trigger path)
UPDATE purchases
SET is_refunded = TRUE
WHERE purchase_id IN (
    SELECT purchase_id
    FROM purchases
    WHERE (purchase_id % 13) = 0
    ORDER BY purchase_id
    LIMIT 25
);

-- Populate library activity + installed state (exercises last_played trigger path)
UPDATE library
SET
    total_playtime_minutes = ((user_id * 17 + game_id * 29) % 20000),
    is_installed = ((user_id + game_id) % 3) = 0
WHERE ((user_id * 5 + game_id) % 7) = 0;
