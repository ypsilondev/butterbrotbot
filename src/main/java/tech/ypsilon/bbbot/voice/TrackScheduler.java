package tech.ypsilon.bbbot.voice;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class TrackScheduler implements AudioEventListener {

    private final AudioPlayer PLAYER;
    private final BlockingDeque<AudioTrack> QUEUE = new LinkedBlockingDeque<>();

    public TrackScheduler(final AudioPlayer PLAYER) {
        this.PLAYER = PLAYER;
    }

    public AudioPlayer getPlayer() {
        return PLAYER;
    }

    public void addTrackPrioritized(AudioTrack track){
        if(PLAYER.getPlayingTrack() == null) {
            PLAYER.playTrack(track);
        } else {
            QUEUE.addFirst(track);
        }
    }

    public void addTrack(AudioTrack track) {
        if(PLAYER.getPlayingTrack() == null) {
            PLAYER.playTrack(track);
        } else {
            QUEUE.add(track);
        }
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return QUEUE;
    }

    public void skip(int count) {
        if(count <= 0)
            return;
        AudioTrack track = null;
        for(int i = 0; i < count; i++) {
            track = this.QUEUE.poll();
        }
        if(track == null)
            this.PLAYER.stopTrack();
        else
            this.PLAYER.playTrack(track);
    }


    @Override
    public void onEvent(AudioEvent audioEvent) {
        if(audioEvent instanceof TrackEndEvent) {
            if(((TrackEndEvent) audioEvent).endReason.mayStartNext) {
                AudioTrack poll = QUEUE.poll();
                if(poll != null)
                    PLAYER.playTrack(poll);
            }
        }
    }
}
