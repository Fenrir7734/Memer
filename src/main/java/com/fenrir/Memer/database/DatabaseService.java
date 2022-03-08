package com.fenrir.Memer.database;

import com.fenrir.Memer.Settings;
import com.fenrir.Memer.database.entities.ImgurTagDB;
import com.fenrir.Memer.database.entities.SubredditDB;
import com.fenrir.Memer.database.services.GuildResourceService;
import com.fenrir.Memer.database.services.GuildService;
import com.fenrir.Memer.database.services.ImgurTagService;
import com.fenrir.Memer.database.services.SubredditService;
import com.fenrir.Memer.exceptions.DatabaseException;
import com.fenrir.Memer.exceptions.MigrationException;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

public class DatabaseService {
    private static final String DDL_DIR_NAME = "DDL";
    private static final String DML_DIR_NAME = "DML";

    private final String sqlPath;
    private final DatabaseMigrationService databaseMigrationService;

    private final GuildService guildService;
    private final GuildResourceService<SubredditDB> subredditService;
    private final GuildResourceService<ImgurTagDB> imgurTagService;

    public DatabaseService(Settings settings) throws MigrationException, SQLException, IOException, DatabaseException {
        String sqlPath = settings.getSqlPath();
        String DDLDirPath = Path.of(sqlPath).resolve(DDL_DIR_NAME).toString();
        this.sqlPath = sqlPath;
        this.databaseMigrationService = new DatabaseMigrationService(DDLDirPath);
        this.databaseMigrationService.migrate();

        String DMLPath = Path.of(sqlPath).resolve(DML_DIR_NAME).toString();
        this.guildService = new GuildService(DMLPath);
        this.subredditService = new SubredditService(DMLPath, settings.getSubredditsGuildLimit());
        this.imgurTagService = new ImgurTagService(DMLPath, settings.getImgurTagsGuildLimit());
    }

    public GuildService getGuildService() {
        return guildService;
    }

    public GuildResourceService<SubredditDB> getSubredditService() {
        return subredditService;
    }

    public GuildResourceService<ImgurTagDB> getImgurTagService() {
        return imgurTagService;
    }
}
