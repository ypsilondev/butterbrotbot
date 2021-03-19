package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

import java.awt.*;

public class GitHubCommand extends FullStackedExecutor {


    @Override
    public String[] getAlias() {
        return new String[]{"git", "github", "contribute"};
    }

    @Override
    public String getDescription() {
        return "Liefert Informationen, wie man an Butterbrot mitentwickeln kann.";
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {
        e.getMessage().delete().queue();
        answer(e.getChannel());
    }

    @Override
    public void onPrivateExecute(PrivateMessageReceivedEvent e, String[] args) {
        answer(e.getChannel());
    }

    private void answer(MessageChannel channel) {
        EmbedBuilder b = new EmbedBuilder();
        b.setColor(Color.ORANGE);
        b.setThumbnail("https://github.githubassets.com/images/modules/logos_page/Octocat.png");
        b.setTitle("Butterbrot auf GitHub", "https://github.com/ypsilondev/butterbrotbot/tree/dev");
        b.setDescription("Butterbrot ist ein Open-Source-Projekt, das bedeutet, "
                + "dass jeder (der Lust und Zeit hat und obendrein das Chaos auch noch versteht) helfen kann, diesen Bot zu verbessern, wobei das ja kaum möglich ist. Butterbrot ist ja eigentlich schon perfekt :D");
        b.addField("GitHub-Repository",
                "Hier findest du Butterbrot auf GitHub: https://github.com/ypsilondev/butterbrotbot/tree/dev",
                false);
        channel.sendMessage(b.build()).queue();
    }
}
