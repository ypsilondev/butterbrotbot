package tech.ypsilon.bbbot;

import tech.ypsilon.bbbot.console.ConsoleManager;
import tech.ypsilon.bbbot.database.MongoController;
import tech.ypsilon.bbbot.discord.CommandManager;
import tech.ypsilon.bbbot.discord.DiscordController;
import tech.ypsilon.bbbot.discord.ServiceManager;
import tech.ypsilon.bbbot.discord.command.CreateInviteCommand;
import tech.ypsilon.bbbot.discord.command.StudiengangCommand;
import tech.ypsilon.bbbot.discord.command.VerifyCommand;
import tech.ypsilon.bbbot.settings.SettingsController;
import tech.ypsilon.bbbot.stats.StatsManager;
import tech.ypsilon.bbbot.voice.AudioManager;

import static tech.ypsilon.bbbot.ButterBrot.LOGGER;

public class Init {

    static boolean shutdown = false;

    static void preInit() throws Exception {
        LOGGER.info("Starting pre-init state");
        new SettingsController(ButterBrot.SETTINGS_FILE);
        StatsManager.getInstance();
        LOGGER.info("Passed pre-init state");
    }

    static void init() throws Exception {
        LOGGER.info("Starting init state");
        new DiscordController();
        LOGGER.info("Passed init state");
    }

    static void postInit() throws Exception {
        LOGGER.info("Starting post-init state");
        CommandManager commandManager = new CommandManager();
        commandManager.registerFunctions();
        new AudioManager();
        LOGGER.info("Passed post-init state");
    }

    static void databaseModulesInit() throws Exception {
        if (!ButterBrot.DEBUG_MODE) {
            new MongoController();
            CommandManager.getInstance().registerFunction(new StudiengangCommand());
            CommandManager.getInstance().registerFunction(new CreateInviteCommand());
            CommandManager.getInstance().registerFunction(new VerifyCommand());
        }
    }

    static void startupComplete() throws Exception {
        new ConsoleManager();
        // Register the Notifiers.
        new ServiceManager().initialize();
        LOGGER.info("Startup complete");
    }

    static void addShutdownHook() throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> stopBot(false)));
    }

    public static void stopBot(boolean systemExit) {
        if (shutdown)
            return;

        DiscordController.getJDA().shutdown();
        shutdown = true;

        LOGGER.info("Good Bye! :c");
        if (systemExit) System.exit(0);
    }

}
