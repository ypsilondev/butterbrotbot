package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.List;

public class GroupCommand extends Command {
    @Override
    public String[] getAlias() {
        return new String[]{"group", "lerngruppe"};
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {
        if (args.length < 2) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Falsche Syntax");
            e.getChannel().sendMessage(b.build()).queue();
            return;
        }

        switch (args[0]) {
            case "create": createGroup(e); break;
            case "add": addPlayerToGroup(e); break;
        }
    }

    private void createGroup(GuildMessageReceivedEvent e) {
        List<User> contributors = e.getMessage().getMentionedUsers();
        EmbedBuilder b = EmbedUtil.createSuccessEmbed();
        b.setDescription("Die Lerngruppe wurde erfolgreich erstellt");
        e.getChannel().sendMessage(b.build()).queue();
    }

    private void addPlayerToGroup(GuildMessageReceivedEvent e) {
        List<User> contributors = e.getMessage().getMentionedUsers();


        EmbedBuilder b = EmbedUtil.createSuccessEmbed();
        b.setDescription((contributors.size() > 1 ? "Der " : "Die ") + "User wurden hinzugefügt");
        e.getChannel().sendMessage(b.build()).queue();
    }

    @Override
    public String getDescription() {
        return null;
    }
}
