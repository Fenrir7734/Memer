package com.fenrir.Memer;

import com.fenrir.Memer.api.Imgur;
import com.fenrir.Memer.api.Reddit;
import com.fenrir.Memer.command.CommandManager;
import com.fenrir.Memer.command.commands.Help;
import com.fenrir.Memer.command.commands.Meme;
import com.fenrir.Memer.command.commands.Ping;
import com.fenrir.Memer.database.DatabaseService;
import com.fenrir.Memer.exceptions.BotInvalidSettingsException;
import com.fenrir.Memer.exceptions.HttpException;
import com.fenrir.Memer.listener.DirectMessageListener;
import com.fenrir.Memer.listener.GuildEventListener;
import com.fenrir.Memer.listener.GuildMessageListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Optional;

public class Memer {
    private static final Logger logger = LoggerFactory.getLogger(Memer.class);

    private JDA client;
    private Settings settings;
    private CommandManager commandManager;
    private DatabaseService databaseService;
    private Reddit redditMediaProvider;
    private Imgur imgurMediaProvider;

    public Memer() {
        logger.info("Starting...");
        try {
            loadSettings();
            databaseService = new DatabaseService(settings);
            loadMediaProviders();
            loadDefaultCommands();
            bootBot();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Stopping bot.");
            System.exit(0);
        }
        logger.info("The bot has been successfully build and is ready to go!");
    }

    private void loadSettings() throws IOException, BotInvalidSettingsException {
        logger.info("Trying to read settings...");
        settings = new Settings();
        try {
            settings.load();
        } catch (JSONException e) {
            throw e;
        } catch (IOException e) {
            logger.error("Failed to read settings.json file.");
            throw e;
        } catch (BotInvalidSettingsException e) {
            logger.error("Provided settings are invalid.");
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
            logger.error("New JDA instance could not be created. Check if provided token is valid.");
            throw e;
        } catch (InterruptedException e) {
            logger.error("Thread was interrupted during creating new JDA instance.");
            throw e;
        }
        logger.info("JDA instance build successfully!");
    }

    public void loadMediaProviders() throws IOException, InterruptedException, HttpException {
        logger.info("Loading media providers...");
        redditMediaProvider = new Reddit(settings.getRedditRefresh());
        try {
            imgurMediaProvider = new Imgur(settings.getImgurClientId(), settings.getImgurRefresh());
        } catch (IOException | InterruptedException e) {
            logger.error("An error occurred during loading imgur media provider. {}", e.getMessage());
            throw e;
        } catch (HttpException e) {
            logger.error(
                    "An error occurred during loading imgur media provider. {}. Status code: {}",
                    e.getMessage(), e.getStatusCode());
            throw e;
        }

    }

    public void loadDefaultCommands() {
        logger.info("Loading default commands...");
        commandManager = new CommandManager(
                new Ping(),
                new Meme(this),
                new com.fenrir.Memer.command.commands.Settings(this),
                new Help(this)
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

    public Reddit getRedditMediaProvider() {
        return redditMediaProvider;
    }

    public Imgur getImgurMediaProvider() {
        return imgurMediaProvider;
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
