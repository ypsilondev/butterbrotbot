package tech.ypsilon.bbbot.database.codecs;

import com.mongodb.client.MongoCollection;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;
import tech.ypsilon.bbbot.database.MongoController;

import java.time.Instant;
import java.util.Date;
import java.util.Random;

/**
 * @author Christian Schliz
 * @version 1.0
 */
public class VerificationCodec implements Codec<VerificationCodec> {

    private static final String MONGO_COLLECTION = "Verification";
    private static final int VERIFICATION_CODE_LENGTH = 3;

    private final ObjectId _id;
    private final Long userId;
    private final String studentCode;
    private final String verificationCode;
    private final Boolean verified;
    private final Date emailLastSent;

    public VerificationCodec(ObjectId id, Long userId, String studentCode,
                             String verificationCode, Boolean verified, Date emailLastSent) {
        _id = id;
        this.userId = userId;
        this.studentCode = studentCode;
        this.verificationCode = verificationCode;
        this.verified = verified;
        this.emailLastSent = emailLastSent;
    }

    private static String generateVerificationCode() {
        int number = new Random().nextInt(1000);
        StringBuilder builder = new StringBuilder(number);
        while (builder.length() < VERIFICATION_CODE_LENGTH)
            builder.insert(0, "0");
        return builder.toString();
    }

    public static VerificationCodec insert(Long userId, String studentCode) {
        VerificationCodec verificationCodec = new VerificationCodec(new ObjectId(), userId, studentCode,
                generateVerificationCode(), false, Date.from(Instant.now()));

        getCollection().insertOne(verificationCodec);
        return verificationCodec;
    }

    private static MongoCollection<VerificationCodec> getCollection() {
        return MongoController.getInstance().getCollection(MONGO_COLLECTION, VerificationCodec.class);
    }

    @Override
    public VerificationCodec decode(BsonReader bsonReader, DecoderContext decoderContext) {
        bsonReader.readStartDocument();
        ObjectId _id = bsonReader.readObjectId("_id");
        Long userId = bsonReader.readInt64("userId");
        String studentCode = bsonReader.readString("studentCode");
        String verificationCode = bsonReader.readString("verificationCode");
        Boolean verified = bsonReader.readBoolean("verified");
        Date emailLastSent = new Date(bsonReader.readDateTime("emailLastSent"));
        bsonReader.readEndDocument();
        return new VerificationCodec(_id, userId, studentCode, verificationCode, verified, emailLastSent);
    }

    @Override
    public void encode(BsonWriter bsonWriter, VerificationCodec authCodec, EncoderContext encoderContext) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeObjectId("_id", authCodec._id);
        bsonWriter.writeInt64("userId", authCodec.userId);
        bsonWriter.writeString("studentCode", authCodec.studentCode);
        bsonWriter.writeString("verificationCode", authCodec.verificationCode);
        bsonWriter.writeBoolean("verified", authCodec.verified);
        bsonWriter.writeDateTime("emailLastSent", authCodec.emailLastSent.getTime());
        bsonWriter.writeEndDocument();
    }

    @Override
    public Class<VerificationCodec> getEncoderClass() {
        return VerificationCodec.class;
    }
}
