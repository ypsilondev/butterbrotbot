package tech.ypsilon.bbbot.util;

import net.dv8tion.jda.api.EmbedBuilder;
import java.awt.*;

public class EmbedUtil {

    public static EmbedBuilder createErrorEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Fehler");
        embedBuilder.setColor(Color.decode("#FF7770"));
        embedBuilder.setFooter("Elite-Nachricht",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3a/Logo_KIT.svg/2000px-Logo_KIT.svg.png");
        return embedBuilder;
    }

    public static EmbedBuilder createSuccessEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Erfolg");
        embedBuilder.setColor(Color.decode("#97DBA2"));
        embedBuilder.setFooter("Elite-Nachricht",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3a/Logo_KIT.svg/2000px-Logo_KIT.svg.png");
        return embedBuilder;
    }

    public static EmbedBuilder createListEmbed(boolean hasMatches) {
        if (!hasMatches) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Keine Ergebnisse");
            embedBuilder.setColor(Color.decode("#FF7770"));
            embedBuilder.setFooter("Elite-Nachricht",
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3a/Logo_KIT.svg/2000px-Logo_KIT.svg.png");
            return embedBuilder;
        } else {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Ergebnisliste");
            embedBuilder.setColor(Color.decode("#97DBA2"));
            embedBuilder.setFooter("Elite-Nachricht",
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3a/Logo_KIT.svg/2000px-Logo_KIT.svg.png");
            return embedBuilder;
        }
    }

    public static EmbedBuilder createDirectoryEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Dein Directory");
        embedBuilder.setColor(Color.decode("#6470FA"));
        embedBuilder.setFooter("Elite-Nachricht",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3a/Logo_KIT.svg/2000px-Logo_KIT.svg.png");
        return embedBuilder;
    }

}
