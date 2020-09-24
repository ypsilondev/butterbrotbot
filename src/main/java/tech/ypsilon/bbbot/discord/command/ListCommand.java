package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class ListCommand extends Command {
    @Override
    public String[] getAlias() {
        return new String[]{"list", "l", "get"};
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {
        if (args.length == 0) {
            // todo: list all
            return;
        }

        // todo: args-filter
    }

    @Override
    public String getDescription() {
        return "Liste alle Link-Verknüpfungen oder suche nach bestimmten";
    }
}
