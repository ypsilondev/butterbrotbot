package tech.ypsilon.bbbot.util;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.Nullable;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.config.DiscordRoles;

public final class DiscordUtil {

    private static DiscordUtil INSTANCE;

    private final ButterBrot parent;

    public DiscordUtil(ButterBrot parent) {
        this.parent = parent;
        INSTANCE = this;
    }

    public static void init(ButterBrot parent) {
        new DiscordUtil(parent);
    }

    public static boolean isAdmin(@Nullable Member member) {
        if (member == null) {
            return false;
        }

        long adminRoleId = INSTANCE.parent.getConfig()
                .getDiscord()
                .getDiscordRoles()
                .get(DiscordRoles.ADMIN.toString());

        return member.getRoles()
                .stream()
                .anyMatch(role -> role.getIdLong() == adminRoleId)
                || member.getPermissions().contains(Permission.ADMINISTRATOR);
    }

}
