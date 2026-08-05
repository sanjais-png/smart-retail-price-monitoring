-- Creating roles table if not exists
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Note: Spring Data JPA (Hibernate) is configured with ddl-auto=update, 
-- so it will automatically generate all other tables based on the Entity classes.
-- We are just providing schema.sql to ensure any initial setup that Hibernate might miss.
