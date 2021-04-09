package tech.ypsilon.bbbot.database.codecs;

import com.mongodb.client.MongoCollection;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;
import tech.ypsilon.bbbot.database.MongoController;
import tech.ypsilon.bbbot.database.structs.VerificationDocument;

import java.time.Instant;
import java.util.Date;
import java.util.Random;

/**
 * @author Christian Schliz
 * @version 1.0
 */
public class VerificationCodec implements Codec<VerificationDocument> {

    private static final String MONGO_COLLECTION = "Verification";
    private static final int VERIFICATION_CODE_LENGTH = 3;

    private static final String FIELD_ID = "_id";
    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_STUDENT_CODE = "studentCode";
    private static final String FIELD_VERIFICATION_CODE = "verificationCode";
    private static final String FIELD_VERIFIED = "verified";
    private static final String FIELD_EMAIL_LAST_SENT = "emailLastSent";

    private static String generateVerificationCode() {
        int number = new Random().nextInt((int) Math.pow(10, VERIFICATION_CODE_LENGTH));
        StringBuilder builder = new StringBuilder(number);
        while (builder.length() < VERIFICATION_CODE_LENGTH)
            builder.insert(0, "0");
        return builder.toString();
    }

    public static VerificationDocument insert(Long userId, String studentCode) {
        VerificationDocument verificationCodec = new VerificationDocument(new ObjectId(), userId, studentCode,
                generateVerificationCode(), false, Date.from(Instant.now()));

        getCollection().insertOne(verificationCodec);
        return verificationCodec;
    }

    private static MongoCollection<VerificationDocument> getCollection() {
        return MongoController.getInstance().getCollection(MONGO_COLLECTION, VerificationDocument.class);
    }

    @Override
    public VerificationDocument decode(BsonReader bsonReader, DecoderContext decoderContext) {
        bsonReader.readStartDocument();
        ObjectId _id = bsonReader.readObjectId("_id");
        Long userId = bsonReader.readInt64("userId");
        String studentCode = bsonReader.readString("studentCode");
        String verificationCode = bsonReader.readString("verificationCode");
        Boolean verified = bsonReader.readBoolean("verified");
        Date emailLastSent = new Date(bsonReader.readDateTime("emailLastSent"));
        bsonReader.readEndDocument();
        return new VerificationDocument(_id, userId, studentCode, verificationCode, verified, emailLastSent);
    }

    @Override
    public void encode(BsonWriter bsonWriter, VerificationDocument authCodec, EncoderContext encoderContext) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeObjectId(FIELD_ID, authCodec.get_id());
        bsonWriter.writeInt64(FIELD_USER_ID, authCodec.getUserId());
        bsonWriter.writeString(FIELD_STUDENT_CODE, authCodec.getStudentCode());
        bsonWriter.writeString(FIELD_VERIFICATION_CODE, authCodec.getVerificationCode());
        bsonWriter.writeBoolean(FIELD_VERIFIED, authCodec.getVerified());
        bsonWriter.writeDateTime(FIELD_EMAIL_LAST_SENT, authCodec.getEmailLastSent().getTime());
        bsonWriter.writeEndDocument();
    }

    @Override
    public Class<VerificationDocument> getEncoderClass() {
        return VerificationDocument.class;
    }
}
