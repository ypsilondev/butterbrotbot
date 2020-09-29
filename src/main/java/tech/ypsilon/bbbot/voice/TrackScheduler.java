package tech.ypsilon.bbbot.voice;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class TrackScheduler implements AudioEventListener {

    private final AudioPlayer PLAYER;
    private final BlockingQueue<AudioTrack> TRACKS = new LinkedBlockingDeque<>();

    public TrackScheduler(final AudioPlayer PLAYER) {
        this.PLAYER = PLAYER;
    }

    public AudioPlayer getPlayer() {
        return PLAYER;
    }

    public void addTrack(AudioTrack track) {
        if(PLAYER.getPlayingTrack() == null) {
            PLAYER.playTrack(track);
        } else {
            TRACKS.add(track);
        }
    }

    @Override
    public void onEvent(AudioEvent audioEvent) {
        if(audioEvent instanceof TrackEndEvent) {
            if(((TrackEndEvent) audioEvent).endReason.mayStartNext) {
                AudioTrack poll = TRACKS.poll();
                if(poll != null)
                    PLAYER.playTrack(poll);
            }
        }
    }
}
