package tech.ypsilon.bbbot;

import tech.ypsilon.bbbot.database.MongoController;
import tech.ypsilon.bbbot.settings.SettingsController;

import tech.ypsilon.bbbot.discord.DiscordController;

import javax.security.auth.login.LoginException;

import static tech.ypsilon.bbbot.ButterBrot.LOGGER;

public class Init {



    static void preInit() throws Exception {
        LOGGER.info("Starting pre-init state");
        SettingsController settingsController = new SettingsController(ButterBrot.SETTINGS_FILE);
        MongoController mongoController = new MongoController();
        LOGGER.info("Passed pre-init state");
    }

    static void init() throws Exception {
        LOGGER.info("Starting init state");
        try {
            ButterBrot.discordController = new DiscordController();
        } catch (LoginException e) {
            e.printStackTrace();
            LOGGER.error("Error while building JDA", e);
        }
        LOGGER.info("Passed init state");
    }

    static void postInit() throws Exception {
        LOGGER.info("Starting post-init state");

        LOGGER.info("Passed post-init state");
    }

    static void startupComplete() throws Exception {
        LOGGER.info("Startup complete");
    }

    static void addShutdownHook() throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Good Bye! :c");
        }));
    }

}
