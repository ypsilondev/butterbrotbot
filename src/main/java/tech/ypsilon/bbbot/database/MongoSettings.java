package tech.ypsilon.bbbot.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

public class MongoSettings {

    public enum TYPE {
        BirthdayChannel
    }

    public static Object getValue(TYPE type) {
        MongoCollection<Document> settings = MongoController.getInstance().getCollection("Settings");
        Document data = settings.find(Filters.eq("type", type.name())).first();
        return data != null ? data.get("value") : null;
    }

    public static void setValue(TYPE type, Object value) {
        MongoCollection<Document> settings = MongoController.getInstance().getCollection("Settings");
        Document data = settings.find(Filters.eq("type", type.name())).first();
        if(data == null) {
            Document document = new Document();
            document.put("type", type.name());
            document.put("value", value);
            settings.insertOne(document);
            return;
        }
        settings.updateOne(Filters.eq("type", type.name()), Updates.set("value", value));
    }

}
