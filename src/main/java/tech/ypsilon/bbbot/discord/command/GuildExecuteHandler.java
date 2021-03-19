package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public interface GuildExecuteHandler extends Command {

    void onExecute(GuildMessageReceivedEvent e, String[] args);

}
