package goatHeaven.highLog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
import java.time.Duration;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${SPRING_CLOUD_AWS_CREDENTIALS_ACCESS_KEY}")
    private String accessKey;

    @Value("${SPRING_CLOUD_AWS_CREDENTIALS_SECRET_KEY}")
    private String secretKey;

    @Value("${SPRING_CLOUD_AWS_REGION_STATIC}")
    private String region;

    @Value("${s3.endpoint}")  // S3 Compatible endpoint (Oracle Object Storage 등)
    private String endpoint;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        // S3 Compatible API 지원 (Oracle Object Storage, MinIO 등)
        if (endpoint != null && !endpoint.isEmpty()) {
            return S3Client.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .region(Region.of(region))
                    .endpointOverride(URI.create(endpoint))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .chunkedEncodingEnabled(false)  // Oracle Object Storage 필수!
                            .build())
                    .build();
        } else {
            return S3Client.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .region(Region.of(region))
                    .build();
        }
    }

    @Bean
    public S3Presigner s3Presigner() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        // S3 Compatible API 지원 (Oracle Object Storage, MinIO 등)
        if (endpoint != null && !endpoint.isEmpty()) {
            return S3Presigner.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .region(Region.of(region))
                    .endpointOverride(URI.create(endpoint))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .chunkedEncodingEnabled(false)  // Oracle Object Storage 필수!
                            .build())
                    .build();
        } else {
            return S3Presigner.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .region(Region.of(region))
                    .build();
        }
    }
}
