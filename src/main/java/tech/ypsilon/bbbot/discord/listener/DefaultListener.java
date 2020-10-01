package tech.ypsilon.bbbot.discord.listener;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import tech.ypsilon.bbbot.BotInfo;
import tech.ypsilon.bbbot.settings.SettingsController;

import java.util.List;

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
}
