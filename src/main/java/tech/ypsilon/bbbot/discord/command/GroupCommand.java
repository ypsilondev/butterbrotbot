package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import tech.ypsilon.bbbot.database.codecs.StudyGroupCodec;
import tech.ypsilon.bbbot.discord.DiscordController;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.ArrayList;
import java.util.List;

public class GroupCommand extends LegacyCommand {
    @Override
    public String[] getAlias() {
        return new String[]{"group", "lerngruppe"};
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {
        if (args.length == 0 ||
                (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("add")) &&
                        args.length < 2) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Falsche Syntax");
            e.getChannel().sendMessage(b.build()).queue();
            return;
        }

        switch (args[0]) {
            case "create": createGroup(e, args[1]); break;
            case "add": addPlayerToGroup(e); break;
            case "leave": leaveGroup(e); break;
            case "list": listGroupMembers(e); break;
        }
    }

    private void createGroup(GuildMessageReceivedEvent e, String name) {
        List<User> members = new ArrayList<>();
        members.add(e.getAuthor());
        StudyGroupCodec group = StudyGroupCodec.createGroup(name, members);
        if (group == null) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Dieser Lerngruppenname ist bereits vergeben");
            e.getChannel().sendMessage(b.build()).queue();
            return;
        }
        EmbedBuilder b = EmbedUtil.createSuccessEmbed();
        b.setDescription("Die Lerngruppe '" + group.getName() + "'wurde erfolgreich erstellt");
        e.getChannel().sendMessage(b.build()).queue();
    }

    private void addPlayerToGroup(GuildMessageReceivedEvent e) {
        List<User> contributors = e.getMessage().getMentionedUsers();
        if (contributors.size() == 0) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Markiere/Nenne mindestens einen User mit @Username");
            e.getChannel().sendMessage(b.build()).queue();
            return;
        }

        boolean hasErrored = false;
        List<User> erroredUser = new ArrayList<>();
        StudyGroupCodec group = StudyGroupCodec.retrieveStudyGroup(e.getAuthor());
        for (User contributor : contributors) {
            if (!group.addToGroup(contributor)) {
                hasErrored = true;
                erroredUser.add(contributor);
            }
        }

        EmbedBuilder b;
        if (hasErrored) {
            b = EmbedUtil.createErrorEmbed();
            b.setDescription((erroredUser.size() > 1 ? "Ein " : "Mehrere ") + "User konnten nicht hinzugefügt hinzugefügt werden");
            for (User user : erroredUser) {
                b.addField(user.getName(), "", false);
            }
        } else {
            b = EmbedUtil.createSuccessEmbed();
            b.setDescription((contributors.size() > 1 ? "Der " : "Die ") + "User wurden hinzugefügt");
        }
        e.getChannel().sendMessage(b.build()).queue();
    }

    private void listGroupMembers(GuildMessageReceivedEvent e) {
        StudyGroupCodec group = StudyGroupCodec.retrieveStudyGroup(e.getAuthor());
        if (group == null) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Du bist in keiner Lerngruppe");
            e.getChannel().sendMessage(b.build()).queue();
            return;
        }

        EmbedBuilder b = EmbedUtil.createSuccessEmbed();
        b.setDescription("Lerngruppe " + group.getName());
        for (Long userID : group.getUserIDs()) {
            JDA jda = DiscordController.getJDA();
            User userById = jda.getUserById(userID);
            if (userById == null) userById = jda.retrieveUserById(userID).complete();
            b.addField(userById.getName(), "", false);
        }
        e.getChannel().sendMessage(b.build()).queue();
    }

    private void leaveGroup(GuildMessageReceivedEvent e) {
        StudyGroupCodec group = StudyGroupCodec.retrieveStudyGroup(e.getAuthor());
        if (group == null) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Du bist in keiner Lerngruppe");
            e.getChannel().sendMessage(b.build()).queue();
            return;
        }

        group.leaveGroup(e.getAuthor());
        EmbedBuilder b = EmbedUtil.createSuccessEmbed();
        b.setDescription("Du hast die Lerngruppe " + group.getName() + " verlassen");
        e.getChannel().sendMessage(b.build()).queue();
    }

    @Override
    public String getDescription() {
        return "Erstelle Lerngruppen mit 'kit lerngruppe create [NAME_OHNE_LEERZEICHEN]', " +
                "füge User hinzu mit 'kit lerngruppe add [@USER]' " +
                "oder zeige an, welche User deiner Lerngruppe angehören mit 'kit lerngruppe list'." +
                "Joine in den Channel 'Lerngruppenchannel' und es wird automatisch ein lerngruppeneigener Channel mit " +
                "eurem Lerngruppennamen erstellt. In diesen können alle Lerngruppenteilnehmer joinen, in dem sie " +
                "ebenfalls in den 'Lerngruppenchannel' joinen. Der Channel wird nach dem Verlassen aller Mitglieder " +
                "gelöscht, und bei Bedarf wieder mit vorherigem Schema erstellt.\n Verlasse deine Lerngruppe mit " +
                "'kit lerngruppe leave'";
    }
}
