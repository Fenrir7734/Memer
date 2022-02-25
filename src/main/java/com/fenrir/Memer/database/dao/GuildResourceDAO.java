package com.fenrir.Memer.database.dao;

import com.fenrir.Memer.database.entities.GuildResourceEntity;

import java.util.List;

public interface GuildResourceDAO<T extends GuildResourceEntity> {
    List<T> select(long id);
    boolean insert(T t);
    boolean update(T t);
    boolean delete(T t);
}
