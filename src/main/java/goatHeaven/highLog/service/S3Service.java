package goatHeaven.highLog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import goatHeaven.highLog.dto.response.PresignedUrlResponse;
import goatHeaven.highLog.exception.CustomException;
import goatHeaven.highLog.exception.ErrorCode;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    private static final Duration PRESIGNED_URL_EXPIRATION = Duration.ofMinutes(5); // 5분

    @Transactional(readOnly = true)
    public PresignedUrlResponse generatePresignedUrl(String fileName, Long userId) {
        // 파일 확장자 검증 (PDF만 허용)
        validateFileExtension(fileName);

        String s3Key = generateS3Key(userId, fileName);

        try {
            // Presigned URL 생성
            String presignedUrl = s3Presigner.presignPutObject(b -> b
                    .signatureDuration(PRESIGNED_URL_EXPIRATION)
                    .putObjectRequest(builder -> builder
                            .bucket(bucket)
                            .key(s3Key)
                            .contentType("application/pdf")
                            .build())
            ).url().toString();

            log.info("Generated presigned URL for user: {}, s3Key: {}", userId, s3Key);

            return new PresignedUrlResponse(
                    presignedUrl,
                    s3Key,
                    PRESIGNED_URL_EXPIRATION.toSeconds()
            );

        } catch (Exception e) {
            log.error("Failed to generate presigned URL for user: {}, fileName: {}", userId, fileName, e);
            throw new CustomException(ErrorCode.S3_PRESIGNED_URL_GENERATION_FAILED);
        }
    }

    private void validateFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_FILE_NAME);
        }

        String lowerCaseFileName = fileName.toLowerCase();
        if (!lowerCaseFileName.endsWith(".pdf")) {
            throw new CustomException(ErrorCode.INVALID_FILE_EXTENSION);
        }
    }

    private String generateS3Key(Long userId, String fileName) {
        String uuid = UUID.randomUUID().toString();
        return String.format("users/%d/records/%s_%s", userId, uuid, fileName);
    }


    @Transactional
    public void deleteFile(String s3Key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Deleted file from S3: {}", s3Key);
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", s3Key, e);
            throw new CustomException(ErrorCode.S3_FILE_DELETE_FAILED);
        }
    }
}
