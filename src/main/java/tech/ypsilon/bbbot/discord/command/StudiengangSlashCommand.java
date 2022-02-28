package tech.ypsilon.bbbot.discord.command;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import io.prometheus.client.Counter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;
import org.bson.Document;
import org.bson.types.ObjectId;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.database.MongoController;
import tech.ypsilon.bbbot.discord.DiscordController;
import tech.ypsilon.bbbot.util.DiscordUtil;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This command handles one of the core-features of ButterBrot:
 * Reaction-roles for Studiengänge.
 */
public class StudiengangSlashCommand extends SlashCommand {

    private static final String MESSAGE = "Herzlich willkommen auf dem allgemeinen Studierenden Server für das <:KIT:759041596460236822> . " +
            "Wähle per Klick auf ein Emoji unter der Nachricht deinen Studiengang um die Informationen des Discord-Servers für dich zu personalisieren :star_struck: .\n\n" +
            "Dein Studiengang fehlt? Schreibe einem Moderator <@&757718320526000138> :100:";

    private static MongoCollection<Document> collection = null;
    private static MongoCollection<Document> collectionCategories = null;

    /**
     * Prometheus-counter for statistics
     */
    private final Counter counter = Counter.build().name("butterbrot_role").help("-").labelNames("fach").register();

    private final TextChannel textChannel;

    public StudiengangSlashCommand(ButterBrot parent) {
        super(parent);

        textChannel = Objects.requireNonNull(parent.getDiscordController().getJda()
                .getTextChannelById(parent.getConfig().getDiscord().getCourseSelectionConfig().getChannel()));
    }


    @Override
    public CommandData commandData() {
        return new CommandData("studiengang", "Fügt einen neuen Studiengang hinzu (admin only)").addSubcommands(
                new SubcommandData("add", "Fügt einen Studiengang hinzu").addOptions(
                        new OptionData(OptionType.INTEGER, "category-id", "Kategorie", true),
                        new OptionData(OptionType.STRING, "emote", "Emote", true),
                        new OptionData(OptionType.ROLE, "role", "Rolle", true),
                        new OptionData(OptionType.STRING, "name", "Name", true)
                ),
                new SubcommandData("remove", "Entfernt einen Studiengang").addOptions(
                        new OptionData(OptionType.ROLE, "role", "Rolle", true)
                ),
                new SubcommandData("list", "Listet alle Studiengänge auf."),
                new SubcommandData("name-category", "Setzt den Namen einer Kategorie").addOptions(
                        new OptionData(OptionType.INTEGER, "category-id", "Kategorie", true),
                        new OptionData(OptionType.STRING, "name", "Name für die Kategorie", true)
                ),
                new SubcommandData("update", "Aktuallisiert die Nachricht")
        );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        event.deferReply(true).queue();


        if (!DiscordUtil.isAdmin(event.getMember())) {
            event.getHook().editOriginalEmbeds(EmbedUtil.createNoPermEmbed().build()).queue();
        }

        // Switch over the subcommand-type
        switch (Objects.requireNonNull(event.getSubcommandName())) {
            case "add":
                this.add(event);
                break;
            case "remove":
                this.remove(event);
                break;
            case "list":
                this.list(event);
                break;
            case "name-category":
                this.nameCategory(event);
                break;
            case "update":
                this.update(event);
                break;
        }
    }

    /**
     * Stores a new reaction-role in the database
     *
     * @param event the {@link SlashCommandEvent}
     */
    private void add(SlashCommandEvent event) {
        int categoryId = (int) Objects.requireNonNull(event.getOption("category-id")).getAsLong();
        Role role = Objects.requireNonNull(event.getOption("role")).getAsRole();
        String emote = Objects.requireNonNull(event.getOption("emote")).getAsString();
        String name = Objects.requireNonNull(event.getOption("name")).getAsString();

        if (getCollection().countDocuments(new Document("roleId", role.getIdLong())) > 0) {
            // Role already exists
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
                .append("category", categoryId)
                .append("roleId", role.getIdLong())
                .append("emote", emote)
                .append("name", name));

        event.getHook().editOriginalEmbeds(EmbedUtil.createSuccessEmbed()
                .addField("Studiengang hinzugefügt", "Der Studiengang wurde erfolgreich hinzugefügt",
                        false).build()).queue();
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
                .addField("Studiengang entfernt", "Der Studiengang wurde erfolgreich entfernt",
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
            list.append(", ").append(doc.getString("name"));
        }
        list = new StringBuilder(list.toString().replace(", ", ""));

        event.getHook().editOriginalEmbeds(EmbedUtil.createInfoEmbed().addField("Studiengänge", list.toString(), false).build()).queue();
    }

    /**
     * Changes / set the name of a role
     *
     * @param event the {@link SlashCommandEvent}
     */
    private void nameCategory(SlashCommandEvent event) {
        int id = (int) Objects.requireNonNull(event.getOption("category-id")).getAsLong();
        String name = Objects.requireNonNull(event.getOption("name")).getAsString();

        if (getCollectionCategories().countDocuments(Filters.eq("id", id)) > 0) {
            getCollectionCategories().updateOne(Filters.eq("id", id),
                    Updates.combine(Updates.set("id", id), Updates.set("name", name)),
                    new UpdateOptions().upsert(true));
        } else {
            Document doc = new Document();
            doc.append("id", id);
            doc.append("name", name);
            getCollectionCategories().insertOne(doc);
        }


        event.getHook().editOriginalEmbeds(EmbedUtil.createSuccessEmbed()
                .addField("Kategorie erfolgreich gesetzt",
                        "Die Kategorie wurde erfolgreich gesetzt", false).build()).queue();
    }

