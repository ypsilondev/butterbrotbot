package tech.ypsilon.bbbot.discord.command.text;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import tech.ypsilon.bbbot.discord.listener.CensorWatcherListener;
import tech.ypsilon.bbbot.util.DiscordUtil;

import java.util.Objects;

@Deprecated
public class CensorshipCommand implements GuildExecuteHandler {
    @Override
    public String[] getAlias() {
        return new String[]{"censor"};
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {
        // kit censor
        // kit censor @User [future messages to censor]
        if (DiscordUtil.isAdmin(Objects.requireNonNull(e.getMember()))) {
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
