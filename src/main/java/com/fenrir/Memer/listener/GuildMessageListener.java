package com.fenrir.Memer.listener;

import com.fenrir.Memer.Memer;
import com.fenrir.Memer.command.CommandEvent;
import com.fenrir.Memer.command.CommandManager;
import com.fenrir.Memer.command.commands.Command;
import com.fenrir.Memer.database.DatabaseService;
import com.fenrir.Memer.database.entities.GuildDB;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GuildMessageListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(GuildMessageListener.class);

    private final Memer memer;
    private final CommandManager commandManager;
    private final DatabaseService databaseService;
    private final Executor executor = Executors.newFixedThreadPool(64);

    public GuildMessageListener(Memer memer) {
        this.memer = memer;
        this.commandManager = memer.getCommandManager();
        this.databaseService = memer.getDatabaseService();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()
                || event.isFromType(ChannelType.PRIVATE)
                || !event.isFromType(ChannelType.TEXT)
        ) {
            return;
        }

        long guildId = event.getGuild().getIdLong();
        Optional<GuildDB> guildDBOptional = databaseService.getGuildService().get(guildId);
        if (guildDBOptional.isEmpty()) {
            Optional<TextChannel> channelOptional = memer.findChannelWithSendingPermission(event);
            channelOptional.ifPresentOrElse(
                    channel -> channel.sendMessage(
                            "It's appear that your guild is not present in my database. " +
                                    "To prevent greater problems from occurring I have to leave this guild. " +
                                    "To try and fix this problem, you can try to add me again. " +
                                    "If this does not solve the problem, contact the bot administration."
                    ).queue(message -> message.getGuild().leave().queue()),
                    () -> event.getGuild().leave().queue()
            );
            logger.warn(
                    "Guild `{}` not found in database. Leaving guild. Guild id: {}",
                    event.getGuild().getName(),
                    guildId
            );
            return;
        }

        if (!event.getChannel().canTalk()) {
            return;
        }

        GuildDB guildDB = guildDBOptional.get();
        String prefix = guildDB.getPrefix();
        String messageContent = event.getMessage().getContentRaw().trim();
        if (!messageContent.startsWith(prefix)) {
            return;
        }

        try {
            CommandEvent commandEvent = new CommandEvent(event, guildDB);
            executor.execute(() -> handleCommand(commandEvent));
        } catch (Exception e) {
            logger.error("An error occurred during guild command execution: {}", e.getMessage());
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
