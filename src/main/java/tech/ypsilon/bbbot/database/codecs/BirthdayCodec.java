package tech.ypsilon.bbbot.database.codecs;

import com.mongodb.client.MongoCollection;
import net.dv8tion.jda.api.entities.User;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;
import tech.ypsilon.bbbot.database.MongoController;

import java.util.Calendar;
import java.util.Date;

public class BirthdayCodec implements Codec<BirthdayCodec> {

    public static final BirthdayCodec EMPTY_CODEC = new BirthdayCodec(null, null, null);

    private final ObjectId _id;
    private final Long userId;
    private final Date birthday;

    public BirthdayCodec(ObjectId _id, Long userId, Date birthday) {
        this._id = _id;
        this.userId = userId;
        this.birthday = birthday;
    }
    
    public BirthdayCodec fixTimeZone() {
    	birthday.setTime(birthday.getTime() + Calendar.getInstance().get(Calendar.DST_OFFSET) + Calendar.getInstance().get(Calendar.ZONE_OFFSET));
    	return this;
    }

    public static BirthdayCodec newBirthday(User user, Date birthday) {
        BirthdayCodec birthdayCodec = new BirthdayCodec(new ObjectId(), user.getIdLong(), birthday).fixTimeZone();
        getCollection().insertOne(birthdayCodec);
        return birthdayCodec;
    }

    public static BirthdayCodec newBirthday(Long userId, Date birthday) {
        BirthdayCodec birthdayCodec = new BirthdayCodec(new ObjectId(), userId, birthday).fixTimeZone();
        getCollection().deleteMany(new Document("userId", userId));
        getCollection().insertOne(birthdayCodec);
        return birthdayCodec;
    }

    private static MongoCollection<BirthdayCodec> getCollection() {
        return MongoController.getInstance().getCollection("Birthdays", BirthdayCodec.class);
    }

    @Override
    public BirthdayCodec decode(BsonReader reader, DecoderContext decoderContext) {
        reader.readStartDocument();
        ObjectId _id = reader.readObjectId("_id");
        Long userId = reader.readInt64("userId");
        Date birthday = new Date(reader.readDateTime("birthday"));
        reader.readEndDocument();
        return new BirthdayCodec(_id, userId, birthday);
    }

    @Override
    public void encode(BsonWriter writer, BirthdayCodec data, EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeObjectId("_id", data._id);
        writer.writeInt64("userId", data.userId);
        writer.writeDateTime("birthday", data.birthday.getTime());
        writer.writeEndDocument();
    }

    @Override
    public Class<BirthdayCodec> getEncoderClass() {
        return BirthdayCodec.class;
    }
}
