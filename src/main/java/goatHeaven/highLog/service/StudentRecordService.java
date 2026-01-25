package goatHeaven.highLog.service;

import goatHeaven.highLog.domain.Question;
import goatHeaven.highLog.domain.StudentRecord;
import goatHeaven.highLog.domain.User;
import goatHeaven.highLog.dto.request.StudentRecordRequest;
import goatHeaven.highLog.dto.response.StudentRecordResponse;
import goatHeaven.highLog.enums.RecordStatus;
import goatHeaven.highLog.exception.CustomException;
import goatHeaven.highLog.exception.ErrorCode;
import goatHeaven.highLog.repository.QuestionRepository;
import goatHeaven.highLog.repository.StudentRecordRepository;
import goatHeaven.highLog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentRecordService {

    private final StudentRecordRepository studentRecordRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final AiService aiService;

    @Transactional
    public StudentRecordResponse saveRecord(Long userId,
                                           StudentRecordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        StudentRecord record = StudentRecord.builder()
                .user(user)
                .title(request.getTitle())
                .s3Key(request.getS3Key())
                .targetSchool(request.getTargetSchool())
                .targetMajor(request.getTargetMajor())
                .interviewType(request.getInterviewType())
                .status(RecordStatus.PENDING)
                .build();

        record = studentRecordRepository.save(record);

        boolean vectorizationStarted = aiService.vectorizeRecord(record.getId());
        if (!vectorizationStarted) {
            log.warn("Failed to start vectorization for record: {}", record.getId());
        } else {
            record.updateStatus(RecordStatus.READY);
        }

        return new StudentRecordResponse(record);
    }

    /**
     * 사용자의 모든 생기부 목록을 조회합니다.
     */
    public List<StudentRecordResponse> getRecords(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return studentRecordRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(StudentRecordResponse::new)
                .toList();
    }

    /**
     * 특정 생기부를 조회합니다.
     */
    public StudentRecordResponse getRecord(Long recordId, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        StudentRecord record = studentRecordRepository.findByUserIdAndId(userId, recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));

        return new StudentRecordResponse(record);
    }

    /**
     * 생기부를 삭제합니다.
     */
    @Transactional
    public void deleteRecord(Long recordId, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        StudentRecord record = studentRecordRepository.findByUserIdAndId(userId, recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));

        // 연관된 질문들도 자동 삭제 (Cascade)
        studentRecordRepository.delete(record);
    }

    /**
     * 생기부의 질문들을 모두 조회합니다.
     */
    public List<Question> getQuestions(Long recordId,
                                       String category,
                                       Question.Difficulty difficulty,
                                       Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        StudentRecord record = studentRecordRepository.findByUserIdAndId(userId, recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));

        if (category == null && difficulty == null) {
            return questionRepository.findByRecordId(recordId);
        } else {
            return questionRepository.findByRecordIdWithFilters(recordId, category, difficulty);
        }
    }
}
