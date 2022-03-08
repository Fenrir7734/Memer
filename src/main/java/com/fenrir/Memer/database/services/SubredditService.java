package com.fenrir.Memer.database.services;

import com.fenrir.Memer.database.dao.GuildResourceDAO;
import com.fenrir.Memer.database.dao.SubredditDAO;
import com.fenrir.Memer.database.entities.SubredditDB;
import com.fenrir.Memer.database.managers.GuildResourceEntityManager;
import com.fenrir.Memer.exceptions.DatabaseException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class SubredditService implements GuildResourceService<SubredditDB> {
    private final GuildResourceDAO<SubredditDB> dao;
    private final int limit;
    private final Cache<Long, GuildResourceEntityManager<SubredditDB>> guildSubredditsCache = Caffeine.newBuilder()
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .maximumSize(200)
            .build();
    public SubredditService(String DMLDirPath, int limit) throws DatabaseException, SQLException, IOException {
        this.dao = new SubredditDAO(DMLDirPath);
        this.limit = limit;
    }

    @Override
    public GuildResourceEntityManager<SubredditDB> get(long id) {
        return guildSubredditsCache.get(id, k -> new GuildResourceEntityManager<>(id, limit, dao));
    }

    @Override
    public void invalidate(long id) {
        guildSubredditsCache.invalidate(id);
    }

    @Override
    public GuildResourceEntityManager<SubredditDB> refresh(long id) {
        invalidate(id);
        return get(id);
    }
}
