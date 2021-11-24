package tech.ypsilon.bbbot.util;


import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.TextChannel;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.discord.DiscordController;

@Slf4j
public class LogUtil {

    private static TextChannel logChannel;

    public static void init() {
        Long channelId = ButterBrot.getConfigStatic().getDiscord().getLogChannelId();
        assert channelId != null;
        logChannel = DiscordController.getJDAStatic().getTextChannelById(channelId);
    }

    public static void log(String message) {
        logChannel.sendMessage(message).queue();
        log.info(message);
    }

}
