package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import tech.ypsilon.bbbot.util.DiscordUtil;

public class WriteAfterMeCommand implements GuildExecuteHandler {
    @Override
    public String[] getAlias() {
        return new String[]{"wam"};
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {
        if (DiscordUtil.isAdmin(e.getMember())) {
            e.getChannel().sendMessage(e.getMessage().getContentRaw().substring(8)).complete();
            e.getMessage().delete().complete();
        }
    }

    @Override
    public String getDescription() {
        return "Schreibt die eigene Nachricht erneut (admin only)";
    }
}
