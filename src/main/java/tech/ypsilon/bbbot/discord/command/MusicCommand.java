package tech.ypsilon.bbbot.discord.command;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import tech.ypsilon.bbbot.voice.AudioManager;
import tech.ypsilon.bbbot.voice.TrackScheduler;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public class MusicCommand extends SlashCommand {

    private final YoutubeSearchProvider ytsp;
    private final YoutubeAudioSourceManager yasm;

    public MusicCommand() {
        ytsp = new YoutubeSearchProvider();
        yasm = new YoutubeAudioSourceManager();
    }

    @Override
    public CommandData commandData() {
        return new CommandData("music", "Spiele Musik über diesen Bot ab").addSubcommands(
                new SubcommandData("join", "Tritt deinem Sprachkanal bei"),
                new SubcommandData("leave", "Verlässt den Sprachkanal"),
                new SubcommandData("play", "Spielt eine URL ab")
                        .addOptions(new OptionData(OptionType.STRING, "url", "Die URL zum abspielen", true)),
                new SubcommandData("playlist", "Lädt eine Playlist und spielt diese ab")
                        .addOptions(new OptionData(OptionType.STRING, "url", "Die URL zum abspielen", true)),
                new SubcommandData("search", "Suche nach einem YouTube Video")
                        .addOptions(new OptionData(OptionType.STRING, "query", "Die Sucheingabe", true)),
                new SubcommandData("pause", "Pausiert die Wiedergabe"),
                new SubcommandData("resume", "Setzt die Wiedergabe fort"),
                new SubcommandData("jumpto", "Springt zu einem beliebigen Zeitpunkt im Lied")
                        .addOptions(new OptionData(OptionType.STRING, "time", "Die Zielzeit", true)),
                new SubcommandData("skip", "Überspringt das aktuelle Lied"),
                new SubcommandData("clear-queue", "Löscht die Warteschlange")
        );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        if (event.getSubcommandName() == null) throw new CommandFailedException();

        VoiceChannel channel;

        try {
            channel = Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).getChannel();
        } catch (NullPointerException exception) {
            throw new CommandFailedException("Du musst dich in einem Sprachkanal befinden "
                    + "um diesen Befehl ausführen zu können");
        }

        switch (event.getSubcommandName()) {
            case "join":
                join(event, channel);
                return;
            case "play":
                play(event, channel);
                return;
            case "playlist":
                playlist(event, channel);
                return;
            case "search":
                search(event, channel);
                return;
        }

        if (channel.getIdLong() != event.getGuild().getAudioManager().getConnectedChannel().getIdLong()) {
            throw new CommandFailedException("Du musst dich in einem Sprachkanal befinden "
                    + "um diesen Befehl ausführen zu können");
        }

        switch (event.getSubcommandName()) {
            case "leave":
                leave(event);
                return;
            case "pause":
                pause(event);
                return;
            case "resume":
                resume(event);
                return;
            case "jumpto":
                jumpto(event);
                return;
            case "skip":
                skip(event);
                return;
            case "clear-queue":
                clearQueue(event);
                return;
            default:
                throw new CommandFailedException();
        }
    }

    protected void join(SlashCommandEvent event, VoiceChannel channel) {
        Objects.requireNonNull(event.getGuild()).getAudioManager().openAudioConnection(channel);
        event.reply(channel.getAsMention() + " betreten").queue();
    }

    protected void leave(SlashCommandEvent event) {
        AudioManager.getInstance().getScheduler(event.getGuild()).getPlayer().stopTrack();
        AudioManager.getInstance().getScheduler(event.getGuild()).getQueue().clear();
        Objects.requireNonNull(event.getGuild()).getAudioManager().closeAudioConnection();
        event.reply("Aktuellen Kanal verlassen").queue();
    }

    protected void play(SlashCommandEvent event, VoiceChannel channel) {
        if (event.getOption("url") == null) throw new CommandFailedException();
        String url = event.getOption("url").getAsString();

        // join the new channel
        Objects.requireNonNull(event.getGuild()).getAudioManager().openAudioConnection(channel);

        AudioManager.getInstance().getPlayerManager().loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                AudioManager.getInstance().addTrack(event.getGuild(), audioTrack);
                event.reply("Neues Lied zur Warteschlange hinzugefügt: "
                        + audioTrack.getInfo().title + "**").queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                AudioTrack track = Objects.requireNonNullElseGet(audioPlaylist.getSelectedTrack(), () ->
                        audioPlaylist.getTracks().get(0));
                AudioManager.getInstance().addTrack(event.getGuild(), track);
                event.reply("Neues Lied zur Warteschlange hinzugefügt: **"
                        + track.getInfo().title + "**").queue();
            }

            @Override
            public void noMatches() {
                throw new CommandFailedException("Dieser Link ist nicht abspielbar");
            }

            @Override
            public void loadFailed(FriendlyException e) {
                throw new CommandFailedException("Dieser Link ist nicht abspielbar");
            }
        });
    }

    protected void playlist(SlashCommandEvent event, VoiceChannel channel) {
        if (event.getOption("url") == null) throw new CommandFailedException();
        String url = event.getOption("url").getAsString();

        // join the new channel
        Objects.requireNonNull(event.getGuild()).getAudioManager().openAudioConnection(channel);

        AudioManager.getInstance().getPlayerManager().loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                throw new CommandFailedException("Dieser Link ist nicht abspielbar");
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                AudioManager.getInstance().addTrack(event.getGuild(), audioPlaylist);
                event.reply("Neues Playlist zur Warteschlange hinzugefügt: **"
                        + audioPlaylist.getName() + "**").queue();
            }

            @Override
            public void noMatches() {
                throw new CommandFailedException("Dieser Link ist nicht abspielbar");
            }

            @Override
            public void loadFailed(FriendlyException e) {
                throw new CommandFailedException("Dieser Link ist nicht abspielbar");
            }
        });
    }

    protected void search(SlashCommandEvent event, VoiceChannel channel) {
        if (event.getOption("query") == null) throw new CommandFailedException();
        String query = event.getOption("query").getAsString();

        AudioItem ai = ytsp.loadSearchResult(query.strip(), audioTrackInfo -> new YoutubeAudioTrack(audioTrackInfo, yasm));
        if (ai instanceof BasicAudioPlaylist) {
            BasicAudioPlaylist playlist = (BasicAudioPlaylist) ai;
            AudioManager.getInstance().addTrack(event.getGuild(), playlist.getTracks().get(0));
            Objects.requireNonNull(event.getGuild()).getAudioManager().openAudioConnection(channel);
            event.reply("Neues Lied zur Warteschlange hinzugefügt: **"
                    + playlist.getTracks().get(0).getInfo().title + "**").queue();
        } else {
            throw new CommandFailedException("Es konnt leider kein passender Audio-Stream gefunden werden!");
        }
    }

    protected void pause(SlashCommandEvent event) {
        AudioManager.getInstance().getScheduler(event.getGuild()).getPlayer().setPaused(true);
        event.reply("Wiedergabe pausiert").queue();
    }

    protected void resume(SlashCommandEvent event) {
        AudioManager.getInstance().getScheduler(event.getGuild()).getPlayer().setPaused(false);
        event.reply("Wiedergabe fortgesetzt").queue();
    }

    protected void jumpto(SlashCommandEvent event) {
        if (event.getOption("time") == null) throw new CommandFailedException();
        String time = event.getOption("time").getAsString();

        Duration duration;
        try {
            duration = Duration.parse("PT" + time.replace(":", "M") + "S");
        } catch (DateTimeParseException ex) {
            try {
                duration = Duration.parse("PT" + time.replaceFirst(":", "H")
                        .replace(":", "M") + "S");
            } catch (DateTimeParseException ex1) {
                throw new CommandFailedException("Falsches Zeitformat. Bitte verwende: `mm:ss`, `hh:mm:ss` oder `ss`");
            }
        }

        if (duration != null) {
            AudioManager.getInstance().getScheduler(event.getGuild()).getPlayer()
                    .getPlayingTrack().setPosition(duration.getSeconds() * 1000);
        } else {
            throw new CommandFailedException("Falsches Zeitformat. Bitte verwende: `mm:ss`, `hh:mm:ss` oder `ss`");
        }

        event.reply("Zu " + time + " gesprungen").queue();
    }

    protected void skip(SlashCommandEvent event) {
        TrackScheduler scheduler = AudioManager.getInstance().getScheduler(event.getGuild());
        scheduler.skip(1);
        event.reply("Aktuelles Lied übersprungen").queue();
    }

    protected void clearQueue(SlashCommandEvent event) {
        AudioManager.getInstance().getScheduler(event.getGuild()).getQueue().clear();
        event.reply("Warteschlange gelöscht").queue();
    }

    @Override
    public void handleSelectionMenu(SelectionMenuEvent event, String data) {

    }
}
