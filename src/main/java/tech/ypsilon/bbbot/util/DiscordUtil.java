package tech.ypsilon.bbbot.util;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import tech.ypsilon.bbbot.settings.SettingsController;

public class DiscordUtil {

    public static boolean isAdmin(Member member) {
        Long adminRoleId = SettingsController.getLong("discord.roles.admin");
        return member.getRoles().stream().anyMatch(role -> role.getIdLong() == adminRoleId) || member.getPermissions().contains(Permission.ADMINISTRATOR);
    }

}
