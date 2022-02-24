package com.fenrir.Memer.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public class StatementLoader {
    private static final Logger logger = LoggerFactory.getLogger(StatementLoader.class);

    public String load(String filePath) throws IOException {
        logger.info("Attempting to load sql statement from {}", filePath);

        StringBuilder statementBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                statementBuilder.append(line)
                        .append(" ");
            }
        } catch (IOException e) {
            logger.error("Failed to load sql statement. {}", e.getMessage());
            throw e;
        }

        logger.info("Statement loaded.");
        return statementBuilder.toString();
    }

    public String load(String dirPath, String statement) throws IOException {
        String fileName = String.format("%s.sql", statement);
        String path = Path.of(dirPath)
                .resolve(fileName)
                .toString();
        return load(path);
    }
}
