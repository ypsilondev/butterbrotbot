package tech.ypsilon.bbbot.discord.listener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelCreateEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import tech.ypsilon.bbbot.database.codecs.StudyGroupCodec;

public class ChannelListener extends ListenerAdapter {
    private final static long CAT_ID = 762052348259467275L;

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent e) {
        updateChannels(e.getChannelLeft(), e.getMember());
        updateUser(e.getChannelJoined(), e.getChannelLeft(), e.getGuild(), e.getMember());
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent e) {
        updateUser(e.getChannelJoined(), e.getChannelLeft(), e.getGuild(), e.getMember());
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        updateChannels(event.getChannelLeft(), event.getMember());
    }

    private void updateUser(VoiceChannel channelJoined, VoiceChannel channelLeft, Guild guild, Member member) {
        if (channelJoined.getIdLong() == 762050439859929149L) {
            StudyGroupCodec group = StudyGroupCodec.retrieveStudyGroup(member.getUser());
            if (group == null) {
                guild.kickVoiceMember(member).queue();
                return;
            }

            for (VoiceChannel voiceChannel : guild.getCategoryById(CAT_ID).getVoiceChannels()) {
                if (voiceChannel.getName().equals(group.getName())) {
                    guild.moveVoiceMember(member, voiceChannel).queue();
                    return;
                }
            }

            guild.getCategoryById(CAT_ID).createVoiceChannel(group.getName())
                    .queue(voiceChannel -> {
                        guild.moveVoiceMember(member, voiceChannel).queue();
                        voiceChannel.getManager().setUserLimit(1).queue();
                    });
        }
    }

    private void updateChannels(VoiceChannel channelLeft, Member member) {
        Category parent = channelLeft.getParent();
        if(parent == null) return;
        if(parent.getIdLong() != CAT_ID) return;
        StudyGroupCodec group = StudyGroupCodec.retrieveStudyGroup(member.getUser());
        if (channelLeft.getName().equals(group.getName()) && channelLeft.getMembers().size() == 0) {
            channelLeft.delete().queue();
        }
    }

}
