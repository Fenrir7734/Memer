package com.fenrir.Memer.database;

import com.fenrir.Memer.exceptions.MigrationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DatabaseMigrationService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigrationService.class);

    private static final String INFO_DIR_NAME = "info";
    private static final String MIGRATION_FILE_NAME = "migration.txt";
    private static final String MIGRATED_FILE_NAME = "migrated.txt";

    private final DatabaseDriver databaseDriver = DatabaseDriver.getInstance();
    private final StatementLoader statementLoader = new StatementLoader();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final String migrationDirPath;

    public DatabaseMigrationService(String migrationDirPath) {
        this.migrationDirPath = migrationDirPath;
        scheduledExecutorService.scheduleWithFixedDelay(this::runMigration, 5, 5, TimeUnit.MINUTES);
    }

    private synchronized void runMigration() {
        try {
            migrate();
        } catch (MigrationException | IOException | SQLException e) {
            logger.error("Migration failed. {}", e.getMessage());
        }
    }

    public synchronized void migrate() throws IOException, SQLException, MigrationException {
        logger.info("Running migration...");
        Deque<String> migrationQueue = readInfoFile(MIGRATION_FILE_NAME);
        Deque<String> migratedQueue = readInfoFile(MIGRATED_FILE_NAME);

        if (!isFilesContentConsistent(migrationQueue, migratedQueue)) {
            logger.error("Inconsistency between the migration and migrated files.");
            throw new MigrationException("Inconsistency between the migration and migrated files.");
        }

        Deque<String> newMigrations = getNewMigrations(migrationQueue, migratedQueue);
        if (!newMigrations.isEmpty()) {
            logger.info("{} new migrations.", newMigrations.size());
            try {
                processMigrations(newMigrations, migratedQueue);
            } finally {
                saveFinishedMigrations(migratedQueue);
            }
        } else {
            logger.info("New migrations not found.");
        }
        logger.info("Migration finished.");
    }

    private Deque<String> readInfoFile(String fileName) throws IOException {
        logger.info("Reading {} file...", fileName);
        String migrationFilePath = Path.of(migrationDirPath)
                .resolve(INFO_DIR_NAME)
                .resolve(fileName)
                .toString();
        Deque<String> fileContent = new ArrayDeque<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(migrationFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.offerLast(line);
            }

            return fileContent;
        } catch (FileNotFoundException e) {
            logger.info("{} file not found. Path to migration file should be {}", fileName, migrationFilePath);
            throw e;
        } catch (IOException e) {
            logger.info("Failed to read {} file.", fileName);
            throw e;
        }
    }

    private boolean isFilesContentConsistent(Deque<String> migrationQueue, Deque<String> migratedQueue) {
        if (migrationQueue.size() < migratedQueue.size()) {
            return false;
        }

        Deque<String> migrationQueueClone = new ArrayDeque<>(migrationQueue);
        Deque<String> migratedQueueClone = new ArrayDeque<>(migratedQueue);

        while (!migratedQueueClone.isEmpty()) {
            String migrationEntry = migrationQueueClone.pollFirst();
            String migratedEntry = migratedQueueClone.pollFirst();

            if (!migratedEntry.equals(migrationEntry)) {
                return false;
            }
        }
        return true;
    }

    private Deque<String> getNewMigrations(Deque<String> migrationQueue, Deque<String> migratedQueue) {
        if (migratedQueue.size() == migrationQueue.size()) {
            return new ArrayDeque<>();
        }

        Deque<String> toMigrate = new ArrayDeque<>();
        while (migrationQueue.size() != migratedQueue.size() && !migrationQueue.isEmpty()) {
            String migration = migrationQueue.pollLast();
            toMigrate.offerFirst(migration);
        }
        return toMigrate;
    }

    private void processMigrations(Deque<String> toMigrate, Deque<String> migrated) throws IOException, SQLException {
        logger.info("Starting processing migrations...");
        while (!toMigrate.isEmpty()) {
            String migration = toMigrate.pollFirst();
            String sqlFile = String.format("%s.sql", migration);
            logger.info("Processing migration `{}`...", migration);
            String pathToSqlFile = Path.of(migrationDirPath).resolve(sqlFile).toString();
            String sqlStatement = statementLoader.load(pathToSqlFile);
            executeMigration(sqlStatement);
            migrated.offerLast(migration);
            logger.info("Migration `{}` processed.", migration);
        }
        logger.info("Processing migrations finished.");
    }

    private void executeMigration(String sqlStatement) throws SQLException {
        logger.info("Executing sql statement...");

        try (
                Connection connection = databaseDriver.getConnection();
                PreparedStatement statement = connection.prepareStatement(sqlStatement)
        ) {
            statement.execute();
        } catch (SQLException e) {
            logger.error("Failed to execute sql statement. {}", e.getMessage());
            throw e;
        }
        logger.info("Statement executed.");
    }

    private void saveFinishedMigrations(Deque<String> finishedMigrations) throws IOException {
        logger.info("Saving migrations...");
        String migratedFile = Path.of(migrationDirPath)
                .resolve(INFO_DIR_NAME)
                .resolve(MIGRATED_FILE_NAME)
                .toString();

        try (FileWriter writer = new FileWriter(migratedFile, false)) {
            while (!finishedMigrations.isEmpty()) {
                String migration = finishedMigrations.pollFirst();
                String entryToSave = String.format("%s\n", migration);
                writer.write(entryToSave);
            }
        } catch (IOException e) {
            logger.error("Failed to save migrations. {}", e.getMessage());
            throw e;
        }
        logger.info("Migrations saved.");
    }

    public static synchronized boolean checkIfTableExists(DatabaseDriver driver, String table) throws SQLException {
        try (Connection connection = driver.getConnection()) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet tables = databaseMetaData.getTables(null, null, table, null);
            return tables.next();
        }
    }
}
