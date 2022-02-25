package com.fenrir.Memer.database.services;

import com.fenrir.Memer.database.entities.GuildResourceEntity;
import com.fenrir.Memer.database.managers.GuildResourceEntityManager;

public interface GuildResourceService<T extends GuildResourceEntity> {
    GuildResourceEntityManager<T> get(long id);
    void invalidate(long id);
    GuildResourceEntityManager<T> refresh(long id);
}
