package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

public class TestCommand extends SlashCommand {

    @Override
    public CommandData commandData() {
        return new CommandData("test", "Test").addSubcommands(
                new SubcommandData("button", "button"),
                new SubcommandData("select", "select")
        );
    }

    @Override
    public boolean isGlobal() {
        return super.isGlobal();
    }

    @Override
    public void execute(SlashCommandEvent event) {
        if (event.getSubcommandName().equals("button")) {
            event.reply("Button")
                    .addActionRow(Button.of(ButtonStyle.PRIMARY, createButtonId("hello :wave:"), "Press Me"))
                    .queue();
        } else if (event.getSubcommandName().equals("select")) {
            SelectionMenu menu = SelectionMenu.create(createSelectMenuId("custom-data"))
                    .setRequiredRange(1, 1)
                    .addOption("option 1", "value 1")
                    .addOption("option 2", "value 2")
                    .build();
            event.reply("SelectMenu").addActionRow(menu).queue();
        }
    }

    @Override
    public void handleButtonInteraction(ButtonClickEvent event, String data) {
        event.reply("Clicked button with data: " + data).queue();
    }

    @Override
    public void handleSelectionMenu(SelectionMenuEvent event, String data) {
        event.reply("Selected option " + event.getSelectedOptions().get(0).getLabel()
                + " with data: " + data).queue();
    }
}
