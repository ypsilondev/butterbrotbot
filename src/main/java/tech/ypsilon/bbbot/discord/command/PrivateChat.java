package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public interface PrivateChat {

    void onPrivateExecute(PrivateMessageReceivedEvent e, String[] args);

}
