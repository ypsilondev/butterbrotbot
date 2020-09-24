package tech.ypsilon.bbbot.database.codecs;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.entities.User;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;
import tech.ypsilon.bbbot.database.MongoController;

import java.util.List;
import java.util.Objects;

public class LinkCodec implements Codec<LinkCodec> {

    public static final LinkCodec EMPTY_HOLDER = new LinkCodec(null, null, null, null);

    private final ObjectId _id;
    private final List<String> keywords;
    private final Long userId;
    private final String link;

    public static LinkCodec createLink(User user, String link, List<String> keywords) {
        LinkCodec linkCodec = new LinkCodec(new ObjectId(), keywords, user.getIdLong(), link);
        getCollection().insertOne(linkCodec);
        return linkCodec;
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

    private LinkCodec(ObjectId _id, List<String> keywords, Long userId, String link) {
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

    @Override
    public LinkCodec decode(BsonReader bsonReader, DecoderContext decoderContext) {
        return null;
    }

    @Override
    public void encode(BsonWriter bsonWriter, LinkCodec linkCodec, EncoderContext encoderContext) {

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
