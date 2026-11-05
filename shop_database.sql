-- ============================================================
-- FILE    : shop_database.sql
-- PROJECT : CoolShop Website MVP
-- DB      : MySQL 8 | InnoDB | utf8mb4_unicode_ci
-- IMPORT  : HeidiSQL hoặc: mysql -u root -p < shop_database.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS CoolShop
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE CoolShop;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

-- ============================================================
-- TABLE: users
-- ============================================================
CREATE TABLE users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    fullname      VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    password      VARCHAR(255) NOT NULL COMMENT 'BCrypt hash',
    role          ENUM('ADMIN','CUSTOMER') NOT NULL DEFAULT 'CUSTOMER',
    status        ENUM('ACTIVE','INACTIVE','BANNED') NOT NULL DEFAULT 'ACTIVE',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email),
    INDEX idx_users_role        (role),
    INDEX idx_users_status      (status),
    INDEX idx_users_created_at  (created_at),
    INDEX idx_users_fullname    (fullname)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: categories
-- ============================================================
CREATE TABLE categories (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    description TEXT         NULL,
    status      ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_categories_status (status),
    INDEX idx_categories_name   (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: products
-- ============================================================
CREATE TABLE products (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    category_id    BIGINT        NOT NULL,
    name           VARCHAR(255)  NOT NULL,
    description    TEXT          NULL,
    price          DECIMAL(15,2) NOT NULL,
    image_url      VARCHAR(500)  NULL,
    stock_quantity INT           NOT NULL DEFAULT 0,
    status         ENUM('ACTIVE','INACTIVE','OUT_OF_STOCK') NOT NULL DEFAULT 'ACTIVE',
    created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_products_price
        CHECK (price > 0),
    CONSTRAINT chk_products_stock
        CHECK (stock_quantity >= 0),

    INDEX idx_products_category_id (category_id),
    INDEX idx_products_status      (status),
    INDEX idx_products_name        (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: orders
-- ============================================================
CREATE TABLE orders (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    user_id          BIGINT        NOT NULL,
    total_amount     DECIMAL(15,2) NOT NULL,
    receiver_name    VARCHAR(100)  NOT NULL,
    receiver_phone   VARCHAR(20)   NOT NULL,
    shipping_address TEXT          NOT NULL,
    note             TEXT          NULL,
    status           ENUM('PENDING','CONFIRMED','SHIPPING','COMPLETED','CANCELLED')
                                   NOT NULL DEFAULT 'PENDING',
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_orders_total
        CHECK (total_amount > 0),

    INDEX idx_orders_user_id    (user_id),
    INDEX idx_orders_status     (status),
    INDEX idx_orders_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: order_items
-- ============================================================
CREATE TABLE order_items (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    order_id    BIGINT        NOT NULL,
    product_id  BIGINT        NOT NULL,
    quantity    INT           NOT NULL,
    unit_price  DECIMAL(15,2) NOT NULL COMMENT 'Giá tại thời điểm đặt hàng',
    subtotal    DECIMAL(15,2) NOT NULL COMMENT 'quantity * unit_price',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id) REFERENCES products(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_order_items_quantity
        CHECK (quantity > 0),
    CONSTRAINT chk_order_items_unit_price
        CHECK (unit_price > 0),
    CONSTRAINT chk_order_items_subtotal
        CHECK (subtotal > 0),

    INDEX idx_order_items_order_id   (order_id),
    INDEX idx_order_items_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: payments
-- MVP: 1 order = 1 payment (UNIQUE on order_id)
-- ============================================================
CREATE TABLE payments (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    order_id        BIGINT        NOT NULL,
    payment_method  ENUM('COD')   NOT NULL DEFAULT 'COD',
    amount          DECIMAL(15,2) NOT NULL,
    payment_status  ENUM('PENDING','PAID','FAILED','REFUNDED')
                                  NOT NULL DEFAULT 'PENDING',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_payments_order_id (order_id),
    CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id) REFERENCES orders(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_payments_amount
        CHECK (amount > 0),

    INDEX idx_payments_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- SAMPLE DATA
-- ============================================================

-- users
-- Admin@123   => BCrypt $2a$12$...
-- Customer@123 => BCrypt $2a$12$...
INSERT INTO users (id, fullname, email, password, role, status) VALUES
(1, 'Administrator', 'admin@shop.local',
 '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
 'ADMIN', 'ACTIVE'),
(2, 'Test Customer', 'customer@shop.local',
 '$2a$12$u4JeKmHf4fJHy1oN5EoXIeORmzF3LPjvK4tE.Bwq9l7k0ROQ0UWpq',
 'CUSTOMER', 'ACTIVE');

-- categories
INSERT INTO categories (id, name, description, status) VALUES
(1, 'Điện thoại',         'Điện thoại thông minh các hãng nổi tiếng',  'ACTIVE'),
(2, 'Laptop',             'Máy tính xách tay học tập và làm việc',      'ACTIVE'),
(3, 'Phụ kiện',           'Sạc, cáp, ốp lưng và phụ kiện công nghệ',  'ACTIVE'),
(4, 'Đồng hồ thông minh', 'Smartwatch theo dõi sức khỏe',              'ACTIVE'),
(5, 'Tai nghe',           'Tai nghe có dây và không dây',               'ACTIVE');

-- products (20 sản phẩm)
INSERT INTO products (id, category_id, name, description, price, image_url, stock_quantity, status) VALUES
(1,  1, 'iPhone 15 Pro',         'Chip A17 Pro, camera 48MP, khung titan.',            29990000.00, 'https://placehold.co/600x400?text=iPhone+15+Pro',       50,  'ACTIVE'),
(2,  1, 'iPhone 16',             'Chip A18, hỗ trợ Apple Intelligence.',               24990000.00, 'https://placehold.co/600x400?text=iPhone+16',           80,  'ACTIVE'),
(3,  1, 'Samsung Galaxy S24 Ultra','S Pen tích hợp, camera 200MP, AI Galaxy.',         27990000.00, 'https://placehold.co/600x400?text=Galaxy+S24+Ultra',    40,  'ACTIVE'),
(4,  1, 'Samsung Galaxy A55',    'AMOLED 120Hz, pin 5000mAh, tầm trung cao cấp.',      10490000.00, 'https://placehold.co/600x400?text=Galaxy+A55',         100,  'ACTIVE'),
(5,  1, 'Xiaomi 14',             'Snapdragon 8 Gen 3, camera Leica, sạc 90W.',         16990000.00, 'https://placehold.co/600x400?text=Xiaomi+14',           60,  'ACTIVE'),
(6,  2, 'MacBook Air M3',        'Siêu mỏng nhẹ, pin 18 giờ, chip Apple M3.',          32990000.00, 'https://placehold.co/600x400?text=MacBook+Air+M3',      30,  'ACTIVE'),
(7,  2, 'MacBook Pro M3',        'Liquid Retina XDR, RAM 18GB, SSD 512GB.',             45990000.00, 'https://placehold.co/600x400?text=MacBook+Pro+M3',      20,  'ACTIVE'),
(8,  2, 'Dell XPS 13',           'Intel Core Ultra 7, thiết kế sang trọng 13.4 inch.', 28990000.00, 'https://placehold.co/600x400?text=Dell+XPS+13',         25,  'ACTIVE'),
(9,  2, 'ASUS ROG Zephyrus G14', 'Gaming AMD Ryzen 9, RTX 4060, 165Hz.',               35990000.00, 'https://placehold.co/600x400?text=ASUS+ROG+G14',        15,  'ACTIVE'),
(10, 2, 'Lenovo ThinkPad X1 Carbon','Intel Core Ultra 7, nhẹ 1.12kg.',                38990000.00, 'https://placehold.co/600x400?text=ThinkPad+X1',         20,  'ACTIVE'),
(11, 3, 'Apple MagSafe 20W',     'Sạc không dây MagSafe chính hãng Apple.',             1290000.00, 'https://placehold.co/600x400?text=MagSafe+20W',        200,  'ACTIVE'),
(12, 3, 'Samsung Charger 45W',   'Sạc nhanh USB-C 45W tương thích rộng.',               890000.00, 'https://placehold.co/600x400?text=Samsung+45W',        150,  'ACTIVE'),
(13, 3, 'Anker PowerBank 20000', 'Pin dự phòng 20000mAh, sạc nhanh 22.5W.',             990000.00, 'https://placehold.co/600x400?text=Anker+20000',        120,  'ACTIVE'),
(14, 4, 'Apple Watch Series 9',  'Chip S9, 2000 nit, tính năng Double Tap.',           10990000.00, 'https://placehold.co/600x400?text=AppleWatch+S9',       45,  'ACTIVE'),
(15, 4, 'Samsung Galaxy Watch 6','Theo dõi sức khỏe, Wear OS 4, pin 40 giờ.',          7490000.00, 'https://placehold.co/600x400?text=GalaxyWatch+6',       55,  'ACTIVE'),
(16, 4, 'Garmin Fenix 7',        'GPS, pin 18 ngày, chống nước 10ATM.',                18990000.00, 'https://placehold.co/600x400?text=Garmin+Fenix+7',      20,  'ACTIVE'),
(17, 4, 'Xiaomi Smart Band 8',   'Vòng tay thông minh, pin 16 ngày.',                    990000.00, 'https://placehold.co/600x400?text=XiaomiBand+8',       200,  'ACTIVE'),
(18, 5, 'AirPods Pro 2nd Gen',   'ANC, chip H2, Spatial Audio.',                        6490000.00, 'https://placehold.co/600x400?text=AirPods+Pro+2',       70,  'ACTIVE'),
(19, 5, 'Samsung Galaxy Buds2 Pro','ANC, âm thanh 360 độ, Bluetooth 5.3.',              4490000.00, 'https://placehold.co/600x400?text=GalaxyBuds2+Pro',     80,  'ACTIVE'),
(20, 5, 'Sony WH-1000XM5',       'Over-ear, chống ồn hàng đầu, pin 30 giờ.',            8990000.00, 'https://placehold.co/600x400?text=Sony+WH1000XM5',      35,  'ACTIVE');

-- orders (5 đơn để có đủ 5 payments)
INSERT INTO orders (id, user_id, total_amount, receiver_name, receiver_phone, shipping_address, note, status) VALUES
(1, 2, 56480000.00, 'Nguyen Van A', '0901234567', '123 Lê Lợi, Q.1, TP.HCM',            'Giao giờ hành chính', 'PENDING'),
(2, 2, 39480000.00, 'Tran Thi B',  '0912345678', '456 Nguyễn Huệ, Q.1, TP.HCM',        NULL,                  'CONFIRMED'),
(3, 2,  7480000.00, 'Le Van C',    '0923456789', '789 Trần Hưng Đạo, Q.5, TP.HCM',     'Để trước cửa',        'COMPLETED'),
(4, 2, 10990000.00, 'Pham Thi D',  '0934567890', '101 Hoàng Diệu, Q.4, TP.HCM',        NULL,                  'SHIPPING'),
(5, 2, 24990000.00, 'Hoang Van E', '0945678901', '202 CMT8, Q.3, TP.HCM',              'Hủy do đặt nhầm',     'CANCELLED');

-- order_items
-- Đơn 1: iPhone 15 Pro (29,990,000) + MacBook Air M3 (26,490,000 snapshot) = 56,480,000
-- Đơn 2: MacBook Pro M3 (32,990,000) + AirPods Pro (6,490,000) = 39,480,000
-- Đơn 3: AirPods Pro (6,490,000) + Xiaomi Band 8 (990,000) = 7,480,000
-- Đơn 4: Apple Watch S9 (10,990,000) = 10,990,000
-- Đơn 5: iPhone 16 (24,990,000) = 24,990,000
INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal) VALUES
(1, 1,  1, 29990000.00, 29990000.00),
(1, 6,  1, 26490000.00, 26490000.00),
(2, 7,  1, 32990000.00, 32990000.00),
(2, 18, 1,  6490000.00,  6490000.00),
(3, 18, 1,  6490000.00,  6490000.00),
(3, 17, 1,   990000.00,   990000.00),
(4, 14, 1, 10990000.00, 10990000.00),
(5, 2,  1, 24990000.00, 24990000.00);

-- payments (5 bản ghi, 1-1 với orders)
INSERT INTO payments (order_id, payment_method, amount, payment_status) VALUES
(1, 'COD', 56480000.00, 'PENDING'),
(2, 'COD', 39480000.00, 'PENDING'),
(3, 'COD',  7480000.00, 'PAID'),
(4, 'COD', 10990000.00, 'PENDING'),
(5, 'COD', 24990000.00, 'REFUNDED');

-- ============================================================
-- VERIFICATION QUERIES
-- ============================================================
SELECT 'users'       AS tbl, COUNT(*) AS cnt FROM users
UNION ALL SELECT 'categories', COUNT(*) FROM categories
UNION ALL SELECT 'products',   COUNT(*) FROM products
UNION ALL SELECT 'orders',     COUNT(*) FROM orders
UNION ALL SELECT 'order_items',COUNT(*) FROM order_items
UNION ALL SELECT 'payments',   COUNT(*) FROM payments;

-- Kiểm tra tổng tiền đơn hàng khớp order_items
SELECT o.id,
       o.total_amount                                              AS stored,
       SUM(oi.subtotal)                                            AS calculated,
       IF(ABS(o.total_amount - SUM(oi.subtotal)) < 0.01,'OK','MISMATCH') AS result
FROM orders o JOIN order_items oi ON oi.order_id = o.id
GROUP BY o.id, o.total_amount;
