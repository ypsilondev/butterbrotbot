package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.awt.Color;

/**
 * Slash command for general information about the bot
 *
 * @author Christian Schliz (code@foxat.de)
 */
public class ButterbrotCommand extends SlashCommand {

    @Override
    public CommandData commandData() {
        return new CommandData("butterbrot", "Informationen über den Butterbrot-Bot");
    }

    @Override
    public boolean isGlobal() {
        return true;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setEmbeds(getInformationEmbed(), getContributionEmbed());
        event.reply(messageBuilder.build()).queue();
    }

    private MessageEmbed getInformationEmbed() {
        return EmbedUtil.createInfoEmbed()
                .setTitle("Was ist Butterbrot?")
                .setDescription("Butterbrot ist ein Bot, der speziell für den KIT Discord Server entwickelt wurde. " +
                        "An dieser Stelle könnte man natürlich eine gescheite Beschreibung machen " +
                        "aber ich bin gerade ziemlich unkreativ :)")
                .build();
    }

    private MessageEmbed getContributionEmbed() {
        return new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setThumbnail("https://github.githubassets.com/images/modules/logos_page/Octocat.png")
                .setTitle("Butterbrot auf GitHub", "https://github.com/ypsilondev/butterbrotbot/tree/dev")
                .setDescription("Butterbrot ist ein Open-Source-Projekt, das bedeutet, dass jeder " +
                        "(der Lust und Zeit hat und obendrein das Chaos auch noch versteht) helfen kann, " +
                        "diesen Bot zu verbessern, wobei das ja kaum möglich ist. " +
                        "Butterbrot ist ja eigentlich schon perfekt :D")
                .addField("GitHub-Repository", "Hier findest du Butterbrot auf GitHub: " +
                        "https://github.com/ypsilondev/butterbrotbot/", false)
                .build();
    }
}
