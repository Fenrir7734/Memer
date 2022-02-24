package com.fenrir.Memer.listener;

import com.fenrir.Memer.Memer;
import com.fenrir.Memer.Settings;
import com.fenrir.Memer.command.CommandEvent;
import com.fenrir.Memer.command.CommandManager;
import com.fenrir.Memer.command.commands.Command;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class DirectMessageListener extends ListenerAdapter {
    private final Settings settings;
    private final CommandManager commandManager;

    public DirectMessageListener(Memer memer) {
        this.settings = memer.getSettings();
        this.commandManager = memer.getCommandManager();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.isFromType(ChannelType.PRIVATE)) {
            return;
        }

        String prefix = settings.getPrefix();
        String messageContent = event.getMessage().getContentRaw();
        if (!messageContent.startsWith(prefix)) {
            return;
        }

        CommandEvent commandEvent = new CommandEvent(event);
        Optional<Command> optionalCommand = commandManager.search(commandEvent.getCommandName());
        if (optionalCommand.isPresent()) {
            Command command = optionalCommand.get();
            command.execute(commandEvent);
        } else {
            event.getChannel()
                    .sendMessage("I have not heard of this command")
                    .queue();
        }
    }
}
