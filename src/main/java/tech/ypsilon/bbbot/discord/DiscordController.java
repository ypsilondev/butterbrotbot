package tech.ypsilon.bbbot.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.util.Arrays;
import java.util.Collection;

public class DiscordController {

    private JDA jda;

    //FIXME: tmp
    private final String token = "";

    public DiscordController() throws LoginException {
        Collection<GatewayIntent> gatewayIntents = Arrays.asList(GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS);

        jda = JDABuilder.createDefault(token, gatewayIntents).build();
    }

    public JDA getJDA() {
        return jda;
    }
}
