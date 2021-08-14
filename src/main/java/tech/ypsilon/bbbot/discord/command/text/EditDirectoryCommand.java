package tech.ypsilon.bbbot.discord.command.text;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import tech.ypsilon.bbbot.database.codecs.DirectoryCodec;
import tech.ypsilon.bbbot.database.codecs.LinkCodec;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.ArrayList;
import java.util.List;

public class EditDirectoryCommand extends FullStackedExecutor {
    @Override
    public String[] getAlias() {
        return new String[]{"editDir", "ed", "editDirectory", "editdir"};
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {
        if (args.length < 3 || (!args[1].equalsIgnoreCase("add") && !args[1].equalsIgnoreCase("remove"))) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Syntax: ... [Name der Sammlung] [add/remove] [Name der Verknüpfung1]" +
                    "(NameDerVerknüpfung2 NameDerVerknüpfung3)");
            e.getChannel().sendMessage(b.build()).queue();
            return;
        }

        DirectoryCodec directory = DirectoryCodec.getDirectory(e.getAuthor(), args[0]);
        if (directory == null) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Keine Sammlung zu diesem Namen gefunden");
            e.getChannel().sendMessage(b.build()).queue();
            return;
        }

        if (args[1].equalsIgnoreCase("add")) {
            List<LinkCodec> compatibleNames = new ArrayList<>();
            List<String> incompatibleNames = new ArrayList<>();
            for (int i = 2; i < args.length; i++) {
                boolean found = false;
                for (LinkCodec linkCodec : LinkCodec.getLinksForName("^" + args[i] + "$")) {
                    compatibleNames.add(linkCodec);
                    found = true;
                }
                if (!found) incompatibleNames.add(args[i]);
            }

            if (compatibleNames.size() > 0) {
                EmbedBuilder b = EmbedUtil.createSuccessEmbed();
                b.setDescription("Folgende Verknüpfungen wurden hinzugefügt:");
                for (LinkCodec compatibleName : compatibleNames) {
                    directory.addLink(compatibleName);
                    b.addField(compatibleName.getName(), compatibleName.getLink(), false);
                }
                e.getChannel().sendMessage(b.build()).queue();
            }

            if (incompatibleNames.size() > 0) {
                EmbedBuilder b = EmbedUtil.createErrorEmbed();
                b.setDescription("Folgende Verknüpfungen konnten nicht gefunden und hinzugefügt werden:");
                for (String incompatibleName : incompatibleNames) {
                    b.addField(incompatibleName, "", false);
                }
                e.getChannel().sendMessage(b.build()).queue();
            }
        } else {
            List<LinkCodec> compatibleNames = new ArrayList<>();
            List<String> incompatibleNames = new ArrayList<>();
            for (int i = 2; i < args.length; i++) {
                boolean found = false;
                for (LinkCodec linkCodec : LinkCodec.getLinksForName("^" + args[i] + "$")) {
                    compatibleNames.add(linkCodec);
                    found = true;
                }
                if (!found) incompatibleNames.add(args[i]);
            }

            if (compatibleNames.size() > 0) {
                EmbedBuilder b = EmbedUtil.createSuccessEmbed();
                b.setDescription("Folgende Verknüpfungen wurden entfernt:");
                for (LinkCodec compatibleName : compatibleNames) {
                    directory.removeLink(compatibleName);
                    b.addField(compatibleName.getName(), compatibleName.getLink(), false);
                }
                e.getChannel().sendMessage(b.build()).queue();
            } else {
                EmbedBuilder b = EmbedUtil.createErrorEmbed();
                b.setDescription("Folgende Verknüpfungen konnten nicht gefunden und entfernt werden:");
                for (String incompatibleName : incompatibleNames) {
                    b.addField(incompatibleName, "", false);
                }
                e.getChannel().sendMessage(b.build()).queue();
                return;
            }

            if (incompatibleNames.size() > 0) {
                EmbedBuilder b = EmbedUtil.createErrorEmbed();
                b.setDescription("Folgende Verknüpfungen konnten nicht gefunden und entfernt werden:");
                for (String incompatibleName : incompatibleNames) {
                    b.addField(incompatibleName, "", false);
                }
                e.getChannel().sendMessage(b.build()).queue();
            }
        }
    }

    @Override
    public String getDescription() {
        return "Editiere einen deiner Ordner mit 'kit editDir [NameDesOrdners] [add/remove] [Name der Verknüpfung1]" +
                " (NameDerVerknüpfung2) (NameDerVerknüpfung3) ... Mindestens ein Name einer Verknüpfung muss angegeben " +
                "werden, weitere sind optional. Für 'kit editDir [NameDesOrdners] add ... werden Verknüpfungen " +
                "hinzugefügt, für '... remove ...' entfernt";
    }

    @Override
    public void onPrivateExecute(PrivateMessageReceivedEvent e, String[] args) {

    }
}
