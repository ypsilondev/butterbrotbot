package tech.ypsilon.bbbot.discord.command;

import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import tech.ypsilon.bbbot.util.EmbedUtil;
import tech.ypsilon.bbbot.voice.AudioManager;
import tech.ypsilon.bbbot.voice.AudioUtil;
import tech.ypsilon.bbbot.voice.TrackScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;

public class VoiceCommands implements CommandBucket {
    @Override
    public void register(ArrayList<DiscordFunction> functions) {

        // Play Command
        new CommandBuilder("play")
                .setDescription("Mit 'kit play [Link]' joined der Bot deinem Voice-Channel und spielt die Audio des Links")
                .setExecutor((GuildExecuteHandler) (e, args) -> {
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
                })
                .buildAndAdd(functions);

        // Leave Command
        new CommandBuilder("leave")
                .setDescription("Let the bot leave the channel")
                .setExecutor((GuildExecuteHandler) (e, args) -> {
                    if (Objects.equals(Objects.requireNonNull(Objects.requireNonNull(e.getMember()).getVoiceState()).getChannel(), e.getGuild().getAudioManager().getConnectedChannel())) {
                        e.getGuild().getAudioManager().closeAudioConnection();
                    }
                })
                .buildAndAdd(functions);

        // Clear Queue
        new CommandBuilder("clearQueue")
                .setDescription("Clear the playback queue")
                .setExecutor((GuildExecuteHandler) (e, args) -> {
                    TrackScheduler scheduler = AudioManager.getInstance().getScheduler(e.getGuild());
                    scheduler.getQueue().clear();
                })
                .buildAndAdd(functions);

        // Skip Track
        new CommandBuilder("skip")
                .setDescription("Skip a Track")
                .setExecutor((GuildExecuteHandler) (e, args) -> {
                    TrackScheduler scheduler = AudioManager.getInstance().getScheduler(e.getGuild());
                    scheduler.skip(1);
                }).buildAndAdd(functions);

    }
}
