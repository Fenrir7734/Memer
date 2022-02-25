package com.fenrir.Memer.database.entities;

public class GuildDB {
    private final Long id;
    private final String prefix;
    private final boolean nsfw;

    public GuildDB(Long guildId, String prefix, boolean nsfw) {
        this.id = guildId;
        this.prefix = prefix;
        this.nsfw = nsfw;
    }

    public Long getId() {
        return id;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isNsfw() {
        return nsfw;
    }

    @Override
    public String toString() {
        return "GuildDB{" +
                "id=" + id +
                ", prefix='" + prefix + '\'' +
                ", nsfw=" + nsfw +
                '}';
    }
}
