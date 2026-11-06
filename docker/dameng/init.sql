-- Dameng (DM8) demo objects
-- Run as SYSDBA on a fresh container:
--   cd /home/dmdba/dmdbms/bin
--   ./disql -S SYSDBA/SYSDBA@127.0.0.1:5236 < /tmp/init.sql
-- The script creates user/schema DEMO with password Demo@2026.

CREATE USER DEMO IDENTIFIED BY "Demo@2026";
GRANT DBA TO DEMO;
COMMIT;

SET SCHEMA DEMO;

CREATE TABLE users (
    id BIGINT IDENTITY(1, 1) NOT NULL,
    name VARCHAR(64) NOT NULL,
    email VARCHAR(255),
    age INT DEFAULT 18,
    balance DECIMAL(12, 2) DEFAULT 0,
    active TINYINT DEFAULT 1,
    avatar BLOB,
    profile CLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_age CHECK (age >= 0)
);

CREATE INDEX idx_users_name ON users (name);

CREATE TABLE orders (
    id BIGINT IDENTITY(1, 1) NOT NULL,
    user_id BIGINT NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    amount DECIMAL(12, 2) DEFAULT 0,
    status VARCHAR(16) DEFAULT 'NEW',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT uk_orders_no UNIQUE (order_no),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

INSERT INTO users (name, email, age, balance, active, profile)
VALUES ('alice', 'a@example.com', 20, 10.50, 1, '{"level": 1}');

INSERT INTO users (name, email, age, balance, active, profile)
VALUES ('bob', 'b@example.com', 30, 20.00, 0, '{"level": 2}');

INSERT INTO orders (user_id, order_no, amount, status)
VALUES (1, 'ORD-0001', 99.00, 'PAID');

INSERT INTO orders (user_id, order_no, amount, status)
VALUES (2, 'ORD-0002', 20.00, 'NEW');

CREATE VIEW v_users AS
SELECT id, name, email, age, active
FROM users;

CREATE SEQUENCE seq_order
    INCREMENT BY 1
    START WITH 1
    MINVALUE 1
    MAXVALUE 999999
    CACHE 10
    CYCLE;

COMMENT ON TABLE users IS 'user table';
COMMENT ON COLUMN users.name IS 'user name';
COMMENT ON COLUMN users.email IS 'email';
COMMENT ON COLUMN users.profile IS 'profile json';
COMMENT ON TABLE orders IS 'order table';

CREATE OR REPLACE FUNCTION fn_add(a INT, b INT) RETURN INT AS
BEGIN
    RETURN a + b;
END;
/

CREATE OR REPLACE PROCEDURE sp_noop AS
BEGIN
    NULL;
END;
/

CREATE OR REPLACE TRIGGER trg_users_normalize
BEFORE INSERT OR UPDATE ON users
FOR EACH ROW
BEGIN
    :NEW.email := LOWER(:NEW.email);
END;
/

COMMIT;
