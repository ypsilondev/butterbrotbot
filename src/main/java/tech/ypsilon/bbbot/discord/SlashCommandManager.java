package tech.ypsilon.bbbot.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import tech.ypsilon.bbbot.discord.command.ButterbrotCommand;
import tech.ypsilon.bbbot.discord.command.CommandFailedException;
import tech.ypsilon.bbbot.discord.command.SlashCommand;

import java.util.HashMap;
import java.util.Map;

public class SlashCommandManager extends ListenerAdapter {

    private final Map<String, SlashCommand> commandMap;

    public SlashCommandManager(JDA jda) {
        commandMap = new HashMap<>();
        registerCommands(jda, new ButterbrotCommand());
    }

    private void registerCommands(JDA jda, SlashCommand... commands) {
        // put all commands into the map
        for (SlashCommand command : commands) {
            commandMap.put(command.commandData().getName(), command);
        }

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
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        for (SlashCommand command : commandMap.values()) {
            if (!command.isGlobal()) {
                event.getGuild().upsertCommand(command.commandData()).queue();
            }
        }
    }
}
