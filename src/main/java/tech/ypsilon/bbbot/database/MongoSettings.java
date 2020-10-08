package tech.ypsilon.bbbot.database;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;

public class MongoSettings {

    public enum TYPE {
        BirthdayChannel
    }

    public static Object getValue(TYPE type, @Nullable Long guild) {
        MongoCollection<Document> settings = MongoController.getInstance().getCollection("Settings");
        Document data = settings.find(Filters.and(Filters.eq("type", type.name()), Filters.eq("guild", guild != null ? guild : 0L))).first();
        return data != null ? data.get("value") : null;
    }

    public static void setValue(TYPE type, Object value, @Nullable Long guild) {
        MongoCollection<Document> settings = MongoController.getInstance().getCollection("Settings");
        Document data = settings.find(Filters.and(Filters.eq("type", type.name()), Filters.eq("guild", guild != null ? guild : 0L))).first();
        if(data == null) {
            Document document = new Document();
            document.put("type", type.name());
            document.put("value", value);
            document.put("guild", guild != null ? guild : 0L);
            settings.insertOne(document);
            return;
        }
        settings.updateOne(Filters.and(Filters.eq("type", type.name()), Filters.eq("guild", guild != null ? guild : 0L)), Updates.set("value", value));
    }

    public static FindIterable<Document> getValueForAllGuild(TYPE type) {
        MongoCollection<Document> settings = MongoController.getInstance().getCollection("Settings");
        return settings.find(Filters.eq("type", type.name()));
    }

}
