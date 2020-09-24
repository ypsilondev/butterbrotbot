package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public abstract class Command {

    public abstract String[] getAlias();
    public abstract void onExecute(GuildMessageReceivedEvent e, String[] args);
    public abstract String getDescription();

}
