package tech.ypsilon.bbbot;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import lombok.Getter;
import net.dv8tion.jda.api.JDAInfo;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.ypsilon.bbbot.config.ButterbrotConfig;
import tech.ypsilon.bbbot.config.DefaultConfigFactory;
import tech.ypsilon.bbbot.console.ConsoleController;
import tech.ypsilon.bbbot.database.MongoController;
import tech.ypsilon.bbbot.discord.DiscordController;
import tech.ypsilon.bbbot.discord.ServiceController;
import tech.ypsilon.bbbot.discord.SlashCommandManager;
import tech.ypsilon.bbbot.discord.TextCommandManager;
import tech.ypsilon.bbbot.discord.command.text.CreateInviteCommand;
import tech.ypsilon.bbbot.discord.command.text.StudiengangCommand;
import tech.ypsilon.bbbot.discord.command.text.VerifyCommand;
import tech.ypsilon.bbbot.settings.SettingsController;
import tech.ypsilon.bbbot.stats.StatsController;
import tech.ypsilon.bbbot.util.LogUtil;
import tech.ypsilon.bbbot.voice.AudioController;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

/**
 * Main class from the ButterBrot Bot
 *
 * @author Julian
 * @author Niklas
 * @author Gregyyy
 * @author Julian²
 * @author Christian
 *
 * @version 1.1
 */
public class ButterBrot {

    public static final Logger LOGGER = LoggerFactory.getLogger("tech.ypsilon.bbbot");

    private static final int MISCONFIGURATION_EXIT_CODE = 78;

    private static boolean MISSING_CONFIGURATION;
    public static boolean DEBUG_MODE;
    private static File SETTINGS_FILE;

    private final @Getter ButterbrotConfig config;

    private final @Getter DiscordController discordController;
    private final @Getter StatsController statsController;
    private final @Getter @Nullable MongoController mongoController;
    private final @Getter TextCommandManager textCommandManager;
    private final @Getter AudioController audioController;
    private final @Getter SlashCommandManager slashCommandManager;
    private final @Getter ConsoleController consoleController;
    private final @Getter ServiceController serviceController;

    public ButterBrot(ButterbrotConfig config) throws Exception {
        this.config = config;

        preInit();
        LOGGER.info("Starting pre-init state");
        new SettingsController(ButterBrot.SETTINGS_FILE);
        this.statsController = new StatsController(this);
        LOGGER.info("Passed pre-init state");

        addShutdownHook();

        init();
        LOGGER.info("Starting init state");
        this.discordController = new DiscordController(this);
        LOGGER.info("Passed init state");


        postInit();
        LOGGER.info("Starting post-init state");
        this.textCommandManager = new TextCommandManager(this);
        this.textCommandManager.registerFunctions();
        this.audioController = new AudioController(this);

        DiscordController.getJDA().awaitReady();
        this.slashCommandManager = SlashCommandManager.initialize(DiscordController.getJDA(), this);

        LogUtil.init();

        LOGGER.info("Passed post-init state");


        if(!ButterBrot.DEBUG_MODE) {
            this.mongoController = new MongoController(this);
            TextCommandManager.getInstance().registerFunction(new StudiengangCommand());
            TextCommandManager.getInstance().registerFunction(new CreateInviteCommand());
            TextCommandManager.getInstance().registerFunction(new VerifyCommand());
        } else {
            this.mongoController = null;
        }


        this.consoleController = new ConsoleController(this);
        // Register the Notifiers.
        this.serviceController = new ServiceController(this);
        this.serviceController.initialize();
        LOGGER.info("Startup complete");
    }

    private void preInit() {
        // TODO: do everything before initializing JDA
    }

    private void init() {
        // TODO: initialize JDA
    }

    private void postInit() {
        // TODO: console and services
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> stopBot(false)));
    }

    static boolean shutdown = false;
    public static void stopBot(boolean systemExit) {
        if (shutdown) return;

        DiscordController.getJDA().shutdown();
        shutdown = true;

        LOGGER.info("Good Bye! :c");
        if (systemExit) System.exit(0);
    }

    /**
     * Program entry point
     *
     * @param args command line args
     * @throws Exception when something goes wrong during initialization
     */
    public static void main(String[] args) throws Exception {
        System.out.println("__________        __    __              __________                __   \n" +
                "\\______   \\__ ___/  |__/  |_  __________\\______   \\_______  _____/  |_ \n" +
                " |    |  _/  |  \\   __\\   __\\/ __ \\_  __ \\    |  _/\\_  __ \\/  _ \\   __\\\n" +
                " |    |   \\  |  /|  |  |  | \\  ___/|  | \\/    |   \\ |  | \\(  <_> )  |  \n" +
                " |______  /____/ |__|  |__|  \\___  >__|  |______  / |__|   \\____/|__|  \n" +
                "        \\/                       \\/             \\/                     ");
        System.out.println(BotInfo.NAME + " v" + BotInfo.VERSION);
        System.out.println("JDA v" + JDAInfo.VERSION + " | LavaPlayer v" + PlayerLibrary.VERSION);

        DEBUG_MODE = Arrays.stream(args).anyMatch(s -> s.equalsIgnoreCase("--debug"));

        final File dataDirectory = initAndGetDataDirectory();
        ButterbrotConfig butterbrotConfig;

        try {
            butterbrotConfig = loadConfiguration(
                    new File(dataDirectory, "settings.yml"),
                    ButterbrotConfig.class,
                    new YAMLFactory()
            );
        } catch (ReflectiveOperationException exception) {
            LOGGER.error("Reflective Error while initializing configuration", exception);
            System.exit(MISCONFIGURATION_EXIT_CODE);
            return;
        }

        new ButterBrot(butterbrotConfig);
    }

    private static File initAndGetDataDirectory() {
        File data = new File("data");

        if (!data.exists()) {
            LOGGER.info("Data directory could not be found, trying to create a new one.");
            if (data.mkdir()) {
                LOGGER.info("Successfully created data directory.");
            } else {
                throw new IllegalStateException("Could not create data directory! Path: " + data.getAbsolutePath());
            }
        }

        SETTINGS_FILE = new File(data, "settings.yml");
        return data;
    }

    @SuppressWarnings("unchecked")
    public static <T extends DefaultConfigFactory> T loadConfiguration(File configFile, Class<T> configClass, JsonFactory factory)
            throws IOException, ReflectiveOperationException {

        ObjectMapper objectMapper = new ObjectMapper(factory);
        T config;

        try {
            LOGGER.info("Loading configuration {}, mapped by {}", configFile.getName(), configClass.getName());
            config = objectMapper.readValue(configFile, configClass);
            objectMapper.writeValue(configFile, config);
        } catch (FileNotFoundException exception) {
            LOGGER.info("Creating empty configuration...");
            config = (T) configClass.getDeclaredMethod("createDefault").invoke(null);
            objectMapper.writeValue(configFile, config);
            LOGGER.warn("Please configure file {}, " +
                    "the bot will stop after initializing all configs...", configFile.getName());
            MISSING_CONFIGURATION = true;
        } catch (IOException exception) {
            config = null;
            exception.printStackTrace();
            LOGGER.warn("Please configure file {}, " +
                    "the bot will stop after initializing all configs...", configFile.getName());
            System.exit(MISCONFIGURATION_EXIT_CODE);
        }

        T defaultConfig = (T) configClass.getDeclaredMethod("createDefault").invoke(null);

        if (defaultConfig.equals(config)) {
            LOGGER.warn("Config file {} is still the default config " +
                    "and might need to be configured!", configFile.getName());
        }

        return config;
    }

}
