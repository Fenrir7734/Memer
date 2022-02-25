package com.fenrir.Memer.database.services;

import com.fenrir.Memer.database.dao.GuildResourceDAO;
import com.fenrir.Memer.database.dao.ImgurTagDAO;
import com.fenrir.Memer.database.entities.ImgurTagDB;
import com.fenrir.Memer.database.managers.GuildResourceEntityManager;
import com.fenrir.Memer.exceptions.DatabaseException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class ImgurTagService implements GuildResourceService<ImgurTagDB> {
    private final GuildResourceDAO<ImgurTagDB> dao;
    private final Cache<Long, GuildResourceEntityManager<ImgurTagDB>> guildSubredditsCache = Caffeine.newBuilder()
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .maximumSize(200)
            .build();

    public ImgurTagService(String DMLDirPath) throws DatabaseException, SQLException, IOException {
        this.dao = new ImgurTagDAO(DMLDirPath);
    }

    @Override
    public GuildResourceEntityManager<ImgurTagDB> get(long id) {
        return guildSubredditsCache.get(id, k -> new GuildResourceEntityManager<>(id, 30, dao));
    }

    @Override
    public void invalidate(long id) {
        guildSubredditsCache.invalidate(id);
    }

    @Override
    public GuildResourceEntityManager<ImgurTagDB> refresh(long id) {
        invalidate(id);
        return get(id);
    }
}
