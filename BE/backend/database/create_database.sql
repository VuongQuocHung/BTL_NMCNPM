-- MySQL script to create a fresh database for VPH Laptop Store.
-- Change `btl_cnpm_new` if you want another database name, then update DB_URL in .env.

CREATE DATABASE IF NOT EXISTS btl_cnpm_new
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE btl_cnpm_new;

SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_roles_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS brands (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    logo_url VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE KEY uk_brands_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    PRIMARY KEY (id),
    UNIQUE KEY uk_categories_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    phone VARCHAR(20),
    role_id BIGINT NOT NULL,
    created_at DATETIME(6),
    reset_token VARCHAR(255),
    reset_token_expiry DATETIME(6),
    enabled BIT(1) NOT NULL DEFAULT b'0',
    verificationToken VARCHAR(255),
    verificationTokenExpiry DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    UNIQUE KEY uk_users_verification_token (verificationToken),
    KEY idx_users_role_id (role_id),
    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(15,2) NOT NULL,
    import_price DECIMAL(15,2) NOT NULL,
    stock INT NOT NULL,
    description TEXT,
    brand_id BIGINT,
    category_id BIGINT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    KEY idx_products_brand_id (brand_id),
    KEY idx_products_category_id (category_id),
    CONSTRAINT fk_products_brand
        FOREIGN KEY (brand_id) REFERENCES brands (id),
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS product_images (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    is_primary BIT(1),
    PRIMARY KEY (id),
    KEY idx_product_images_product_id (product_id),
    CONSTRAINT fk_product_images_product
        FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS product_specifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    cpu VARCHAR(255),
    ram VARCHAR(255),
    storage VARCHAR(255),
    vga VARCHAR(255),
    screen VARCHAR(255),
    os VARCHAR(255),
    battery VARCHAR(255),
    weight VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_specifications_product_id (product_id),
    CONSTRAINT fk_product_specifications_product
        FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS carts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    active BIT(1) NOT NULL DEFAULT b'1',
    applied_voucher_code VARCHAR(50),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_carts_user_id (user_id),
    CONSTRAINT fk_carts_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_cart_product (cart_id, product_id),
    KEY idx_cart_items_product_id (product_id),
    CONSTRAINT fk_cart_items_cart
        FOREIGN KEY (cart_id) REFERENCES carts (id),
    CONSTRAINT fk_cart_items_product
        FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    order_date DATETIME(6),
    status VARCHAR(50) NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    shipping_address VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    KEY idx_orders_user_id (user_id),
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS order_details (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_order_details_order_id (order_id),
    KEY idx_order_details_product_id (product_id),
    CONSTRAINT fk_order_details_order
        FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_details_product
        FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    created_at DATETIME(6),
    PRIMARY KEY (id),
    KEY idx_reviews_product_id (product_id),
    KEY idx_reviews_user_id (user_id),
    CONSTRAINT fk_reviews_product
        FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_reviews_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS vouchers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    discount_type VARCHAR(30) NOT NULL,
    discount_value DECIMAL(15,2) NOT NULL,
    min_order_amount DECIMAL(15,2),
    max_discount_amount DECIMAL(15,2),
    usage_limit INT,
    used_count INT NOT NULL DEFAULT 0,
    start_date DATETIME(6),
    end_date DATETIME(6),
    active BIT(1) NOT NULL DEFAULT b'1',
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_voucher_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO roles (name)
VALUES ('ADMIN'), ('CUSTOMER')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO brands (name, logo_url)
VALUES
    ('Dell', 'https://upload.wikimedia.org/wikipedia/commons/4/48/Dell_Logo.svg'),
    ('ASUS', 'https://upload.wikimedia.org/wikipedia/commons/2/2e/ASUS_Logo.svg'),
    ('Lenovo', 'https://upload.wikimedia.org/wikipedia/commons/b/b8/Lenovo_logo_2015.svg'),
    ('Apple', 'https://upload.wikimedia.org/wikipedia/commons/f/fa/Apple_logo_black.svg'),
    ('MSI', 'https://upload.wikimedia.org/wikipedia/commons/9/93/Msi-logo.svg')
ON DUPLICATE KEY UPDATE
    logo_url = VALUES(logo_url);

INSERT INTO categories (name, description)
VALUES
    ('Gaming', 'High performance laptops for gaming and demanding tasks'),
    ('Ultrabook', 'Thin and light laptops for mobility and daily work'),
    ('Office', 'Balanced laptops for office workloads and study'),
    ('Creator', 'Laptops optimized for design, video, and content creation')
ON DUPLICATE KEY UPDATE
    description = VALUES(description);

INSERT INTO products (name, price, import_price, stock, description, brand_id, category_id, created_at, updated_at)
SELECT
    'Dell G15 5530', 25990000.00, 22000000.00, 15,
    '15.6 inch gaming laptop with Intel Core i7 and RTX graphics',
    b.id, c.id, NOW(6), NOW(6)
FROM brands b
JOIN categories c ON c.name = 'Gaming'
WHERE b.name = 'Dell'
  AND NOT EXISTS (SELECT 1 FROM products p WHERE p.name = 'Dell G15 5530');

INSERT INTO products (name, price, import_price, stock, description, brand_id, category_id, created_at, updated_at)
SELECT
    'ASUS ROG Zephyrus G14', 32990000.00, 28700000.00, 10,
    'Compact premium gaming laptop with strong CPU and GPU performance',
    b.id, c.id, NOW(6), NOW(6)
FROM brands b
JOIN categories c ON c.name = 'Gaming'
WHERE b.name = 'ASUS'
  AND NOT EXISTS (SELECT 1 FROM products p WHERE p.name = 'ASUS ROG Zephyrus G14');

INSERT INTO products (name, price, import_price, stock, description, brand_id, category_id, created_at, updated_at)
SELECT
    'Lenovo ThinkPad X1 Carbon Gen 11', 41990000.00, 37000000.00, 8,
    'Business ultrabook with premium keyboard and long battery life',
    b.id, c.id, NOW(6), NOW(6)
FROM brands b
JOIN categories c ON c.name = 'Ultrabook'
WHERE b.name = 'Lenovo'
  AND NOT EXISTS (SELECT 1 FROM products p WHERE p.name = 'Lenovo ThinkPad X1 Carbon Gen 11');

INSERT INTO products (name, price, import_price, stock, description, brand_id, category_id, created_at, updated_at)
SELECT
    'MacBook Air M2 13', 27990000.00, 24500000.00, 12,
    'Lightweight laptop powered by Apple M2 chip for everyday productivity',
    b.id, c.id, NOW(6), NOW(6)
FROM brands b
JOIN categories c ON c.name = 'Ultrabook'
WHERE b.name = 'Apple'
  AND NOT EXISTS (SELECT 1 FROM products p WHERE p.name = 'MacBook Air M2 13');

INSERT INTO products (name, price, import_price, stock, description, brand_id, category_id, created_at, updated_at)
SELECT
    'MSI Creator M16', 36990000.00, 32100000.00, 6,
    'Creator laptop tuned for design workflows and media production',
    b.id, c.id, NOW(6), NOW(6)
FROM brands b
JOIN categories c ON c.name = 'Creator'
WHERE b.name = 'MSI'
  AND NOT EXISTS (SELECT 1 FROM products p WHERE p.name = 'MSI Creator M16');

INSERT INTO product_specifications (product_id, cpu, ram, storage, vga, screen, os, battery, weight)
SELECT p.id, 'Intel Core i7-13650HX', '16GB DDR5', '512GB SSD', 'NVIDIA GeForce RTX 4050', '15.6 inch FHD 165Hz', 'Windows 11', '86Wh', '2.65kg'
FROM products p
WHERE p.name = 'Dell G15 5530'
  AND NOT EXISTS (SELECT 1 FROM product_specifications ps WHERE ps.product_id = p.id);

INSERT INTO product_specifications (product_id, cpu, ram, storage, vga, screen, os, battery, weight)
SELECT p.id, 'AMD Ryzen 9 7940HS', '32GB LPDDR5', '1TB SSD', 'NVIDIA GeForce RTX 4060', '14 inch QHD 165Hz', 'Windows 11', '76Wh', '1.65kg'
FROM products p
WHERE p.name = 'ASUS ROG Zephyrus G14'
  AND NOT EXISTS (SELECT 1 FROM product_specifications ps WHERE ps.product_id = p.id);

INSERT INTO product_specifications (product_id, cpu, ram, storage, vga, screen, os, battery, weight)
SELECT p.id, 'Intel Core i7-1365U', '16GB LPDDR5', '1TB SSD', 'Intel Iris Xe Graphics', '14 inch WUXGA', 'Windows 11 Pro', '57Wh', '1.12kg'
FROM products p
WHERE p.name = 'Lenovo ThinkPad X1 Carbon Gen 11'
  AND NOT EXISTS (SELECT 1 FROM product_specifications ps WHERE ps.product_id = p.id);

INSERT INTO product_specifications (product_id, cpu, ram, storage, vga, screen, os, battery, weight)
SELECT p.id, 'Apple M2', '16GB Unified Memory', '512GB SSD', 'Apple Integrated GPU', '13.6 inch Liquid Retina', 'macOS', '52.6Wh', '1.24kg'
FROM products p
WHERE p.name = 'MacBook Air M2 13'
  AND NOT EXISTS (SELECT 1 FROM product_specifications ps WHERE ps.product_id = p.id);

INSERT INTO product_specifications (product_id, cpu, ram, storage, vga, screen, os, battery, weight)
SELECT p.id, 'Intel Core i9-13900H', '32GB DDR5', '1TB SSD', 'NVIDIA GeForce RTX 4070', '16 inch QHD+', 'Windows 11', '99.9Wh', '2.26kg'
FROM products p
WHERE p.name = 'MSI Creator M16'
  AND NOT EXISTS (SELECT 1 FROM product_specifications ps WHERE ps.product_id = p.id);
