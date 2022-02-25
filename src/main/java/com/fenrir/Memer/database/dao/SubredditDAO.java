package com.fenrir.Memer.database.dao;

import com.fenrir.Memer.database.DatabaseDriver;
import com.fenrir.Memer.database.DatabaseMigrationService;
import com.fenrir.Memer.database.StatementLoader;
import com.fenrir.Memer.database.entities.SubredditDB;
import com.fenrir.Memer.exceptions.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SubredditDAO implements GuildResourceDAO<SubredditDB> {
    private static final Logger logger = LoggerFactory.getLogger(SubredditDAO.class);
    private static final String TABLE_NAME = "subreddit";

    private final DatabaseDriver databaseDriver = DatabaseDriver.getInstance();

    private final String insertStatement;
    private final String updateStatement;
    private final String deleteStatement;
    private final String selectStatement;

    public SubredditDAO(String DMLDirPath) throws IOException, SQLException, DatabaseException {
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

    public List<SubredditDB> select(long guildId) {
        try (
                Connection connection = databaseDriver.getConnection();
                PreparedStatement statement = connection.prepareStatement(selectStatement)
        ) {
            statement.setLong(1, guildId);
            ResultSet resultSet = statement.executeQuery();

            List<SubredditDB> subreddits = new ArrayList<>();

            if (resultSet == null) {
                return subreddits;
            }

            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String subredditName = resultSet.getString("name");
                SubredditDB subreddit = new SubredditDB(id, subredditName, guildId);
                subreddits.add(subreddit);
            }

            return subreddits;
        } catch (SQLException e) {
            logger.error(
                    "Could not retrieve guilds subreddits from database. Guild id: {} operation: SELECT cause: {}",
                    guildId,
                    e.getMessage()
            );
        }
        return null;
    }

    public boolean insert(SubredditDB subredditDB) {
        try (
                Connection connection = databaseDriver.getConnection();
                PreparedStatement statement = connection.prepareStatement(insertStatement)
        ) {
            statement.setString(1, subredditDB.getName());
            statement.setLong(2, subredditDB.getGuildId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error(
                    "Could not insert subreddit for a guild. Guild id: {} operation: INSERT cause: {}",
                    subredditDB.getGuildId(),
                    e.getMessage()
            );
        }
        return false;
    }

    public boolean update(SubredditDB subredditDB) {
        try (
                Connection connection = databaseDriver.getConnection();
                PreparedStatement statement = connection.prepareStatement(updateStatement)
        ) {
            if (subredditDB.getId().isEmpty()) {
                logger.error("Update failed. ID of row to update is not present.");
                return false;
            }

            statement.setString(1, subredditDB.getName());
            statement.setLong(2, subredditDB.getGuildId());
            statement.setLong(3, subredditDB.getId().get());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error(
                    "Could not update subreddit of a guild. Guild id: {} operation: UPDATE cause: {}",
                    subredditDB.getGuildId(),
                    e.getMessage()
            );
        }
        return false;
    }

    public boolean delete(SubredditDB subredditDB) {
        try (
                Connection connection = databaseDriver.getConnection();
                PreparedStatement statement = connection.prepareStatement(deleteStatement)
        ) {
            if (subredditDB.getId().isEmpty()) {
                logger.error("Delete failed. ID of row to delete is not present.");
                return false;
            }

            statement.setLong(1, subredditDB.getId().get());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error(
                    "Could not delete subreddit of a guild. Guild id: {} operation: DELETE cause: {}",
                    subredditDB.getGuildId(),
                    e.getMessage()
            );
        }
        return false;
    }
}
