package tech.ypsilon.bbbot.discord;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import tech.ypsilon.bbbot.discord.command.*;
import tech.ypsilon.bbbot.discord.listener.*;
import tech.ypsilon.bbbot.settings.SettingsController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static tech.ypsilon.bbbot.discord.DiscordController.getJDA;
import static tech.ypsilon.bbbot.util.StringUtil.parseString;

public class CommandManager {

    private static CommandManager instance;

    private final List<Command> commands = new ArrayList<>();

    public CommandManager(){
        instance = this;

        registerFunction(new ListCommand());
        registerFunction(new StoreCommand());
        registerFunction(new GetDirectoryCommand());
        registerFunction(new AddDirectoryCommand());
        registerFunction(new EditDirectoryCommand());
        registerFunction(new StudiengangCommand());
        registerFunction(new WriteAfterMeCommand());
        registerFunction(new VoicePlayCommand());
        registerFunction(new VoiceLeaveCommand());
        registerFunction(new CreateChannelCommand());
        registerFunction(new GroupCommand());
        registerFunction(new BirthdayCommand());
        registerFunction(new HelpCommand());
        //registerFunction(new NotifySelectRoleCommand());
        registerFunction(new CensorshipCommand());

        registerEventListener(new DefaultListener());
        registerEventListener(new CommandListener());
        registerEventListener(new RoleListener());
        registerEventListener(new ChannelListener());
        registerEventListener(new NewMemberJoinListener());
        registerEventListener(new CensorWatcherListener());
    }

    private void registerFunction(DiscordFunction... functions) {
        for (DiscordFunction function : functions) {
            if(function instanceof Command) {
                commands.add((Command) function);
            }
        }
    }

    private void registerEventListener(Object... eventListeners) {
        getJDA().addEventListener(eventListeners);
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

        String[] arguments = parseString(msg).toArray(new String[0]);

        if(arguments.length == 0){
            return null;
        }
        return arguments;
    }

}
