package tech.ypsilon.bbbot.discord;

import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.util.GenericController;
import tech.ypsilon.bbbot.util.Initializable;

import javax.security.auth.login.LoginException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class DiscordController extends GenericController implements Initializable {

    private static DiscordController instance;

    private @Getter JDA jda;
    private @Getter Guild home;
    private @Getter TextChannel logChannel;

    /**
     * Registering the JDA with the needed GatewayIntents.
     */
    public DiscordController(ButterBrot parent) {
        super(parent);
        instance = this;
    }

    @Override
    public void init() throws InterruptedException, LoginException {
        Collection<GatewayIntent> gatewayIntents = Arrays.asList(
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.GUILD_PRESENCES
        );

        try {
            jda = JDABuilder.createDefault(getParent().getConfig().getDiscord().getDiscordBotToken(), gatewayIntents)
                    .disableCache(CacheFlag.EMOTE, CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS)
                    .build();

            jda.awaitReady();

            home = jda.getGuildById(getParent().getConfig().getDiscord().getHomeGuildId());

            logChannel = Objects.requireNonNull(jda
                    .getTextChannelById(getParent().getConfig().getDiscord().getLogChannelId()));
        } catch (LoginException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    /**
     * Get the Main JDA instance for executing future tasks
     * @return the JDA instance from the BOT
     */
    @Deprecated(forRemoval = true)
    public static JDA getJDAStatic() {
        return instance.jda;
    }

    /**
     * Get the home Guild object
     * @return home Guild object
     */
    @Deprecated(forRemoval = true)
    public static Guild getHomeGuildStatic() {
        throw new RuntimeException("This operation is unsupported!");
    }
}
