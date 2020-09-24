package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import tech.ypsilon.bbbot.database.codecs.LinkCodec;
import tech.ypsilon.bbbot.util.EmbedUtil;

public class ListCommand extends Command {
    @Override
    public String[] getAlias() {
        return new String[]{"list", "l", "get"};
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {
        if (args.length == 0) {
            boolean hasMatches = false;
            for (LinkCodec linkCodec : LinkCodec.getAllLinks()) {
                hasMatches = true;
            }

            EmbedBuilder b = EmbedUtil.createListEmbed(hasMatches);
            if (!hasMatches) {
                b.setDescription("Es gibt noch keine Links");
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Alle Verknüpfungen:\n");
                for (LinkCodec linkCodec : LinkCodec.getAllLinks()) {
                    stringBuilder.append(" - ").append(linkCodec.getName()).append(": ").append(linkCodec.getLink());
                }
                b.setDescription(stringBuilder.toString());
            }
            e.getChannel().sendMessage(b.build()).queue();
            return;
        }


        boolean hasMatches = false;
        for (String keyword : args) {
            for (LinkCodec linkCodec : LinkCodec.getLinksFromKeyword(keyword)) {
                hasMatches = true;
            }
        }

        EmbedBuilder b = EmbedUtil.createListEmbed(hasMatches);
        if (!hasMatches) {
            b.setDescription("Es wurden anhand deines Filters keine Ergebnisse gefunden");
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Folgende Ergebnisse wurden gefunden:\n");
            for (String keyword : args) {
                for (LinkCodec linkCodec : LinkCodec.getLinksFromKeyword(keyword)) {
                    stringBuilder.append(" - ").append(linkCodec.getName()).append(": ").append(linkCodec.getLink());
                }
            }
            b.setDescription(stringBuilder.toString());
        }
        e.getChannel().sendMessage(b.build()).queue();
    }

    @Override
    public String getDescription() {
        return "Liste alle Link-Verknüpfungen oder suche nach bestimmten";
    }
}
