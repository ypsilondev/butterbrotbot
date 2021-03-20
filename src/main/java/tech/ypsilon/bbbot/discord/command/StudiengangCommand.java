package tech.ypsilon.bbbot.discord.command;

import com.mongodb.client.MongoCollection;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.bson.Document;
import org.bson.types.ObjectId;
import tech.ypsilon.bbbot.database.MongoController;
import tech.ypsilon.bbbot.discord.DiscordController;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class StudiengangCommand extends LegacyCommand {

    private final String messageStart = "Herzlich willkommen auf dem Ersti-Server fürs <:KIT:759041596460236822> . " +
            "Wähle per Klick auf ein Emoji unter der Nachricht deinen Studiengang um die Informationen des Discord-Servers für dich zu personalisieren :star_struck: .\n";
    private final String messageEnd = "\n" + "Dein Studiengang fehlt? Schreibe einem Moderator <@&757718320526000138> :100:";
    public static final long messageId = 759043590432882798L;
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
                || role.getIdLong() == 757718320526000138L)) {
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
                ArrayList<String> emotes = new ArrayList<>();

                StringBuilder msg = new StringBuilder();
                for (Document doc : collection.find()) {
                    emotes.add(doc.getString("emote"));
                    msg.append(doc.getString("emote")).append(" - ").append(doc.getString("name")).append("\n");
                }

                TextChannel textChannel = Objects.requireNonNull(DiscordController.getJDA().getTextChannelById(channelId));
                textChannel.retrieveMessageById(messageId).queue(message ->
                        message.editMessage(messageStart + msg.toString() + messageEnd).queue());

                ArrayList<Message> messageList = new ArrayList<>();
                textChannel.retrievePinnedMessages().queue(messages -> {
                    messageList.addAll(messages);
                    addReactionsToMessage(emotes, messageList, textChannel);
                });

                e.getChannel().sendMessage(EmbedUtil.createSuccessEmbed()
                        .addField("Nachricht wird aktualisiert", "Die Nachricht wird jetzt aktualisiert",
                                false).build()).queue();
        }
    }

    @SuppressWarnings("unchecked")
    private void addReactionsToMessage(ArrayList<String> emotes, ArrayList<Message> messages, TextChannel textChannel) {
        HashMap<Message, List<MessageReaction>> reactions = new HashMap<>();
        HashMap<Message, Integer> reactionSize = new HashMap<>();
        for (Message message : messages) {
            List<MessageReaction> list = retrieveMessageReaction(message);
            reactions.put(message, list);
            reactionSize.put(message, list.size());
        }

        for (String emote : ((ArrayList<String>) emotes.clone())) {
            if (messages.stream().noneMatch(message -> reactions.get(message).stream()
                    .anyMatch(reaction -> reaction.getReactionEmote().getEmoji().equals(emote)))) {
                for (Message message : (ArrayList<Message>) messages.clone()) {
                    if (reactionSize.get(message) < 20) {
                        message.addReaction(emote).queue();
                        emotes.remove(emote);
                        reactionSize.put(message, reactionSize.get(message) + 1);
                        break;
                    } else {
                        messages.remove(message);
                    }
                }
            } else {
                emotes.remove(emote);
            }
        }

        if (emotes.size() > 0) {
            textChannel.sendMessage("-").queue(message -> {
                message.pin().queue();
                messages.add(message);
                addReactionsToMessage(emotes, messages, textChannel);
            });
        }
    }

    private List<MessageReaction> retrieveMessageReaction(Message message) {
        AtomicReference<List<MessageReaction>> list = new AtomicReference<>();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        message.getTextChannel().retrieveMessageById(message.getId()).queue(msg -> {
            list.set(msg.getReactions());
            countDownLatch.countDown();
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException ignored) {
        }
        return list.get();
    }

}
