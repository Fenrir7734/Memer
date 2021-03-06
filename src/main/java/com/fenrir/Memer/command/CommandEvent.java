package com.fenrir.Memer.command;

import com.fenrir.Memer.database.entities.GuildDB;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Optional;

public class CommandEvent {
    private final MessageReceivedEvent event;
    private GuildDB guildDB;
    private String commandName;
    private String[] args;

    public CommandEvent(MessageReceivedEvent event, GuildDB guildDB) {
        this.event = event;
        this.guildDB = guildDB;
        parseMessage(event.getMessage());
    }

    public CommandEvent(MessageReceivedEvent event) {
        this(event, null);
    }

    private void parseMessage(Message message) {
        String[] content = message.getContentRaw()
                .split(" ");
        String prefix = guildDB.getPrefix();
        commandName = content[0].replaceFirst(prefix, "");
        args = content.length > 1 ? Arrays.copyOfRange(content, 1, content.length) : new String[0];
    }

    public MessageReceivedEvent getEvent() {
        return event;
    }

    public String getCommandName() {
        return commandName;
    }

    public TextChannel getChannel() {
        return event.getTextChannel();
    }

    public Member getAuthor() {
        return event.getMember();
    }

    public Member getBot() {
        return event.getGuild().getSelfMember();
    }

    public long getGuildId() {
        return event.getGuild().getIdLong();
    }

    public Optional<GuildDB> getGuildDB() {
        return Optional.ofNullable(guildDB);
    }

    public String[] getArgs() {
        return args;
    }
}
