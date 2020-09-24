package tech.ypsilon.bbbot.discord;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import tech.ypsilon.bbbot.discord.command.Command;
import tech.ypsilon.bbbot.discord.command.ListCommand;
import tech.ypsilon.bbbot.discord.command.StoreCommand;
import tech.ypsilon.bbbot.discord.listener.CommandListener;
import tech.ypsilon.bbbot.settings.SettingsController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandManager {

    private static CommandManager instance;

    private final List<Command> commands = new ArrayList<>();

    public CommandManager(){
        instance = this;

        commands.add(new ListCommand());
        commands.add(new StoreCommand());

        DiscordController.getJDA().addEventListener(new CommandListener());
    }

    public static List<Command> getCommands() {
        return instance.commands;
    }

    @SuppressWarnings("unchecked")
    public static void checkForExecute(GuildMessageReceivedEvent event){
        String message = event.getMessage().getContentDisplay();
        if(((List<String>) SettingsController.getValue("discord.prefix")).stream().noneMatch(message::startsWith)){
           return;
        }

        String[] arguments = message.split(" ");

        if(arguments.length == 0){
            return;
        }

        for(Command command : instance.commands){
            if(Arrays.stream(command.getAlias()).anyMatch(s -> s.equalsIgnoreCase(arguments[0]))){
                String[] args = message.replaceFirst(message.split(" ")[0], "").split(" ");
                command.onExecute(event, args);
            }
        }
    }

}
