package tech.ypsilon.bbbot;

import tech.ypsilon.bbbot.discord.DiscordController;

import javax.security.auth.login.LoginException;

import static tech.ypsilon.bbbot.ButterBrot.LOGGER;

public class Init {

    static void preInit(){
        LOGGER.info("Starting pre-init state");

        LOGGER.info("Passed pre-init state");
    }

    static void init(){
        LOGGER.info("Starting init state");
        try {
            ButterBrot.discordController = new DiscordController();
        } catch (LoginException e) {
            e.printStackTrace();
            LOGGER.error("Error while building JDA", e);
        }
        LOGGER.info("Passed init state");
    }

    static void postInit(){
        LOGGER.info("Starting post-init state");

        LOGGER.info("Passed post-init state");
    }

    static void startupComplete(){
        LOGGER.info("Startup complete");
    }

    static void addShutdownHook(){
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Good Bye! :c");
        }));
    }

}
