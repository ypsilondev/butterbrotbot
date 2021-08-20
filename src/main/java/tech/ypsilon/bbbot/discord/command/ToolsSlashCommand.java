package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import tech.ypsilon.bbbot.discord.services.ToolUpdaterService;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.Map;
import java.util.stream.Collectors;

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
        SelectionMenu.Builder selectBuilder = SelectionMenu.create(createSelectMenuId(""))
                .setRequiredRange(1, 10).setPlaceholder("Wähle eine Kategorie...");

        for (String category : getInfoData().keySet())
            selectBuilder.addOption(category, "Nützliche Infos zu " + category);

        event.reply("Bitte wähle zuerst eine Kategorie. (Du kannst auch nacheinander verschiedene auswählen)")
                .addActionRow(selectBuilder.build()).setEphemeral(true).queue();
    }

    @Override
    public void handleSelectionMenu(SelectionMenuEvent event, String data) {
        if (event.getSelectedOptions() == null) throw new CommandFailedException("Es ist ein Fehler aufgetreten");

        event.editMessage("").setEmbeds(
                event.getSelectedOptions().stream().map(option -> fromCategory(option.getLabel(), getInfoData().get(option.getLabel()))).collect(Collectors.toList())
        ).queue();
    }

    private static MessageEmbed fromCategory(String title, String content) {
        return EmbedUtil.createInfoEmbed().setTitle(title).setDescription(content).build();
    }

    private static Map<String, String> getInfoData() {
        return ToolUpdaterService.links;
    }
}
