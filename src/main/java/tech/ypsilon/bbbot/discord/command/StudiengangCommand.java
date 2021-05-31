package tech.ypsilon.bbbot.discord.command;

import com.mongodb.client.MongoCollection;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;
import org.bson.Document;
import org.bson.types.ObjectId;
import tech.ypsilon.bbbot.database.MongoController;
import tech.ypsilon.bbbot.discord.DiscordController;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.*;

public class StudiengangCommand extends LegacyCommand {

    private final String messageStart = "Herzlich willkommen auf dem Ersti-Server fürs <:KIT:759041596460236822> . " +
            "Wähle per Klick auf ein Emoji unter der Nachricht deinen Studiengang um die Informationen des Discord-Servers für dich zu personalisieren :star_struck: .\n";
    private final String messageEnd = "\n" + "Dein Studiengang fehlt? Schreibe einem Moderator <@&757718320526000138> :100:";
    public static final long channelId = 759033520680599553L;
    final MongoCollection<Document> collection = MongoController.getInstance().getCollection("Studiengaenge");

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

        switch (checkArgs(0, args, new String[]{"add", "remove", "list", "reload", "update"}, e)) {
            case "add":
                if (args.length < 4) {
                    e.getChannel().sendMessage(EmbedUtil.createErrorEmbed().addField("Falsche Argumente",
                            "Eingabe: :emote @role Name", false).build()).queue();
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

                if (collection.countDocuments(new Document("emote", e.getMessage().getContentRaw().split(" ")[3])) > 0) {
                    e.getChannel().sendMessage(EmbedUtil.createErrorEmbed().addField("Fehler beim Erstellen",
                            "Der Emote wird schon benutzt", false).build()).queue();
                    return;
                }

                if (collection.countDocuments(new Document("name", args[3])) > 0) {
                    e.getChannel().sendMessage(EmbedUtil.createErrorEmbed().addField("Fehler beim Erstellen",
                            "Der Name wird schon benutzt", false).build()).queue();
                    return;
                }

                collection.insertOne(new Document("_id", new ObjectId())
                        .append("roleId", e.getMessage().getMentionedRoles().get(0).getIdLong())
                        .append("emote", e.getMessage().getContentRaw().split(" ")[3])
                        .append("name", args[3]));

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
            case "update":
                List<ActionRow> actionRows = new ArrayList<>();

                List<Component> components = new ArrayList<>();
                for (Document doc : collection.find()) {
                    String id = "studiengang-" + doc.getObjectId("_id").toHexString();
                    components.add(Button.primary(id, doc.getString("emote") + " " + doc.getString("name")));

                    if (components.size() == 5) {
                        actionRows.add(ActionRow.of(components));
                        components.clear();
                    }
                }

                if (components.size() > 0)
                    actionRows.add(ActionRow.of(components));

                TextChannel textChannel = Objects.requireNonNull(DiscordController.getJDA().getTextChannelById(channelId));

                List<Message> pinnedMessages = textChannel.retrievePinnedMessages().complete();
                if (pinnedMessages.size() > 0) {
                    textChannel.editMessageById(pinnedMessages.get(0).getId(), messageStart + messageEnd)
                            .setActionRows(actionRows).queue();
                } else {
                    textChannel.sendMessage(messageStart + messageEnd).setActionRows(actionRows).queue(message -> {
                        textChannel.pinMessageById(message.getId()).queue();
                    });
                }

                e.getChannel().sendMessage(EmbedUtil.createSuccessEmbed()
                        .addField("Nachricht wird aktualisiert", "Die Nachricht wird jetzt aktualisiert",
                                false).build()).queue();
        }
    }

}
