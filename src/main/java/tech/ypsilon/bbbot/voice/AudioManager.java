package tech.ypsilon.bbbot.voice;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;

public class AudioManager {

    private static AudioManager instance;

    private final HashMap<Guild, TrackScheduler> TRACK_SCHEDULERS = new HashMap<>();
    private final AudioPlayerManager PLAYER_MANAGER;

    public AudioManager() {
        instance = this;
        PLAYER_MANAGER = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(PLAYER_MANAGER);
    }

    public static AudioManager getInstance() {
        return instance;
    }

    public AudioPlayerManager getPLAYER_MANAGER() {
        return PLAYER_MANAGER;
    }

    public void addTrack(Guild g, AudioItem i) {
        TrackScheduler scheduler = getScheduler(g);
        if(i instanceof AudioTrack){
            scheduler.addTrack((AudioTrack) i);
        } else if(i instanceof AudioPlaylist) {
            AudioPlaylist i1 = (AudioPlaylist) i;
            for (AudioTrack track : i1.getTracks())
                scheduler.addTrack(track);
        }
    }

    public void addTrackPrioritized(Guild g, AudioItem i) {
        TrackScheduler scheduler = getScheduler(g);
        if(i instanceof AudioTrack){
            scheduler.addTrackPrioritized((AudioTrack) i);
        } else if(i instanceof AudioPlaylist) {
            AudioPlaylist i1 = (AudioPlaylist) i;
            for (AudioTrack track : i1.getTracks())
                scheduler.addTrackPrioritized(track);
        }
    }

    public TrackScheduler getScheduler(Guild g) {
        if(TRACK_SCHEDULERS.containsKey(g))
            return TRACK_SCHEDULERS.get(g);
        AudioPlayer player = PLAYER_MANAGER.createPlayer();
        TrackScheduler trackScheduler = new TrackScheduler(player);
        player.addListener(trackScheduler);
        TRACK_SCHEDULERS.put(g, trackScheduler);
        g.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(player));

        return trackScheduler;
    }

}
