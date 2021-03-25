package tech.ypsilon.bbbot.discord.command;

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import net.dv8tion.jda.api.EmbedBuilder;
import tech.ypsilon.bbbot.util.EmbedUtil;
import tech.ypsilon.bbbot.voice.AudioManager;
import tech.ypsilon.bbbot.voice.AudioUtil;
import tech.ypsilon.bbbot.voice.TrackScheduler;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

public class VoiceCommands implements CommandBucket {

    @Override
    public void register(List<DiscordFunction> functions) {

        // Play Command [for single videos]
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
                        AudioItem itemBlocking = AudioUtil.getItemBlocking(args[0].split("&list")[0]);
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

        // Play Command [for playlists]
        new CommandBuilder("playlist")
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

        // Play Command [for single videos]
        new CommandBuilder("pplay")
                .setDescription("Mit 'kit play [Link]' joined der Bot deinem Voice-Channel und spielt die Audio des Links [ohne Beachtung der Queue]")
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
                        AudioItem itemBlocking = AudioUtil.getItemBlocking(args[0].split("&list")[0]);
                        if (itemBlocking == null) {
                            EmbedBuilder b = EmbedUtil.createErrorEmbed();
                            b.setDescription("Link nicht abspielbar");
                            e.getChannel().sendMessage(b.build()).queue();
                            return;
                        }
                        AudioManager.getInstance().addTrackPrioritized(e.getGuild(), itemBlocking);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                })
                .buildAndAdd(functions);

        new CommandBuilder("join")
                .setDescription("Let's the bot join your channel")
                .setExecutor((e, args) -> {
                    if (!Objects.requireNonNull(Objects.requireNonNull(e.getMember()).getVoiceState()).inVoiceChannel()) {
                        EmbedBuilder b = EmbedUtil.createErrorEmbed();
                        b.setDescription("Bot kann nur aus einem Voice-Channel heraus gerufen werden");
                        e.getChannel().sendMessage(b.build()).queue();
                        return;
                    }
                    e.getGuild().getAudioManager().openAudioConnection(Objects.requireNonNull(e.getMember().getVoiceState()).getChannel());
                }).buildAndAdd(functions);

        new CommandBuilder("pause")
                .setDescription("Pauses the playback")
                .setExecutor((e, args) -> {
                    if (Objects.equals(Objects.requireNonNull(Objects.requireNonNull(e.getMember()).getVoiceState()).getChannel(), e.getGuild().getAudioManager().getConnectedChannel())) {
                        AudioManager.getInstance().getScheduler(e.getGuild()).getPlayer().setPaused(true);
                        e.getMessage().delete().queue();
                    }
                }).buildAndAdd(functions);

        new CommandBuilder("resume")
                .setDescription("Resumes the playback")
                .setExecutor((e, args) -> {
                    if (Objects.equals(Objects.requireNonNull(Objects.requireNonNull(e.getMember()).getVoiceState()).getChannel(), e.getGuild().getAudioManager().getConnectedChannel())) {
                        AudioManager.getInstance().getScheduler(e.getGuild()).getPlayer().setPaused(false);
                        e.getMessage().delete().queue();
                    }
                }).buildAndAdd(functions);

        // Leave Command
        new CommandBuilder("leave")
                .setDescription("Let the bot leave the channel")
                .setExecutor((e, args) -> {
                    if (Objects.equals(Objects.requireNonNull(Objects.requireNonNull(e.getMember()).getVoiceState()).getChannel(), e.getGuild().getAudioManager().getConnectedChannel())) {
                        AudioManager.getInstance().getScheduler(e.getGuild()).getPlayer().stopTrack();
                        AudioManager.getInstance().getScheduler(e.getGuild()).getQueue().clear();
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

        //Jump to
        new CommandBuilder("jumpto")
                .setDescription("Jump to a specific time in an audio track")
                .setExecutor((e, args) -> {
                    if (args.length == 0) {
                        e.getChannel().sendMessage(EmbedUtil.createErrorEmbed().addField("Argument fehlt",
                                "Bitte gebe die Zeit an im Format: mm:ss, hh:mm:ss, ss", false).build()).queue();
                        return;
                    }

                    if (!Objects.requireNonNull(Objects.requireNonNull(e.getMember()).getVoiceState()).inVoiceChannel()) {
                        EmbedBuilder b = EmbedUtil.createErrorEmbed();
                        b.setDescription("Bot kann nur aus einem Voice-Channel heraus gerufen werden");
                        e.getChannel().sendMessage(b.build()).queue();
                        return;
                    }

                    Duration duration = null;
                    try {
                        duration = Duration.parse("PT" + args[0].replace(":", "M") + "S");
                    } catch (DateTimeParseException ex) {
                        try {
                            duration = Duration.parse("PT" + args[0].replaceFirst(":", "H")
                                    .replace(":", "M") + "S");
                        } catch (DateTimeParseException ex1) {
                            e.getChannel().sendMessage(EmbedUtil.createErrorEmbed().addField("Argument fehlerhaft",
                                    "Bitte gebe die Zeit an im Format: mm:ss, hh:mm:ss, ss", false).build()).queue();
                        }
                    }

                    if (duration != null){
                        AudioManager.getInstance().getScheduler(e.getGuild()).getPlayer()
                                .getPlayingTrack().setPosition(duration.getSeconds() * 1000);
                    }
                }).buildAndAdd(functions);
    }
}
