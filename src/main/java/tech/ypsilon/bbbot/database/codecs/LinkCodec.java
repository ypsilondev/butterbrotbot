package tech.ypsilon.bbbot.database.codecs;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.entities.User;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;
import tech.ypsilon.bbbot.database.MongoController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LinkCodec implements Codec<LinkCodec> {

    public static final LinkCodec EMPTY_HOLDER = new LinkCodec(null, null, null, null, null);

    private final ObjectId _id;
    private final String name;
    private final List<String> keywords;
    private final Long userId;
    private final String link;

    public static LinkCodec createLink(String name, User user, String link, List<String> keywords) {
        LinkCodec linkCodec = new LinkCodec(new ObjectId(), name, keywords, user.getIdLong(), link);
        getCollection().insertOne(linkCodec);
        return linkCodec;
    }

    public static FindIterable<LinkCodec> getAllLinks() {
        return getCollection().find();
    }

    public static boolean isPresent(String name, String link) {
        return getCollection().countDocuments(Filters.or(Filters.eq("name", name), Filters.eq("link", link))) > 0;
    }

    public static FindIterable<LinkCodec> getLinksForName(String regEx) {
        return getCollection().find(Filters.regex("name", regEx));
    }

    public static FindIterable<LinkCodec> getLinksFromUser(User user) {
        return getCollection().find(Filters.eq("userId", user.getIdLong()));
    }

    public static FindIterable<LinkCodec> getLinksFromKeyword(String keyword) {
        return getCollection().find(Filters.in("keywords", keyword));
    }

    public static LinkCodec getLinkWithId(ObjectId _id) {
        return getCollection().find(Filters.eq("_id", _id)).first();
    }

    private LinkCodec(ObjectId _id, String name, List<String> keywords, Long userId, String link) {
        this.name = name;
        this._id = _id;
        this.keywords = keywords;
        this.userId = userId;
        this.link = link;
    }

    private static MongoCollection<LinkCodec> getCollection() {
        return MongoController.getInstance().getCollection("links", LinkCodec.class);
    }

    public ObjectId getId() {
        return _id;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public Long getUserId() {
        return userId;
    }

    public String getLink() {
        return link;
    }

    public String getName() {
        return name;
    }

    @Override
    public LinkCodec decode(BsonReader reader, DecoderContext decoderContext) {
        reader.readStartDocument();
        ObjectId _id = reader.readObjectId("_id");
        String name = reader.readString("name");

        reader.readName("keywords");
        reader.readStartArray();
        List<String> keywords = new ArrayList<>();
        while(reader.readBsonType() != BsonType.END_OF_DOCUMENT)
            keywords.add(reader.readString());
        reader.readEndArray();

        Long userId = reader.readInt64("userId");
        String link = reader.readString("link");

        reader.readEndDocument();
        return new LinkCodec(_id, name, keywords, userId, link);
    }

    @Override
    public void encode(BsonWriter writer, LinkCodec data, EncoderContext encoderContext) {
        writer.writeStartDocument();

        writer.writeObjectId("_id", data._id);
        writer.writeString("name", data.name);
        writer.writeStartArray("keywords");
        for (String keyword : data.keywords)
            writer.writeString(keyword);
        writer.writeEndArray();
        writer.writeInt64("userId", data.userId);
        writer.writeString("link", data.link);

        writer.writeEndDocument();
    }

    @Override
    public Class<LinkCodec> getEncoderClass() {
        return LinkCodec.class;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinkCodec linkCodec = (LinkCodec) o;
        return _id.equals(linkCodec._id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_id);
    }
}
