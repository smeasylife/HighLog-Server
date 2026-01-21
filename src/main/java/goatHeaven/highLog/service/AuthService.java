package goatHeaven.highLog.service;

import goatHeaven.highLog.domain.User;
import goatHeaven.highLog.dto.request.*;
import goatHeaven.highLog.dto.response.*;
import goatHeaven.highLog.exception.CustomException;
import goatHeaven.highLog.exception.ErrorCode;
import goatHeaven.highLog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final RedisService redisService;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static final long OTP_EXPIRATION_SECONDS = 180; // 3분
    private static final long EMAIL_VERIFIED_EXPIRATION_SECONDS = 600; // 10분

    // 비밀번호 정책: 8자 이상, 영문 대소문자, 숫자, 특수문자 포함
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    public EmailVerifyResponse sendOtp(EmailVerifyRequest request) {
        String email = request.getEmail();

        // 이메일 형식 검증 (@gmail.com 도메인)
        if (!isValidGoogleEmail(email)) {
            throw new CustomException(ErrorCode.INVALID_EMAIL_FORMAT);
        }

        // 이미 가입된 이메일인지 확인
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // OTP 생성 및 저장
        String otp = emailService.generateOtp();
        redisService.saveOtp(email, otp, OTP_EXPIRATION_SECONDS);

        // 이메일 발송
        emailService.sendOtpEmail(email, otp);

        return EmailVerifyResponse.of((int) OTP_EXPIRATION_SECONDS);
    }

    public EmailConfirmResponse confirmOtp(EmailConfirmRequest request) {
        String email = request.getEmail();
        String code = request.getCode();

        String storedOtp = redisService.getOtp(email);

        if (storedOtp == null || !storedOtp.equals(code)) {
            throw new CustomException(ErrorCode.INVALID_OTP);
        }

        // OTP 삭제 및 인증 완료 상태 저장
        redisService.deleteOtp(email);
        redisService.setEmailVerified(email, EMAIL_VERIFIED_EXPIRATION_SECONDS);

        return EmailConfirmResponse.success();
    }

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        String email = request.getEmail();

        // 이메일 인증 완료 여부 확인
        if (!redisService.isEmailVerified(email)) {
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        // 비밀번호 정책 확인
        if (!isValidPassword(request.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }


        // 사용자 생성
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .marketingAgreement(request.getMarketingAgreement())
                .build();

        User savedUser = userRepository.save(user);

        // 인증 완료 상태 삭제
        redisService.deleteEmailVerified(email);

        return SignupResponse.from(savedUser);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        // Refresh Token을 Redis에 저장
        redisService.saveRefreshToken(user.getId(), refreshToken,
                jwtService.getRefreshTokenExpiration() / 1000);

        return LoginResponse.of(accessToken, refreshToken, user);
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // 토큰 유효성 검증
        jwtService.validateToken(refreshToken);

        Long userId = jwtService.getUserIdFromToken(refreshToken);
        String email = jwtService.getEmailFromToken(refreshToken);

        // Redis에 저장된 토큰과 비교
        if (!redisService.validateRefreshToken(userId, refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // 새 토큰 발급
        String newAccessToken = jwtService.generateAccessToken(userId, email);
        String newRefreshToken = jwtService.generateRefreshToken(userId, email);

        // 새 Refresh Token 저장
        redisService.saveRefreshToken(userId, newRefreshToken,
                jwtService.getRefreshTokenExpiration() / 1000);

        return TokenResponse.of(newAccessToken, newRefreshToken);
    }

    public MessageResponse logout(Long userId, LogoutRequest request) {
        // 리프레시 토큰 검증
        if (!redisService.validateRefreshToken(userId, request.getRefreshToken())) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // Redis에서 Refresh Token 삭제
        redisService.deleteRefreshToken(userId);

        return MessageResponse.of("로그아웃되었습니다.");
    }

    private boolean isValidGoogleEmail(String email) {
        return email != null && !email.endsWith(".@gmail.com");
    }

    private boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

}
