package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class WriteAfterMeCommand extends Command {
    @Override
    public String[] getAlias() {
        return new String[]{"wam"};
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {
        if(e.getMember().getRoles().stream().anyMatch(role -> role.getIdLong() == 757718320526000138L)) {
            e.getChannel().sendMessage(e.getMessage().getContentRaw().substring(8)).complete();
            e.getMessage().delete().complete();
        }
    }

    @Override
    public String getDescription() {
        return "Schreibt die eigene Nachricht erneut (admin only)";
    }
}
