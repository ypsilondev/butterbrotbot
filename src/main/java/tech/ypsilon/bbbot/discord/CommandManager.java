package tech.ypsilon.bbbot.discord;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import tech.ypsilon.bbbot.discord.command.*;
import tech.ypsilon.bbbot.discord.listener.CommandListener;
import tech.ypsilon.bbbot.discord.listener.RoleListener;
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
        commands.add(new GetDirectoryCommand());
        commands.add(new AddDirectoryCommand());
        commands.add(new EditDirectoryCommand());
        commands.add(new StudiengangCommand());

        DiscordController.getJDA().addEventListener(new CommandListener());
        DiscordController.getJDA().addEventListener(new RoleListener());
    }

    public static List<Command> getCommands() {
        return instance.commands;
    }

    public static void checkForExecute(GuildMessageReceivedEvent event){
        String[] arguments = checkPrefix(event.getMessage());
        if(arguments == null) return;

        for(Command command : instance.commands){
            if(Arrays.stream(command.getAlias()).anyMatch(s -> s.equalsIgnoreCase(arguments[1]))){
                String[] args = Arrays.copyOfRange(arguments,2, arguments.length);
                command.onExecute(event, args);
            }
        }
    }

    public static void checkForExecute(PrivateMessageReceivedEvent event){
        String[] arguments = checkPrefix(event.getMessage());
        if(arguments == null) return;

        for(Command command : instance.commands){
            if(command instanceof PrivateChat) {
                if(Arrays.stream(command.getAlias()).anyMatch(s -> s.equalsIgnoreCase(arguments[1]))){
                    String[] args = Arrays.copyOfRange(arguments,2, arguments.length);
                    ((PrivateChat)command).onPrivateExecute(event, args);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static String[] checkPrefix(Message message) {
        String msg = message.getContentDisplay();
        if(((List<String>) SettingsController.getValue("discord.prefix")).stream().noneMatch(msg::startsWith)){
            return null;
        }

        String[] arguments = msg.split(" ");

        if(arguments.length == 0){
            return null;
        }
        return arguments;
    }

}
