package tech.ypsilon.bbbot.discord.listener;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NewMemberJoinListener extends ListenerAdapter {
    ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        if (event.getGuild().getIdLong() != 756547960229199902L) return;

        notifyMember(event, 0);
    }

    private void notifyMember(GuildMemberJoinEvent event, int depth) {
        service.schedule(() -> {
            if (event.getMember().getRoles().size() == 0) {
                event.getMember().getUser().openPrivateChannel().flatMap(privateChannel ->
                        privateChannel.sendMessage("Wähle bitte deinen Studiengang auf dem Erstis-Discord-Server. " +
                                "Klicke dazu auf nachfolgenden, blauen Text und wähle dann dort deinen Studiengang " +
                                "indem du auf das entsprechende Icon unter der dort angezeigten Nachricht klickst" +
                                ": <#759033520680599553>.\n Hilfe benötigt? Schreibe uns mit Klick hier: " +
                                "<@!358213000550809600> oder <@!141171046777749504>")).queue();

                if (!event.getMember().getOnlineStatus().equals(OnlineStatus.OFFLINE)) {
                    if(depth < 4)
                        notifyMember(event, depth+1);
                }
            }
        }, 10, TimeUnit.MINUTES);
    }
}
