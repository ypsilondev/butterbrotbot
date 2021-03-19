package tech.ypsilon.bbbot.discord.command;

import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import net.dv8tion.jda.api.EmbedBuilder;
import tech.ypsilon.bbbot.util.EmbedUtil;
import tech.ypsilon.bbbot.voice.AudioManager;
import tech.ypsilon.bbbot.voice.AudioUtil;
import tech.ypsilon.bbbot.voice.TrackScheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;

public class VoiceCommands implements CommandBucket {
    @Override
    public void register(List<DiscordFunction> functions) {

        // Play Command
        new CommandBuilder("play")
                .setDescription("Mit 'kit play [Link]' joined der Bot deinem Voice-Channel und spielt die Audio des Links")
                .setExecutor((e, args) -> {
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
                .setExecutor((e, args) -> {
                    if (Objects.equals(Objects.requireNonNull(Objects.requireNonNull(e.getMember()).getVoiceState()).getChannel(), e.getGuild().getAudioManager().getConnectedChannel())) {
                        e.getGuild().getAudioManager().closeAudioConnection();
                    }
                })
                .buildAndAdd(functions);

        // Clear Queue
        new CommandBuilder("clearQueue")
                .setDescription("Clear the playback queue")
                .setExecutor((e, args) -> {
                    TrackScheduler scheduler = AudioManager.getInstance().getScheduler(e.getGuild());
                    scheduler.getQueue().clear();
                })
                .buildAndAdd(functions);

        // Skip Track
        new CommandBuilder("skip")
                .setDescription("Skip a Track")
                .setExecutor((e, args) -> {
                    TrackScheduler scheduler = AudioManager.getInstance().getScheduler(e.getGuild());
                    scheduler.skip(1);
                }).buildAndAdd(functions);

        YoutubeSearchProvider ytsp = new YoutubeSearchProvider();
        YoutubeAudioSourceManager yasm = new YoutubeAudioSourceManager();
        new CommandBuilder("search")
                .setDescription("Searches a YouTube™ Video")
                .setExecutor((e, args) -> {
                    if (!Objects.requireNonNull(Objects.requireNonNull(e.getMember()).getVoiceState()).inVoiceChannel()) {
                        EmbedBuilder b = EmbedUtil.createErrorEmbed();
                        b.setDescription("Bot kann nur aus einem Voice-Channel heraus gerufen werden");
                        e.getChannel().sendMessage(b.build()).queue();
                        return;
                    }
                    String search = String.join(" ", args);
                    AudioItem ai = ytsp.loadSearchResult(search.strip(), audioTrackInfo -> new YoutubeAudioTrack(audioTrackInfo, yasm));
                    if (ai instanceof BasicAudioPlaylist) {
                        BasicAudioPlaylist pl = (BasicAudioPlaylist) ai;
                        AudioManager.getInstance().addTrack(e.getGuild(), pl.getTracks().get(0));
                        e.getGuild().getAudioManager().openAudioConnection(Objects.requireNonNull(e.getMember().getVoiceState()).getChannel());
                    } else {
                        e.getChannel().sendMessage(EmbedUtil.createErrorEmbed().addField("YouTube-Suche", "Es konnt leider kein passender Audio-Stream gefunden werden!", true).build()).queue();
                    }
                }).buildAndAdd(functions);

    }
}
