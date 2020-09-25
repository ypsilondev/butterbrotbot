package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public class EditDirectoryCommand extends Command implements PrivateChat {
    @Override
    public String[] getAlias() {
        return new String[]{"editDir", "ed", "editDirectory"};
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {

    }

    @Override
    public String getDescription() {
        return "Editiere, erweitere einen deiner Ordner";
    }

    @Override
    public void onPrivateExecute(PrivateMessageReceivedEvent e, String[] args) {

    }
}
