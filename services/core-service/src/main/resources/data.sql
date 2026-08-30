-- Initialize Roles (three supported roles only)
INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_AUTHORITY');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');

-- Safe migration: reassign any existing ROLE_ANALYST users to ROLE_USER
-- This runs at startup and is idempotent if ROLE_ANALYST row already removed
UPDATE user_roles SET role_id = (SELECT id FROM roles WHERE name = 'ROLE_USER' LIMIT 1)
  WHERE role_id IN (SELECT id FROM roles WHERE name = 'ROLE_ANALYST');
DELETE FROM roles WHERE name = 'ROLE_ANALYST';

-- Note: Passwords below are 'password' encoded with BCrypt
-- Insert Admin User
INSERT INTO users (username, email, password, is_enabled, is_email_verified, is_otp_verified, created_at, updated_at) 
VALUES ('admin', 'admin@smartretail.com', '$2a$10$EblZqNptyYvcLm/VwDCVAuIssIBZzX1i/N2q7K/Fv3k1fJ98Gz7aG', true, true, true, NOW(), NOW());
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';

-- Insert Regular User
INSERT INTO users (username, email, password, is_enabled, is_email_verified, is_otp_verified, created_at, updated_at) 
VALUES ('john_doe', 'john@example.com', '$2a$10$EblZqNptyYvcLm/VwDCVAuIssIBZzX1i/N2q7K/Fv3k1fJ98Gz7aG', true, true, true, NOW(), NOW());
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'john_doe' AND r.name = 'ROLE_USER';

-- Insert Authority User
INSERT INTO users (username, email, password, is_enabled, is_email_verified, is_otp_verified, created_at, updated_at) 
VALUES ('auth_officer', 'officer@smartretail.com', '$2a$10$EblZqNptyYvcLm/VwDCVAuIssIBZzX1i/N2q7K/Fv3k1fJ98Gz7aG', true, true, true, NOW(), NOW());
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'auth_officer' AND r.name = 'ROLE_AUTHORITY';

-- Initialize Commodity Categories
INSERT INTO commodity_categories (name, description) VALUES ('Vegetables', 'Fresh vegetables');
INSERT INTO commodity_categories (name, description) VALUES ('Fruits', 'Fresh fruits');
INSERT INTO commodity_categories (name, description) VALUES ('Grains', 'Rice, Wheat, and other grains');

-- Initialize States and Districts
INSERT INTO states (name, code) VALUES ('Maharashtra', 'MH');
INSERT INTO districts (name, state_id) SELECT 'Mumbai', id FROM states WHERE name = 'Maharashtra';
INSERT INTO districts (name, state_id) SELECT 'Pune', id FROM states WHERE name = 'Maharashtra';

INSERT INTO states (name, code) VALUES ('Delhi', 'DL');
INSERT INTO districts (name, state_id) SELECT 'New Delhi', id FROM states WHERE name = 'Delhi';

-- Initialize Markets
INSERT INTO markets (name, code, city, district_id, latitude, longitude)
SELECT 'Dadar APMC Market', 'MKT_DADAR', 'Mumbai', id, 19.0176, 72.8430 FROM districts WHERE name = 'Mumbai';
INSERT INTO markets (name, code, city, district_id, latitude, longitude)
SELECT 'Pune Marketyard', 'MKT_PUNE', 'Pune', id, 18.5029, 73.8690 FROM districts WHERE name = 'Pune';
INSERT INTO markets (name, code, city, district_id, latitude, longitude)
SELECT 'Azadpur Mandi', 'MKT_AZADPUR', 'New Delhi', id, 28.7118, 77.1685 FROM districts WHERE name = 'New Delhi';

-- Initialize Commodities
INSERT INTO commodities (name, code, description, unit, image_path, category_id, is_active, created_at)
SELECT 'Onion', 'ONION_01', 'Red Onion', 'KG', NULL, id, true, NOW() FROM commodity_categories WHERE name = 'Vegetables';
INSERT INTO commodities (name, code, description, unit, image_path, category_id, is_active, created_at)
SELECT 'Tomato', 'TOMATO_01', 'Hybrid Tomato', 'KG', NULL, id, true, NOW() FROM commodity_categories WHERE name = 'Vegetables';
INSERT INTO commodities (name, code, description, unit, image_path, category_id, is_active, created_at)
SELECT 'Rice (Basmati)', 'RICE_01', 'Premium Basmati Rice', 'KG', NULL, id, true, NOW() FROM commodity_categories WHERE name = 'Grains';
