package tech.ypsilon.bbbot.util;

import lombok.Getter;
import java.awt.Color;

public enum DiscordRoleColors {
    TEAL("58B99D", "3A7E6B"),
    GREEN("65C87A", "448852"),
    BLUE("5197D5", "356690"),
    PURPLE("925EB1", "693B86"),
    MAGENTA("D63964", "9F2857"),
    YELLOW("EAC545", "B87E2E"),
    ORANGE("D8823B", "9C481B"),
    RED("D65745", "8D3529"),
    LIGHT_GRAY("98A5A6", "989C9F"),
    DARK_GRAY("667C89", "596D79"),
    // both the same
    DEFAULT("9CA9B4", "9CA9B4");

    @Getter
    private final Color primary;

    @Getter
    private final Color secondary;

    DiscordRoleColors(String primary, String secondary) {
        this.primary = Color.decode("#" + primary);
        this.secondary = Color.decode("#" + secondary);
    }
}
