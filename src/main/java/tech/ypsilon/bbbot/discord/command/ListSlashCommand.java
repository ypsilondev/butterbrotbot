package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import tech.ypsilon.bbbot.database.codecs.LinkCodec;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListSlashCommand extends SlashCommand {
    @Override
    public CommandData commandData() {
        return new CommandData("list", "Liste alle Link-Verknüpfungen oder suche nach bestimmten").addOptions(
                new OptionData(OptionType.STRING, "search", "Der Name der Verknüpfung", false)
        );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        event.deferReply(true).queue();


        List<String> args = new ArrayList<>();

        OptionMapping nameMapping = event.getOption("search");
        if (nameMapping != null) {
            args.addAll(Arrays.asList(nameMapping.getAsString().split(" ")));
        }


        if (args.size() == 0) {
            boolean hasMatches = false;
            for (LinkCodec linkCodec : LinkCodec.getAllLinks()) {
                hasMatches = true;
            }

            EmbedBuilder b = EmbedUtil.createListEmbed(hasMatches);
            if (!hasMatches) {
                b.setDescription("Es gibt noch keine Links");
            } else {
                for (LinkCodec linkCodec : LinkCodec.getAllLinks()) {
                    b.addField(linkCodec.getName(), linkCodec.getLink(), false);
                }
            }
            event.getHook().editOriginalEmbeds(b.build()).queue();
        } else {
            boolean hasMatches = false;
            for (String keyword : args) {
                // TODO: what is this and why does it work?
                for (LinkCodec linkCodec : LinkCodec.getLinksFromKeyword(keyword)) {
                    hasMatches = true;
                }
                for (LinkCodec linkCodec : LinkCodec.getLinksForName(keyword)) {
                    hasMatches = true;
                }
            }

            EmbedBuilder b = EmbedUtil.createListEmbed(hasMatches);
            if (!hasMatches) {
                b.setDescription("Es wurden anhand deines Filters keine Ergebnisse gefunden");
            } else {
                for (String keyword : args) {
                    for (LinkCodec linkCodec : LinkCodec.getLinksFromKeyword(keyword)) {
                        b.addField(linkCodec.getName(), linkCodec.getLink(), false);
                    }
                    for (LinkCodec linkCodec : LinkCodec.getLinksForName(keyword)) {
                        b.addField(linkCodec.getName(), linkCodec.getLink(), false);
                    }
                }
            }
            event.getHook().editOriginalEmbeds(b.build()).queue();
        }

    }
}
