package com.fenrir.Memer.command.commands;

import com.fenrir.Memer.command.CommandEvent;

public interface Command {
    void execute(CommandEvent event);
    String getName();
    String getDescription();
    String getExample();
}
