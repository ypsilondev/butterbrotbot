package tech.ypsilon.bbbot.discord.listener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.database.codecs.StudyGroupCodec;
import tech.ypsilon.bbbot.util.EmbedUtil;
import tech.ypsilon.bbbot.util.Initializable;

public class StudyGroupVoiceListener extends ButterbrotListener implements Initializable {

    private static final MessageEmbed NO_GROUP_GERMAN = EmbedUtil.createErrorEmbed()
            .setTitle("🇩🇪 Du bist in keiner Lerngruppe")
            .setDescription("Juhu, du hast ein neues Feature entdeckt! 🥳 Du kannst private "
                    + "Lerngruppen-Kanäle auf dem KIT Discord Sever benutzen. So kannst du das Feature "
                    + "benutzen:")
            .addField("Gruppe Erstellen", "`/group create` - Erstelle deine Gruppe "
                    + "und gib ihr einen Namen. Wähle einen guten Namen, du kannst ihn aktuell "
                    + "nicht selbst ändern. Du kannst nun über den Auto-Move-Kanal deinem privaten "
                    + "Lerngruppen-Sprachkanal beitreten!", false)
            .addField("Deine Freunde Hinzufügen", "`/group add` - Füge deine Lernpartner "
                    + "zu deiner Gruppe hinzu. Pass auf wen du hinzufügst, jeder kann neue Mitglieder "
                    + "einladen.", false)
            .addField("Gruppe Verlassen", "`/group leave` - Wurdest du versehentlich "
                    + "zur falschen Gruppe hinzugefügt? So kannst du eine Gruppe verlassen. Gib acht, "
                    + "du musst danach von einem Mitglied wieder hinzugefügt werden.", false)
            .addField("Noch Fragen?", "Hast du Fragen oder Verbesserungsvorschläge? "
                    + "Schreib uns: `/butterbrot`", false)
            .setFooter("")
            .build();

    private static final MessageEmbed NO_GROUP_ENGLISH = EmbedUtil.createErrorEmbed()
            .setTitle("🇬🇧🇺🇸 You are missing a study group...")
            .setDescription("You've just discovered a brand-new feature! 🥳 You can create private "
                    + "study group channels on the KIT Discord guild. Here is how to use them:")
            .addField("Create group", "`/group create` - Create your group "
                    + "and give it a name. Come up with a good name, as you currently cannot change it "
                    + "yourself. Congratulations, you can now join your own private study group "
                    + "voice channel!", false)
            .addField("Add your friends", "`/group add` - Add your study partners to "
                            + "your group. Be careful who you add though, as any member can invite new ones",
                    false)
            .addField("Leave group", "`/group leave` - Have you been added to the wrong "
                    + "group on accident? This is how you leave it again but be careful, another "
                    + "member will have to add you again if you decide to stay.", false)
            .addField("Any questions?", "Do you have any questions or suggestions? "
                    + "Write to us: `/butterbrot`", false)
            .setFooter("")
            .build();

    private Category studyGroupCategory;
    private VoiceChannel studyJoinChannel;

    public StudyGroupVoiceListener(ButterBrot parent) {
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

                // kick user and send message if in no study group
                member.getUser().openPrivateChannel().queue(privateChannel -> {
                    if (privateChannel != null) {
                        privateChannel.sendMessage(new MessageBuilder()
                                .setEmbeds(NO_GROUP_GERMAN, NO_GROUP_ENGLISH, new EmbedBuilder()
                                        .setDescription(studyJoinChannel.getAsMention())
                                        .build())
                                .build()).queue();
                    }
                });

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

        // break if group is null, otherwise NPE is thrown
        if (group == null) return;

        if (channelLeft.getName().equals(group.getName()) && channelLeft.getMembers().size() == 0) {
            channelLeft.delete().queue();
        }
    }
}
