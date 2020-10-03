package tech.ypsilon.bbbot.database.codecs;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.entities.User;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;
import tech.ypsilon.bbbot.database.MongoController;

import java.util.ArrayList;
import java.util.List;

/**
 * Codec for the StudyGroups
 * Holds all the information that is later on stored in the database
 */
public class StudyGroupCodec implements Codec<StudyGroupCodec> {

    public static final StudyGroupCodec EMPTY_CODEC = new StudyGroupCodec(null, null, null);

    private final ObjectId _id;
    private final String name;
    private final List<Long> users;

    /**
     * Retrieve a group with a given name
     * @param name the name from the group
     * @return the group or null if the name is not found
     */
    public static StudyGroupCodec retrieveStudyGroup(String name) {
        return getCollection().find(Filters.eq("name", name)).first();
    }

    /**
     * Retrieve the group from a user
     * @param user the user from JDA
     * @return the group or null if the user is in no group
     */
    public static StudyGroupCodec retrieveStudyGroup(User user) {
        long userId = user.getIdLong();
        return getCollection().find(Filters.in("users", userId)).first();
    }

    /**
     * Create a new group with a given name
     * @param name the name from the group
     * @param members the initial members that should be in the group
     * @return the group
     * @throws ArrayIndexOutOfBoundsException if the member array is empty
     * @throws UserInGroupException if one of the members is already in a group
     */
    public static StudyGroupCodec createGroup(String name, List<User> members) {
        StudyGroupCodec studyGroupCodec = retrieveStudyGroup(name);
        if(studyGroupCodec != null) {
            return null; //Group name already taken
        }
        if(members.size() == 0)
            throw new ArrayIndexOutOfBoundsException();
        if (members.stream().anyMatch(user -> retrieveStudyGroup(user) != null)) {
            throw new UserInGroupException();
        }
        List<Long> users = new ArrayList<>();
        members.forEach(user -> users.add(user.getIdLong()));
        studyGroupCodec = new StudyGroupCodec(new ObjectId(), name, users);
        getCollection().insertOne(studyGroupCodec);
        return studyGroupCodec;
    }

    /**
     * Adds a user to a given group
     * @param group The group the user should be added to
     * @param user The user that should be added
     * @return false if the user is already in a group
     * @deprecated Use {@link StudyGroupCodec#addToGroup(User)} inside an object instead
     */
    @Deprecated
    public static boolean addToGroup(StudyGroupCodec group, User user) {
        return group.addToGroup(user);
    }

    /**
     * Add a user to a given group
     * @param user the user that should be added
     * @return false if the user is in a group. true if not
     */
    public boolean addToGroup(User user) {
        if(retrieveStudyGroup(user) != null)    //Check if user is already in a group
            return false;                       //And return false if he is.
        this.users.add(user.getIdLong());
        return getCollection().updateOne(Filters.eq("_id", _id),
                Updates.addToSet("users", user.getIdLong())).wasAcknowledged();
    }

    /**
     * Internal method for creating objects
     * @param _id the ID
     * @param name the name
     * @param users the users
     */
    private StudyGroupCodec(ObjectId _id, String name, List<Long> users) {
        this._id = _id;
        this.name = name;
        this.users = users;
    }

    /**
     * Get the ID from the dataset
     * @return the ObjectID
     */
    public ObjectId getID() {
        return _id;
    }

    /**
     * Get the name from the dataset
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the Discord ids from the members
     * @return the List<Long> with the ids
     */
    public List<Long> getUserIDs() {
        return users;
    }

    @Override
    public StudyGroupCodec decode(BsonReader reader, DecoderContext decoderContext) {
        reader.readStartDocument();

        ObjectId _id = reader.readObjectId("_id");
        String name = reader.readString("name");
        reader.readName("users");
        reader.readStartArray();
        List<Long> users = new ArrayList<>();
        while(reader.readBsonType() != BsonType.END_OF_DOCUMENT)
            users.add(reader.readInt64());
        reader.readEndArray();

        reader.readEndDocument();
        return new StudyGroupCodec(_id, name, users);
    }

    @Override
    public void encode(BsonWriter writer, StudyGroupCodec data, EncoderContext encoderContext) {
        writer.writeStartDocument();

        writer.writeObjectId("_id", data._id);
        writer.writeString("name", data.name);
        writer.writeStartArray("users");
        for (Long userId : data.users)
            writer.writeInt64(userId);
        writer.writeEndArray();

        writer.writeEndDocument();
    }

    private static MongoCollection<StudyGroupCodec> getCollection() {
        return MongoController.getInstance().getCollection("studyGroups", StudyGroupCodec.class);
    }

    @Override
    public Class<StudyGroupCodec> getEncoderClass() {
        return StudyGroupCodec.class;
    }

    public static class UserInGroupException extends RuntimeException{

    }

}
