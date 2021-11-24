package tech.ypsilon.bbbot.voice;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.util.GenericController;

import java.util.HashMap;

public class AudioController extends GenericController {

    private static AudioController instance;

    private final HashMap<Guild, TrackScheduler> trackSchedulers = new HashMap<>();
    private final AudioPlayerManager playerManager;

    public AudioController(ButterBrot parent) {
        super(parent);
        instance = this;
        playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
    }

    public static AudioController getInstance() {
        return instance;
    }

    public AudioPlayerManager getPlayerManager() {
        return playerManager;
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
        if(trackSchedulers.containsKey(g))
            return trackSchedulers.get(g);
        AudioPlayer player = playerManager.createPlayer();
        TrackScheduler trackScheduler = new TrackScheduler(player);
        player.addListener(trackScheduler);
        trackSchedulers.put(g, trackScheduler);
        g.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(player));

        return trackScheduler;
    }

}
