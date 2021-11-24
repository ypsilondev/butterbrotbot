package tech.ypsilon.bbbot.database.codecs;

import com.mongodb.client.FindIterable;
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
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ypsilon.bbbot.database.MongoController;
import tech.ypsilon.bbbot.discord.DiscordController;

import java.util.ArrayList;
import java.util.List;

public class DirectoryCodec implements Codec<DirectoryCodec> {

    public static final DirectoryCodec EMPTY_CODEC = new DirectoryCodec(null, null, null, false, null);

    private final ObjectId _id;
    private final String name;
    private final Long userId;
    private final boolean shared;
    private final List<ObjectId> links;

    public static DirectoryCodec addDirectory(User user, String name, List<LinkCodec> codecs) {
        if(getCollection().countDocuments(Filters.or(
                Filters.and(Filters.eq("name", name), Filters.eq("userId", user.getIdLong())),
                Filters.and(Filters.eq("shared", true), Filters.eq("name", name)))
        ) > 0)
            return null;

        List<ObjectId> codecsList = new ArrayList<>();
        for (LinkCodec codec : codecs)
            codecsList.add(codec.getId());
        DirectoryCodec directoryCodec = new DirectoryCodec(new ObjectId(), name, user.getIdLong(), false, codecsList);
        getCollection().insertOne(directoryCodec);
        return directoryCodec;
    }

    private static MongoCollection<DirectoryCodec> getCollection() {
        return MongoController.getInstance().getCollection("directory", DirectoryCodec.class);
    }

    public static DirectoryCodec getDirectory(ObjectId id) {
        return getCollection().find(Filters.eq("_id", id)).first();
    }

    public static FindIterable<DirectoryCodec> getDirectories(User user, boolean withShared) {
        List<Bson> filters = new ArrayList<>();
        filters.add(Filters.eq("userId", user.getIdLong()));
        if(withShared) {
            filters.add(Filters.eq("shared", true));
        }
        return getCollection().find(Filters.or(filters));
    }

    public static DirectoryCodec getDirectory(User user, String name) {
        return getCollection().find(Filters.or(
                Filters.and(Filters.eq("userId", user.getIdLong()), Filters.eq("name", name)),
                Filters.and(Filters.eq("shared", true), Filters.eq("name", name)))
        ).first();
    }

    public void addLink(LinkCodec link) {
        getCollection().updateOne(Filters.eq("_id", _id), Updates.addToSet("links", link.getId()));
    }

    public void removeLink(LinkCodec link) {
        getCollection().updateOne(Filters.eq("_id", _id), Updates.pull("links", link.getId()));
    }

    public boolean setShared(boolean shared) {
        if(shared) {
            if(getCollection().countDocuments(Filters.and(Filters.eq("shared", true), Filters.eq("name", name))) > 0)
                return false;
        }
        getCollection().updateOne(Filters.eq("_id", _id), Updates.set("shared", shared));
        return true;
    }

    public ObjectId getId() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public Long getUserId() {
        return userId;
    }

    public User getUser() {
        return DiscordController.getJDAStatic().getUserById(userId);
    }

    public boolean isShared() {
        return shared;
    }

    public List<ObjectId> getLinksRaw() {
        return links;
    }

    public FindIterable<LinkCodec> getLinks() {
        //List<LinkCodec> links = new ArrayList<>();
        //for (ObjectId link : this.links)
        //    links.add(LinkCodec.getLinkWithId(link));
        //return links;
        return LinkCodec.getLinksWithIdsBulk(links);
    }

    private DirectoryCodec(ObjectId _id, String name, Long userId, boolean shared, List<ObjectId> links) {
        this._id = _id;
        this.name = name;
        this.userId = userId;
        this.shared = shared;
        this.links = links;
    }

    @Override
    public DirectoryCodec decode(BsonReader reader, DecoderContext decoderContext) {
        reader.readStartDocument();
        ObjectId id = reader.readObjectId("_id");
        String name = reader.readString("name");
        long userId = reader.readInt64("userId");
        boolean shared = reader.readBoolean("shared");

        reader.readName("links");
        reader.readStartArray();
        List<ObjectId> links = new ArrayList<>();
        while(reader.readBsonType() != BsonType.END_OF_DOCUMENT)
            links.add(reader.readObjectId());
        reader.readEndArray();
        reader.readEndDocument();

        return new DirectoryCodec(id, name, userId, shared, links);
    }

    @Override
    public void encode(BsonWriter writer, DirectoryCodec data, EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeObjectId("_id", data._id);
        writer.writeString("name", data.name);
        writer.writeInt64("userId", data.userId);
        writer.writeBoolean("shared", data.shared);

        writer.writeStartArray("links");
        for (ObjectId link : data.links)
            writer.writeObjectId(link);
        writer.writeEndArray();

        writer.writeEndDocument();
    }

    @Override
    public Class<DirectoryCodec> getEncoderClass() {
        return DirectoryCodec.class;
    }
}
