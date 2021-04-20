package tech.ypsilon.bbbot.discord;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import tech.ypsilon.bbbot.discord.command.*;
import tech.ypsilon.bbbot.discord.listener.*;
import tech.ypsilon.bbbot.settings.SettingsController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static tech.ypsilon.bbbot.discord.DiscordController.getJDA;
import static tech.ypsilon.bbbot.util.StringUtil.parseString;

public class CommandManager extends ListenerAdapter {

    private static CommandManager instance;

    private final List<Command> commands = new ArrayList<>();

    /**
     * Registering all the Commands by calling the {@link #registerFunction(DiscordFunction...)}
     * and registering the EventListeners by calling {@link #registerEventListener(Object...)}
     */
    public CommandManager() {
        instance = this;

        registerFunction(new ListCommand());
        registerFunction(new StoreCommand());
        registerFunction(new GetDirectoryCommand());
        registerFunction(new AddDirectoryCommand());
        registerFunction(new EditDirectoryCommand());



        registerFunction(new WriteAfterMeCommand());
        //registerFunction(new VoicePlayCommand());
        //registerFunction(new VoiceLeaveCommand());
        registerFunction(new CreateChannelCommand());
        registerFunction(new GroupCommand());
        registerFunction(new BirthdayCommand());
        registerFunction(new HelpCommand());
        //registerFunction(new NotifySelectRoleCommand());
        registerFunction(new CensorshipCommand());
        registerFunction(new DudenCommand());
        registerFunction(new RankSystemCommand());
        registerFunction(new VoiceCommands());
        // registerFunction(new GBILocationCommand()); // Disabled; test over...
        registerFunction(new GitHubCommand());
        registerFunction(new VerifyCommand());

        registerEventListener(this);
        registerEventListener(new DefaultListener());
        registerEventListener(new RoleListener());
        registerEventListener(new ChannelListener());
        registerEventListener(new NewMemberJoinListener());
        registerEventListener(new CensorWatcherListener());
        registerEventListener(new RankSystemListener());
    }

    /**
     * Register a new function for Discord
     * You can add a new command by passing a instance of {@link Command} or {@link CommandBucket}
     *
     * @param functions an instance from the Function
     */
    public void registerFunction(DiscordFunction... functions) {
        for (DiscordFunction function : functions) {
            if (function instanceof Command) {
                commands.add((Command) function);
            }
            if (function instanceof CommandBucket) {
                ArrayList<DiscordFunction> discordFunctions = new ArrayList<>();
                ((CommandBucket) function).register(discordFunctions);
                discordFunctions.forEach(this::registerFunction);
            }
        }
    }

    public static CommandManager getInstance() {
        return instance;
    }

    /**
     * Register new eventListeners
     * Just adds it to the JDA instance but use the method anyway for future feature compatibility
     *
     * @param eventListeners an instance from the EventListener
     */
    private void registerEventListener(Object... eventListeners) {
        getJDA().addEventListener(eventListeners);
    }

    /**
     * Get all currently registered commands
     *
     * @return a List with Commands
     */
    public static List<Command> getCommands() {
        return instance.commands;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        CommandManager.checkForExecute(event);
    }

    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent event) {
        CommandManager.checkForExecute(event);
    }

    /**
     * Check if a received message is a command by checking the prefix and if the alias is a registered command.
     * If so the command gets executed by calling the
     * {@link LegacyCommand#onExecute(GuildMessageReceivedEvent, String[]) method.
     * <p>
     * THIS METHOD IS FOR INTERNAL USE AND SHOULD NEVER BE CALLED FROM A COMMAND OR OTHER CLASS!
     *
     * @param event from the EventHandler
     */
    private static void checkForExecute(GuildMessageReceivedEvent event) {
        String[] arguments = checkPrefix(event.getMessage());
        if (arguments == null) return;

        for (Command command : instance.commands) {
            if (command instanceof GuildExecuteHandler) {
                if (Arrays.stream(command.getAlias()).anyMatch(s -> s.equalsIgnoreCase(arguments[1]))) {
                    String[] args = Arrays.copyOfRange(arguments, 2, arguments.length);
                    ((GuildExecuteHandler) command).onExecute(event, args);
                }
            }
        }
    }

    /**
     * Check if a received private message is a command by checking the prefix and if the alias is a registered command.
     * If so the command gets executed by calling
     * the {@link PrivateExecuteHandler#onPrivateExecute(PrivateMessageReceivedEvent, String[])} method.
     * <p>
     * THIS METHOD IS FOR INTERNAL USE AND SHOULD NEVER BE CALLED FROM A COMMAND OR OTHER CLASS!
     *
     * @param event fromt eh EventHandler
     */
    private static void checkForExecute(PrivateMessageReceivedEvent event) {
        String[] arguments = checkPrefix(event.getMessage());
        if (arguments == null) return;

        for (Command command : instance.commands) {
            if (command instanceof PrivateExecuteHandler) {
                if (Arrays.stream(command.getAlias()).anyMatch(s -> s.equalsIgnoreCase(arguments[1]))) {
                    String[] args = Arrays.copyOfRange(arguments, 2, arguments.length);
                    ((PrivateExecuteHandler) command).onPrivateExecute(event, args);
                }
            }
        }
    }

    /**
     * Checks if the prefix from a given command is one of the prefix defined in the settings.yml
     * If to it parses the message to a array later used in the onExecuted from {@link LegacyCommand} or {@link PrivateExecuteHandler}
     * Uses a parsed to detect longer strings by double quotes.
     * <p>
     * THIS METHOD IS FOR INTERNAL USE AND SHOULD NOT BE CALLED UNLESS YOU KNOW WHAT YOU ARE DOING!
     *
     * @param message the message as an JDA Object
     * @return a String[] or null if the arguments are empty
     */
    @SuppressWarnings("unchecked")
    private static String[] checkPrefix(Message message) {
        String msg = message.getContentDisplay();
        if (((List<String>) SettingsController.getValue("discord.prefix")).stream().noneMatch(msg::startsWith)) {
            return null;
        }

        String[] arguments = parseString(msg).toArray(new String[0]);

        if (arguments.length == 0) {
            return null;
        }
        return arguments;
    }

}
