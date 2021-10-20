package tech.ypsilon.bbbot.discord.listener;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import tech.ypsilon.bbbot.util.DiscordUtil;
import tech.ypsilon.bbbot.util.LogUtil;

import java.util.List;

@Slf4j
public class BadWordListener extends ListenerAdapter {

    List<String> badWords = List.of(".*http(s)?:\\/\\/dis[\\S]*\\.gift\\/\\S*.*");

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (!event.isWebhookMessage() && !event.getAuthor().isBot() && !DiscordUtil.isAdmin(event.getMember())) {
            // Message to be checked
            String messageString = event.getMessage().getContentStripped();

            if (badWords.stream().anyMatch(messageString::matches)) {
                LogUtil.log(String.format("%s#%s was maybe spamming. (Message: %s)",
                        event.getAuthor().getName(),
                        event.getAuthor().getDiscriminator(),
                        messageString));
                event.getMessage().delete().queue();
            }
        }
    }

}
