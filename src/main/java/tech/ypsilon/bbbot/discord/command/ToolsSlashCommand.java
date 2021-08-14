package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import tech.ypsilon.bbbot.discord.services.ToolUpdaterService;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ToolsSlashCommand extends SlashCommand {
    @Override
    public CommandData commandData() {
        return new CommandData("tools", "Liefert eine Auflistung sinnvoller und praktischer Werkzeuge für den Studienalltag");
    }

    @Override
    public boolean isGlobal() {
        return true;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        // event.deferReply(true).queue();
        // event.getHook().editOriginalEmbeds(getInfoEmbed().build()).queue();
        event.reply("Wähle eine Kategorie aus:").addActionRow(
                SelectionMenu.create(createSelectMenuId("a"))
                        .addOption("option 1", "option 1")
                        .addOption("option 2", "option 2")
                        .build()
        ).setEphemeral(true).queue();
    }

    @Override
    public void handleSelectionMenu(SelectionMenuEvent event, String data) {
        event.editMessage("test").queue();
    }

    private static EmbedBuilder getInfoEmbed() {
        HashMap<String, String> data = ToolUpdaterService.links;
        EmbedBuilder builder = EmbedUtil.createDefaultEmbed();
        builder.setDescription("Auflistung nützlicher Tools für Studies:");
        data.forEach((title, content) -> builder.addField(title, content, false));
        return builder;
    }
}
