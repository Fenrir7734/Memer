package com.fenrir.Memer.listener;

import com.fenrir.Memer.Memer;
import com.fenrir.Memer.Settings;
import com.fenrir.Memer.database.entities.GuildDB;
import com.fenrir.Memer.database.services.GuildService;
import com.fenrir.Memer.exceptions.DatabaseException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class GuildEventListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(GuildEventListener.class);

    private final Memer memer;

    public GuildEventListener(Memer memer) {
        this.memer = memer;
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        Guild guild = event.getGuild();
        long guildId = guild.getIdLong();

        logger.info("Joining guild `{}` with id: {}", guild.getName(), guildId);

        GuildService guildService = memer.getDatabaseService().getGuildService();
        Settings settings = memer.getSettings();

        GuildDB guildDB = new GuildDB(guildId, settings.getPrefix(), false);

        if (!guildService.add(guildDB)) {
            Optional<TextChannel> channelOptional = memer.findChannelWithSendingPermission(guild);
            channelOptional.ifPresentOrElse(
                    channel -> channel.sendMessage(
                    "Sorry, I couldn't add your guild to my database. " +
                            "Because of this, in order to prevent more problems, I have to leave this server. " +
                            "To try to resolve this issue you can try to add me again. " +
                            "If this does not solve the problem please contact with bot administration."
                    ).queue(message -> message.getGuild().leave().queue()),
                    () -> guild.leave().queue()
            );
            logger.warn("Failed to add new guild to database on join event. Guild id: {}", guildId);
        }
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        Guild guild = event.getGuild();
        long guildId = guild.getIdLong();

        logger.info("Leaving guild `{}` with id: {}", guild.getName(), guildId);

        GuildService guildService = memer.getDatabaseService().getGuildService();
        try {
            guildService.remove(guildId);
        } catch (DatabaseException e) {
            logger.error("Failed to remove guild from database on leave event. Guild id: {}", guildId);
        } finally {
            guildService.invalidate(guildId);
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {

    }
}
