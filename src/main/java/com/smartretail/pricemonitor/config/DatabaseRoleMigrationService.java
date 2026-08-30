package com.smartretail.pricemonitor.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Pre-Hibernate Database Role Migration Component.
 * Executes raw JDBC SQL migration BEFORE Spring JPA/Hibernate initializes,
 * preventing DDL auto-update failures (Data truncated for column 'name' at row 'ROLE_ANALYST')
 * and ensuring zero enum mapping errors.
 */
@Component
@Slf4j
public class DatabaseRoleMigrationService {

    public DatabaseRoleMigrationService(DataSource dataSource) {
        log.info("Executing pre-Hibernate database role migration check...");
        runMigration(dataSource);
    }

    private void runMigration(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            Long analystRoleId = null;
            Long userRoleId = null;

            // 1. Check if ROLE_ANALYST exists in roles table
            try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM roles WHERE name = 'ROLE_ANALYST'")) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        analystRoleId = rs.getLong("id");
                    }
                }
            }

            // 2. If ROLE_ANALYST does not exist, exit cleanly (idempotent)
            if (analystRoleId == null) {
                log.info("Pre-Hibernate role migration: 0 ROLE_ANALYST records found in database.");
                return;
            }

            log.info("Found obsolete ROLE_ANALYST with ID: {}. Proceeding with migration...", analystRoleId);

            // 3. Find or create ROLE_USER ID
            try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM roles WHERE name = 'ROLE_USER'")) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        userRoleId = rs.getLong("id");
                    }
                }
            }

            if (userRoleId == null) {
                log.warn("ROLE_USER entry missing in roles table! Inserting ROLE_USER...");
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO roles (name) VALUES ('ROLE_USER')", Statement.RETURN_GENERATED_KEYS)) {
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            userRoleId = rs.getLong(1);
                        }
                    }
                }
            }

            if (userRoleId != null) {
                // 4. Remove duplicate ROLE_ANALYST mappings for users who ALREADY have ROLE_USER
                String deleteDuplicatesSql = "DELETE FROM user_roles WHERE role_id = ? AND user_id IN (" +
                        "  SELECT user_id FROM (SELECT user_id FROM user_roles WHERE role_id = ?) AS tmp" +
                        ")";
                try (PreparedStatement ps = conn.prepareStatement(deleteDuplicatesSql)) {
                    ps.setLong(1, analystRoleId);
                    ps.setLong(2, userRoleId);
                    int deleted = ps.executeUpdate();
                    log.info("Removed {} duplicate ROLE_ANALYST mappings for users already holding ROLE_USER.", deleted);
                }

                // 5. Convert remaining ROLE_ANALYST mappings to ROLE_USER
                String updateMappingsSql = "UPDATE user_roles SET role_id = ? WHERE role_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateMappingsSql)) {
                    ps.setLong(1, userRoleId);
                    ps.setLong(2, analystRoleId);
                    int updated = ps.executeUpdate();
                    log.info("Migrated {} user role assignments from ROLE_ANALYST -> ROLE_USER.", updated);
                }
            }

            // 6. Remove obsolete ROLE_ANALYST row from roles table
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM roles WHERE name = 'ROLE_ANALYST'")) {
                int deletedRows = ps.executeUpdate();
                log.info("Deleted obsolete ROLE_ANALYST row from roles table (count: {}).", deletedRows);
            }

            conn.commit();
            log.info("Pre-Hibernate role migration successfully committed!");

        } catch (Exception e) {
            log.error("Pre-Hibernate role migration encountered error: {}", e.getMessage(), e);
        }
    }
}
