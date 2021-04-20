package tech.ypsilon.bbbot.database.structs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.Date;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class VerificationDocument {
    private final ObjectId _id;
    private final Long userId;
    private String studentCode;
    private @Setter String verificationCode;
    private @Setter Boolean verified;
    private @Setter Date emailLastSent;
}
