package tech.ypsilon.bbbot.discord.listener;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class CensorWatcherListener extends ListenerAdapter {
    public static HashMap<TextChannel, Message> lastMessages = new HashMap<>();
    public static HashMap<User, Integer> censoredMember = new HashMap<>();

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getMember().getRoles().stream().noneMatch(role -> role.getIdLong() == 757718320526000138L)) {
            Integer deletionLimit = censoredMember.get(event.getMember().getUser());
            if (deletionLimit != null && deletionLimit > 0) {
                event.getMessage().delete().queue();
                censoredMember.put(event.getMember().getUser(), deletionLimit-1);
            } else {
                lastMessages.put(event.getChannel(), event.getMessage());
            }
        }
    }
}
