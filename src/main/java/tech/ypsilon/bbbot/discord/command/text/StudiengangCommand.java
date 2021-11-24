package tech.ypsilon.bbbot.discord.command.text;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;
import org.bson.Document;
import org.bson.types.ObjectId;
import tech.ypsilon.bbbot.database.MongoController;
import tech.ypsilon.bbbot.discord.DiscordController;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.*;

@Deprecated
public class StudiengangCommand extends LegacyCommand {

    private final String MESSAGE = "Herzlich willkommen auf dem Zweities-Server fürs <:KIT:759041596460236822> . " +
            "Wähle per Klick auf ein Emoji unter der Nachricht deinen Studiengang um die Informationen des Discord-Servers für dich zu personalisieren :star_struck: .\n\n" +
            "Dein Studiengang fehlt? Schreibe einem Moderator <@&757718320526000138> :100:";
    public static final long channelId = 759033520680599553L;
    final MongoCollection<Document> collection = MongoController.getInstance().getCollection("Studiengaenge");
    final MongoCollection<Document> collectionCategories = MongoController.getInstance().getCollection("StudiengaengeCategories");

    @Override
    public String[] getAlias() {
        return new String[]{"studiengang"};
    }

    @Override
    public String getDescription() {
        return "Fügt einen neuen Studiengang hinzu (admin only)";
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {
        if (Objects.requireNonNull(e.getMember()).getRoles().stream().noneMatch(role -> role.getIdLong() == 759072770751201361L
                || role.getIdLong() == 757718320526000138L) && e.getAuthor().getIdLong() != 117625148785295363L) {
            e.getChannel().sendMessage(EmbedUtil.createErrorEmbed().addField("Kein Recht",
                    "Du hast kein Recht diesen Befehl auszuführen", false).build()).queue();
            return;
        }

        switch (checkArgs(0, args, new String[]{"add", "remove", "list", "setCategory", "update"}, e)) {
            case "add":
                if (args.length < 5) {
                    e.getChannel().sendMessage(EmbedUtil.createErrorEmbed().addField("Falsche Argumente",
                            "Eingabe: <categoryId> :emote: <@role> <Name>", false).build()).queue();
                    return;
                }

                if (e.getMessage().getMentionedRoles().size() == 0) {
                    e.getChannel().sendMessage(EmbedUtil.createErrorEmbed().addField("Falsche Argumente",
                            "Es muss eine Rolle erwähnt werden", false).build()).queue();
                    return;
                }

                if (collection.countDocuments(new Document("roleId", e.getMessage().getMentionedRoles().get(0).getIdLong())) > 0) {
                    e.getChannel().sendMessage(EmbedUtil.createErrorEmbed().addField("Fehler beim Erstellen",
                            "Diese Rolle ist schon eingetragen", false).build()).queue();
                    return;
                }

                if (collection.countDocuments(new Document("emote", e.getMessage().getContentRaw().split(" ")[4])) > 0) {
                    e.getChannel().sendMessage(EmbedUtil.createErrorEmbed().addField("Fehler beim Erstellen",
                            "Der Emote wird schon benutzt", false).build()).queue();
                    return;
                }

                if (collection.countDocuments(new Document("name", args[4])) > 0) {
                    e.getChannel().sendMessage(EmbedUtil.createErrorEmbed().addField("Fehler beim Erstellen",
                            "Der Name wird schon benutzt", false).build()).queue();
                    return;
                }

                if (!args[1].matches("-?\\d+(\\.\\d+)?")) {
                    e.getChannel().sendMessage(EmbedUtil.createErrorEmbed().addField("Keine Zahl",
                            "Die Kategorie muss eine Zahl sein", false).build()).queue();
                    return;
                }

                collection.insertOne(new Document("_id", new ObjectId())
                        .append("category", Integer.parseInt(args[1]))
                        .append("roleId", e.getMessage().getMentionedRoles().get(0).getIdLong())
                        .append("emote", e.getMessage().getContentRaw().split(" ")[4])
                        .append("name", args[4]));

                e.getChannel().sendMessage(EmbedUtil.createSuccessEmbed()
                        .addField("Studiengang hinzugefügt", "Der Studiengang wurde erfolgreich hinzugefügt",
                                false).build()).queue();
                break;
            case "remove":
                if (args.length < 2) {
                    e.getChannel().sendMessage(EmbedUtil.createErrorEmbed().addField("Falsche Argumente",
                            "Eingabe: @role", false).build()).queue();
                    return;
                }


                if (e.getMessage().getMentionedRoles().size() == 0) {
                    e.getChannel().sendMessage(EmbedUtil.createErrorEmbed().addField("Falsche Argumente",
                            "Es muss eine Rolle erwähnt werden", false).build()).queue();

                }

                if (collection.countDocuments(new Document("roleId", e.getMessage().getMentionedRoles().get(0).getIdLong())) == 0) {
                    e.getChannel().sendMessage(EmbedUtil.createErrorEmbed().addField("Fehler beim Löschen",
                            "Diese Rolle ist noch nicht eingetragen", false).build()).queue();
                    return;
                }

                collection.deleteOne(new Document("roleId", e.getMessage().getMentionedRoles().get(0).getIdLong()));
                e.getChannel().sendMessage(EmbedUtil.createSuccessEmbed()
                        .addField("Studiengang entfernt", "Der Studiengang wurde erfolgreich entfernt",
                                false).build()).queue();
            case "list":
                StringBuilder list = new StringBuilder();
                for (Document doc : collection.find()) {
                    list.append(", ").append(doc.getString("name"));
                }
                list = new StringBuilder(list.toString().replace(", ", ""));

                e.getChannel().sendMessage(EmbedUtil.createInfoEmbed().addField("Studiengänge", list.toString(), false).build()).queue();
            case "setCategory":
                if (args.length < 3) {
                    e.getChannel().sendMessage(EmbedUtil.createErrorEmbed().addField("Falsche Argumente",
                            "Eingabe: <Id> <Name>", false).build()).queue();
                    return;
                }

                try {
                    int id = Integer.parseInt(args[1]);
                    String[] nameArray = Arrays.copyOfRange(args, 2, args.length);
                    String name = String.join(" ", nameArray);

                    collectionCategories.updateOne(Filters.eq("id", id),
                            Updates.combine(Updates.set("id", id), Updates.set("name", name)),
                            new UpdateOptions().upsert(true));

                    e.getChannel().sendMessage(EmbedUtil.createSuccessEmbed()
                            .addField("Kategorie erfolgreich gesetzt",
                                    "Die Kategorie wurde erfolgreich gesetzt", false).build()).queue();
                } catch (NumberFormatException ex) {
                    e.getChannel().sendMessage(EmbedUtil.createSuccessEmbed().addField("Keine Zahl",
                            "Die Id muss eine Zahl sein", false).build()).queue();
                    return;
                }
                break;
            case "update":
                TextChannel textChannel = Objects.requireNonNull(DiscordController.getJDAStatic().getTextChannelById(channelId));

                e.getChannel().sendMessage(EmbedUtil.createInfoEmbed()
                        .addField("Nachricht wird aktualisiert", "Die Nachricht wird jetzt aktualisiert. " +
                                "Dies kann ein paar Sekunden dauern, da Discord Rate-Limits hat.",
                                false).build()).queue();

                boolean first = true;
                int messageSend = 0;
                for (Document catDoc : collectionCategories.find().sort(Sorts.ascending("id"))) {
                    List<ActionRow> actionRows = new ArrayList<>();

                    List<Component> components = new ArrayList<>();
                    for (Document doc : collection.find(Filters.eq("category", catDoc.getInteger("id")))) {
                        String id = "studiengang-" + doc.getObjectId("_id").toHexString();
                        components.add(Button.primary(id, doc.getString("emote") + " " + doc.getString("name")));

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

                e.getChannel().sendMessage(EmbedUtil.createSuccessEmbed()
                        .addField("Nachricht wurde aktualisiert", "Die Nachricht wurde aktualisiert",
                                false).build()).queue();
        }
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

}
