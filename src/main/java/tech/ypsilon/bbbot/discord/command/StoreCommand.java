package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import tech.ypsilon.bbbot.database.codecs.LinkCodec;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.ArrayList;
import java.util.List;

public class StoreCommand extends LegacyCommand {
    @Override
    public String[] getAlias() {
        return new String[]{"store", "s"};
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {
        // bb store [name] [link] {...}

        /* unsupported:
        if (args.length == 0) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Falsche Syntax: Mindestens entweder Name und Datei oder Name und Link übergeben");
            e.getChannel().sendMessage(b.build()).queue();
            return;
        }

        for (Message.Attachment attachment : e.getMessage().getAttachments()) {
            return;
        }*/

        if (args.length < 2) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Falsche Syntax: Mindestens Link und Name übergeben");
            e.getChannel().sendMessage(b.build()).queue();
            return;
        }
        /*else if (!URI.isWellFormedAddress(args[1])) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("URL nicht gültig");
            e.getChannel().sendMessage(b.build()).queue();
            return;
        }*/

        if (LinkCodec.isPresent(args[0], args[1])) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Name oder Link schon vorhanden");
            e.getChannel().sendMessage(b.build()).queue();
            return;
        }

        List<String> keywords = new ArrayList<>();
        for (int i = 2; i < args.length; i++) {
            keywords.add(args[i]);
        }

        LinkCodec.createLink(args[0], e.getAuthor(), args[1], keywords);

        EmbedBuilder b = EmbedUtil.createSuccessEmbed();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Dein Link wurde erfolgreich mit dem Namen '").append(args[0]).append("' ");

        if (args.length == 3) {
            stringBuilder.append("und dem Keyword ").append(args[2]);
        } else if (args.length > 3) {
            stringBuilder.append("und den Keywords ").append(args[2]);
            for (int i = 3; i < args.length; i++) {
                stringBuilder.append(", ").append(args[i]);
            }
        }
        stringBuilder.append(" verknüpft");
        b.setDescription(stringBuilder.toString());
        e.getChannel().sendMessage(b.build()).queue();
    }

    @Override
    public String getDescription() {
        return "Speichert mit 'kit store [Name_Ohne_Leerzeichen] [Link] (Keyword1) (Keayword2) ...' Links zu Namen und " +
                "(optionalen) Keywords ab. Keywords sollten klein geschrieben werden.";
    }
}
