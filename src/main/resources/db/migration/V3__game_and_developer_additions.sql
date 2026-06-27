ALTER TABLE games
    ADD COLUMN description TEXT,
    ADD COLUMN sale_price NUMERIC(10, 2) CHECK (sale_price >= 0);

ALTER TABLE developers
    ADD COLUMN bio TEXT;