package tech.ypsilon.bbbot.util;

import net.dv8tion.jda.api.EmbedBuilder;
import java.awt.*;

public class EmbedUtil {

    public static EmbedBuilder createDefaultEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.decode("#1a4064"));
        embedBuilder.setFooter("Elite-Nachricht",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3a/Logo_KIT.svg/2000px-Logo_KIT.svg.png");
        return embedBuilder;
    }

    public static EmbedBuilder createErrorEmbed() {
        EmbedBuilder embedBuilder = createDefaultEmbed();
        embedBuilder.setTitle("Fehler");
        embedBuilder.setColor(Color.decode("#FF7770"));
        return embedBuilder;
    }

    public static EmbedBuilder createSuccessEmbed() {
        EmbedBuilder embedBuilder = createDefaultEmbed();
        embedBuilder.setTitle("Erfolg");
        embedBuilder.setColor(Color.decode("#97DBA2"));
        return embedBuilder;
    }

    public static EmbedBuilder createListEmbed(boolean hasMatches) {
        if (!hasMatches) {
            EmbedBuilder embedBuilder = createDefaultEmbed();
            embedBuilder.setTitle("Keine Ergebnisse");
            embedBuilder.setColor(Color.decode("#FF7770"));
            return embedBuilder;
        } else {
            EmbedBuilder embedBuilder = createDefaultEmbed();
            embedBuilder.setTitle("Ergebnisliste");
            embedBuilder.setColor(Color.decode("#97DBA2"));
            return embedBuilder;
        }
    }

    public static EmbedBuilder createDirectoryEmbed() {
        EmbedBuilder embedBuilder = createDefaultEmbed();
        embedBuilder.setTitle("Dein Directory");
        embedBuilder.setColor(Color.decode("#6470FA"));
        return embedBuilder;
    }

    public static EmbedBuilder createInfoEmbed() {
        EmbedBuilder embedBuilder = createDefaultEmbed();
        embedBuilder.setTitle("Info");
        embedBuilder.setColor(Color.decode("#41e3fc"));
        return embedBuilder;
    }

}
