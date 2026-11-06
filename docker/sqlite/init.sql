-- SQLite 测试对象
-- 用法：sqlite3 demo.db < init.sql，或在 EasyShell 中连接 demo.db 后运行 SQL 文件
-- SQLite 没有模式、序列、存储过程，使用 AUTOINCREMENT、视图和触发器覆盖主要对象

PRAGMA foreign_keys = ON;

DROP VIEW IF EXISTS v_users;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT UNIQUE,
    age INTEGER DEFAULT 18 CHECK (age >= 0),
    balance NUMERIC DEFAULT 0,
    active INTEGER DEFAULT 1 CHECK (active IN (0, 1)),
    avatar BLOB,
    profile TEXT CHECK (profile IS NULL OR json_valid(profile)),
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_name ON users (name);

CREATE TABLE orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    order_no TEXT NOT NULL UNIQUE,
    amount NUMERIC NOT NULL DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'NEW',
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX idx_orders_user_id ON orders (user_id);

INSERT INTO users (name, email, age, balance, active, profile)
VALUES ('alice', 'a@example.com', 20, 10.50, 1, '{"level": 1}'),
       ('bob', 'b@example.com', 30, 20.00, 0, '{"level": 2}');

INSERT INTO orders (user_id, order_no, amount, status)
VALUES (1, 'ORD-0001', 99.00, 'PAID'),
       (2, 'ORD-0002', 20.00, 'NEW');

CREATE VIEW v_users AS
SELECT id, name, email, age, active
FROM users;

-- SQLite 的 BEFORE 触发器不能直接改写 NEW，使用 AFTER 触发器回写
CREATE TRIGGER trg_users_normalize_insert
AFTER INSERT ON users
FOR EACH ROW
WHEN NEW.email IS NOT NULL AND NEW.email <> LOWER(NEW.email)
BEGIN
    UPDATE users SET email = LOWER(email) WHERE id = NEW.id;
END;

CREATE TRIGGER trg_users_normalize_update
AFTER UPDATE OF email ON users
FOR EACH ROW
WHEN NEW.email IS NOT NULL AND NEW.email <> LOWER(NEW.email)
BEGIN
    UPDATE users SET email = LOWER(email) WHERE id = NEW.id;
END;

PRAGMA user_version = 1;
