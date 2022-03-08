package com.fenrir.Memer.command.commands;

import com.fenrir.Memer.Memer;
import com.fenrir.Memer.api.MediaProvider;
import com.fenrir.Memer.api.entity.ImageData;
import com.fenrir.Memer.command.CommandEvent;
import com.fenrir.Memer.database.DatabaseService;
import com.fenrir.Memer.database.entities.GuildDB;
import com.fenrir.Memer.database.entities.GuildResourceEntity;
import com.fenrir.Memer.database.services.GuildResourceService;
import com.fenrir.Memer.exceptions.BotRuntimeException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Meme implements Command {
    private final Permission[] userPermission = new Permission[] { Permission.MESSAGE_SEND, Permission.MESSAGE_ATTACH_FILES };
    private final Permission[] botPermission = new Permission[] { Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES };

    private final Map<String, GuildMediaProvider<? extends GuildResourceEntity>> mediaProviders;
    private final DatabaseService databaseService;

    public Meme(Memer memer) {
        this.databaseService = memer.getDatabaseService();
        this.mediaProviders = Map.of(
                "reddit", new GuildMediaProvider<>(memer.getRedditMediaProvider(), databaseService.getSubredditService()),
                "imgur", new GuildMediaProvider<>(memer.getImgurMediaProvider(), databaseService.getImgurTagService())
        );
    }

    @Override
    public void execute(CommandEvent event) {
        Member userMember = event.getAuthor();
        Member botMember = event.getBot();
        TextChannel channel = event.getChannel();

        if (!userMember.hasPermission(userPermission)) {
            channel.sendMessage("You can't execute this command on this channel.").queue();
            return;
        }
        if (!botMember.hasPermission(botPermission)) {
            channel.sendMessage("I can't send memes on this channel.").queue();
            return;
        }

        parseCommand(event);
    }

    private void parseCommand(CommandEvent event) {
        TextChannel channel = event.getChannel();
        long guildId = event.getGuildId();
        String[] args = event.getArgs();
        Optional<ImageData> imageDataOptional;

        if (args.length == 0) {
            imageDataOptional = getMeme(guildId);
        } else if (args.length == 1) {
            String site = args[0];
            if (!mediaProviders.containsKey(site)) {
                channel.sendMessage("Sorry, I don't recognize this site.").queue();
                return;
            }
            imageDataOptional = getMeme(guildId, site);
        } else if (args.length == 2) {
            String site = args[0];
            if (!mediaProviders.containsKey(site)) {
                channel.sendMessage("Sorry, I don't recognize this site.").queue();
                return;
            }

            String source = args[1];
            if (!mediaProviders.get(site).containsSource(guildId, source)) {
                channel.sendMessage("Sorry, this guild doesn't support this source.").queue();
                return;
            }
            imageDataOptional = getMeme(guildId, site, source);
        } else {
            channel.sendMessage("I don't understand this command.").queue();
            return;
        }

        if (imageDataOptional.isPresent()) {
            MessageEmbed message = buildEmbedMessage(imageDataOptional.get());
            channel.sendMessageEmbeds(message).queue();
        } else {
            channel.sendMessage("Sorry, I couldn't fetch any meme.").queue();
        }
    }

    private Optional<ImageData> getMeme(long guildId) {
        List<String> sites = new ArrayList<>(mediaProviders.keySet());
        Collections.shuffle(sites);

        Optional<ImageData> imageDataOptional = Optional.empty();
        for (int i = 0; i < 2 && i < sites.size() && imageDataOptional.isEmpty();  i++) {
            String site = sites.get(i);
            imageDataOptional = getMeme(guildId, site);
        }
        return imageDataOptional;
    }

    private Optional<ImageData> getMeme(long guildId, String site) {
        List<? extends GuildResourceEntity> sources = mediaProviders.get(site).getAllSource(guildId);
        Collections.shuffle(sources);

        Optional<ImageData> imageDataOptional = Optional.empty();
        for (int i = 0; i < 3 && i < sources.size() && imageDataOptional.isEmpty();  i++) {
            String source = sources.get(i).getName();
            imageDataOptional = getMeme(guildId, site, source);
        }
        return imageDataOptional;
    }

    private Optional<ImageData> getMeme(long guildId, String site, String source) {
        Optional<GuildDB> guildDBOptional = databaseService.getGuildService().get(guildId);
        GuildDB guildDB = guildDBOptional.orElseThrow(() -> new BotRuntimeException("Could not found guild settings."));
        boolean allowNSFW = guildDB.isNsfw();
        return mediaProviders.get(site).getMeme(guildId, source, allowNSFW);
    }

    private MessageEmbed buildEmbedMessage(ImageData imageData) {
        String footerContent = String.format("Site: %s\nSource: %s", imageData.getSite(), imageData.getSource());
        return new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle(imageData.getTitle(), imageData.getPostURL())
                .setAuthor("u/" + imageData.getAuthor())
                .setImage(imageData.getImageURL())
                .setFooter(footerContent)
                .build();
    }

    @Override
    public String getName() {
        return "meme";
    }

    @Override
    public String getDescription() {
        return "Sends random meme.";
    }

    @Override
    public String getExample() {
        return """
                **<prefix>**meme
                **<prefix>**meme **<reddit|imgur>**
                **<prefix>**meme **<source>** **<tag>**
                """;
    }

    @Override
    public Permission[] getUserPermission() {
        return userPermission;
    }

    private record GuildMediaProvider<T extends GuildResourceEntity>(
            MediaProvider<ImageData> mediaProvider,
            GuildResourceService<T> resourceService) {

        public Optional<ImageData> getMeme(long guildId, String source, boolean allowNSFW) {
            if (containsSource(guildId, source)) {
                return mediaProvider.getMeme(source, allowNSFW);
            } else {
                return Optional.empty();
            }
        }

        public boolean containsSource(long guildId, String source) {
            return resourceService.get(guildId).contains(source);
        }

        public List<T> getAllSource(long guildId) {
            return resourceService.get(guildId).getAll();
        }
    }
}
