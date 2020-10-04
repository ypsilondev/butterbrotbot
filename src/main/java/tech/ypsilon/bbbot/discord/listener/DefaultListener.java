package tech.ypsilon.bbbot.discord.listener;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import tech.ypsilon.bbbot.BotInfo;
import tech.ypsilon.bbbot.discord.DiscordController;
import tech.ypsilon.bbbot.discord.command.CreateChannelCommand;
import tech.ypsilon.bbbot.settings.SettingsController;

import java.util.List;
import java.util.Objects;

public class DefaultListener extends ListenerAdapter {

    @SuppressWarnings("unchecked")
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        try {
            String prefix = ((List<String>) SettingsController.getValue("discord.prefix")).get(0);
            event.getJDA().getPresence().setActivity(Activity.playing(prefix + " | v" + BotInfo.VERSION));
        }catch (Exception e){
            event.getJDA().getPresence().setActivity(Activity.playing("v" + BotInfo.VERSION));
        }
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        checkChannel(event.getChannelLeft());
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        checkChannel(event.getChannelLeft());
    }

    private void checkChannel(VoiceChannel channel){
        if(CreateChannelCommand.channels.contains(channel) && channel.getMembers().size() == 0){
            new Thread(() -> {
                long channelId = channel.getIdLong();

                try {
                    Thread.sleep(60 * 1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(Objects.requireNonNull(DiscordController.getJDA().getVoiceChannelById(channelId)).getMembers().size() == 0){
                    Objects.requireNonNull(DiscordController.getJDA().getVoiceChannelById(channelId)).getGuild().delete().queue();
                }
            });
        }
    }
}
