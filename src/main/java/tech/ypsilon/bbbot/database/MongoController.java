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
import tech.ypsilon.bbbot.database.codecs.BirthdayCodec;
import tech.ypsilon.bbbot.database.codecs.DirectoryCodec;
import tech.ypsilon.bbbot.database.codecs.LinkCodec;
import tech.ypsilon.bbbot.database.codecs.StudyGroupCodec;
import tech.ypsilon.bbbot.settings.SettingsController;

import java.util.Collections;

public class MongoController {

    private static MongoController instance;

    private final MongoClient CLIENT;
    private final MongoDatabase DATABASE;

    /**
     * Initializes the MongoController.
     * Fetches all the necessary settings from the {@link SettingsController}
     * and registers the Codecs inside the codecs package
     */
    public MongoController() {
        instance = this;
        MongoCredential credential = MongoCredential.createCredential(
                ((String) SettingsController.getValue("mongo.username")),
                ((String) SettingsController.getValue("mongo.authDatabase")),
                ((String) SettingsController.getValue("mongo.password")).toCharArray()
        );

        CodecRegistry extraCodecs = CodecRegistries.fromCodecs(
                LinkCodec.EMPTY_CODEC,
                DirectoryCodec.EMPTY_CODEC,
                StudyGroupCodec.EMPTY_CODEC,
                BirthdayCodec.EMPTY_CODEC
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

    /**
     * @return the current instance from the MongoController
     */
    public static MongoController getInstance() {
        return instance;
    }

    /**
     * Get the MongoClient database connection
     * @return the Client
     */
    public MongoClient getClient() {
        return CLIENT;
    }

    /**
     * Returns the database that is used to store all the collections that are necessary for
     * the bot to function.
     * Name of the Database is ButterBrot {@link BotInfo#NAME}
     * @return the project database
     */
    public MongoDatabase getDatabase() {
        return DATABASE;
    }

    /**
     * Return a collection inside the database for the project
     * @param name the name from the collection
     * @param aClass the class type
     * @param <T> the Datatype
     * @return a collection with the datatype T
     */
    public <T> MongoCollection<T> getCollection(String name, Class<T> aClass) {
        return DATABASE.getCollection(name, aClass);
    }

    /**
     * Return a collection inside the database for the project with type Document(BSON)
     * @param name the name from the collection
     * @return a collection with the name and type Document(BSON)
     */
    public MongoCollection<Document> getCollection(String name) {
        return DATABASE.getCollection(name, Document.class);
    }

    /**
     * Get the Host
     * @return the hostname as a String
     */
    public String getHost() {
        return (String) SettingsController.getValue("mongo.host");
    }

    /**
     * Get the Port
     * @return the port as a Integer
     */
    public int getPort() {
        return (int) SettingsController.getValue("mongo.port");
    }

}
