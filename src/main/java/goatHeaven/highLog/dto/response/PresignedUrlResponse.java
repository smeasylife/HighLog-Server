package goatHeaven.highLog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PresignedUrlResponse {
    private final String presignedUrl;
    private final String s3Key;
    private final Long expiresIn;
}
