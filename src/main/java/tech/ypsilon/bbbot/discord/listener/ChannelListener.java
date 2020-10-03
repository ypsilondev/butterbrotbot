package tech.ypsilon.bbbot.discord.listener;

import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import tech.ypsilon.bbbot.database.codecs.StudyGroupCodec;

public class ChannelListener extends ListenerAdapter {
    private final static long CAT_ID = 762052348259467275L;

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent e) {
        if (e.getChannelJoined().getIdLong() == 762050439859929149L) {
            StudyGroupCodec group = StudyGroupCodec.retrieveStudyGroup(e.getMember().getUser());
            if (group == null) {
                e.getGuild().kickVoiceMember(e.getMember()).queue();
                return;
            }

            for (VoiceChannel voiceChannel : e.getGuild().getCategoryById(CAT_ID).getVoiceChannels()) {
                if (voiceChannel.getName().equals(group.getName())) {
                    e.getGuild().moveVoiceMember(e.getMember(), voiceChannel);
                    return;
                }
            }

            e.getGuild().getCategoryById(CAT_ID).createVoiceChannel(group.getName())
                    .queue(voiceChannel -> {
                e.getGuild().moveVoiceMember(e.getMember(), voiceChannel).queue();
            });
        }
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        StudyGroupCodec group = StudyGroupCodec.retrieveStudyGroup(event.getMember().getUser());
        if (event.getChannelLeft().getName().equals(group.getName()) && event.getChannelLeft().getMembers().size() == 0) {
            event.getChannelLeft().delete().queue();
        }
    }
}
