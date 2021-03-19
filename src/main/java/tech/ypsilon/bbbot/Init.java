package tech.ypsilon.bbbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import tech.ypsilon.bbbot.console.ConsoleManager;
import tech.ypsilon.bbbot.database.MongoController;
import tech.ypsilon.bbbot.discord.CommandManager;
import tech.ypsilon.bbbot.discord.command.BirthdayCommand;
import tech.ypsilon.bbbot.discord.listener.RoleListener;
import tech.ypsilon.bbbot.settings.SettingsController;

import tech.ypsilon.bbbot.discord.DiscordController;
import tech.ypsilon.bbbot.voice.AudioManager;

import static tech.ypsilon.bbbot.ButterBrot.LOGGER;

public class Init {

    static void preInit() throws Exception {
        LOGGER.info("Starting pre-init state");
        new SettingsController(ButterBrot.SETTINGS_FILE);
        // new MongoController();
        LOGGER.info("Passed pre-init state");
    }

    static void init() throws Exception {
        LOGGER.info("Starting init state");
        new DiscordController();
        LOGGER.info("Passed init state");
    }

    static void postInit() throws Exception {
        LOGGER.info("Starting post-init state");
        new CommandManager();
        new AudioManager();
        LOGGER.info("Passed post-init state");
    }

    static void startupComplete() throws Exception {
        new ConsoleManager();
        // Register the Birthday Notifier.
        BirthdayCommand.startNotifierService(24);
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
