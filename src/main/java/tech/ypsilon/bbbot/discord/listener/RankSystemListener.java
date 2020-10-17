package tech.ypsilon.bbbot.discord.listener;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import tech.ypsilon.bbbot.database.MongoController;
import tech.ypsilon.bbbot.discord.DiscordController;

import java.util.ArrayList;
import java.util.Date;
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
    private static MongoCollection<Document> getCollection() {
        return MongoController.getInstance().getCollection("UserActivity");
    }

    /**
     * Returns milliseconds for a given amount of days
     * @param days the amount of days
     * @return the millis they take
     */
    private Long getMillisForDays(int days) {
        return TimeUnit.DAYS.toMillis(days);
    }

    /**
     * Method to calculate points to add
     * @param streak the currentStreak
     * @return the points to add
     */
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
            user.put("userId", event.getAuthor().getIdLong());
            user.put("lastMessage", new Date());
            user.put("points", 0);
            user.put("currentStreak", 0);
            user.put("bestStreak", 1);
            getCollection().insertOne(user);
            filer = Filters.eq("userId", event.getAuthor().getIdLong());
            newCreate = true;
        }
        Date lastMsgUser = user.getDate("lastMessage");
        if(lastMsgUser.getTime()+getMillisForDays(1) > System.currentTimeMillis()) { //Skip if database hits the user last message
            lastMessage.put(event.getAuthor().getIdLong(), lastMsgUser.getTime());    //Refreshed the local cache if cache didnt hit but database does
            return;
        }

        Integer bestStreak = user.getInteger("bestStreak");
        Integer currentStreak = user.getInteger("currentStreak");

        List<Bson> updates = new ArrayList<>();
        if(lastMsgUser.getTime()+getMillisForDays(2) < System.currentTimeMillis() && !newCreate) {  //Lost Streak; No message for more than 48h
            if(currentStreak > bestStreak) {    //CurrentStreak higher than bestStreak
                updates.add(Updates.set("bestStreak", (currentStreak)));    //Set bestStreak = currentStreak
            }
            updates.add(Updates.set("currentStreak", 1));
            currentStreak = 1;
        } else {
            updates.add(Updates.inc("currentStreak", 1));   //increase currentStreak
        }
        updates.add(Updates.inc("points", calculatePoints(currentStreak)));
        lastMessage.put(event.getAuthor().getIdLong(), System.currentTimeMillis());

        getCollection().updateOne(filer, Updates.combine(updates));
    }

    /**
     * Get information from User
     * @param user the {@link User}
     * @return the RankInformation
     */
    public static RankInformation getRankInformation(User user) {
        return new RankInformation(user.getIdLong());
    }

    /**
     * Databucket of RankInformation
     */
    public static class RankInformation {

        private ObjectId _id;
        private Long userId;
        private int points = 0;
        private int currentStreak = 0;
        private int bestStreak = 0;
        private Date lastMessage;

        private RankInformation(Long userId) {
            Document document = getCollection().find(Filters.eq("userId", userId)).first();
            this.userId = userId;
            if(document != null) {
                _id = document.getObjectId("_id");
                this.points = document.getInteger("points");
                this.currentStreak = document.getInteger("currentStreak");
                this.bestStreak = document.getInteger("bestStreak");
                this.lastMessage = document.getDate("lastMessage");
                if(this.currentStreak > this.bestStreak)
                    this.bestStreak = this.currentStreak;
            }
        }

        public ObjectId getID() {
            return _id;
        }

        public Long getUserId() {
            return userId;
        }

        public User getUser() {
            return DiscordController.getJDA().getUserById(this.userId);
        }

        public int getPoints() {
            return points;
        }

        public int getCurrentStreak() {
            return currentStreak;
        }

        public int getBestStreak() {
            return bestStreak;
        }

        public Date getLastMessage() {
            return lastMessage;
        }
    }

}
