-- Initial schema for Game Platform Demo (PostgreSQL)

-- Core Entities
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

-- User Sub-Tables
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

-- Game Catalog
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

-- Transactions & Ownership
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

-- Function: Sync Library after a successful Purchase insert
CREATE OR REPLACE FUNCTION fn_library_on_purchase_insert()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO library (user_id, game_id, acquisition_date)
    VALUES (NEW.user_id, NEW.game_id, NEW.purchase_date)
    ON CONFLICT (user_id, game_id) DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function: Remove Library entry on refund and stamp refunded_at
CREATE OR REPLACE FUNCTION fn_library_on_purchase_refund()
RETURNS TRIGGER AS $$
BEGIN
    IF (NEW.is_refunded = TRUE AND OLD.is_refunded = FALSE) THEN
        DELETE FROM library WHERE user_id = NEW.user_id AND game_id = NEW.game_id;
        NEW.refunded_at = CURRENT_TIMESTAMP;
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

-- Triggers
CREATE TRIGGER tr_check_dupes BEFORE INSERT ON purchases
    FOR EACH ROW EXECUTE FUNCTION fn_prevent_duplicate_purchase();

CREATE TRIGGER tr_purchase_to_library AFTER INSERT ON purchases
    FOR EACH ROW EXECUTE FUNCTION fn_library_on_purchase_insert();

CREATE TRIGGER tr_purchase_refund BEFORE UPDATE OF is_refunded ON purchases
    FOR EACH ROW EXECUTE FUNCTION fn_library_on_purchase_refund();

CREATE TRIGGER tr_update_time BEFORE UPDATE ON library
    FOR EACH ROW EXECUTE FUNCTION fn_update_last_played();

