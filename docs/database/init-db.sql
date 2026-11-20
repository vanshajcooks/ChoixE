-- ==============================================================================
-- CampusConnect Database Initialization Script
-- ==============================================================================
-- Usage:
--   sudo mysql -u root -p < docs/database/init-db.sql
-- Or via MySQL interactive shell:
--   mysql> source /path/to/docs/database/init-db.sql;
-- ==============================================================================

-- Create database with UTF-8 support
CREATE DATABASE IF NOT EXISTS campusconnect
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Create or update application user for localhost and TCP connections
CREATE USER IF NOT EXISTS 'campusconnect'@'localhost' IDENTIFIED BY 'campusconnect_dev_password';
ALTER USER 'campusconnect'@'localhost' IDENTIFIED BY 'campusconnect_dev_password';

CREATE USER IF NOT EXISTS 'campusconnect'@'%' IDENTIFIED BY 'campusconnect_dev_password';
ALTER USER 'campusconnect'@'%' IDENTIFIED BY 'campusconnect_dev_password';

-- Grant required privileges to application user
GRANT ALL PRIVILEGES ON campusconnect.* TO 'campusconnect'@'localhost';
GRANT ALL PRIVILEGES ON campusconnect.* TO 'campusconnect'@'%';

-- Apply changes
FLUSH PRIVILEGES;

-- Verify database creation
SHOW DATABASES LIKE 'campusconnect';
