package tech.ypsilon.bbbot;

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import net.dv8tion.jda.api.JDAInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static tech.ypsilon.bbbot.Init.*;

public class ButterBrot {

    public static final Logger LOGGER = LoggerFactory.getLogger("tech.ypsilon.bbbot");

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
