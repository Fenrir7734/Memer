package com.fenrir.Memer.listener;

import com.fenrir.Memer.Memer;
import com.fenrir.Memer.Settings;
import com.fenrir.Memer.command.CommandManager;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildMessageListener extends ListenerAdapter {
    private final Settings settings;
    private final CommandManager commandManager;

    public GuildMessageListener(Memer memer) {
        this.settings = memer.getSettings();
        this.commandManager = memer.getCommandManager();
    }
}
