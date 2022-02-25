package com.fenrir.Memer.database.managers;

import com.fenrir.Memer.database.dao.GuildResourceDAO;
import com.fenrir.Memer.database.entities.GuildResourceEntity;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GuildResourceEntityManager<T extends GuildResourceEntity> {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final GuildResourceDAO<T> dao;
    private final long guildId;
    private final List<T> entities;
    private final int limit;

    public GuildResourceEntityManager(long guildId, int limit, GuildResourceDAO<T> dao) {
        this.guildId = guildId;
        this.entities = dao.select(guildId);
        this.limit = limit;
        this.dao = dao;
    }

    public T get(int i) {
        lock.readLock().lock();
        try {
            return entities.size() > i ? entities.get(i) : null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public T getRandom() {
        lock.readLock().lock();
        try {
            int size = entities.size();
            int rnd = ThreadLocalRandom.current().nextInt(size);
            return entities.get(rnd);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean contains(T entity) {
        lock.readLock().lock();
        try {
            return entities.contains(entity);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean contains(String name) {
        lock.readLock().lock();
        try {
            return entities.stream()
                    .map(T::getName)
                    .anyMatch(s -> s.equals(name));
        } finally {
            lock.readLock().unlock();
        }
    }

    public int size() {
        lock.readLock().lock();
        try {
            return entities.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean add(T entity) {
        lock.writeLock().lock();
        try {
            if (entities.size() < limit && isUniq(entity)) {
                if (dao.insert(entity)) {
                    refresh();
                    return true;
                }
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private synchronized boolean isUniq(T entity) {
        for (T e: entities) {
            String existingEntityName = e.getName();
            String newEntityName = e.getName();

            if (existingEntityName.equals(newEntityName)) {
                return false;
            }
        }
        return true;
    }

    public boolean remove(T entity) {
        lock.writeLock().lock();
        try {
            return dao.delete(entity) && entities.remove(entity);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public long getGuildId() {
        return guildId;
    }

    private void refresh() {
        lock.writeLock().lock();
        try {
            List<T> refreshedEntities = dao.select(guildId);
            entities.clear();
            entities.addAll(refreshedEntities);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
