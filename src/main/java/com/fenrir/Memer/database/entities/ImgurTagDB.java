package com.fenrir.Memer.database.entities;

import java.util.Optional;

public class ImgurTagDB implements GuildResourceEntity {
    private final Long id;
    private final String name;
    private final long guildId;

    public ImgurTagDB(Long id, String name, long guildId) {
        this.id = id;
        this.name = name;
        this.guildId = guildId;
    }

    public ImgurTagDB(String name, long guildId) {
        this(null, name, guildId);
    }

    public Optional<Long> getId() {
        return Optional.ofNullable(id);
    }

    public String getName() {
        return name;
    }

    public long getGuildId() {
        return guildId;
    }

    @Override
    public String toString() {
        return "ImgurTagDB{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", guildId=" + guildId +
                '}';
    }
}
