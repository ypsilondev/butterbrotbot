package tech.ypsilon.bbbot;

import tech.ypsilon.bbbot.console.ConsoleManager;
import tech.ypsilon.bbbot.database.MongoController;
import tech.ypsilon.bbbot.discord.SlashCommandManager;
import tech.ypsilon.bbbot.discord.TextCommandManager;
import tech.ypsilon.bbbot.discord.DiscordController;
import tech.ypsilon.bbbot.discord.ServiceManager;
import tech.ypsilon.bbbot.discord.command.text.CreateInviteCommand;
import tech.ypsilon.bbbot.discord.command.text.StudiengangCommand;
import tech.ypsilon.bbbot.discord.command.text.VerifyCommand;
import tech.ypsilon.bbbot.settings.SettingsController;
import tech.ypsilon.bbbot.stats.StatsManager;
import tech.ypsilon.bbbot.voice.AudioManager;

import static tech.ypsilon.bbbot.ButterBrot.LOGGER;

public class Init {

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
        TextCommandManager commandManager = new TextCommandManager();
        commandManager.registerFunctions();
        new AudioManager();

        DiscordController.getJDA().awaitReady();
        SlashCommandManager slashCommandManager = new SlashCommandManager(DiscordController.getJDA());

        LOGGER.info("Passed post-init state");
    }

    static void databaseModulesInit() throws Exception {
        if(!ButterBrot.DEBUG_MODE){
            new MongoController();
            TextCommandManager.getInstance().registerFunction(new StudiengangCommand());
            TextCommandManager.getInstance().registerFunction(new CreateInviteCommand());
            TextCommandManager.getInstance().registerFunction(new VerifyCommand());
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

    static boolean shutdown = false;
    public static void stopBot(boolean systemExit){
        if(shutdown)
            return;

        DiscordController.getJDA().shutdown();
        shutdown = true;

        LOGGER.info("Good Bye! :c");
        if(systemExit) System.exit(0);
    }

}
