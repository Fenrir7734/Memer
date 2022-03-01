package com.fenrir.Memer;

import com.fenrir.Memer.command.CommandManager;
import com.fenrir.Memer.command.commands.Ping;
import com.fenrir.Memer.database.DatabaseService;
import com.fenrir.Memer.exceptions.MigrationException;
import com.fenrir.Memer.listener.DirectMessageListener;
import com.fenrir.Memer.listener.GuildEventListener;
import com.fenrir.Memer.listener.GuildMessageListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.checkerframework.checker.units.qual.C;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class Memer {
    private static final Logger logger = LoggerFactory.getLogger(Memer.class);

    private JDA client;
    private Settings settings;
    private CommandManager commandManager;
    private DatabaseService databaseService;

    public Memer() {
        logger.info("Starting...");
        try {
            loadSettings();
            databaseService = new DatabaseService(settings.getSqlPath());
            loadDefaultCommands();
            bootBot();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Stopping bot.");
            System.exit(0);
        }
        logger.info("The bot has been successfully build and is ready to go!");
    }

    private void loadSettings() throws IOException {
        logger.info("Trying to read settings...");
        settings = new Settings();
        try {
            settings.load();
        } catch (JSONException e) {
            logger.error("The settings.json file is invalid");
            throw e;
        } catch (IOException e) {
            logger.error("Failed to read settings.json file");
            throw e;
        }
        logger.info("Settings loaded");
    }

    public void bootBot() throws LoginException, InterruptedException {
        logger.info("Trying to build new JDA instance...");
        try {
            client = JDABuilder.createLight(
                    settings.getToken(),
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.DIRECT_MESSAGES
            )
                    .addEventListeners(
                            new GuildMessageListener(this),
                            new DirectMessageListener(this),
                            new GuildEventListener(this)
                    )
                    .build();
            client.awaitReady();
        } catch (LoginException | IllegalArgumentException e) {
            logger.error("New JDA instance could not be created. Check if provided token is valid");
            throw e;
        } catch (InterruptedException e) {
            logger.error("Thread was interrupted during creating new JDA instance");
            throw e;
        }
        logger.info("JDA instance build successfully!");
    }

    public void loadDefaultCommands() {
        logger.info("Loading default commands...");
        commandManager = new CommandManager(
                new Ping()
        );
        logger.info("Commands loaded.");
    }

    public JDA getClient() {
        return client;
    }

    public Settings getSettings() {
        return settings;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public DatabaseService getDatabaseService() {
        return databaseService;
    }

    public Optional<TextChannel> findChannelWithSendingPermission(Guild guild) {
        TextChannel channel = guild.getDefaultChannel();
        if (channel == null || !channel.canTalk()) {
            return guild.getTextChannels()
                    .stream()
                    .filter(TextChannel::canTalk)
                    .findAny();
        }
        return Optional.of(channel);
    }

    public Optional<TextChannel> findChannelWithSendingPermission(MessageReceivedEvent event) {
        TextChannel channel = event.getTextChannel();
        if (channel.canTalk()) {
            return Optional.of(channel);
        }
        return findChannelWithSendingPermission(event.getGuild());
    }

    public static void main(String[] args) {
        new Memer();
    }
}
