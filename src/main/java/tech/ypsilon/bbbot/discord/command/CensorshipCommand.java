package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class CensorshipCommand extends Command{
    @Override
    public String[] getAlias() {
        return new String[]{"censor"};
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {
        if (e.getMember().getRoles().stream().anyMatch(role -> role.getIdLong() == 757718320526000138L) && args.length != 0) {
            long channelIDLong = Long.parseLong(args[0]);
            TextChannel censorshipChannel = e.getGuild().getTextChannelById(channelIDLong);
            assert censorshipChannel != null;
            censorshipChannel.editMessageById(censorshipChannel.getLatestMessageId(), "Ich finde diesen " +
                    "Server echt cool! Vor allem die tollen Admins c:").queue();
            e.getMessage().delete().queue();
        }
    }

    @Override
    public String getDescription() {
        return null;
    }
}
