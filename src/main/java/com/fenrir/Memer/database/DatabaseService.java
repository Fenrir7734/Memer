package com.fenrir.Memer.database;

import com.fenrir.Memer.exceptions.MigrationException;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

public class DatabaseService {
    private static final String DDL_DIR_NAME = "DDL";
    private static final String DML_DIR_NAME = "DML";

    private final String sqlPath;
    private final DatabaseMigrationService databaseMigrationService;

    public DatabaseService(String sqlPath) throws MigrationException, SQLException, IOException {
        String DDLDirPath = Path.of(sqlPath).resolve(DDL_DIR_NAME).toString();
        this.sqlPath = sqlPath;
        this.databaseMigrationService = new DatabaseMigrationService(DDLDirPath);
        this.databaseMigrationService.migrate();
    }
}
