package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import tech.ypsilon.bbbot.database.codecs.DirectoryCodec;
import tech.ypsilon.bbbot.database.codecs.LinkCodec;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.ArrayList;
import java.util.List;

public class AddDirectoryCommand extends Command implements PrivateChat {
    @Override
    public String[] getAlias() {
        return new String[]{"mkdir", "addDirectory", "createDirectory", "ad"};
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {

    }

    @Override
    public String getDescription() {
        return "Erstelle personalisierte Ordner mit einer eigenen Sammlung an Links/Verknüpfungen: " +
                "'kit mkdir [OrdnerName] [public/private] (NameVerknüpfung1) (NameVerknüpfung2) ...' " +
                "Verknüpfungsnamen sind optional und können auch im Nachhinhein hinzugefügt werden.";
    }

    @Override
    public void onPrivateExecute(PrivateMessageReceivedEvent e, String[] args) {
        // bb mkdir [name] [shared] {names...}
        if(args.length < 2) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Übergebe den Namen und die Sichtbarkeit (public/private) des zu erstellenden Ordners");
            e.getAuthor().openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage(b.build())).queue();
            return;
        }

        if (!args[1].equals("public") && !args[1].equals("private")) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Übergebe entweder public oder private nach dem Namen");
            e.getAuthor().openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage(b.build())).queue();
            return;
        }

        boolean shared = args[1].equals("public");

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

        DirectoryCodec directoryCodec = DirectoryCodec.addDirectory(e.getAuthor(), args[0], compatibleNames);
        if (directoryCodec == null) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Eine Sammlung mit diesem Namen existiert bereits");
            e.getAuthor().openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage(b.build())).queue();
            return;
        }

        directoryCodec.setShared(shared);

        EmbedBuilder b = EmbedUtil.createSuccessEmbed();
        if (args.length > 2) {
            b.setDescription("Deine Sammlung wurde erfolgreich erstellt. Insgesamt wurden " + compatibleNames.size() +
                    " gefunden und hinzugefügt. Nicht gefunden: " + incompatibleNames.size() + ". Hinzugefügt: ");
            for (LinkCodec compatibleName : compatibleNames) {
                b.addField(compatibleName.getName(), compatibleName.getLink(), false);
            }
            e.getAuthor().openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage(b.build())).queue();

            if (incompatibleNames.size() != 0) {
                EmbedBuilder b2 = EmbedUtil.createErrorEmbed();
                b2.setDescription("Folgende Namen konnten nicht gefunden werden." +
                        "Du kannst sie nachträglich per editDirectory Command hinzufügen:");
                for (String incompatibleName : incompatibleNames) {
                    b.addField(incompatibleName, "", false);
                }
                e.getAuthor().openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage(b2.build())).queue();
            }
        } else {
            b.setDescription("Deine Sammlung wurde erfolgreich erstellt. Füge Verknüpfungen mit dem editDirectory Command hinzu");
        }
    }
}
