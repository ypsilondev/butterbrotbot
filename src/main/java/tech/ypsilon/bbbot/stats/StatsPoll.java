package tech.ypsilon.bbbot.stats;

import io.prometheus.client.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.ypsilon.bbbot.discord.DiscordController;

public class StatsPoll implements Runnable {

    private static final Gauge totalUsers = Gauge.build().name("butterbrot_total_users").help("-").register();

    private static final Logger LOGGER = LoggerFactory.getLogger(StatsPoll.class);

    /**
     * When an object implementing interface {@code Runnable} is used
     * to create a thread, starting the thread causes the object's
     * {@code run} method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method {@code run} is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        // maybe replace with loadMembers
        int presences = DiscordController.getHomeGuildStatic().retrieveMetaData().complete().getApproximatePresences();
        LOGGER.info("Updated member count: {}", presences);
        totalUsers.set(presences);
    }
}
