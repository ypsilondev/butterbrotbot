package tech.ypsilon.bbbot.discord.listener;

import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.database.codecs.StudyGroupCodec;
import tech.ypsilon.bbbot.util.Initializable;

public class ChannelListener extends ButterbrotListener implements Initializable {

    private Category studyGroupCategory;
    private VoiceChannel studyJoinChannel;

    public ChannelListener(ButterBrot parent) {
        super(parent);
    }

    @Override
    public void init() throws IllegalStateException {
        this.studyGroupCategory = getParent().getDiscordController().getHome()
                .getCategoryById(getParent().getConfig().getDiscord().getStudyGroupCategory());
        this.studyJoinChannel = getParent().getDiscordController().getHome()
                .getVoiceChannelById(getParent().getConfig().getDiscord().getStudyJoinChannel());

        if (studyGroupCategory == null) throw new IllegalStateException("study-group category cannot be null!");
        if (studyJoinChannel == null) throw new IllegalStateException("study join channel cannot be null!");
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getMessage().isWebhookMessage()) {
            if (event.getMessage().getAuthor().getName().toLowerCase().contains("birthday")) {
                // Birthday-webhook
                event.getMessage().addReaction("U+1F381").queue();
                event.getMessage().addReaction("U+1F382").queue();
            }
        }
    }

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
        if (channelJoined.getIdLong() == studyJoinChannel.getIdLong()) {
            StudyGroupCodec group = StudyGroupCodec.retrieveStudyGroup(member.getUser());
            if (group == null) {
                guild.kickVoiceMember(member).queue();
                return;
            }

            for (VoiceChannel voiceChannel : studyGroupCategory.getVoiceChannels()) {
                if (voiceChannel.getName().equals(group.getName())) {
                    guild.moveVoiceMember(member, voiceChannel).queue();
                    return;
                }
            }

            studyGroupCategory.createVoiceChannel(group.getName())
                    .queue(voiceChannel -> {
                        guild.moveVoiceMember(member, voiceChannel).queue();
                        voiceChannel.getManager().setUserLimit(1).queue();
                    });
        }
    }

    private void updateChannels(VoiceChannel channelLeft, Member member) {
        Category parent = channelLeft.getParent();
        if (parent == null) return;
        if (parent.getIdLong() != studyGroupCategory.getIdLong()) return;
        StudyGroupCodec group = StudyGroupCodec.retrieveStudyGroup(member.getUser());
        if (channelLeft.getName().equals(group.getName()) && channelLeft.getMembers().size() == 0) {
            channelLeft.delete().queue();
        }
    }

}
