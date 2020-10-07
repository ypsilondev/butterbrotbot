package tech.ypsilon.bbbot.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import tech.ypsilon.bbbot.settings.SettingsController;

import javax.security.auth.login.LoginException;
import java.util.Arrays;
import java.util.Collection;

public class DiscordController {

    private static DiscordController instance;

    private final JDA jda;

    public DiscordController() throws LoginException {
        instance = this;
        Collection<GatewayIntent> gatewayIntents = Arrays.asList(GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.GUILD_MEMBERS);

        jda = JDABuilder.createDefault((String) SettingsController.getValue("discord.token"), gatewayIntents)
                .disableCache(CacheFlag.EMOTE, CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS)
                .build();
    }

    public static JDA getJDA() {
        return instance.jda;
    }
}
