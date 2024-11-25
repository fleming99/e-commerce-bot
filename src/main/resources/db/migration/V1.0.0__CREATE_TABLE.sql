CREATE TABLE IF NOT EXISTS products(
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    product_name VARCHAR(50) NOT NULL,
    product_description VARCHAR(100) NOT NULL,
    product_type VARCHAR(50) NOT NULL
)