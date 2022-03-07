package com.fenrir.Memer.database.services;

import com.fenrir.Memer.database.dao.GuildDAO;
import com.fenrir.Memer.database.entities.GuildDB;
import com.fenrir.Memer.exceptions.DatabaseException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class GuildService {
    private final GuildDAO dao;
    private final Cache<Long, Optional<GuildDB>> guildCache = Caffeine.newBuilder()
            .expireAfterAccess(3, TimeUnit.MINUTES)
            .maximumSize(200)
            .build();

    public GuildService(String DMLDirPath) throws DatabaseException, SQLException, IOException {
        this.dao = new GuildDAO(DMLDirPath);
    }

    public Optional<GuildDB> get(long id) {
        return guildCache.get(id, k -> dao.select(id));
    }

    public boolean add(GuildDB guildDB) {
        return dao.insert(guildDB);
    }

    public boolean update(GuildDB guildDB) {
        boolean result = dao.update(guildDB);
        invalidate(guildDB.getId());
        return result;
    }

    public boolean remove(GuildDB guildDB) {
        boolean result = dao.delete(guildDB);
        guildCache.invalidate(guildDB.getId());
        return result;
    }

    public boolean remove(long id) throws DatabaseException {
        Optional<GuildDB> guildDB = get(id);
        if (guildDB.isPresent()) {
            boolean result = remove(guildDB.get());
            invalidate(id);
            return result;
        } else {
            throw new DatabaseException("Guild not found.");
        }
    }

    public Optional<GuildDB> refresh(long id) {
        guildCache.invalidate(id);
        return get(id);
    }

    public void invalidate(long id) {
        guildCache.invalidate(id);
    }
}
