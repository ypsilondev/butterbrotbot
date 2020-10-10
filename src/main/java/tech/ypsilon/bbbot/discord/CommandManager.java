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

    /**
     * Registering all the Commands by calling the {@link #registerFunction(DiscordFunction...)}
     * and registering the EventListeners by calling {@link #registerEventListener(Object...)}
     */
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
        registerFunction(new DudenCommand());

        registerEventListener(new DefaultListener());
        registerEventListener(new CommandListener());
        registerEventListener(new RoleListener());
        registerEventListener(new ChannelListener());
        registerEventListener(new NewMemberJoinListener());
        registerEventListener(new CensorWatcherListener());
        registerEventListener(new RankSystemListener());
    }

    /**
     * Register a new function for Discord
     * Also the way to register {@link Command}
     * @param functions an instance from the Function
     */
    private void registerFunction(DiscordFunction... functions) {
        for (DiscordFunction function : functions) {
            if(function instanceof Command) {
                commands.add((Command) function);
            }
        }
    }

    /**
     * Register new eventListeners
     * Just adds it to the JDA instance but use the method anyway for future feature compatibility
     * @param eventListeners an instance from the EventListener
     */
    private void registerEventListener(Object... eventListeners) {
        getJDA().addEventListener(eventListeners);
    }

    /**
     * Get all currently registered commands
     * @return a List with Commands
     */
    public static List<Command> getCommands() {
        return instance.commands;
    }

    /**
     * Check if a received message is a command by checking the prefix and if the alias is a registered command.
     * If so the command gets executed by calling the
     * {@link Command#onExecute(GuildMessageReceivedEvent, String[]) method.
     *
     * THIS METHOD IS FOR INTERNAL USE AND SHOULD NEVER BE CALLED FROM A COMMAND OR OTHER CLASS!
     *
     * @param event from the EventHandler
     */
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

    /**
     * Check if a received private message is a command by checking the prefix and if the alias is a registered command.
     * If so the command gets executed by calling
     * the {@link PrivateChat#onPrivateExecute(PrivateMessageReceivedEvent, String[])} method.
     *
     * THIS METHOD IS FOR INTERNAL USE AND SHOULD NEVER BE CALLED FROM A COMMAND OR OTHER CLASS!
     *
     * @param event fromt eh EventHandler
     */
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

    /**
     * Checks if the prefix from a given command is one of the prefix defined in the settings.yml
     * If to it parses the message to a array later used in the onExecuted from {@link Command} or {@link PrivateChat}
     * Uses a parsed to detect longer strings by double quotes.
     *
     * THIS METHOD IS FOR INTERNAL USE AND SHOULD NOT BE CALLED UNLESS YOU KNOW WHAT YOU ARE DOING!
     *
     * @param message the message as an JDA Object
     * @return a String[] or null if the arguments are empty
     */
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
