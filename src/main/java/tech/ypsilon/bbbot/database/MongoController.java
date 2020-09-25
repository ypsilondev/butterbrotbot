package tech.ypsilon.bbbot.database;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import tech.ypsilon.bbbot.BotInfo;
import tech.ypsilon.bbbot.database.codecs.DirectoryCodec;
import tech.ypsilon.bbbot.database.codecs.LinkCodec;
import tech.ypsilon.bbbot.settings.SettingsController;

import java.util.Collections;

public class MongoController {

    private static MongoController instance;

    private final MongoClient CLIENT;
    private final MongoDatabase DATABASE;

    public MongoController() {
        instance = this;
        MongoCredential credential = MongoCredential.createCredential(
                ((String) SettingsController.getValue("mongo.username")),
                ((String) SettingsController.getValue("mongo.authDatabase")),
                ((String) SettingsController.getValue("mongo.password")).toCharArray()
        );

        CodecRegistry extraCodecs = CodecRegistries.fromCodecs(
                LinkCodec.EMPTY_CODEC,
                DirectoryCodec.EMPTY_CODEC
        );
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry()
                , extraCodecs);

        MongoClientSettings settings = MongoClientSettings.builder()
                .credential(credential)
                .applyToClusterSettings(builder -> builder.hosts(Collections.singletonList(
                        new ServerAddress(
                                ((String) SettingsController.getValue("mongo.host")),
                                ((int) SettingsController.getValue("mongo.port"))
                        )
                )))
                .codecRegistry(codecRegistry)
                .build();


        CLIENT = MongoClients.create(settings);
        this.DATABASE = CLIENT.getDatabase(BotInfo.NAME);
    }

    public static MongoController getInstance() {
        return instance;
    }

    public MongoClient getClient() {
        return CLIENT;
    }

    public MongoDatabase getDatabase() {
        return DATABASE;
    }

    public <T> MongoCollection<T> getCollection(String name, Class<T> aClass) {
        return DATABASE.getCollection(name, aClass);
    }

    public MongoCollection<Document> getCollection(String name) {
        return DATABASE.getCollection(name, Document.class);
    }

    public String getHost() {
        return (String) SettingsController.getValue("mongo.host");
    }

    public int getPort() {
        return (int) SettingsController.getValue("mongo.port");
    }

}
