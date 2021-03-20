package tech.ypsilon.bbbot;

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import net.dv8tion.jda.api.JDAInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static tech.ypsilon.bbbot.Init.*;

/**
 * Main class from the ButterBrot Bot
 *
 * @author Julian, Niklas, Gregyyy
 * @version 1.1
 */
public class ButterBrot {

    public static final Logger LOGGER = LoggerFactory.getLogger("tech.ypsilon.bbbot");
    static final File SETTINGS_FILE;

    static {
        File data = new File("data");
        if (!data.exists()) {
            data.mkdir();
        }
        SETTINGS_FILE = new File(data, "settings.yml");
    }

    /**
     * Program entry point
     *
     * @param args command line args
     * @throws Exception when something goes wrong during initialization
     */
    public static void main(String[] args) throws Exception{
        System.out.println("__________        __    __              __________                __   \n" +
                "\\______   \\__ ___/  |__/  |_  __________\\______   \\_______  _____/  |_ \n" +
                " |    |  _/  |  \\   __\\   __\\/ __ \\_  __ \\    |  _/\\_  __ \\/  _ \\   __\\\n" +
                " |    |   \\  |  /|  |  |  | \\  ___/|  | \\/    |   \\ |  | \\(  <_> )  |  \n" +
                " |______  /____/ |__|  |__|  \\___  >__|  |______  / |__|   \\____/|__|  \n" +
                "        \\/                       \\/             \\/                     ");
        System.out.println(BotInfo.NAME + " v" + BotInfo.VERSION);
        System.out.println("JDA v" + JDAInfo.VERSION + " | LavaPlayer v" + PlayerLibrary.VERSION);

        preInit();
        addShutdownHook();
        init();
        postInit();
        startupComplete();
    }

}
