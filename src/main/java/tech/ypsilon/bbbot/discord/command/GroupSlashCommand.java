package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.Nullable;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.database.codecs.StudyGroupCodec;
import tech.ypsilon.bbbot.discord.DiscordController;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupSlashCommand extends SlashCommand {

    public GroupSlashCommand(ButterBrot parent) {
        super(parent);
    }

    @Override
    public CommandData commandData() {
        return new CommandData("group", "Kümmert sich um Lerngruppen")
                .addSubcommands(
                        new SubcommandData("create", "Erstellt eine Lerngruppe")
                                .addOption(OptionType.STRING, "name", "Gruppenname", true),
                        new SubcommandData("add", "Fügt einen Benutzer zur Lerngruppe hinzu")
                                .addOption(OptionType.USER, "benutzer", "Benutzer, der hinzugefügt werden soll.", true),
                        new SubcommandData("leave", "Lässt dich die Lerngruppe verlassen"),
                        new SubcommandData("list", "Listet alle Mitglieder der Lerngruppe auf")
                );
    }

    @Override
    public @Nullable String getHelpDescription() {
        return "/group create <name> erstellt einen neue Lerngruppe mit dem Namen <name>\n" +
                "/group add <member> fügt den Benutzer <member> der eigenen Lerngruppe hinzu\n" +
                "/group leave entfernt dich aus deiner Lerngruppe\n" +
                "/group list listet alle Mitglieder deiner Lerngruppe auf";
    }

    @Override
    public void execute(SlashCommandEvent event) {
        event.deferReply(true).queue();
        switch (Objects.requireNonNull(event.getSubcommandName())) {
            case "create":
                createGroup(event);
                break;
            case "add":
                addPlayerToGroup(event);
                break;
            case "leave":
                leaveGroup(event);
                break;
            case "list":
                listGroupMembers(event);
                break;
        }
    }

    private void createGroup(SlashCommandEvent event) {
        List<User> members = new ArrayList<>();
        members.add(event.getUser());
        StudyGroupCodec group = StudyGroupCodec.createGroup(Objects.requireNonNull(event.getOption("name")).getAsString(), members);
        if (group == null) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Dieser Lerngruppenname ist bereits vergeben");
            event.getHook().editOriginalEmbeds(b.build()).queue();
        } else {
            EmbedBuilder b = EmbedUtil.createSuccessEmbed();
            b.setDescription("Die Lerngruppe '" + group.getName() + "'wurde erfolgreich erstellt");
            event.getHook().editOriginalEmbeds(b.build()).queue();
        }
    }

    private void addPlayerToGroup(SlashCommandEvent event) {
        User contributor = event.getOption("benutzer").getAsUser();

        boolean hasErrored = false;
        List<User> erroredUser = new ArrayList<>();
        StudyGroupCodec group = StudyGroupCodec.retrieveStudyGroup(event.getUser());
        if (!group.addToGroup(contributor)) {
            hasErrored = true;
            erroredUser.add(contributor);
        }

        EmbedBuilder b;
        if (hasErrored) {
            b = EmbedUtil.createErrorEmbed();
            b.setDescription("Der User konnte nicht hinzugefügt hinzugefügt werden");
            for (User user : erroredUser) {
                b.addField(user.getName(), "", false);
            }
        } else {
            b = EmbedUtil.createSuccessEmbed();
            b.setDescription("Der User wurden hinzugefügt");
        }
        event.getHook().editOriginalEmbeds(b.build()).queue();
    }

    private void listGroupMembers(SlashCommandEvent event) {
        StudyGroupCodec group = StudyGroupCodec.retrieveStudyGroup(event.getUser());
        if (group == null) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Du bist in keiner Lerngruppe");
            event.getHook().editOriginalEmbeds(b.build()).queue();
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
        event.getHook().editOriginalEmbeds(b.build()).queue();
    }

    private void leaveGroup(SlashCommandEvent event) {
        StudyGroupCodec group = StudyGroupCodec.retrieveStudyGroup(event.getUser());
        if (group == null) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Du bist in keiner Lerngruppe");
            event.getHook().editOriginalEmbeds(b.build()).queue();
            return;
        }

        group.leaveGroup(event.getUser());
        EmbedBuilder b = EmbedUtil.createSuccessEmbed();
        b.setDescription("Du hast die Lerngruppe " + group.getName() + " verlassen");
        event.getHook().editOriginalEmbeds(b.build()).queue();
    }

}
