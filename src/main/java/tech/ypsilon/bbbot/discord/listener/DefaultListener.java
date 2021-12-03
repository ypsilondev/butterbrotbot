package tech.ypsilon.bbbot.discord.listener;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import org.jetbrains.annotations.NotNull;
import tech.ypsilon.bbbot.BotInfo;
import tech.ypsilon.bbbot.ButterBrot;

public class DefaultListener extends ButterbrotListener {

    public DefaultListener(ButterBrot parent) {
        super(parent);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        try {
            String prefix = getParent().getConfig().getDiscord().getPrefixList().get(0);
            event.getJDA().getPresence().setActivity(Activity.playing(prefix + " | v" + BotInfo.VERSION));
        }catch (Exception e){
            event.getJDA().getPresence().setActivity(Activity.playing("v" + BotInfo.VERSION));
        }
    }

}
