package tech.ypsilon.bbbot.discord.command;

import com.sun.org.apache.xml.internal.utils.URI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.net.URL;

public class StoreCommand extends Command {
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
        } else if (!URI.isWellFormedAddress(args[1])) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("URL nicht gültig");
            e.getChannel().sendMessage(b.build()).queue();
            return;
        }

        // todo: Name schon vorhanden?
        // todo: Link schon vorhanden?

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
    }

    @Override
    public String getDescription() {
        return "Links oder Dateien zu Namen und Keywords speichern";
    }
}
