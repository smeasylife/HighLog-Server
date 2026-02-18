//package goatHeaven.highLog.service;
//
//import goatHeaven.highLog.domain.StudentRecord;
//import goatHeaven.highLog.domain.User;
//import goatHeaven.highLog.dto.request.ChangePasswordRequest;
//import goatHeaven.highLog.dto.request.DeleteAccountRequest;
//import goatHeaven.highLog.dto.response.AccountInfoResponse;
//import goatHeaven.highLog.dto.response.DashboardResponse;
//import goatHeaven.highLog.dto.response.MessageResponse;
//import goatHeaven.highLog.enums.RecordStatus;
//import goatHeaven.highLog.exception.CustomException;
//import goatHeaven.highLog.exception.ErrorCode;
//import goatHeaven.highLog.repository.QuestionRepository;
//import goatHeaven.highLog.repository.StudentRecordRepository;
//import goatHeaven.highLog.repository.UserRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.verify;
//
//@ExtendWith(MockitoExtension.class)
//class UserServiceTest {
//
//    @InjectMocks
//    private UserService userService;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private StudentRecordRepository studentRecordRepository;
//
//    @Mock
//    private QuestionRepository questionRepository;
//
//    @Mock
//    private RedisService redisService;
//
//    @Mock
//    private S3Service s3Service;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    private static final Long TEST_USER_ID = 1L;
//
//    @Nested
//    @DisplayName("getDashboard 메서드")
//    class GetDashboardTest {
//
//        @Test
//        @DisplayName("성공 - 대시보드 정보 조회")
//        void getDashboard_Success() {
//            // given
//            User user = createUser(TEST_USER_ID, "홍길동");
//            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));
//            given(questionRepository.countBookmarkedQuestionsByUserId(TEST_USER_ID)).willReturn(5);
//
//            // when
//            DashboardResponse response = userService.getDashboard(TEST_USER_ID);
//
//            // then
//            assertThat(response.getUserName()).isEqualTo("홍길동");
//            assertThat(response.getQuestionBookmarkCnt()).isEqualTo(5);
//            assertThat(response.getInterviewSessionCnt()).isNull();
//            assertThat(response.getInterviewResponseAvg()).isNull();
//        }
//
//        @Test
//        @DisplayName("실패 - 사용자를 찾을 수 없음")
//        void getDashboard_UserNotFound() {
//            // given
//            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.empty());
//
//            // when & then
//            assertThatThrownBy(() -> userService.getDashboard(TEST_USER_ID))
//                    .isInstanceOf(CustomException.class)
//                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
//                            .isEqualTo(ErrorCode.USER_NOT_FOUND));
//        }
//    }
//
//    @Nested
//    @DisplayName("getAccountInfo 메서드")
//    class GetAccountInfoTest {
//
//        @Test
//        @DisplayName("성공 - 계정 정보 조회")
//        void getAccountInfo_Success() {
//            // given
//            User user = createUser(TEST_USER_ID, "홍길동");
//            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));
//
//            // when
//            AccountInfoResponse response = userService.getAccountInfo(TEST_USER_ID);
//
//            // then
//            assertThat(response.getUserName()).isEqualTo("홍길동");
//            assertThat(response.getEmail()).isEqualTo("test@example.com");
//        }
//
//        @Test
//        @DisplayName("실패 - 사용자를 찾을 수 없음")
//        void getAccountInfo_UserNotFound() {
//            // given
//            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.empty());
//
//            // when & then
//            assertThatThrownBy(() -> userService.getAccountInfo(TEST_USER_ID))
//                    .isInstanceOf(CustomException.class)
//                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
//                            .isEqualTo(ErrorCode.USER_NOT_FOUND));
//        }
//    }
//
//    @Nested
//    @DisplayName("changePassword 메서드")
//    class ChangePasswordTest {
//
//        @Test
//        @DisplayName("성공 - 비밀번호 변경")
//        void changePassword_Success() {
//            // given
//            User user = createUser(TEST_USER_ID, "홍길동");
//            ChangePasswordRequest request = createChangePasswordRequest("OldPass123!", "NewPass456!");
//
//            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));
//            given(passwordEncoder.matches("OldPass123!", user.getPassword())).willReturn(true);
//            given(passwordEncoder.encode("NewPass456!")).willReturn("encodedNewPassword");
//
//            // when
//            MessageResponse response = userService.changePassword(TEST_USER_ID, request);
//
//            // then
//            assertThat(response.getMessage()).isEqualTo("비밀번호가 변경되었습니다.");
//        }
//
//        @Test
//        @DisplayName("실패 - 현재 비밀번호 불일치")
//        void changePassword_PasswordMismatch() {
//            // given
//            User user = createUser(TEST_USER_ID, "홍길동");
//            ChangePasswordRequest request = createChangePasswordRequest("WrongPass123!", "NewPass456!");
//
//            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));
//            given(passwordEncoder.matches("WrongPass123!", user.getPassword())).willReturn(false);
//
//            // when & then
//            assertThatThrownBy(() -> userService.changePassword(TEST_USER_ID, request))
//                    .isInstanceOf(CustomException.class)
//                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
//                            .isEqualTo(ErrorCode.PASSWORD_MISMATCH));
//        }
//
//        @Test
//        @DisplayName("실패 - 새 비밀번호 정책 미달")
//        void changePassword_InvalidNewPassword() {
//            // given
//            User user = createUser(TEST_USER_ID, "홍길동");
//            ChangePasswordRequest request = createChangePasswordRequest("OldPass123!", "weak");
//
//            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));
//            given(passwordEncoder.matches("OldPass123!", user.getPassword())).willReturn(true);
//
//            // when & then
//            assertThatThrownBy(() -> userService.changePassword(TEST_USER_ID, request))
//                    .isInstanceOf(CustomException.class)
//                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
//                            .isEqualTo(ErrorCode.INVALID_PASSWORD_FORMAT));
//        }
//
//        @Test
//        @DisplayName("실패 - 사용자를 찾을 수 없음")
//        void changePassword_UserNotFound() {
//            // given
//            ChangePasswordRequest request = createChangePasswordRequest("OldPass123!", "NewPass456!");
//            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.empty());
//
//            // when & then
//            assertThatThrownBy(() -> userService.changePassword(TEST_USER_ID, request))
//                    .isInstanceOf(CustomException.class)
//                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
//                            .isEqualTo(ErrorCode.USER_NOT_FOUND));
//        }
//    }
//
//    @Nested
//    @DisplayName("deleteAccount 메서드")
//    class DeleteAccountTest {
//
//        @Test
//        @DisplayName("성공 - 회원 탈퇴")
//        void deleteAccount_Success() {
//            // given
//            User user = createUser(TEST_USER_ID, "홍길동");
//            DeleteAccountRequest request = createDeleteAccountRequest("Password123!");
//            List<StudentRecord> records = List.of(
//                    createRecord(10L, user, "users/1/records/test1.pdf"),
//                    createRecord(11L, user, "users/1/records/test2.pdf")
//            );
//
//            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));
//            given(passwordEncoder.matches("Password123!", user.getPassword())).willReturn(true);
//            given(studentRecordRepository.findByUserId(TEST_USER_ID)).willReturn(records);
//
//            // when
//            MessageResponse response = userService.deleteAccount(TEST_USER_ID, request);
//
//            // then
//            assertThat(response.getMessage()).isEqualTo("회원 탈퇴가 완료되었습니다.");
//            verify(s3Service).deleteFile("users/1/records/test1.pdf");
//            verify(s3Service).deleteFile("users/1/records/test2.pdf");
//            verify(redisService).deleteRefreshToken(TEST_USER_ID);
//            verify(userRepository).delete(user);
//        }
//
//        @Test
//        @DisplayName("실패 - 비밀번호 불일치")
//        void deleteAccount_PasswordMismatch() {
//            // given
//            User user = createUser(TEST_USER_ID, "홍길동");
//            DeleteAccountRequest request = createDeleteAccountRequest("WrongPassword!");
//
//            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));
//            given(passwordEncoder.matches("WrongPassword!", user.getPassword())).willReturn(false);
//
//            // when & then
//            assertThatThrownBy(() -> userService.deleteAccount(TEST_USER_ID, request))
//                    .isInstanceOf(CustomException.class)
//                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
//                            .isEqualTo(ErrorCode.PASSWORD_MISMATCH));
//        }
//
//        @Test
//        @DisplayName("실패 - 사용자를 찾을 수 없음")
//        void deleteAccount_UserNotFound() {
//            // given
//            DeleteAccountRequest request = createDeleteAccountRequest("Password123!");
//            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.empty());
//
//            // when & then
//            assertThatThrownBy(() -> userService.deleteAccount(TEST_USER_ID, request))
//                    .isInstanceOf(CustomException.class)
//                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
//                            .isEqualTo(ErrorCode.USER_NOT_FOUND));
//        }
//    }
//
//    // 테스트 객체 생성 헬퍼 메서드
//    private User createUser(Long id, String name) {
//        User user = User.builder()
//                .email("test@example.com")
//                .password("encodedPassword")
//                .name(name)
//                .marketingAgreement(true)
//                .build();
//        ReflectionTestUtils.setField(user, "id", id);
//        ReflectionTestUtils.setField(user, "createdAt", LocalDateTime.of(2025, 1, 18, 10, 0));
//        return user;
//    }
//
//    private StudentRecord createRecord(Long id, User user, String s3Key) {
//        StudentRecord record = StudentRecord.builder()
//                .user(user)
//                .title("테스트 생기부")
//                .s3Key(s3Key)
//                .status(RecordStatus.READY)
//                .build();
//        ReflectionTestUtils.setField(record, "id", id);
//        return record;
//    }
//
//    private ChangePasswordRequest createChangePasswordRequest(String currentPassword, String newPassword) {
//        ChangePasswordRequest request = new ChangePasswordRequest();
//        ReflectionTestUtils.setField(request, "currentPassword", currentPassword);
//        ReflectionTestUtils.setField(request, "newPassword", newPassword);
//        return request;
//    }
//
//    private DeleteAccountRequest createDeleteAccountRequest(String password) {
//        DeleteAccountRequest request = new DeleteAccountRequest();
//        ReflectionTestUtils.setField(request, "password", password);
//        return request;
//    }
//}
