package com.fenrir.Memer.command.commands;

import com.fenrir.Memer.command.CommandEvent;
import net.dv8tion.jda.api.Permission;

public interface Command {
    void execute(CommandEvent event);
    String getName();
    String getDescription();
    String getExample();
    Permission[] getUserPermission();
}
