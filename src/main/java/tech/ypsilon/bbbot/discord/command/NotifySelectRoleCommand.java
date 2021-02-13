package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Dont activate the Command
 * Will cause the bot to be quarantined
 */
@Deprecated
public class NotifySelectRoleCommand extends LegacyCommand {
    @Override
    public String[] getAlias() {
        return new String[]{"notify"};
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {
        if (e.getMember().getRoles().stream().anyMatch((role) -> role.getIdLong() == 757718320526000138L)) {
            e.getGuild().loadMembers(member -> {
                if (member.getRoles().size() == 0) {
                    member.getUser().openPrivateChannel().flatMap(privateChannel ->
                            privateChannel.sendMessage("Wähle bitte deinen Studiengang auf dem Erstis-Discord-Server. " +
                                    "Klicke dazu auf nachfolgenden, blauen Text und wähle dann dort deinen Studiengang " +
                                    "indem du auf das entsprechende Icon unter der dort angezeigten Nachricht klickst" +
                                    ": <#759033520680599553>.\n Hilfe benötigt? Schreibe uns mit Klick hier: " +
                                    "<@!358213000550809600> oder <@!141171046777749504>")).queue();
                }
            });
        }
    }

    @Override
    public String getDescription() {
        return "Erinnert User daran, ihre Rolle zu wählen (admin only)";
    }
}
