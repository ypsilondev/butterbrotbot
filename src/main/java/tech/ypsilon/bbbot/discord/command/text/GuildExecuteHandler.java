package tech.ypsilon.bbbot.discord.command.text;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

@Deprecated
public interface GuildExecuteHandler extends Command {

    void onExecute(GuildMessageReceivedEvent e, String[] args);

}
