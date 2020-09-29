package tech.ypsilon.bbbot.voice;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AudioUtil {

    public static AudioItem getItemBlocking(String url) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final AudioItem[] item = new AudioItem[1];
        AudioManager.getInstance().getPLAYER_MANAGER().loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                item[0] = audioTrack;
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                item[0] = audioPlaylist;
            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException e) {

            }
        });
        latch.await(1, TimeUnit.SECONDS);
        return item[0];
    }

}
