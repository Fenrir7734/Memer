package com.fenrir.Memer.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseDriver {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseDriver.class);

    private final HikariDataSource dataSource;

    public DatabaseDriver() {
        logger.info("Attempting to connect to the database...");
        HikariConfig config = new HikariConfig("database.properties");
        dataSource = new HikariDataSource(config);
        logger.info("Connection to database established.");
    }

    public static DatabaseDriver getInstance() {
        return DatabaseDriverSingleton.instance;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private static class DatabaseDriverSingleton {
        private static final DatabaseDriver instance = new DatabaseDriver();
    }
}
