package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * A Proxy to provide the possibility of aliasing one slash-command by another name.
 * A {@link SlashCommandAlias} will behave exactly the same as a normal slash-command, except all functionality
 * will be handled by the underlying command. The alias will not modify the execution of the command at all.
 *
 * @author Shirkanesi
 */
public class SlashCommandAlias extends SlashCommand {

    private final SlashCommand subject;
    private final String commandAlias;

    public SlashCommandAlias(SlashCommand subject, String commandAlias) {
        this.subject = subject;
        this.commandAlias = commandAlias;
    }

    @Override
    public CommandData commandData() {
        return this.subject.commandData().setName(this.commandAlias);
    }

    @Override
    public void execute(SlashCommandEvent event) {
        this.subject.execute(event);
    }
}
