package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import tech.ypsilon.bbbot.database.codecs.DirectoryCodec;
import tech.ypsilon.bbbot.database.codecs.LinkCodec;
import tech.ypsilon.bbbot.util.EmbedUtil;

public class GetDirectoryCommand extends Command implements PrivateChat {
    @Override
    public String[] getAlias() {
        return new String[]{"cd", "getDir", "dir", "d", "getDirectory"};
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {
        if (args.length == 0) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Übergebe den Sammlungsnamen");
            e.getChannel().sendMessage(b.build()).queue();
            return;
        }

        boolean isEmpty = true;
        for (DirectoryCodec directory : DirectoryCodec.getDirectories(e.getAuthor(), true)) {
            if (directory.getName().equalsIgnoreCase(args[0])) {
                isEmpty = false;
                EmbedBuilder b = EmbedUtil.createDirectoryEmbed();
                b.setDescription("Sichtbarkeit public?: " + directory.isShared() + "\nListe aller Verknüpfungen:");
                for (LinkCodec linkCodec : directory.getLinks()) {
                    b.addField(linkCodec.getName(), linkCodec.getLink(), false);
                }
                b.setAuthor(e.getAuthor().getName());
                e.getChannel().sendMessage(b.build()).queue();
            }
        }

        if (isEmpty) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Keine Sammlung für diesen Namen gefunden");
            e.getChannel().sendMessage(b.build()).queue();
        }
    }

    @Override
    public String getDescription() {
        return "Zeige einen Ordner/eine Sammlung mit 'kit cd [Name]' an";
    }

    @Override
    public void onPrivateExecute(PrivateMessageReceivedEvent e, String[] args) {
        if (args.length == 0) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Übergebe den Sammlungsnamen");
            e.getAuthor().openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage(b.build())).queue();
            return;
        }

        boolean isEmpty = true;
        for (DirectoryCodec directory : DirectoryCodec.getDirectories(e.getAuthor(), true)) {
            if (directory.getName().equalsIgnoreCase(args[0])) {
                isEmpty = false;
                EmbedBuilder b = EmbedUtil.createDirectoryEmbed();
                b.setDescription("Sichtbarkeit public?: " + directory.isShared() + "\nListe aller Verknüpfungen:");
                for (LinkCodec linkCodec : directory.getLinks()) {
                    b.addField(linkCodec.getName(), linkCodec.getLink(), false);
                }
                b.setAuthor(e.getAuthor().getName());
                e.getAuthor().openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage(b.build())).queue();
            }
        }

        if (isEmpty) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Keine Sammlung für diesen Namen gefunden");
            e.getAuthor().openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage(b.build())).queue();
        }
    }
}
