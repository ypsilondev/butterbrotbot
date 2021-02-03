package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import tech.ypsilon.bbbot.database.codecs.LinkCodec;
import tech.ypsilon.bbbot.util.EmbedUtil;

public class ListCommand extends LegacyCommand {
    @Override
    public String[] getAlias() {
        return new String[]{"listStores", "l", "get"};
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
                for (LinkCodec linkCodec : LinkCodec.getAllLinks()) {
                    b.addField(linkCodec.getName(), linkCodec.getLink(), false);
                }
            }
            e.getChannel().sendMessage(b.build()).queue();
            return;
        }


        boolean hasMatches = false;
        for (String keyword : args) {
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
        e.getChannel().sendMessage(b.build()).queue();
    }

    @Override
    public String getDescription() {
        return "Liste alle Link-Verknüpfungen ('kit get') oder suche nach bestimmten mit 'kit get [Name/Keyword]'";
    }
}
