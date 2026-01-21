package goatHeaven.highLog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String OTP_PREFIX = "otp:";
    private static final String VERIFIED_PREFIX = "verified:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

    // OTP 관련
    public void saveOtp(String email, String otp, long ttlSeconds) {
        redisTemplate.opsForValue().set(OTP_PREFIX + email, otp, ttlSeconds, TimeUnit.SECONDS);
    }

    public String getOtp(String email) {
        return redisTemplate.opsForValue().get(OTP_PREFIX + email);
    }

    public void deleteOtp(String email) {
        redisTemplate.delete(OTP_PREFIX + email);
    }

    // 이메일 인증 완료 상태
    public void setEmailVerified(String email, long ttlSeconds) {
        redisTemplate.opsForValue().set(VERIFIED_PREFIX + email, "true", ttlSeconds, TimeUnit.SECONDS);
    }

    public boolean isEmailVerified(String email) {
        return "true".equals(redisTemplate.opsForValue().get(VERIFIED_PREFIX + email));
    }

    public void deleteEmailVerified(String email) {
        redisTemplate.delete(VERIFIED_PREFIX + email);
    }

    // Refresh Token 관련
    public void saveRefreshToken(Long userId, String refreshToken, long ttlSeconds) {
        redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + userId, refreshToken, ttlSeconds, TimeUnit.SECONDS);
    }

    public String getRefreshToken(Long userId) {
        return redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
    }

    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }

    public boolean validateRefreshToken(Long userId, String refreshToken) {
        String storedToken = getRefreshToken(userId);
        return storedToken != null && storedToken.equals(refreshToken);
    }
}
