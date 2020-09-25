package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public class AddDirectoryCommand extends Command implements PrivateChat {
    @Override
    public String[] getAlias() {
        return new String[]{"mkdir", "addDirectory", "createDirectory", "ad"};
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {

    }

    @Override
    public String getDescription() {
        return "Erstelle personalisierte Ordner mit einer eigenen Sammlung an Links/Verknüpfungen";
    }

    @Override
    public void onPrivateExecute(PrivateMessageReceivedEvent e, String[] args) {

    }
}
