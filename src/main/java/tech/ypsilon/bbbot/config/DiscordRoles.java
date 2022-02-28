package tech.ypsilon.bbbot.config;

import java.util.Locale;

public enum DiscordRoles {

    ADMIN,
    STUDENT;

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