    /**
     * Updates the reaction-role-message in the channel
     *
     * @param event the {@link SlashCommandEvent}
     */
    private void update(SlashCommandEvent event) {
        event.getHook().editOriginalEmbeds(EmbedUtil.createInfoEmbed()
                .addField("Nachricht wird aktualisiert", "Die Nachricht wird jetzt aktualisiert. " +
                                "Dies kann ein paar Sekunden dauern, da Discord Rate-Limits hat.",
                        false).build()).queue();

        boolean first = true;
        int messageSend = 0;
        for (Document catDoc : getCollectionCategories().find().sort(Sorts.ascending("id"))) {
            List<ActionRow> actionRows = new ArrayList<>();

            // TODO: actually change the buttons to a drop-down-list
            List<Component> components = new ArrayList<>();
            for (Document doc : getCollection().find(Filters.eq("category", catDoc.getInteger("id")))) {
                // String buttonId = "studiengang-" + doc.getObjectId("_id").toHexString();
                String buttonId = createButtonId("studiengang-" + doc.getObjectId("_id").toHexString());
                components.add(Button.primary(buttonId, doc.getString("emote") + " " + doc.getString("name")));

                if (components.size() == 5) {
                    actionRows.add(ActionRow.of(components));
                    components.clear();
                }
            }

            String category = catDoc.getString("name");
            String firstMessage = MESSAGE + "\n\n" + category;

            if (components.size() > 0)
                actionRows.add(ActionRow.of(components));

            List<Message> returnMessages = textChannel.retrievePinnedMessages().complete();
            List<Message> pinnedMessages = returnMessages.subList(0, returnMessages.size() - messageSend);
            if (pinnedMessages.size() > 0) {
                int i = pinnedMessages.size() - 1;
                while (actionRows.size() > 0) {
                    List<ActionRow> sendList = getFirstActionRows(actionRows);

                    if (i >= 0) {
                        textChannel.editMessageById(pinnedMessages.get(i).getId(), first ? firstMessage : category)
                                .setActionRows(sendList).queue();
                        first = false;
                    } else {
                        textChannel.sendMessage(category).setActionRows(sendList)
                                .queue(message -> textChannel.pinMessageById(message.getId()).queue());
                    }
                    messageSend++;
                    i--;
                }
            } else {
                while (actionRows.size() > 0) {
                    List<ActionRow> sendList = getFirstActionRows(actionRows);

                    textChannel.sendMessage(first ? firstMessage : category).setActionRows(sendList)
                            .queue(message -> textChannel.pinMessageById(message.getId()).queue());
                    first = false;
                    messageSend++;
                }
            }
        }

        event.getHook().editOriginalEmbeds(EmbedUtil.createSuccessEmbed()
                .addField("Nachricht wurde aktualisiert", "Die Nachricht wurde aktualisiert",
                        false).build()).queue();

    }

    private List<ActionRow> getFirstActionRows(List<ActionRow> actionRows) {
        int count = 0;
        List<ActionRow> sendList = new ArrayList<>();
        for (ActionRow actionRow : actionRows) {
            if (count >= 5)
                break;
            sendList.add(actionRow);
            count++;
        }
        actionRows.removeAll(sendList);
        return sendList;
    }

    /**
     * Handles user-interaction on the buttons in order to add the role to the member
     *
     * @param event the {@link ButtonClickEvent}
     * @param data  the data passed with the event
     */
    @Override
    public void handleButtonInteraction(ButtonClickEvent event, String data) {

        MongoCollection<Document> collection = MongoController.getInstance().getCollection("Studiengaenge");
        Document doc = collection.find(new Document("_id",
                new ObjectId(data.split("-")[1]))).first();
        assert doc != null;
        Role role = Objects.requireNonNull(event.getGuild()).getRoleById(doc.getLong("roleId"));

        assert role != null;
        if (Objects.requireNonNull(event.getMember()).getRoles().contains(role)) {
            event.getGuild().removeRoleFromMember(event.getMember(), role).queue();
            event.reply("Rolle " + role.getAsMention() + " entfernt").setEphemeral(true).queue();
        } else {
            event.getGuild().addRoleToMember(event.getMember(), role).queue();
            event.reply("Rolle " + role.getAsMention() + " hinzugefügt").setEphemeral(true).queue();

            // increase stats :)
            counter.labels(Objects.requireNonNull(event.getButton()).getLabel()).inc();
        }
    }

    private static MongoCollection<Document> getCollection() {
        if (collection == null) {
            collection = MongoController.getInstance().getCollection("Studiengaenge");
        }
        return collection;
    }

    private static MongoCollection<Document> getCollectionCategories() {
        if (collectionCategories == null) {
            collectionCategories = MongoController.getInstance().getCollection("StudiengaengeCategories");
        }
        return collectionCategories;
    }
}
