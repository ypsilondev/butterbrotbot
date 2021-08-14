package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class TestCommand extends SlashCommand {

    @Override
    public CommandData commandData() {
        return new CommandData("test", "Test").addSubcommands();
    }

    @Override
    public boolean isGlobal() {
        return super.isGlobal();
    }

    @Override
    public void execute(SlashCommandEvent event) {

    }

    @Override
    public void handleButtonInteraction(ButtonClickEvent event, String data) {
        super.handleButtonInteraction(event, data);
    }

    @Override
    public void handleSelectionMenu(SelectionMenuEvent event, String data) {
        super.handleSelectionMenu(event, data);
    }
}
