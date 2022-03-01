package tech.ypsilon.bbbot.discord;

import lombok.Getter;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.discord.listener.*;
import tech.ypsilon.bbbot.util.GenericListenerController;
import tech.ypsilon.bbbot.util.Initializable;

@Getter
public class ListenerController extends GenericListenerController implements Initializable {

    private final ReadyPresenceListener readyPresenceListener;
    // private final RoleListener roleListener;
    private final StudyGroupVoiceListener channelListener;
    private final NewMemberJoinListener newMemberJoinListener;
    private final CensorWatcherListener censorWatcherListener;
    private final RankSystemListener rankSystemListener;
    private final InviteListener inviteListener;
    private final BadWordListener badWordListener;

    public ListenerController(ButterBrot parent) {
        super(parent);

        this.readyPresenceListener = new ReadyPresenceListener(parent);
        // this.roleListener = new RoleListener(parent);
        this.channelListener = new StudyGroupVoiceListener(parent);
        this.newMemberJoinListener = new NewMemberJoinListener(parent);
        this.censorWatcherListener = new CensorWatcherListener(parent);
        this.rankSystemListener = new RankSystemListener(parent);
        this.inviteListener = new InviteListener(parent);
        this.badWordListener = new BadWordListener(parent);
    }

    @Override
    public void init() throws Exception {
        ButterBrot.LOGGER.info("Initializing Listeners...");

        channelListener.safeInit();

        ButterBrot.LOGGER.info("Registering Listeners...");
        registerEventListener(
                readyPresenceListener,
                // roleListener,
                channelListener,
                newMemberJoinListener,
                censorWatcherListener,
                rankSystemListener,
                inviteListener,
                badWordListener
        );
        ButterBrot.LOGGER.debug("Done Registering Listeners!");
    }

    private void registerEventListener(ListenerAdapter... listenerAdapter) {
        for (ListenerAdapter adapter : listenerAdapter) {
            ButterBrot.LOGGER.debug("- registering Listener {}", adapter.getClass().getSimpleName());
            getParent().getDiscordController().getJda().addEventListener(adapter);
        }
    }
}
