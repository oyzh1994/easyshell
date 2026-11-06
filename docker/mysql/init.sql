-- MySQL 测试对象
-- Docker 首次初始化时由 /docker-entrypoint-initdb.d 自动执行
-- 建议使用 MySQL 5.7+（JSON 类型、CHECK 约束需要 8.0+ 才会强制执行）

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS demo
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;
USE demo;

-- 便于脚本重复执行
DROP TRIGGER IF EXISTS trg_users_normalize_insert;
DROP TRIGGER IF EXISTS trg_users_normalize_update;
DROP EVENT IF EXISTS ev_demo_heartbeat;
DROP FUNCTION IF EXISTS fn_add;
DROP PROCEDURE IF EXISTS sp_noop;
DROP VIEW IF EXISTS v_users;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(64) NOT NULL COMMENT '用户名',
    email VARCHAR(255) DEFAULT NULL COMMENT '邮箱',
    age INT DEFAULT 18 COMMENT '年龄',
    balance DECIMAL(12, 2) DEFAULT 0.00 COMMENT '余额',
    active TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    avatar LONGBLOB COMMENT '头像',
    profile JSON COMMENT '扩展信息',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    KEY idx_users_name (name),
    CONSTRAINT chk_users_age CHECK (age >= 0),
    CONSTRAINT chk_users_balance CHECK (balance >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

CREATE TABLE orders (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '金额',
    status VARCHAR(16) NOT NULL DEFAULT 'NEW' COMMENT '状态',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_orders_order_no (order_no),
    KEY idx_orders_user_id (user_id),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单表';

INSERT INTO users (name, email, age, balance, active, profile)
VALUES ('alice', 'a@example.com', 20, 10.50, 1, '{"level": 1}'),
       ('bob', 'b@example.com', 30, 20.00, 0, '{"level": 2}');

INSERT INTO orders (user_id, order_no, amount, status)
VALUES (1, 'ORD-0001', 99.00, 'PAID'),
       (2, 'ORD-0002', 20.00, 'NEW');

CREATE OR REPLACE VIEW v_users AS
SELECT id, name, email, age, active
FROM users;

CREATE FUNCTION fn_add(a INT, b INT)
    RETURNS INT
    DETERMINISTIC
    COMMENT '加法函数'
    RETURN a + b;

CREATE PROCEDURE sp_noop()
    COMMENT '空过程'
    SELECT 1;

-- 使用单语句触发器，避免 DELIMITER，方便 EasyShell 直接执行
CREATE TRIGGER trg_users_normalize_insert
    BEFORE INSERT
    ON users
    FOR EACH ROW
    SET NEW.email = LOWER(NEW.email);

CREATE TRIGGER trg_users_normalize_update
    BEFORE UPDATE
    ON users
    FOR EACH ROW
    SET NEW.email = LOWER(NEW.email);

CREATE EVENT ev_demo_heartbeat
    ON SCHEDULE EVERY 1 DAY
    ON COMPLETION PRESERVE
    DO UPDATE users SET active = active WHERE id = -1;

SET FOREIGN_KEY_CHECKS = 1;
