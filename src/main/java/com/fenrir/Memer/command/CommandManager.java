package com.fenrir.Memer.command;

import com.fenrir.Memer.command.commands.Command;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CommandManager {
    private final Map<String, Command> defaultCommands = new HashMap<>();

    public CommandManager(Command... commands) {
        for (Command command: commands) {
            defaultCommands.put(command.getName(), command);
        }
    }

    public Optional<Command> search(String name) {
        Command command = defaultCommands.get(name);
        return Optional.ofNullable(command);
    }
}
