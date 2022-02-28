package tech.ypsilon.bbbot.discord.listener;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import org.jetbrains.annotations.NotNull;
import tech.ypsilon.bbbot.BotInfo;
import tech.ypsilon.bbbot.ButterBrot;

public class ReadyPresenceListener extends ButterbrotListener {

    public ReadyPresenceListener(ButterBrot parent) {
        super(parent);
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        try {
            String prefix = BotInfo.PRESENCE_PREFIX;
            event.getJDA().getPresence().setActivity(Activity.playing(prefix + " | v" + BotInfo.VERSION));
        } catch (Exception exception){
            event.getJDA().getPresence().setActivity(Activity.playing("v" + BotInfo.VERSION));
        }
    }

}
