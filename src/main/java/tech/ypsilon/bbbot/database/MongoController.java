package tech.ypsilon.bbbot.database;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import tech.ypsilon.bbbot.BotInfo;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.config.MongoSubconfig;
import tech.ypsilon.bbbot.database.codecs.*;
import tech.ypsilon.bbbot.util.GenericController;
import tech.ypsilon.bbbot.util.Initializable;

import java.util.Collections;

public class MongoController extends GenericController implements Initializable {

    private static MongoController instance;

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    private @Getter boolean disabled = false;

    /**
     * Initializes the MongoController.
     * Fetches all the necessary settings from the {@link tech.ypsilon.bbbot.config.ButterbrotConfig}
     * and registers the Codecs inside the codecs package
     */
    public MongoController(ButterBrot parent) {
        super(parent);
        instance = this;
    }

    @Override
    public void init() {
        MongoCredential credential = null;
        MongoSubconfig config = getParent().getConfig().getMongo();

        if (config.getUsername() != null) {
            // System.out.println("not null");
            credential = MongoCredential.createCredential(
                    config.getUsername(),
                    config.getAuthDatabase(),
                    config.getPassword().toCharArray()
            );
        }

        CodecRegistry extraCodecs = CodecRegistries.fromCodecs(
                LinkCodec.EMPTY_CODEC,
                DirectoryCodec.EMPTY_CODEC,
                StudyGroupCodec.EMPTY_CODEC,
                BirthdayCodec.EMPTY_CODEC,
                new VerificationCodec()
        );
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry()
                , extraCodecs);

        MongoClientSettings.Builder builder = MongoClientSettings.builder()
                .applyToClusterSettings(b -> b.hosts(Collections.singletonList(
                        new ServerAddress(
                                config.getHost(),
                                config.getPort()
                        )
                )))
                .codecRegistry(codecRegistry);
        if (credential != null) builder.credential(credential);
        MongoClientSettings settings = builder.build();

        mongoClient = MongoClients.create(settings);
        this.mongoDatabase = mongoClient.getDatabase(config.getButterbrotDatabase());
    }

    public void disable() {
        this.disabled = true;
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
        return mongoClient;
    }

    /**
     * Returns the database that is used to store all the
     * collections that are necessary for the bot to function.
     *<br/>
     * Name of the Database <b>WAS</b> ButterBrot {@link BotInfo#NAME}
     * Now it is as configured in {@link MongoSubconfig}
     *
     * @return the project database
     */
    public MongoDatabase getDatabase() {
        return mongoDatabase;
    }

    /**
     * Return a collection inside the database for the project
     * @param name the name from the collection
     * @param aClass the class type
     * @param <T> the Datatype
     * @return a collection with the datatype T
     */
    public <T> MongoCollection<T> getCollection(String name, Class<T> aClass) {
        return mongoDatabase.getCollection(name, aClass);
    }

    /**
     * Return a collection inside the database for the project with type Document(BSON)
     * @param name the name from the collection
     * @return a collection with the name and type Document(BSON)
     */
    public MongoCollection<Document> getCollection(String name) {
        return mongoDatabase.getCollection(name, Document.class);
    }

    /**
     * Get the Host
     * @return the hostname as a String
     */
    public String getHost() {
        return getParent().getConfig().getMongo().getHost();
    }

    /**
     * Get the Port
     * @return the port as a Integer
     */
    public int getPort() {
        return getParent().getConfig().getMongo().getPort();
    }

}
