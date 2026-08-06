package com.hrms.migration;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EmployeeDirectoryMigrationChange implements CustomTaskChange {

    @Override
    public void execute(Database database) throws CustomChangeException {
        try {
            JdbcConnection connection = (JdbcConnection) database.getConnection();

            String selectSql = "SELECT id, first_name, last_name FROM employee";
            String insertSql = "INSERT INTO employee_directory (employee_id, first_name, last_name) VALUES (?, ?, ?)";
            String existsSql = "SELECT 1 FROM employee_directory WHERE employee_id = ?";

            try (PreparedStatement selectStmt = connection.prepareStatement(selectSql);
                 ResultSet rs = selectStmt.executeQuery()) {

                while (rs.next()) {
                    Long employeeId = rs.getLong("id");
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");

                    try (PreparedStatement existsStmt = connection.prepareStatement(existsSql)) {
                        existsStmt.setLong(1, employeeId);
                        try (ResultSet existsRs = existsStmt.executeQuery()) {
                            if (existsRs.next()) {
                                continue;
                            }
                        }
                    }

                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                        insertStmt.setLong(1, employeeId);
                        insertStmt.setString(2, firstName);
                        insertStmt.setString(3, lastName);
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            throw new CustomChangeException("Failed to migrate employee data into employee_directory", e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "Employee directory data migration completed successfully.";
    }

    @Override
    public void setUp() throws SetupException {

    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {

    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }
}