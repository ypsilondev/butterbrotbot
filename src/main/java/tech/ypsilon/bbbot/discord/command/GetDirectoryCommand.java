package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public class GetDirectoryCommand extends Command implements PrivateChat {
    @Override
    public String[] getAlias() {
        return new String[]{"cd", "getDir", "dir", "d", "getDirectory"};
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {

    }

    @Override
    public String getDescription() {
        return "Zeige einen personalisierten Ordner an";
    }

    @Override
    public void onPrivateExecute(PrivateMessageReceivedEvent e, String[] args) {

    }
}
