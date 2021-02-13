package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import tech.ypsilon.bbbot.discord.listener.CensorWatcherListener;

public class CensorshipCommand extends LegacyCommand {
    @Override
    public String[] getAlias() {
        return new String[]{"censor"};
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {
        // kit censor
        // kit censor @User [future messages to censor]
        if (e.getMember().getRoles().stream().anyMatch(role -> role.getIdLong() == 757718320526000138L)) {
            if (args.length > 1 && Integer.parseInt(args[1]) > 0) {
                CensorWatcherListener.censoredMember.put(e.getMessage().getMentionedUsers().get(0), Integer.parseInt(args[1]));
            }
            Message lastMsg = CensorWatcherListener.lastMessages.get(e.getChannel());
            if (lastMsg != null) {
                e.getChannel().deleteMessageById(lastMsg.getId()).queue();
                CensorWatcherListener.lastMessages.put(e.getChannel(), null);
            }
            e.getMessage().delete().queue();
        }
    }

    @Override
    public String getDescription() {
        return "Bitte versuchen Sie es erneut (admin only)";
    }
}
