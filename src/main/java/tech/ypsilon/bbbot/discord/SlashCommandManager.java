package tech.ypsilon.bbbot.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.discord.command.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manager to handle all the {@link SlashCommand}s send to the bot
 *
 * @author DeveloperTK | Shirkanesi
 * @version 1.0
 * @since 1.4.0
 */
public class SlashCommandManager extends ListenerAdapter {

    /**
     * This String is used by the {@link SlashCommand#createButtonId(String)} and
     * {@link SlashCommand#createSelectMenuId(String)} to use a unique identifier per
     * {@link net.dv8tion.jda.api.interactions.components.Button} /
     * {@link net.dv8tion.jda.api.interactions.components.selections.SelectionMenu}
     */
    public static final String INTERACTION_ID_DELIMITER = "∆";
    public static final String BUTTON_PREFIX = "button";
    public static final String SELECT_MENU_PREFIX = "select";

    private static SlashCommandManager instance;

    private final Map<String, SlashCommand> commandMap;

    /**
     * Creates a new {@link SlashCommandManager} and registers all {@link SlashCommand}s
     *
     * @param jda the {@link JDA} to register the commands on
     */
    public SlashCommandManager(JDA jda) {
        commandMap = new HashMap<>();
        jda.addEventListener(this);
        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            ButterBrot.LOGGER.warn("Could not await jda to get ready...");
        }
        // Register all slash-commands
        registerCommands(jda,
                new ButterbrotCommand(),
                new BirthdayCommand(),
                new GroupSlashCommand(),
                new CreateInviteSlashCommand(),
                new DudenSlashCommand(),
                new ReloadSlashCommand(),
                new ToolsSlashCommand(),
                new BirthdayCommand(),
                // new TestCommand(),
                new MusicCommand(),
                new MassMoveCommand(),
                new CreateChannelSlashCommand(),
                new WriteAfterMeSlashCommand(),
                new CensorSlashCommand(),
                new StoreSlashCommand(),
                new ListSlashCommand(),
                new RankSystemSlashCommand(),
                new StudiengangSlashCommand(),
                new HelpSlashCommand(),
                // new VerifySlashCommand(),
                new DirectorySlashCommand(),

                new JahrgangSlashCommand()
        );
    }

    private void registerCommands(JDA jda, SlashCommand... commands) {
        // put all commands into the map
        for (SlashCommand command : commands) {
            commandMap.put(command.commandData().getName(), command);
        }

        if (ButterBrot.DEBUG_MODE) {
            // add all commands to guild
            jda.updateCommands().queue();
            for (Guild guild : jda.getGuilds()) {
                guild.updateCommands().addCommands(
                        commandMap.values().stream()
                                .map(SlashCommand::commandData)
                                .collect(Collectors.toList())
                ).queue();
            }
        } else {
            // register global commands
            jda.updateCommands().addCommands(
                    commandMap.values().stream()
                            .filter(SlashCommand::isGlobal)
                            .map(SlashCommand::commandData)
                            .collect(Collectors.toList())
            ).queue();

            // register all guild commands
            for (Guild guild : jda.getGuilds()) {
                guild.updateCommands().addCommands(
                        commandMap.values().stream()
                                .filter(command -> !command.isGlobal())
                                .map(SlashCommand::commandData)
                                .collect(Collectors.toList())
                ).queue();
            }
        }
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if (commandMap.containsKey(event.getName())) {
            try {
                commandMap.get(event.getName()).execute(event);
            } catch (NullPointerException | CommandFailedException exception) {
                if (event.isAcknowledged()) {
                    event.getHook().sendMessage(exception.getMessage().length() == 0 ? exception.getClass().getSimpleName() : exception.getMessage()).queue();
                } else {
                    event.reply(exception.getMessage()).queue();
                }
            }
        } else {
            event.reply("Fehler: der Befehl konnte nicht gefunden werden!").setEphemeral(true).queue();
        }
    }

    @Override
    public void onSelectionMenu(@NotNull SelectionMenuEvent event) {
        String[] split = event.getComponentId().split(INTERACTION_ID_DELIMITER);

        if (split.length != 4 || !split[0].equalsIgnoreCase(SELECT_MENU_PREFIX)) {
            event.reply("Ein interner Fehler ist aufgetreten").setEphemeral(true).queue();
        } else if (commandMap.containsKey(split[1])) {
            try {
                commandMap.get(split[1]).handleSelectionMenu(event, split[2]);
            } catch (NullPointerException | CommandFailedException exception) {
                if (event.isAcknowledged()) {
                    event.getHook().sendMessage(exception.getMessage()).queue();
                } else {
                    event.reply(exception.getMessage()).queue();
                }
            }
        } else {
            event.reply("Ein interner Fehler ist aufgetreten").setEphemeral(true).queue();
        }
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        String[] split = event.getComponentId().split(INTERACTION_ID_DELIMITER);

        if (split.length != 4 || !split[0].equalsIgnoreCase(BUTTON_PREFIX)) {
            event.reply("Ein interner Fehler ist aufgetreten").setEphemeral(true).queue();
        } else if (commandMap.containsKey(split[1])) {
            try {
                commandMap.get(split[1]).handleButtonInteraction(event, split[2]);
            } catch (NullPointerException | CommandFailedException exception) {
                if (event.isAcknowledged()) {
                    event.getHook().sendMessage(exception.getMessage()).queue();
                } else {
                    event.reply(exception.getMessage()).queue();
                }
            }
        } else {
            event.reply("Ein interner Fehler ist aufgetreten").setEphemeral(true).queue();
        }
    }

    /**
     * Adds the commands to a guild, when new guild is joined.
     *
     * @param event the event
     */
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        for (SlashCommand command : commandMap.values()) {
            if (!command.isGlobal()) {
                event.getGuild().upsertCommand(command.commandData()).queue();
            }
        }
    }

    public Map<String, SlashCommand> getCommandMap() {
        return commandMap;
    }

    public static SlashCommandManager initialize(JDA jda) {
        if (instance != null) {
            throw new IllegalStateException("SlashCommandManager has already been initialized!");
        }
        instance = new SlashCommandManager(jda);
        return instance;
    }

    public static SlashCommandManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SlashCommandManager has not been initialized yet...");
        }
        return instance;
    }
}
