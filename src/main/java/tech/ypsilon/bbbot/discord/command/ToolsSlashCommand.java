package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import tech.ypsilon.bbbot.discord.services.ToolUpdaterService;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.HashMap;

public class ToolsSlashCommand extends SlashCommand{
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
        event.deferReply(true).queue();
        event.getHook().editOriginalEmbeds(getInfoEmbed().build()).queue();
    }

    private static EmbedBuilder getInfoEmbed() {
        HashMap<String, String> data = ToolUpdaterService.links;
        EmbedBuilder builder = EmbedUtil.createDefaultEmbed();
        builder.setDescription("Auflistung nützlicher Tools für Studies:");
        data.forEach((title, content) -> builder.addField(title, content, false));
        return builder;
    }
}
