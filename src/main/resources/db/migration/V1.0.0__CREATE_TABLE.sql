CREATE TABLE IF NOT EXISTS products(
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    product_name VARCHAR(50) NOT NULL,
    product_description VARCHAR(100) NOT NULL,
    product_type VARCHAR(50) NOT NULL,
    active CHAR NOT NULL,
    CONSTRAINT uq_product_id UNIQUE (product_id)
);

CREATE TABLE IF NOT EXISTS telegram_user(
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    first_name VARCHAR(30) NOT NULL,
    last_name VARCHAR(60) NOT NULL,
    cellphone VARCHAR(255) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    CONSTRAINT uq_user_id UNIQUE (user_id),
    CONSTRAINT uq_cellphone UNIQUE (cellphone),
    CONSTRAINT uq_cpf UNIQUE (cpf)
);