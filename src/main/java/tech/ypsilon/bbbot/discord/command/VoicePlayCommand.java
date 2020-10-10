package tech.ypsilon.bbbot.discord.command;

import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import tech.ypsilon.bbbot.util.EmbedUtil;
import tech.ypsilon.bbbot.voice.AudioManager;
import tech.ypsilon.bbbot.voice.AudioUtil;
import tech.ypsilon.bbbot.voice.TrackScheduler;

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

        if (!Objects.requireNonNull(Objects.requireNonNull(e.getMember()).getVoiceState()).inVoiceChannel()) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Bot kann nur aus einem Voice-Channel heraus gerufen werden");
            e.getChannel().sendMessage(b.build()).queue();
            return;
        }

        e.getGuild().getAudioManager().openAudioConnection(Objects.requireNonNull(e.getMember().getVoiceState()).getChannel());

        try {
            AudioItem itemBlocking = AudioUtil.getItemBlocking(args[0]);
            if (itemBlocking == null) {
                EmbedBuilder b = EmbedUtil.createErrorEmbed();
                b.setDescription("Link nicht abspielbar");
                e.getChannel().sendMessage(b.build()).queue();
                return;
            }
            AudioManager.getInstance().addTrack(e.getGuild(), itemBlocking);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String getDescription() {
        return "Mit 'kit play [Link]' joined der Bot deinem Voice-Channel und spielt die Audio des Links";
    }
}
