package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.Nullable;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.awt.*;

/**
 * Slash command for general information about the bot
 *
 * @author Christian Schliz (code@foxat.de)
 */
public class ButterbrotCommand extends SlashCommand {

    public ButterbrotCommand(ButterBrot parent) {
        super(parent);
    }

    @Override
    public CommandData commandData() {
        return new CommandData("butterbrot", "Informationen über den Butterbrot-Bot");
    }

    @Override
    public @Nullable String getHelpDescription() {
        return "/butterbrot gibt einige interessante Informationen zu Butterbrot";
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
                .setDescription("Butterbrot ist ein Bot, der speziell für den KIT Discord Server entwickelt wurde. ")
                .addField("Contributors", "<@!141171046777749504>, <@!358213000550809600>, " +
                        "<@!117625148785295363>, <@!699011153208016926>, <@!237593967137390592>", false)
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
