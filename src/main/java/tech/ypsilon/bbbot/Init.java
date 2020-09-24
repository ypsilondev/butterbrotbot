package tech.ypsilon.bbbot;

import tech.ypsilon.bbbot.database.MongoController;
import tech.ypsilon.bbbot.settings.SettingsController;

import java.io.File;

public class Init {

    private static final File SETTINGS_FILE = new File("settings.yml");

    static void preInit() throws Exception {
        SettingsController settingsController = new SettingsController(SETTINGS_FILE);
        MongoController mongoController = new MongoController();
    }

    static void init() throws Exception {

    }

    static void postInit() throws Exception {

    }

    static void startupComplete() throws Exception {

    }

    static void addShutdownHook() throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

        }));
    }

}
