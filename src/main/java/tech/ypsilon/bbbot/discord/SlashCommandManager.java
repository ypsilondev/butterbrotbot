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
import tech.ypsilon.bbbot.discord.command.BirthdayCommand;
import tech.ypsilon.bbbot.discord.command.ButterbrotCommand;
import tech.ypsilon.bbbot.discord.command.CommandFailedException;
import tech.ypsilon.bbbot.discord.command.SlashCommand;

import java.util.HashMap;
import java.util.Map;

public class SlashCommandManager extends ListenerAdapter {

    public static final String INTERACTION_ID_DELIMITER = ":";
    public static final String BUTTON_PREFIX = "button";
    public static final String SELECT_MENU_PREFIX = "select";

    private final Map<String, SlashCommand> commandMap;

    public SlashCommandManager(JDA jda) {
        commandMap = new HashMap<>();
        jda.addEventListener(this);
        registerCommands(jda,
                new ButterbrotCommand(),
                new BirthdayCommand()
        );
    }

    private void registerCommands(JDA jda, SlashCommand... commands) {
        // put all commands into the map
        for (SlashCommand command : commands) {
            commandMap.put(command.commandData().getName(), command);
        }

        if (ButterBrot.DEBUG_MODE) {
            jda.updateCommands().queue();

            // upsert all commands as guild commands
            for (Guild guild : jda.getGuilds()) {
                for (SlashCommand command : commands) {
                    guild.upsertCommand(command.commandData()).queue();
                }
            }
        } else {
            // upsert all global commands
            commandMap.values().stream()
                    .filter(SlashCommand::isGlobal)
                    .forEach(command -> jda.upsertCommand(command.commandData()).queue());

            // upsert all guild commands
            for (Guild guild : jda.getGuilds()) {
                for (SlashCommand command : commands) {
                    if (!command.isGlobal()) {
                        guild.upsertCommand(command.commandData()).queue();
                    }
                }
            }
        }
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if (commandMap.containsKey(event.getName())) {
            try {
                commandMap.get(event.getName()).execute(event);
            } catch (CommandFailedException exception) {
                if (event.isAcknowledged()) {
                    event.getHook().sendMessage(exception.getMessage()).queue();
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
        String[] split = event.getId().split(INTERACTION_ID_DELIMITER);

        if (split.length != 4 || !split[0].equalsIgnoreCase(SELECT_MENU_PREFIX)) {
            event.reply("Ein interner Fehler ist aufgetreten").setEphemeral(true).queue();
        } else if (commandMap.containsKey(split[1])) {
            commandMap.get(split[1]).handleSelectionMenu(event, split[2]);
        } else {
            event.reply("Ein interner Fehler ist aufgetreten").setEphemeral(true).queue();
        }
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        String[] split = event.getId().split(INTERACTION_ID_DELIMITER);

        if (split.length != 4 || !split[0].equalsIgnoreCase(BUTTON_PREFIX)) {
            event.reply("Ein interner Fehler ist aufgetreten").setEphemeral(true).queue();
        } else if (commandMap.containsKey(split[1])) {
            commandMap.get(split[1]).handleButtonInteraction(event, split[2]);
        } else {
            event.reply("Ein interner Fehler ist aufgetreten").setEphemeral(true).queue();
        }
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        for (SlashCommand command : commandMap.values()) {
            if (!command.isGlobal()) {
                event.getGuild().upsertCommand(command.commandData()).queue();
            }
        }
    }
}
