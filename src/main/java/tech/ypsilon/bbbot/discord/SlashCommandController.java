package tech.ypsilon.bbbot.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.util.GenericListenerController;
import tech.ypsilon.bbbot.discord.command.*;
import tech.ypsilon.bbbot.util.Initializable;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manager to handle all the {@link SlashCommand}s send to the bot
 *
 * @author DeveloperTK
 * @author Shirkanesi
 * @version 1.0
 * @since 1.4.0
 */
public class SlashCommandController extends GenericListenerController implements Initializable {

    /**
     * This String is used by the {@link SlashCommand#createButtonId(String)} and
     * {@link SlashCommand#createSelectMenuId(String)} to use a unique identifier per
     * {@link net.dv8tion.jda.api.interactions.components.Button} /
     * {@link net.dv8tion.jda.api.interactions.components.selections.SelectionMenu}
     */
    public static final String INTERACTION_ID_DELIMITER = "∆";
    public static final String BUTTON_PREFIX = "button";
    public static final String SELECT_MENU_PREFIX = "select";

    private static SlashCommandController instance;

    private final Map<String, SlashCommand> commandMap;

    /**
     * Creates a new {@link SlashCommandController} and registers all {@link SlashCommand}s
     */
    public SlashCommandController(ButterBrot parent) {
        super(parent);
        instance = this;
        commandMap = new HashMap<>();
    }

    @Override
    public void init() throws Exception {
        final JDA jda = getParent().getDiscordController().getJda();
        jda.addEventListener(this);

        try {
            jda.awaitReady();
        } catch (InterruptedException ignored) {
            ButterBrot.LOGGER.warn("Could not await jda to get ready...");
        }

        // Register all slash-commands
        registerCommands(jda,
                new ButterbrotCommand(getParent()),
                new BirthdayCommand(getParent()),
                new GroupSlashCommand(getParent()),
                new CreateInviteSlashCommand(getParent()),
                new DudenSlashCommand(getParent()),
                new ReloadSlashCommand(getParent()),
                new ToolsSlashCommand(getParent()),
                new BirthdayCommand(getParent()),
                // new TestCommand(getParent()),
                new MusicCommand(getParent()),
                new MassMoveCommand(getParent()),
                new CreateChannelSlashCommand(getParent()),
                new WriteAfterMeSlashCommand(getParent()),
                new CensorSlashCommand(getParent()),
                new StoreSlashCommand(getParent()),
                new ListSlashCommand(getParent()),
                new RankSystemSlashCommand(getParent()),
                new HelpSlashCommand(getParent()),
                // new VerifySlashCommand(getParent()),
                new DirectorySlashCommand(getParent()),
                new ProfileCommand(getParent())
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
                    event.getHook().sendMessage(exception.getMessage()).setEphemeral(true).queue();
                } else {
                    event.reply(exception.getMessage()).setEphemeral(true).queue();
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

    public static SlashCommandController getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SlashCommandManager has not been initialized yet...");
        }
        return instance;
    }
}
