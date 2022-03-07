package com.fenrir.Memer.command.commands;

import com.fenrir.Memer.Memer;
import com.fenrir.Memer.command.CommandEvent;
import com.fenrir.Memer.command.CommandManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.Map;
import java.util.Optional;

public class Help implements Command {
    private final Permission[] userPermission = new Permission[] { Permission.MESSAGE_SEND };
    private final Permission[] botPermission = new Permission[] { Permission.MESSAGE_EMBED_LINKS };

    private final Memer memer;

    public Help(Memer memer) {
        this.memer = memer;
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
        if (!botMember.hasPermission(channel, botPermission)) {
            channel.sendMessage("I can't send memes on this channel.").queue();
            return;
        }

        String[] args = event.getArgs();
        if (args.length == 0) {
            listCommands(event);
        } else if (args.length == 1) {
            commandDescription(event, args[0]);
        } else {
            channel.sendMessage("I don't understand this command.").queue();
        }
    }

    public void listCommands(CommandEvent event) {
        Member userMember = event.getAuthor();
        CommandManager commandManager = memer.getCommandManager();
        Map<String, Command> commands = commandManager.getCommands();

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Help");
        for (String key: commands.keySet()) {
            Command command = commands.get(key);
            if (userMember.hasPermission(command.getUserPermission())) {
                embedBuilder = embedBuilder.addField(command.getName(), command.getDescription(), false);
            }
        }
        MessageEmbed messageEmbed = embedBuilder.build();
        event.getChannel().sendMessageEmbeds(messageEmbed).queue();
    }

    public void commandDescription(CommandEvent event, String commandName) {
        Member userMember = event.getAuthor();
        TextChannel channel = event.getChannel();
        Optional<Command> commandOptional = memer.getCommandManager()
                .search(commandName);

        if (commandOptional.isPresent()) {
            Command command = commandOptional.get();
            if (userMember.hasPermission(command.getUserPermission())) {
                MessageEmbed messageEmbed = new EmbedBuilder().setColor(Color.GREEN)
                        .setTitle("Help")
                        .addField("Name", command.getName(), false)
                        .addField("Description", command.getDescription(), false)
                        .addField("Usage", command.getExample(), false)
                        .build();
                channel.sendMessageEmbeds(messageEmbed).queue();
            } else {
                String message = String.format("You don't have required permission for `%s` command.", command.getName());
                channel.sendMessage(message).queue();
            }
        } else {
            String message = String.format("Sorry, I couldn't find `%s` command.", commandName);
            channel.sendMessage(message).queue();
        }
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Lists all commands with their descriptions or description and usage of specific command.";
    }

    @Override
    public String getExample() {
        return """
                **<prefix>**help
                **<prefix>**help **<command>**
                """;
    }

    @Override
    public Permission[] getUserPermission() {
        return userPermission;
    }
}
