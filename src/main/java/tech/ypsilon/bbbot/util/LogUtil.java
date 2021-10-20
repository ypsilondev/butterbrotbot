package tech.ypsilon.bbbot.util;


import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.TextChannel;
import tech.ypsilon.bbbot.discord.DiscordController;
import tech.ypsilon.bbbot.settings.SettingsController;

@Slf4j
public class LogUtil {

    private static TextChannel logChannel;

    public static void init() {
        Long channelId = SettingsController.getLong("discord.logchannel");
        assert channelId != null;
        logChannel = DiscordController.getJDA().getTextChannelById(channelId);
    }

    public static void log(String message) {
        logChannel.sendMessage(message).queue();
        log.info(message);
    }

}
