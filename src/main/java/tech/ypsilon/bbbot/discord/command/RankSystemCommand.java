package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import tech.ypsilon.bbbot.discord.listener.RankSystemListener;
import tech.ypsilon.bbbot.util.EmbedUtil;

public class RankSystemCommand extends Command {
    @Override
    public String[] getAlias() {
        return new String[]{"rank"};
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {
        EmbedBuilder b = EmbedUtil.createSuccessEmbed();
        RankSystemListener.RankInformation rank;
        if (args.length == 0) {
            rank = RankSystemListener.getRankInformation(e.getAuthor());
            b.addField("User", e.getAuthor().getName(), true);
        } else {
            Member mentionedUser = e.getMessage().getMentionedMembers().get(0);
            rank = RankSystemListener.getRankInformation(mentionedUser.getUser());
            b.addField("User", mentionedUser.getUser().getName(), true);
        }
        b.addField("Level", rank.getPoints() + "", true);
        b.addField("Aktuelle Streak", rank.getCurrentStreak() + " Tage", false);
        b.addField("Längte Streak", rank.getBestStreak() + " Tage", false);
        e.getChannel().sendMessage(b.build()).queue();
    }

    @Override
    public String getDescription() {
        return "Sehe Details zu deinem Rang, oder dem Rang von anderen: 'kit rank (@User)'";
    }
}
