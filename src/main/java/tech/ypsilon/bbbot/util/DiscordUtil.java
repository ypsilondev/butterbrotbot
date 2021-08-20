package tech.ypsilon.bbbot.util;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.Nullable;
import tech.ypsilon.bbbot.settings.SettingsController;

public class DiscordUtil {

    public static boolean isAdmin(@Nullable Member member) {
        if (member == null) return false;
        Long adminRoleId = SettingsController.getLong("discord.roles.admin");
        assert adminRoleId != null;
        return member.getRoles().stream().anyMatch(role -> role.getIdLong() == adminRoleId) || member.getPermissions().contains(Permission.ADMINISTRATOR);
    }

}
