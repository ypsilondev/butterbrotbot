package tech.ypsilon.bbbot.database.codecs;

import com.mongodb.client.MongoCollection;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
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

    public static final String FIELD_ID = "_id";
    public static final String FIELD_USER_ID = "userId";
    public static final String FIELD_STUDENT_CODE = "studentCode";
    public static final String FIELD_VERIFICATION_CODE = "verificationCode";
    public static final String FIELD_VERIFIED = "verified";
    public static final String FIELD_EMAIL_LAST_SENT = "emailLastSent";

    private static final String MONGO_COLLECTION = "Verification";
    private static final int VERIFICATION_CODE_LENGTH = 3;

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

    public static void save(VerificationDocument verificationDocument) {
        // delete object in order to save as is
        getCollection().deleteMany(new Document(FIELD_ID, verificationDocument.get_id()));
        // delete all other instances with same u-code
        getCollection().deleteMany(new Document(FIELD_STUDENT_CODE, verificationDocument.getStudentCode()));
        // insert into database
        getCollection().insertOne(verificationDocument);
    }

    public static MongoCollection<VerificationDocument> getCollection() {
        return MongoController.getInstance().getCollection(MONGO_COLLECTION, VerificationDocument.class);
    }

    @Override
    public VerificationDocument decode(BsonReader bsonReader, DecoderContext decoderContext) {
        bsonReader.readStartDocument();
        ObjectId _id = bsonReader.readObjectId(FIELD_ID);
        Long userId = bsonReader.readInt64(FIELD_USER_ID);
        String studentCode = bsonReader.readString(FIELD_STUDENT_CODE);
        String verificationCode = bsonReader.readString(FIELD_VERIFICATION_CODE);
        Boolean verified = bsonReader.readBoolean(FIELD_VERIFIED);
        Date emailLastSent = new Date(bsonReader.readDateTime(FIELD_EMAIL_LAST_SENT));
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
