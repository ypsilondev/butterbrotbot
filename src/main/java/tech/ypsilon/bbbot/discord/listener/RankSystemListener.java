package tech.ypsilon.bbbot.discord.listener;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import tech.ypsilon.bbbot.database.MongoController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RankSystemListener extends ListenerAdapter {

    //Local cache for speed
    private HashMap<Long, Long> lastMessage = new HashMap<>();

    /**
     * Get the corresponding Collection
     * @return the collection
     */
    private MongoCollection<Document> getCollection() {
        return MongoController.getInstance().getCollection("UserActivity");
    }

    private Long getMillisForDays(int days) {
        return TimeUnit.DAYS.toMillis(days);
    }

    private int calculatePoints(int streak) {
        return Math.min(streak, 5);
    }

    /**
     * Execute the event
     * @param event
     */
    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if(event.getGuild().getIdLong() != 756547960229199902L) return; //Skip if not Kit Guild

        if(lastMessage.getOrDefault(event.getAuthor().getIdLong(), 0L) + getMillisForDays(1) > System.currentTimeMillis())
            return; //Skip if cache hits the user last message
        Bson filer = Filters.eq("userId", event.getAuthor().getIdLong());
        Document user = getCollection().find(filer).first();
        boolean newCreate = false;
        if(user == null) {
            user = new Document("_id", new ObjectId());
            user.put("lastMessage", 0L);
            user.put("points", 0);
            user.put("currentStreak", 0);
            user.put("bestStreak", 0);
            getCollection().insertOne(user);
            newCreate = true;
        }
        Long lastMsgUser = user.getLong("lastMessage");
        if(lastMsgUser+getMillisForDays(1) > System.currentTimeMillis()) { //Skip if database hits the user last message
            lastMessage.put(event.getAuthor().getIdLong(), lastMsgUser);    //Refreshed the local cache if cache didnt hit but database does
            return;
        }

        Integer bestStreak = user.getInteger("bestStreak");
        Integer currentStreak = user.getInteger("currentStreak");

        List<Bson> updates = new ArrayList<>();
        if(lastMsgUser+getMillisForDays(2) < System.currentTimeMillis() && !newCreate) {  //Lost Streak; No message for more than 48h
            if(currentStreak > bestStreak) {    //CurrentStreak higher than bestStreak
                updates.add(Updates.set("bestStreak", (currentStreak)));    //Set bestStreak = currentStreak
                bestStreak = currentStreak;
            }
            updates.add(Updates.set("currentStreak", 1));
            currentStreak = 1;
        }
        updates.add(Updates.inc("points", calculatePoints(currentStreak)));
        getCollection().updateOne(filer, updates);
    }

}
