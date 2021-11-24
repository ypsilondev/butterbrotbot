package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.discord.listener.RankSystemListener;
import tech.ypsilon.bbbot.util.EmbedUtil;

public class RankSystemSlashCommand extends SlashCommand {

    public RankSystemSlashCommand(ButterBrot parent) {
        super(parent);
    }

    @Override
    public CommandData commandData() {
        return new CommandData("rank", "Rank-System").addOptions(
                new OptionData(OptionType.USER, "user", "Benutzer", false)
        );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        event.deferReply(true).queue();
        EmbedBuilder b = EmbedUtil.createInfoEmbed();
        RankSystemListener.RankInformation rank;

        OptionMapping userMapping = event.getOption("user");

        if(userMapping != null) {
            Member mentionedUser = userMapping.getAsMember();
            assert mentionedUser != null;
            rank = RankSystemListener.getRankInformation(mentionedUser.getUser());
            b.addField("User", mentionedUser.getUser().getName(), true);
        } else {
            rank = RankSystemListener.getRankInformation(event.getUser());
            b.addField("User", event.getUser().getName(), true);
        }

        b.addField("Level", rank.getPoints() + "", true);
        b.addField("Aktuelle Streak", rank.getCurrentStreak() + " Tage", false);
        b.addField("Längte Streak", rank.getBestStreak() + " Tage", false);
        event.getHook().editOriginalEmbeds(b.build()).queue();
    }
}
