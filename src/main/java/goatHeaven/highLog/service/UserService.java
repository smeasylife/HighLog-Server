package goatHeaven.highLog.service;

import goatHeaven.highLog.domain.StudentRecord;
import goatHeaven.highLog.domain.User;
import goatHeaven.highLog.dto.request.ChangePasswordRequest;
import goatHeaven.highLog.dto.request.DeleteAccountRequest;
import goatHeaven.highLog.dto.response.DashboardResponse;
import goatHeaven.highLog.dto.response.MessageResponse;
import goatHeaven.highLog.exception.CustomException;
import goatHeaven.highLog.exception.ErrorCode;
import goatHeaven.highLog.repository.QuestionRepository;
import goatHeaven.highLog.repository.StudentRecordRepository;
import goatHeaven.highLog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final StudentRecordRepository studentRecordRepository;
    private final QuestionRepository questionRepository;
    private final RedisService redisService;
    private final S3Service s3Service;
    private final PasswordEncoder passwordEncoder;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    public DashboardResponse getDashboard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String registDate = user.getCreatedAt().format(DATE_FORMATTER);
        int questionBookmarkCnt = questionRepository.countBookmarkedQuestionsByUserId(userId);

        return DashboardResponse.of(user.getName(), registDate, questionBookmarkCnt);
    }

    @Transactional
    public MessageResponse changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 새 비밀번호 정책 확인
        if (!isValidPassword(request.getNewPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }

        // 비밀번호 변경
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));

        log.info("Password changed for user: {}", userId);

        return MessageResponse.of("비밀번호가 변경되었습니다.");
    }

    @Transactional
    public MessageResponse deleteAccount(Long userId, DeleteAccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }

        // S3에서 사용자의 모든 생기부 파일 삭제
        List<StudentRecord> records = studentRecordRepository.findByUserId(userId);
        for (StudentRecord record : records) {
            try {
                s3Service.deleteFile(record.getS3Key());
            } catch (Exception e) {
                log.warn("Failed to delete S3 file for record: {}", record.getId(), e);
            }
        }

        // Redis에서 refresh token 삭제
        redisService.deleteRefreshToken(userId);

        // 사용자 삭제 (cascade로 관련 데이터 삭제)
        userRepository.delete(user);

        log.info("Account deleted for user: {}", userId);

        return MessageResponse.of("회원 탈퇴가 완료되었습니다.");
    }

    private boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
}
