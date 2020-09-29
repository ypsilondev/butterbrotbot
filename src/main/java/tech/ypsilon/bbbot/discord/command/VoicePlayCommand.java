package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.Objects;

public class VoicePlayCommand extends Command {
    @Override
    public String[] getAlias() {
        return new String[]{"play"};
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {
        if (args.length == 0) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Übergebe einen Link");
            e.getChannel().sendMessage(b.build()).queue();
            return;
        }

        // todo: Check for link
        if (!Objects.requireNonNull(Objects.requireNonNull(e.getMember()).getVoiceState()).inVoiceChannel()) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Bot kann nur aus einem Voice-Channel heraus gerufen werden");
            e.getChannel().sendMessage(b.build()).queue();
            return;
        }

        e.getGuild().getAudioManager().openAudioConnection(Objects.requireNonNull(e.getMember().getVoiceState()).getChannel());
    }

    @Override
    public String getDescription() {
        return null;
    }
}
