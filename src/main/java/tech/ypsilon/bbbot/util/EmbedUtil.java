package tech.ypsilon.bbbot.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class EmbedUtil {

    public static EmbedBuilder createDefaultEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(DiscordRoleColors.BLUE.getSecondary());
        embedBuilder.setFooter("Elite-Nachricht",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3a/Logo_KIT.svg/2000px-Logo_KIT.svg.png");
        return embedBuilder;
    }

    public static EmbedBuilder createErrorEmbed() {
        EmbedBuilder embedBuilder = createDefaultEmbed();
        embedBuilder.setTitle("Fehler");
        embedBuilder.setColor(DiscordRoleColors.RED.getPrimary());
        return embedBuilder;
    }

    public static EmbedBuilder createNoPermEmbed() {
        return EmbedUtil.createErrorEmbed().addField("Kein Recht",
                "Du hast kein Recht diesen Befehl auszuführen", false);
    }

    public static EmbedBuilder createSuccessEmbed() {
        EmbedBuilder embedBuilder = createDefaultEmbed();
        embedBuilder.setTitle("Erfolg");
        embedBuilder.setColor(DiscordRoleColors.GREEN.getPrimary());
        return embedBuilder;
    }

    public static EmbedBuilder createListEmbed(boolean hasMatches) {
        if (!hasMatches) {
            EmbedBuilder embedBuilder = createDefaultEmbed();
            embedBuilder.setTitle("Keine Ergebnisse");
            embedBuilder.setColor(DiscordRoleColors.ORANGE.getPrimary());
            return embedBuilder;
        } else {
            EmbedBuilder embedBuilder = createDefaultEmbed();
            embedBuilder.setTitle("Ergebnisliste");
            embedBuilder.setColor(DiscordRoleColors.GREEN.getPrimary());
            return embedBuilder;
        }
    }

    public static EmbedBuilder createDirectoryEmbed() {
        EmbedBuilder embedBuilder = createDefaultEmbed();
        embedBuilder.setTitle("Dein Directory");
        embedBuilder.setColor(DiscordRoleColors.BLUE.getPrimary());
        return embedBuilder;
    }

    public static EmbedBuilder createInfoEmbed() {
        EmbedBuilder embedBuilder = createDefaultEmbed();
        embedBuilder.setTitle("Info");
        embedBuilder.setColor(DiscordRoleColors.TEAL.getPrimary());
        return embedBuilder;
    }

    public static MessageEmbed colorDescriptionBuild(int color, String description) {
        return createDefaultEmbed().setColor(color).setDescription(description).build();
    }

}
