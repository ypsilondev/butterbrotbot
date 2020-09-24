package tech.ypsilon.bbbot;

import tech.ypsilon.bbbot.database.MongoController;
import tech.ypsilon.bbbot.settings.SettingsController;

import java.io.File;

import tech.ypsilon.bbbot.discord.DiscordController;

import javax.security.auth.login.LoginException;

import static tech.ypsilon.bbbot.ButterBrot.LOGGER;

public class Init {

    private static final File SETTINGS_FILE = new File("settings.yml");
    static void preInit(){
        LOGGER.info("Starting pre-init state");

    static void preInit() throws Exception {
        SettingsController settingsController = new SettingsController(SETTINGS_FILE);
        MongoController mongoController = new MongoController();
        LOGGER.info("Passed pre-init state");
    }

    static void init() throws Exception {

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

    static void postInit() throws Exception {
    static void postInit(){
        LOGGER.info("Starting post-init state");

        LOGGER.info("Passed post-init state");
    }

    static void startupComplete() throws Exception {

    static void startupComplete(){
        LOGGER.info("Startup complete");
    }

    static void addShutdownHook() throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Good Bye! :c");
        }));
    }

}
