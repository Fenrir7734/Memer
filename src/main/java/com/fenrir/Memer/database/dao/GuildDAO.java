package com.fenrir.Memer.database.dao;

import com.fenrir.Memer.database.DatabaseDriver;
import com.fenrir.Memer.database.DatabaseMigrationService;
import com.fenrir.Memer.database.StatementLoader;
import com.fenrir.Memer.database.entities.GuildDB;
import com.fenrir.Memer.exceptions.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.*;
import java.util.Optional;

public class GuildDAO {
    private static final Logger logger = LoggerFactory.getLogger(GuildDAO.class);
    private static final String TABLE_NAME = "guild";

    private final DatabaseDriver databaseDriver = DatabaseDriver.getInstance();

    private final String insertStatement;
    private final String updateStatement;
    private final String deleteStatement;
    private final String selectStatement;

    public GuildDAO(String DMLDirPath) throws IOException, SQLException, DatabaseException {
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

    public Optional<GuildDB> select(Long id) {
        try (
                Connection connection = databaseDriver.getConnection();
                PreparedStatement statement = connection.prepareStatement(selectStatement)
        ) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();

            GuildDB guildDB = null;
            if (resultSet.next()) {
                String prefix = resultSet.getString("prefix");
                boolean nsfw = resultSet.getBoolean("nsfw");
                guildDB = new GuildDB(id, prefix, nsfw);
            }

            return Optional.ofNullable(guildDB);
        } catch (SQLException e) {
            logger.error(
                    "Could not retrieve guild from database. Guild id: {} operation: SELECT cause: {}",
                    id,
                    e.getMessage()
            );
        }
        return Optional.empty();
    }

    public boolean insert(GuildDB guildDB) {
        try (
                Connection connection = databaseDriver.getConnection();
                PreparedStatement statement = connection.prepareStatement(insertStatement)
        ) {
            statement.setLong(1, guildDB.getId());
            statement.setString(2, guildDB.getPrefix());
            statement.setBoolean(3, guildDB.isNsfw());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error(
                    "Could not insert guild. Guild id: {} operation: INSERT cause: {}",
                    guildDB.getId(),
                    e.getMessage()
            );
        }
        return false;
    }

    public boolean update(GuildDB guildDB) {
        try (
                Connection connection = databaseDriver.getConnection();
                PreparedStatement statement = connection.prepareStatement(updateStatement)
        ) {
            statement.setString(1, guildDB.getPrefix());
            statement.setBoolean(2, guildDB.isNsfw());
            statement.setLong(3, guildDB.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error(
                    "Could not update guild. Guild id: {} operation: UPDATE cause: {}",
                    guildDB.getId(),
                    e.getMessage()
            );
        }
        return false;
    }

    public boolean delete(GuildDB guildDB) {
        try (
                Connection connection = databaseDriver.getConnection();
                PreparedStatement statement = connection.prepareStatement(deleteStatement)
        ) {
            statement.setLong(1, guildDB.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error(
                    "Could not delete guild. Guild id: {} operation: DELETE cause: {}",
                    guildDB.getId(),
                    e.getMessage()
            );
        }
        return false;
    }
}
