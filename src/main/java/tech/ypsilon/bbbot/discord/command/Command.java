package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Command {

    abstract String[] getAlias();
    abstract void onExecute(MessageReceivedEvent e, String[] args);
    abstract String getDescription();

}
