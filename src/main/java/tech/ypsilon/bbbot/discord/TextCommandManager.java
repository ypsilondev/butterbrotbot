package tech.ypsilon.bbbot.discord;

import io.prometheus.client.Counter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.util.GenericListenerController;
import tech.ypsilon.bbbot.discord.command.text.*;
import tech.ypsilon.bbbot.discord.listener.*;
import tech.ypsilon.bbbot.discord.services.AliasService;
import tech.ypsilon.bbbot.util.Initializable;

import java.util.*;

import static tech.ypsilon.bbbot.discord.DiscordController.getJDAStatic;
import static tech.ypsilon.bbbot.util.StringUtil.parseString;

public class TextCommandManager extends GenericListenerController implements Initializable {

    private static TextCommandManager instance;

    private final List<Command> commands = new ArrayList<>();

    private static Counter guildCommandCounter;
    private static Counter privateCommandCounter;

    /**
     * Registering all the Commands by calling the {@link #registerFunction(DiscordFunction...)}
     * and registering the EventListeners by calling {@link #registerEventListener(Object...)}
     */
    public TextCommandManager(ButterBrot parent) {
        super(parent);
        instance = this;
    }

    @Override
    public void init() {
        guildCommandCounter = Counter.build()
                .name("butterbrot_commands_guild").help("The executed commands").labelNames("command").register();
        privateCommandCounter = Counter.build()
                .name("butterbrot_commands_private").help("The executed commands").labelNames("command").register();

        registerFunctions();
    }

    public void registerFunctions() {
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
        registerFunction(new ToolsCommand());
        registerFunction(new ReloadCommand());

        registerEventListener(this);
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

    public static TextCommandManager getInstance() {
        return instance;
    }

    /**
     * Register new eventListeners
     * Just adds it to the JDA instance but use the method anyway for future feature compatibility
     *
     * @param eventListeners an instance from the EventListener
     */
    private void registerEventListener(Object... eventListeners) {
        getJDAStatic().addEventListener(eventListeners);
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
        TextCommandManager.checkForExecute(event);
    }

    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent event) {
        TextCommandManager.checkForExecute(event);
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
        if (event.getAuthor().isBot()) return;

        String[] arguments = checkPrefix(event.getMessage());
        if (arguments == null) return;

        String alias = AliasService.getAlias(arguments[1]);
        if (alias != null) {
            List<String> override = new LinkedList<>();
            override.add(arguments[0]);

            override.addAll(Arrays.asList(alias.split(" ")));
            override.addAll(Arrays.asList(arguments).subList(2, arguments.length));
            arguments = override.toArray(new String[0]);

            // Check for side-loaded exec-command.
            if (arguments[1].equals("exec")) {
                AliasCommandExecutor.execute(event, Arrays.copyOfRange(arguments, 2, arguments.length));
                return;
            }
        }

        String[] finalArguments = arguments;

        for (Command command : instance.commands) {
            if (command instanceof GuildExecuteHandler) {
                if (Arrays.stream(command.getAlias()).anyMatch(s -> s.equalsIgnoreCase(finalArguments[1]))) {
                    String[] args = Arrays.copyOfRange(arguments, 2, arguments.length);
                    ((GuildExecuteHandler) command).onExecute(event, args);
                    guildCommandCounter.labels(command.getAlias()[0]).inc();
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
        if (event.getAuthor().isBot()) return;

        String[] arguments = checkPrefix(event.getMessage());
        if (arguments == null) return;

        String alias = AliasService.getAlias(arguments[1]);
        if (alias != null) {
            List<String> override = new LinkedList<>();
            override.add(arguments[0]);

            override.addAll(Arrays.asList(alias.split(" ")));
            override.addAll(Arrays.asList(arguments).subList(2, arguments.length));
            arguments = override.toArray(new String[0]);
        }

        String[] finalArguments = arguments;
        for (Command command : instance.commands) {
            if (command instanceof PrivateExecuteHandler) {
                if (Arrays.stream(command.getAlias()).anyMatch(s -> s.equalsIgnoreCase(finalArguments[1]))) {
                    String[] args = Arrays.copyOfRange(arguments, 2, arguments.length);
                    ((PrivateExecuteHandler) command).onPrivateExecute(event, args);
                    privateCommandCounter.labels(command.getAlias()[0]).inc();
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
        if (ButterBrot.getConfigStatic().getDiscord().getPrefixList().stream().noneMatch(msg::startsWith)) {
            return null;
        }

        String[] arguments = parseString(msg).toArray(new String[0]);

        if (arguments.length == 0) {
            return null;
        }
        return arguments;
    }

}
