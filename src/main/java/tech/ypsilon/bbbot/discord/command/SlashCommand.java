package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * Interface to easier handle slash commands
 *
 * @author Christian Schliz (code@foxat.de)
 */
public interface SlashCommand {

    /**
     * JDA Command Data information used to register
     * and search commands.
     *
     * @return command data
     */
    CommandData commandData();

    /**
     * Defines whether the command should be registered
     * globally or per guild only.
     *
     * @return whether the command is global
     */
    default boolean isGlobal() {
        return false;
    }

    /**
     * Execute is called by the onSlashCommand event when
     * this command should be executed
     *
     * @param event original event
     */
    void execute(SlashCommandEvent event);

}
