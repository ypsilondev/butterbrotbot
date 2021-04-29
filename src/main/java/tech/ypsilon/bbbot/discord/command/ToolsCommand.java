package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import tech.ypsilon.bbbot.discord.services.ToolUpdaterService;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.HashMap;

public class ToolsCommand extends FullStackedExecutor {
    @Override
    public String[] getAlias() {
        return new String[]{"tool", "tools"};
    }

    @Override
    public String getDescription() {
        return "Zeigt eine Liste mit hilfreichen Werkzeugen für Studierende an.";
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {
        e.getMessage().delete().queue();
        e.getAuthor().openPrivateChannel().queue(this::response);
    }

    @Override
    public void onPrivateExecute(PrivateMessageReceivedEvent e, String[] args) {
        response(e.getChannel());
    }

    private void response(MessageChannel channel) {
        channel.sendMessage(getInfoEmbed().build()).queue();
    }

    private static EmbedBuilder getInfoEmbed() {
        HashMap<String, String> data = ToolUpdaterService.links;
        EmbedBuilder builder = EmbedUtil.createDefaultEmbed();
        builder.setDescription("Auflistung nützlicher Tools für Studies:");
        data.forEach((title, content) -> builder.addField(title, content, false));
        return builder;
    }
}
