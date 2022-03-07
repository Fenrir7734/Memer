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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DirectMessageListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(DirectMessageListener.class);

    private final Settings settings;
    private final CommandManager commandManager;
    private final Executor executor = Executors.newFixedThreadPool(16);

    public DirectMessageListener(Memer memer) {
        this.settings = memer.getSettings();
        this.commandManager = memer.getCommandManager();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || !event.isFromType(ChannelType.PRIVATE)) {
            return;
        }

        String prefix = settings.getPrefix();
        String messageContent = event.getMessage().getContentRaw().trim();
        if (!messageContent.startsWith(prefix)) {
            return;
        }

        try {
            CommandEvent commandEvent = new CommandEvent(event);
            executor.execute(() -> handleCommand(commandEvent));
        } catch (Exception e) {
            logger.error("An error occurred during direct command execution: {}", e.getMessage());
        }
    }

    private void handleCommand(CommandEvent commandEvent) {
        Optional<Command> commandOptional = commandManager.search(commandEvent.getCommandName());
        if (commandOptional.isPresent()) {
            Command command = commandOptional.get();
            command.execute(commandEvent);
        } else {
            commandEvent.getChannel()
                    .sendMessage("I have not heard of this command.")
                    .queue();
        }
    }
}
