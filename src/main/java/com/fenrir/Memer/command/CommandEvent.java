package com.fenrir.Memer.command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;

public class CommandEvent {
    private final MessageReceivedEvent event;
    private String commandName;
    private String[] args;

    public CommandEvent(MessageReceivedEvent event) {
        this.event = event;
        parseMessage(event.getMessage());
    }

    private void parseMessage(Message message) {
        String[] content = message.getContentRaw()
                .split(" ");
        commandName = content[0].substring(1);
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

    public String[] getArgs() {
        return args;
    }
}
