package com.fenrir.Memer.command.commands;

import com.fenrir.Memer.Memer;
import com.fenrir.Memer.api.Reddit;
import com.fenrir.Memer.command.CommandEvent;
import com.fenrir.Memer.database.DatabaseService;
import com.fenrir.Memer.database.entities.GuildDB;
import com.fenrir.Memer.database.entities.GuildResourceEntity;
import com.fenrir.Memer.database.entities.ImgurTagDB;
import com.fenrir.Memer.database.entities.SubredditDB;
import com.fenrir.Memer.database.managers.GuildResourceEntityManager;
import com.fenrir.Memer.exceptions.BotRuntimeException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class Settings implements Command {
    private final Permission[] userPermission = new Permission[] { Permission.ADMINISTRATOR };

    private final DatabaseService databaseService;
    private final List<String> defaultImgurTags;
    private final Reddit redditMediaProvider;

    public Settings(Memer memer) {
        this.databaseService = memer.getDatabaseService();
        this.defaultImgurTags = memer.getSettings().getImgurTags();
        this.redditMediaProvider = memer.getRedditMediaProvider();
    }

    @Override
    public void execute(CommandEvent event) {
        Member userMember = event.getAuthor();
        TextChannel channel = event.getChannel();

        if (channel.getType() == ChannelType.PRIVATE) {
            channel.sendMessage("This command can be executed only within a guild.").queue();
            return;
        }

        if (!userMember.hasPermission(userPermission)) {
            channel.sendMessage("You can't execute this command on this channel.").queue();
            return;
        }

        parseCommand(event);
    }

    private void parseCommand(CommandEvent event) {
        TextChannel channel = event.getChannel();

        String[] args = event.getArgs();
        if (args.length == 1 && args[0].equals("list")) {
            listSettings(event);
        } else if (args.length == 2) {
            String action = args[0];
            if (action.equals("prefix")) {
                changePrefix(event, args[1]);
            } else if (action.equals("nsfw")) {
                changeNsfw(event, args[1]);
            } else {
                channel.sendMessage("Sorry, I don't recognize this action.").queue();
            }
        } else if (args.length == 3) {
            String action = args[0];
            if (action.equals("reddit")) {
                customizeReddit(event, args[1], args[2]);
            } else if (action.equals("imgur")) {
                customizeImgur(event, args[1], args[2]);
            } else {
                channel.sendMessage("Sorry, I don't recognize this site.").queue();
            }
        } else {
            channel.sendMessage(
                    "Sorry, I could not resolve this command. To check command usage type `<prefix>help settings`."
            ).queue();
        }
    }

    private void listSettings(CommandEvent event) {
        long guildId = event.getGuildId();
        GuildDB guildDB = event.getGuildDB()
                .orElseThrow(() -> new BotRuntimeException("Could not found guild settings."));

        MessageEmbed messageEmbed = new EmbedBuilder()
                .setTitle("Settings")
                .setColor(Color.GREEN)
                .setDescription(getGeneralSettingsAsString(guildDB))
                .appendDescription(getListOfSubreddits(guildId))
                .appendDescription(getListOfImgurTags(guildId))
                .build();
        event.getChannel().sendMessageEmbeds(messageEmbed).queue();
    }

    private String getGeneralSettingsAsString(GuildDB guildDB) {
        return String.format(
                """
                        **General**
                        ```
                        %-15s %s
                        %-15s %s
                        ```
                        """,
                "prefix",
                guildDB.getPrefix(),
                "NSFW",
                booleanToString(guildDB.isNsfw())
        );
    }

    private String getListOfSubreddits(long guildId) {
        List<String> subreddits = databaseService
                .getSubredditService()
                .get(guildId)
                .getAll()
                .stream()
                .map(SubredditDB::getName)
                .toList();

        String subredditsAsString = buildColumnLayout(subreddits, 3, 20, "");
        return String.format("**Reddit**\n```%s```\n", subredditsAsString);
    }

    private String getListOfImgurTags(long guildId) {
        List<String> imgurTags = databaseService
                .getImgurTagService()
                .get(guildId)
                .getAll()
                .stream()
                .map(ImgurTagDB::getName)
                .toList();
        List<String> differences = new ArrayList<>(defaultImgurTags);
        differences.removeAll(imgurTags);

        String enabledTags = buildColumnLayout(imgurTags, 2, 26, "+ ");
        String disabledTags = buildColumnLayout(differences, 2, 26, "- ");

        return String.format("**Imgur**\n```%s\n%s```\n", enabledTags, disabledTags);
    }

    private String buildColumnLayout(List<String> list, int colNum, int padding, String sign) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            String cell;
            if (i % colNum == (colNum - 1)) {
                cell = String.format("%s%-" + padding +"s\n", sign, list.get(i));
            } else {
                cell = String.format("%s%-" + padding +"s", sign, list.get(i));
            }
            stringBuilder.append(cell);
        }
        return stringBuilder.toString();
    }

    private void changePrefix(CommandEvent event, String prefix) {
        TextChannel channel = event.getChannel();

        if(prefix.length() > 3) {
            channel.sendMessage("Prefix cannot be longer than 3 characters.").queue();
            return;
        }

        if (!prefix.matches("[^ {}'\"`\\[\\]()@~]+")) {
            channel.sendMessage("Prefix cannot contain any of the following marks: `` { } [ ] ( )' \" @ ~`." ).queue();
            return;
        }

        GuildDB guildDB = event.getGuildDB()
                .orElseThrow(() -> new BotRuntimeException("Could not found guild settings."));
        GuildDB newGuildDB = new GuildDB(guildDB.getId(), prefix, guildDB.isNsfw());
        boolean result = databaseService.getGuildService().update(newGuildDB);

        if (result) {
            String message = String.format("Prefix has been changed to `%s`.", prefix);
            channel.sendMessage(message).queue();
        } else {
            channel.sendMessage("The prefix could not be changed.").queue();
        }
    }

    private void changeNsfw(CommandEvent event, String value) {
        TextChannel channel = event.getChannel();

        Optional<Boolean> isNSFWOptional = stringToBoolean(value);
        if (isNSFWOptional.isEmpty()) {
            String message = String.format(
                    "`%s` is incorrect value, type `<prefix>help settings` to see usage of this command.", value
            );
            channel.sendMessage(message).queue();
            return;
        }

        GuildDB guildDB = event.getGuildDB()
                .orElseThrow(() -> new BotRuntimeException("Could not found guild settings."));
        GuildDB newGuildDB = new GuildDB(guildDB.getId(), guildDB.getPrefix(), isNSFWOptional.get());
        boolean result = databaseService.getGuildService().update(newGuildDB);

        if (result) {
            String message = String.format("NSFW property changed to %s.", value);
            channel.sendMessage(message).queue();
        } else {
            channel.sendMessage("The NSFW property could not be changed.").queue();
        }
    }

    private String booleanToString(boolean value) {
        return value ? "enabled" : "disabled";
    }

    private Optional<Boolean> stringToBoolean(String value) {
        if (value.equals("enable")) {
            return Optional.of(true);
        } else if (value.equals("disable")) {
            return Optional.of(false);
        } else {
            return Optional.empty();
        }
    }

    private void customizeReddit(CommandEvent event, String action, String value) {
        if (action.equals("add")) {
            addSubreddit(event, value);
        } else if (action.equals("remove")) {
            removeSubreddit(event, value);
        } else {
            String message = String.format(
                    "`%s` is incorrect action, type `<prefix>help settings` to see usage of this command.", action
            );
            event.getChannel().sendMessage(message).queue();
        }
    }

    private void addSubreddit(CommandEvent event, String value) {
        TextChannel channel = event.getChannel();
        try {
            value = value.toLowerCase(Locale.ROOT).trim();

            if (!redditMediaProvider.ping(value)) {
                channel.sendMessage(
                        "Sorry, I could not connect with given subreddit. Check if you provided correct name."
                ).queue();
                return;
            }

            long guildId = event.getGuildId();
            GuildResourceEntityManager<SubredditDB> entityManager = databaseService.getSubredditService().get(guildId);
            SubredditDB entity = new SubredditDB(value, guildId);
            addToDatabase(entityManager, entity, channel, "subreddit");

        } catch (IOException | InterruptedException e) {
            String message = String.format("An error occurred when I tried to connect to `%s` subreddit.", value);
            channel.sendMessage(message).queue();
        }
    }

    private void removeSubreddit(CommandEvent event, String value) {
        TextChannel channel = event.getChannel();
        long guildId = event.getGuildId();
        GuildResourceEntityManager<SubredditDB> entityManager = databaseService.getSubredditService().get(guildId);
        value = value.toLowerCase(Locale.ROOT).trim();
        SubredditDB entity = new SubredditDB(value, guildId);
        removeFromDatabase(entityManager, entity, channel, "subreddit");
    }

    private void customizeImgur(CommandEvent event, String action, String value) {
        if (action.equals("add")) {
            addImgurTag(event, value);
        } else if (action.equals("remove")) {
            removeImgurTag(event, value);
        } else {
            String message = String.format(
                    "`%s` is incorrect action, type `<prefix>help settings` to see usage of this command.", action
            );
            event.getChannel().sendMessage(message).queue();
        }
    }

    private void addImgurTag(CommandEvent event, String value) {
        TextChannel channel = event.getChannel();

        value = value.toLowerCase(Locale.ROOT).trim();
        if (!defaultImgurTags.contains(value)) {
            String message = String.format(
                    "`%s` is not supported Imgur tag. You can only add tags from bot default list of Imgur tags. " +
                            "To see this list type `<prefix>settings list`.", value
            );
            channel.sendMessage(message).queue();
            return;
        }

        long guildId = event.getGuildId();
        GuildResourceEntityManager<ImgurTagDB> entityManager = databaseService.getImgurTagService().get(guildId);
        ImgurTagDB entity = new ImgurTagDB(value, guildId);
        addToDatabase(entityManager, entity, channel, "Imgur tag");
    }

    private void removeImgurTag(CommandEvent event, String value) {
        TextChannel channel = event.getChannel();
        long guildId = event.getGuildId();
        GuildResourceEntityManager<ImgurTagDB> entityManager = databaseService.getImgurTagService().get(guildId);
        value = value.toLowerCase(Locale.ROOT).trim();
        ImgurTagDB entity = new ImgurTagDB(value, guildId);
        removeFromDatabase(entityManager, entity, channel, "Imgur tag");
    }

    private <T extends GuildResourceEntity> void addToDatabase(
            GuildResourceEntityManager<T> entityManager,
            T entity,
            TextChannel channel,
            String listName
    ) {

        if (entityManager.contains(entity.getName())) {
            String message = String.format("Provided %1$s is already on the guild %1$s list.", listName);
            channel.sendMessage(message).queue();
            return;
        }

        int limit = entityManager.getLimit();
        if (entityManager.size() >= limit) {
            String message = String.format("Guild can't have more than %d %s.", limit, listName + "s");
            channel.sendMessage(message).queue();
            return;
        }

        if (entityManager.add(entity)) {
            String message = String.format("`%s` added to guild %s list.", entity.getName(), listName);
            channel.sendMessage(message).queue();
        } else {
            String message = String.format("Failed to add `%s` to guild %s list.", entity.getName(), listName);
            channel.sendMessage(message).queue();
        }
    }

    private <T extends GuildResourceEntity> void removeFromDatabase(
            GuildResourceEntityManager<T> entityManager,
            T entity,
            TextChannel channel,
            String listName
    ) {

        if (!entityManager.contains(entity.getName())) {
            String message = String.format("I couldn't find `%s` %2$s on guild %2$s list.", entity.getName(), listName);
            channel.sendMessage(message).queue();
            return;
        }

        if (entityManager.remove(entity.getName())) {
            String message = String.format("`%s` %2$s has been removed from guild %2$s list.", entity.getName(), listName);
            channel.sendMessage(message).queue();
        } else {
            String message = String.format("Failed to remove `%s` from guild %s list.", entity.getName(), listName);
            channel.sendMessage(message).queue();
        }
    }

    @Override
    public String getName() {
        return "settings";
    }

    @Override
    public String getDescription() {
        return "Allows you to change bot settings.";
    }

    @Override
    public String getExample() {
        return """
                **<prefix>**settings **<list>**
                **<prefix>**settings **<prefix>** *<value>*
                **<prefix>**settings **<nsfw>** **<enable|disable>**
                **<prefix>**settings **<reddit|imgur>** **<add|remove>** *<value>*
                """;
    }

    @Override
    public Permission[] getUserPermission() {
        return userPermission;
    }
}
