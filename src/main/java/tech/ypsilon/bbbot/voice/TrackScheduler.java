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

    private final AudioPlayer player;
    private final BlockingDeque<AudioTrack> audioQueue = new LinkedBlockingDeque<>();

    public TrackScheduler(final AudioPlayer PLAYER) {
        this.player = PLAYER;
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public void addTrackPrioritized(AudioTrack track){
        if(player.getPlayingTrack() == null) {
            player.playTrack(track);
        } else {
            audioQueue.addFirst(track);
        }
    }

    public void addTrack(AudioTrack track) {
        if(player.getPlayingTrack() == null) {
            player.playTrack(track);
        } else {
            audioQueue.add(track);
        }
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return audioQueue;
    }

    public void skip(int count) {
        if(count <= 0)
            return;
        AudioTrack track = null;
        for(int i = 0; i < count; i++) {
            track = this.audioQueue.poll();
        }
        if(track == null)
            this.player.stopTrack();
        else
            this.player.playTrack(track);
    }


    @Override
    public void onEvent(AudioEvent audioEvent) {
        if(audioEvent instanceof TrackEndEvent) {
            if(((TrackEndEvent) audioEvent).endReason.mayStartNext) {
                AudioTrack poll = audioQueue.poll();
                if(poll != null)
                    player.playTrack(poll);
            }
        }
    }
}
