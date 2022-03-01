package com.fenrir.Memer.command.commands;

import com.fenrir.Memer.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.concurrent.TimeUnit;

public class Ping implements Command {
    private final Permission[] userPermission = new Permission[] { Permission.MESSAGE_SEND };

    @Override
    public void execute(CommandEvent commandEvent) {
        MessageReceivedEvent event = commandEvent.getEvent();
        long currentTime = System.currentTimeMillis();

        event.getChannel().sendMessage("Pong")
                .queue(time -> time.editMessageFormat("Pong " + (System.currentTimeMillis() - currentTime) + "ms")
                        .queue(message -> message.delete().queueAfter(2, TimeUnit.MINUTES))
                );
    }

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public String getDescription() {
        return "Checks if the bot is available";
    }

    @Override
    public String getExample() {
        return "**<prefix>**ping";
    }

    @Override
    public Permission[] getUserPermission() {
        return userPermission;
    }
}
