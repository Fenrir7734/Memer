package com.fenrir.Memer.database.dao;

import com.fenrir.Memer.database.DatabaseDriver;
import com.fenrir.Memer.database.DatabaseMigrationService;
import com.fenrir.Memer.database.StatementLoader;
import com.fenrir.Memer.database.entities.ImgurTagDB;
import com.fenrir.Memer.exceptions.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImgurTagDAO implements GuildResourceDAO<ImgurTagDB> {
    private static final Logger logger = LoggerFactory.getLogger(ImgurTagDAO.class);
    private static final String TABLE_NAME = "imgur_tag";

    private final DatabaseDriver databaseDriver = DatabaseDriver.getInstance();

    private final String insertStatement;
    private final String updateStatement;
    private final String deleteStatement;
    private final String selectStatement;

    public ImgurTagDAO(String DMLDirPath) throws IOException, SQLException, DatabaseException {
        if (!DatabaseMigrationService.checkIfTableExists(databaseDriver, TABLE_NAME)) {
            String message = String.format("%s does not exists", TABLE_NAME);
            logger.error(message);
            throw new DatabaseException(message);
        }

        StatementLoader loader = new StatementLoader();
        String statementDirPath = Path.of(DMLDirPath).resolve(TABLE_NAME).toString();
        this.insertStatement = loader.load(statementDirPath, "INSERT");
        this.updateStatement = loader.load(statementDirPath, "UPDATE");
        this.deleteStatement = loader.load(statementDirPath, "DELETE");
        this.selectStatement = loader.load(statementDirPath, "SELECT");
    }

    public List<ImgurTagDB> select(long guildId) {
        try (
                Connection connection = databaseDriver.getConnection();
                PreparedStatement statement = connection.prepareStatement(selectStatement)
        ) {
            statement.setLong(1, guildId);
            ResultSet resultSet = statement.executeQuery();

            List<ImgurTagDB> imgurTags = new ArrayList<>();

            if (resultSet == null) {
                return imgurTags;
            }

            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String name = resultSet.getString("name");
                ImgurTagDB imgurTag = new ImgurTagDB(id, name, guildId);
                imgurTags.add(imgurTag);
            }

            return imgurTags;
        } catch (SQLException e) {
            logger.error(
                    "Could not retrieve guilds imgur tags from database. Guild id: {} operation: SELECT cause: {}",
                    guildId,
                    e.getMessage()
            );
        }
        return null;
    }

    public boolean insert(ImgurTagDB imgurTagDB) {
        try (
                Connection connection = databaseDriver.getConnection();
                PreparedStatement statement = connection.prepareStatement(insertStatement, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setString(1, imgurTagDB.getName());
            statement.setLong(2, imgurTagDB.getGuildId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error(
                    "Could not insert imgur tag for a guild. Guild id: {} operation: INSERT cause: {}",
                    imgurTagDB.getGuildId(),
                    e.getMessage()
            );
        }
        return false;
    }

    public boolean update(ImgurTagDB imgurTagDB) {
        try (Connection connection = databaseDriver.getConnection();
             PreparedStatement statement = connection.prepareStatement(updateStatement)
        ) {
            if (imgurTagDB.getId().isEmpty()) {
                logger.error("Update failed. ID of row to update is not present.");
                return false;
            }

            statement.setString(1, imgurTagDB.getName());
            statement.setLong(2, imgurTagDB.getGuildId());
            statement.setLong(3, imgurTagDB.getId().get());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error(
                    "Could not update imgur tag of a guild. Guild id: {} operation: UPDATE cause: {}",
                    imgurTagDB.getGuildId(),
                    e.getMessage()
            );
        }
        return false;
    }

    public boolean delete(ImgurTagDB imgurTagDB) {
        try (
                Connection connection = databaseDriver.getConnection();
                PreparedStatement statement = connection.prepareStatement(deleteStatement)
        ) {
            if (!imgurTagDB.getId().isPresent()) {
                logger.error("Delete failed. ID of row to delete is not present.");
                return false;
            }

            statement.setLong(1, imgurTagDB.getId().get());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error(
                    "Could not delete imgur tag of a guild. Guild id: {} operation: DELETE cause: {}",
                    imgurTagDB.getGuildId(),
                    e.getMessage()
            );
        }
        return false;
    }
}
