package tech.ypsilon.bbbot.database.structs;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;

import java.util.Date;

@Data
@RequiredArgsConstructor
public class VerificationDocument {
    private final ObjectId _id;
    private final Long userId;
    private final String studentCode;
    private final String verificationCode;
    private final Boolean verified;
    private final Date emailLastSent;
}
