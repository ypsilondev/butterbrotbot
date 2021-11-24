package tech.ypsilon.bbbot.discord.command;

import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.Component;
import org.jetbrains.annotations.Nullable;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.discord.SlashCommandManager;

import java.util.Objects;

/**
 * Abstraction-layer to easier handle slash commands
 *
 * @author Christian Schliz (code@foxat.de) | Shirkanesi
 */
public abstract class SlashCommand {

    private static long counter;

    private final @Getter ButterBrot parent;

    public SlashCommand(ButterBrot parent) {
        this.parent = parent;
    }

    /**
     * JDA Command Data information used to register
     * and search commands.
     *
     * @return command data
     */
    public abstract CommandData commandData();

    /**
     * Defines whether the command should be registered
     * globally or per guild only.
     *
     * @return whether the command is global
     */
    public boolean isGlobal() {
        return false;
    }

    /**
     * Execute is called by the onSlashCommand event when
     * this command should be executed
     *
     * @param event original event
     */
    public abstract void execute(SlashCommandEvent event);

    public void handleButtonInteraction(ButtonClickEvent event, String data) {
    }

    public void handleSelectionMenu(SelectionMenuEvent event, String data) {
    }

    public final String getName() {
        return this.commandData().getName();
    }

    /**
     * Returns a string to be displayed in the help-command
     * @return the help-string
     */
    @Nullable
    public String getHelpDescription() {
        return null;
    }

    public final synchronized String createButtonId(String data) {
        return SlashCommandManager.BUTTON_PREFIX + SlashCommandManager.INTERACTION_ID_DELIMITER
                + commandData().getName() + SlashCommandManager.INTERACTION_ID_DELIMITER + data
                + SlashCommandManager.INTERACTION_ID_DELIMITER + (++counter);
    }

    public final synchronized String createSelectMenuId(String data) {
        return SlashCommandManager.SELECT_MENU_PREFIX + SlashCommandManager.INTERACTION_ID_DELIMITER
                + commandData().getName() + SlashCommandManager.INTERACTION_ID_DELIMITER + data
                + SlashCommandManager.INTERACTION_ID_DELIMITER + (++counter);
    }

}
