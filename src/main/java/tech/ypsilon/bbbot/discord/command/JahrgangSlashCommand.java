package tech.ypsilon.bbbot.discord.command;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import net.dv8tion.jda.api.requests.restaction.order.RoleOrderAction;
import org.bson.Document;
import org.bson.types.ObjectId;
import tech.ypsilon.bbbot.database.MongoController;
import tech.ypsilon.bbbot.database.MongoSettings;
import tech.ypsilon.bbbot.discord.DiscordController;
import tech.ypsilon.bbbot.settings.SettingsController;
import tech.ypsilon.bbbot.util.DiscordUtil;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JahrgangSlashCommand extends SlashCommand {

    private static final String MESSAGE = "Hier kannst du noch angeben, wann du dein Studium begonnen hast. 😀";

    private static long messageID = 1L;

    public static final Long channelId = SettingsController.getLong("discord.studiengaenge.channel");

    private static MongoCollection<Document> collection = null;

    @Override
    public CommandData commandData() {
        return new CommandData("jahrgang", "Kümmert sich um Jahrgänge").addSubcommands(
                new SubcommandData("add", "Fügt einen neuen Jahrgang hinzu").addOptions(
                        new OptionData(OptionType.STRING, "name", "Name", true),
                        new OptionData(OptionType.STRING, "emote", "Emote", true),
                        new OptionData(OptionType.ROLE, "role", "Rolle", true)
                ),
                new SubcommandData("remove", "Entfernt einen Studiengang").addOptions(
                        new OptionData(OptionType.ROLE, "role", "Rolle", true)
                ),
                new SubcommandData("update", "Aktuallisiert die Nachricht"),
                new SubcommandData("list", "Listet alle Jahrgänge auf")
        );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        event.deferReply(true).queue();

        if (!DiscordUtil.isAdmin(event.getMember())) {
            event.getHook().editOriginalEmbeds(EmbedUtil.createNoPermEmbed().build()).queue();
        }

        switch (Objects.requireNonNull(event.getSubcommandName())) {
            case "add":
                this.add(event);
                break;
            case "update":
                this.update(event);
                break;
            case "list":
                this.list(event);
                break;
            case "remove":
                this.remove(event);
                break;
        }
    }

    public void add(SlashCommandEvent event) {

        Role role = event.getOption("role").getAsRole();
        String name = event.getOption("name").getAsString();
        String emote = Objects.requireNonNull(event.getOption("emote")).getAsString();

        if (getCollection().countDocuments(new Document("roleId", role.getIdLong())) > 0) {
            event.getHook().editOriginalEmbeds(EmbedUtil.createErrorEmbed().addField("Fehler beim Erstellen",
                    "Diese Rolle ist schon eingetragen", false).build()).queue();
            return;
        }

        if (getCollection().countDocuments(new Document("emote", emote)) > 0) {
            // Emote already used
            event.getHook().editOriginalEmbeds(EmbedUtil.createErrorEmbed().addField("Fehler beim Erstellen",
                    "Der Emote wird schon benutzt", false).build()).queue();
            return;
        }

        if (getCollection().countDocuments(new Document("name", name)) > 0) {
            // Name already used
            event.getHook().editOriginalEmbeds(EmbedUtil.createErrorEmbed().addField("Fehler beim Erstellen",
                    "Der Name wird schon benutzt", false).build()).queue();
            return;
        }

        getCollection().insertOne(new Document("_id", new ObjectId())
                .append("roleId", role.getIdLong())
                .append("emote", emote)
                .append("name", name));

        event.getHook().editOriginalEmbeds(EmbedUtil.createSuccessEmbed()
                .addField("Jahrgang hinzugefügt", "Der Jahrgang wurde erfolgreich hinzugefügt",
                        false).build()).queue();

    }

    public void update(SlashCommandEvent event) {
        assert channelId != null;
        TextChannel textChannel = Objects.requireNonNull(DiscordController.getJDA().getTextChannelById(channelId));

        Object messageIdObj = MongoSettings.getValue(MongoSettings.TYPE.StudyStartMessage, event.getGuild().getIdLong());

        if (messageIdObj != null) {
            JahrgangSlashCommand.messageID = ((Long) messageIdObj);
        }

        Message message;

        try {
            message = textChannel.retrieveMessageById(messageID).complete();
        } catch (ErrorResponseException ignored) {
            message = null;
        }

        if (message == null) {
            message = textChannel.sendMessage(MESSAGE).complete();
            MongoSettings.setValue(MongoSettings.TYPE.StudyStartMessage, message.getIdLong(), event.getGuild().getIdLong());
        }
        message.editMessage(MESSAGE).queue();

        SelectionMenu.Builder menuBuilder = SelectionMenu.create(createSelectMenuId("study-start-select"));

        for (Document year : getCollection().find().sort(Sorts.ascending("name"))) {
            menuBuilder.addOption(year.getString("name"), year.getString("name"), Emoji.fromUnicode(year.getString("emote")));
        }

        message.editMessageComponents(ActionRow.of(menuBuilder.build())).queue();

        event.getHook().editOriginal("Done!").queue();
    }

    /**
     * Removes a reaction-role
     *
     * @param event the {@link SlashCommandEvent}
     */
    private void remove(SlashCommandEvent event) {
        Role role = Objects.requireNonNull(event.getOption("role")).getAsRole();

        if (getCollection().countDocuments(new Document("roleId", role.getIdLong())) == 0) {
            event.getHook().editOriginalEmbeds(EmbedUtil.createErrorEmbed().addField("Fehler beim Löschen",
                    "Diese Rolle ist noch nicht eingetragen", false).build()).queue();
            return;
        }

        getCollection().deleteOne(new Document("roleId", role.getIdLong()));
        event.getHook().editOriginalEmbeds(EmbedUtil.createSuccessEmbed()
                .addField("Jahrgang entfernt", "Der Jahrgang wurde erfolgreich entfernt",
                        false).build()).queue();
    }

    /**
     * Lists all roles
     *
     * @param event the {@link SlashCommandEvent}
     */
    private void list(SlashCommandEvent event) {
        StringBuilder list = new StringBuilder();
        for (Document doc : getCollection().find()) {
            list.append(", ").append(doc.getString("emote"))
                    .append(" ")
                    .append(doc.getString("name"))
                    .append(" (")
                    .append(Objects.requireNonNull(Objects.requireNonNull(event.getGuild()).getRoleById(doc.getLong("roleId"))).getAsMention())
                    .append(")");
        }
        list = new StringBuilder(list.toString().replace(", ", "\n"));

        event.getHook().editOriginalEmbeds(EmbedUtil.createInfoEmbed().addField("Jahrgänge", list.toString(), false).build()).queue();
    }

    @Override
    public void handleSelectionMenu(SelectionMenuEvent event, String data) {
        event.deferReply(true).queue();

        MongoCollection<Document> collection = getCollection();

        EmbedBuilder builder = EmbedUtil.createSuccessEmbed();

        String name = event.getSelectedOptions().get(0).getLabel();
        List<Long> memberRoles = event.getMember().getRoles().stream().map(Role::getIdLong).collect(Collectors.toList());
        for (Document document : collection.find()) {
            Long roleId = document.getLong("roleId");
            if (document.getString("name").equalsIgnoreCase(name)) {
                if (!memberRoles.contains(roleId)) {
                    Role role = event.getGuild().getRoleById(roleId);
                    builder.addField("", "Rolle " + role.getAsMention()+" hinzugefügt!", false);
                    event.getGuild().addRoleToMember(event.getMember(), role).queue();
                }
            } else {
                if (memberRoles.contains(roleId)) {
                    Role role = event.getGuild().getRoleById(roleId);
                    builder.addField("","Rolle " + role.getAsMention()+ " entfernt!", false);
                    event.getGuild().removeRoleFromMember(event.getMember(), role).queue();
                }
            }
        }

        event.getHook().editOriginalEmbeds(builder.build()).queue();
    }

    private static MongoCollection<Document> getCollection() {
        if (collection == null) {
            collection = MongoController.getInstance().getCollection("Jahrgaenge");
        }
        return collection;
    }
}
