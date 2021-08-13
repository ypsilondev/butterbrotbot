package tech.ypsilon.bbbot.database;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;

public class MongoSettings {

    /**
     * Get a Value from the database
     *
     * @param type  the {@link TYPE} from the value
     * @param guild the guildid or null if it is global
     * @return the value or null if not existent
     */
    public static Object getValue(TYPE type, @Nullable Long guild) {
        MongoCollection<Document> settings = MongoController.getInstance().getCollection("Settings");
        Document data = settings.find(Filters.and(Filters.eq("type", type.name()), Filters.eq("guild", guild != null ? guild : 0L))).first();
        return data != null ? data.get("value") : null;
    }

    /**
     * Store a value in the database
     *
     * @param type  the {@link TYPE} from the value
     * @param value the value that should be stored. Has to be stored as bson
     * @param guild the guild or null if global
     */
    public static void setValue(TYPE type, Object value, @Nullable Long guild) {
        MongoCollection<Document> settings = MongoController.getInstance().getCollection("Settings");
        Document data = settings.find(Filters.and(Filters.eq("type", type.name()), Filters.eq("guild", guild != null ? guild : 0L))).first();
        if (data == null) {
            Document document = new Document();
            document.put("type", type.name());
            document.put("value", value);
            document.put("guild", guild != null ? guild : 0L);
            settings.insertOne(document);
            return;
        }
        settings.updateOne(Filters.and(Filters.eq("type", type.name()), Filters.eq("guild", guild != null ? guild : 0L)), Updates.set("value", value));
    }

    /**
     * Get the values for all guilds
     *
     * @param type the type
     * @return returns a FindIterable from all guilds with a specified setting {@link TYPE}
     */
    public static FindIterable<Document> getValueForAllGuild(TYPE type) {
        MongoCollection<Document> settings = MongoController.getInstance().getCollection("Settings");
        return settings.find(Filters.eq("type", type.name()));
    }

    /**
     * Values that can be stored in the settings
     */
    public enum TYPE {
        BirthdayChannel
    }

}
