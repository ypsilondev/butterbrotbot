package tech.ypsilon.bbbot.discord.command.text;

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public interface PrivateExecuteHandler extends Command {

    void onPrivateExecute(PrivateMessageReceivedEvent e, String[] args);

}