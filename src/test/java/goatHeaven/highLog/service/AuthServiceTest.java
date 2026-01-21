package goatHeaven.highLog.service;

import goatHeaven.highLog.domain.User;
import goatHeaven.highLog.dto.request.*;
import goatHeaven.highLog.dto.response.*;
import goatHeaven.highLog.exception.CustomException;
import goatHeaven.highLog.exception.ErrorCode;
import goatHeaven.highLog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private static final String TEST_EMAIL = "test@gmail.com";
    private static final String TEST_PASSWORD = "Test1234!@";
    private static final String TEST_NAME = "테스트";
    private static final String TEST_OTP = "123456";
    private static final String TEST_ACCESS_TOKEN = "access-token";
    private static final String TEST_REFRESH_TOKEN = "refresh-token";

    @Nested
    @DisplayName("sendOtp 메서드")
    class SendOtpTest {

        @Test
        @DisplayName("성공 - 유효한 이메일로 OTP 발송")
        void sendOtp_Success() {
            // given
            EmailVerifyRequest request = createEmailVerifyRequest(TEST_EMAIL);
            given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
            given(emailService.generateOtp()).willReturn(TEST_OTP);

            // when
            EmailVerifyResponse response = authService.sendOtp(request);

            // then
            assertThat(response.getExpiresIn()).isEqualTo(180);
            verify(redisService).saveOtp(eq(TEST_EMAIL), eq(TEST_OTP), eq(180L));
            verify(emailService).sendOtpEmail(TEST_EMAIL, TEST_OTP);
        }

        @Test
        @DisplayName("실패 - 잘못된 이메일 형식")
        void sendOtp_InvalidEmailFormat() {
            // given
            EmailVerifyRequest request = createEmailVerifyRequest("invalid.@gmail.com");

            // when & then
            assertThatThrownBy(() -> authService.sendOtp(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_EMAIL_FORMAT));
        }

        @Test
        @DisplayName("실패 - 이미 가입된 이메일")
        void sendOtp_EmailAlreadyExists() {
            // given
            EmailVerifyRequest request = createEmailVerifyRequest(TEST_EMAIL);
            given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.sendOtp(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS));
        }
    }

    @Nested
    @DisplayName("confirmOtp 메서드")
    class ConfirmOtpTest {

        @Test
        @DisplayName("성공 - 유효한 OTP 확인")
        void confirmOtp_Success() {
            // given
            EmailConfirmRequest request = createEmailConfirmRequest(TEST_EMAIL, TEST_OTP);
            given(redisService.getOtp(TEST_EMAIL)).willReturn(TEST_OTP);

            // when
            EmailConfirmResponse response = authService.confirmOtp(request);

            // then
            assertThat(response.isVerified()).isTrue();
            verify(redisService).deleteOtp(TEST_EMAIL);
            verify(redisService).setEmailVerified(eq(TEST_EMAIL), eq(600L));
        }

        @Test
        @DisplayName("실패 - OTP가 일치하지 않음")
        void confirmOtp_InvalidOtp() {
            // given
            EmailConfirmRequest request = createEmailConfirmRequest(TEST_EMAIL, "wrong-otp");
            given(redisService.getOtp(TEST_EMAIL)).willReturn(TEST_OTP);

            // when & then
            assertThatThrownBy(() -> authService.confirmOtp(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_OTP));
        }

        @Test
        @DisplayName("실패 - OTP가 만료됨 (null)")
        void confirmOtp_ExpiredOtp() {
            // given
            EmailConfirmRequest request = createEmailConfirmRequest(TEST_EMAIL, TEST_OTP);
            given(redisService.getOtp(TEST_EMAIL)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> authService.confirmOtp(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_OTP));
        }
    }

    @Nested
    @DisplayName("signup 메서드")
    class SignupTest {

        @Test
        @DisplayName("성공 - 유효한 회원가입")
        void signup_Success() {
            // given
            SignupRequest request = createSignupRequest(TEST_EMAIL, TEST_PASSWORD, TEST_NAME, true);
            User savedUser = createUser(1L, TEST_EMAIL, "encoded-password", TEST_NAME);

            given(redisService.isEmailVerified(TEST_EMAIL)).willReturn(true);
            given(passwordEncoder.encode(TEST_PASSWORD)).willReturn("encoded-password");
            given(userRepository.save(any(User.class))).willReturn(savedUser);

            // when
            SignupResponse response = authService.signup(request);

            // then
            assertThat(response.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(response.getName()).isEqualTo(TEST_NAME);
            verify(redisService).deleteEmailVerified(TEST_EMAIL);
        }

        @Test
        @DisplayName("실패 - 이메일 인증 미완료")
        void signup_EmailNotVerified() {
            // given
            SignupRequest request = createSignupRequest(TEST_EMAIL, TEST_PASSWORD, TEST_NAME, true);
            given(redisService.isEmailVerified(TEST_EMAIL)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.EMAIL_NOT_VERIFIED));
        }

        @Test
        @DisplayName("실패 - 비밀번호 정책 위반")
        void signup_InvalidPasswordFormat() {
            // given
            SignupRequest request = createSignupRequest(TEST_EMAIL, "weak", TEST_NAME, true);
            given(redisService.isEmailVerified(TEST_EMAIL)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_PASSWORD_FORMAT));
        }
    }

    @Nested
    @DisplayName("login 메서드")
    class LoginTest {

        @Test
        @DisplayName("성공 - 유효한 로그인")
        void login_Success() {
            // given
            LoginRequest request = createLoginRequest(TEST_EMAIL, TEST_PASSWORD);
            User user = createUser(1L, TEST_EMAIL, "encoded-password", TEST_NAME);

            given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(TEST_PASSWORD, "encoded-password")).willReturn(true);
            given(jwtService.generateAccessToken(1L, TEST_EMAIL)).willReturn(TEST_ACCESS_TOKEN);
            given(jwtService.generateRefreshToken(1L, TEST_EMAIL)).willReturn(TEST_REFRESH_TOKEN);
            given(jwtService.getRefreshTokenExpiration()).willReturn(604800000L);

            // when
            LoginResponse response = authService.login(request);

            // then
            assertThat(response.getAccessToken()).isEqualTo(TEST_ACCESS_TOKEN);
            assertThat(response.getRefreshToken()).isEqualTo(TEST_REFRESH_TOKEN);
            verify(redisService).saveRefreshToken(eq(1L), eq(TEST_REFRESH_TOKEN), eq(604800L));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 이메일")
        void login_UserNotFound() {
            // given
            LoginRequest request = createLoginRequest(TEST_EMAIL, TEST_PASSWORD);
            given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_CREDENTIALS));
        }

        @Test
        @DisplayName("실패 - 비밀번호 불일치")
        void login_WrongPassword() {
            // given
            LoginRequest request = createLoginRequest(TEST_EMAIL, "wrong-password");
            User user = createUser(1L, TEST_EMAIL, "encoded-password", TEST_NAME);

            given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("wrong-password", "encoded-password")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_CREDENTIALS));
        }
    }

    @Nested
    @DisplayName("refresh 메서드")
    class RefreshTest {

        @Test
        @DisplayName("성공 - 유효한 토큰 갱신")
        void refresh_Success() {
            // given
            RefreshTokenRequest request = createRefreshTokenRequest(TEST_REFRESH_TOKEN);
            String newAccessToken = "new-access-token";
            String newRefreshToken = "new-refresh-token";

            given(jwtService.getUserIdFromToken(TEST_REFRESH_TOKEN)).willReturn(1L);
            given(jwtService.getEmailFromToken(TEST_REFRESH_TOKEN)).willReturn(TEST_EMAIL);
            given(redisService.validateRefreshToken(1L, TEST_REFRESH_TOKEN)).willReturn(true);
            given(jwtService.generateAccessToken(1L, TEST_EMAIL)).willReturn(newAccessToken);
            given(jwtService.generateRefreshToken(1L, TEST_EMAIL)).willReturn(newRefreshToken);
            given(jwtService.getRefreshTokenExpiration()).willReturn(604800000L);

            // when
            TokenResponse response = authService.refresh(request);

            // then
            assertThat(response.getAccessToken()).isEqualTo(newAccessToken);
            assertThat(response.getRefreshToken()).isEqualTo(newRefreshToken);
            verify(redisService).saveRefreshToken(eq(1L), eq(newRefreshToken), eq(604800L));
        }

        @Test
        @DisplayName("실패 - Redis에 저장된 토큰과 불일치")
        void refresh_InvalidToken() {
            // given
            RefreshTokenRequest request = createRefreshTokenRequest(TEST_REFRESH_TOKEN);

            given(jwtService.getUserIdFromToken(TEST_REFRESH_TOKEN)).willReturn(1L);
            given(jwtService.getEmailFromToken(TEST_REFRESH_TOKEN)).willReturn(TEST_EMAIL);
            given(redisService.validateRefreshToken(1L, TEST_REFRESH_TOKEN)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_TOKEN));
        }
    }

    @Nested
    @DisplayName("logout 메서드")
    class LogoutTest {

        @Test
        @DisplayName("성공 - 유효한 로그아웃")
        void logout_Success() {
            // given
            Long userId = 1L;
            LogoutRequest request = createLogoutRequest(TEST_REFRESH_TOKEN);
            given(redisService.validateRefreshToken(userId, TEST_REFRESH_TOKEN)).willReturn(true);

            // when
            MessageResponse response = authService.logout(userId, request);

            // then
            assertThat(response.getMessage()).isEqualTo("로그아웃되었습니다.");
            verify(redisService).deleteRefreshToken(userId);
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 리프레시 토큰")
        void logout_InvalidRefreshToken() {
            // given
            Long userId = 1L;
            LogoutRequest request = createLogoutRequest("invalid-token");
            given(redisService.validateRefreshToken(userId, "invalid-token")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.logout(userId, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_TOKEN));
        }
    }

    // 요청 생성 메서드
    private EmailVerifyRequest createEmailVerifyRequest(String email) {
        EmailVerifyRequest request = new EmailVerifyRequest();
        ReflectionTestUtils.setField(request, "email", email);
        return request;
    }

    private EmailConfirmRequest createEmailConfirmRequest(String email, String code) {
        EmailConfirmRequest request = new EmailConfirmRequest();
        ReflectionTestUtils.setField(request, "email", email);
        ReflectionTestUtils.setField(request, "code", code);
        return request;
    }

    private SignupRequest createSignupRequest(String email, String password, String name, Boolean marketingAgreement) {
        SignupRequest request = new SignupRequest();
        ReflectionTestUtils.setField(request, "email", email);
        ReflectionTestUtils.setField(request, "password", password);
        ReflectionTestUtils.setField(request, "name", name);
        ReflectionTestUtils.setField(request, "marketingAgreement", marketingAgreement);
        return request;
    }

    private LoginRequest createLoginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        ReflectionTestUtils.setField(request, "email", email);
        ReflectionTestUtils.setField(request, "password", password);
        return request;
    }

    private RefreshTokenRequest createRefreshTokenRequest(String refreshToken) {
        RefreshTokenRequest request = new RefreshTokenRequest();
        ReflectionTestUtils.setField(request, "refreshToken", refreshToken);
        return request;
    }

    private LogoutRequest createLogoutRequest(String refreshToken) {
        LogoutRequest request = new LogoutRequest();
        ReflectionTestUtils.setField(request, "refreshToken", refreshToken);
        return request;
    }

    private User createUser(Long id, String email, String password, String name) {
        User user = User.builder()
                .email(email)
                .password(password)
                .name(name)
                .marketingAgreement(true)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
