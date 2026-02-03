package goatHeaven.highLog.service;

import goatHeaven.highLog.domain.Question;
import goatHeaven.highLog.enums.RecordStatus;
import goatHeaven.highLog.domain.StudentRecord;
import goatHeaven.highLog.dto.response.QuestionResponse;
import goatHeaven.highLog.exception.CustomException;
import goatHeaven.highLog.exception.ErrorCode;
import goatHeaven.highLog.repository.QuestionRepository;
import goatHeaven.highLog.repository.StudentRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final StudentRecordRepository studentRecordRepository;

    public List<QuestionResponse> getQuestionsByRecordId(Long userId, Long recordId, String category, String difficulty) {
        StudentRecord record = studentRecordRepository.findById(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));

        if (!record.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.RECORD_ACCESS_DENIED);
        }

        if (record.getStatus() != RecordStatus.READY) {
            throw new CustomException(ErrorCode.RECORD_NOT_READY);
        }

        Question.Difficulty difficultyEnum = null;
        if (difficulty != null && !difficulty.isBlank()) {
            try {
                difficultyEnum = Question.Difficulty.valueOf(difficulty.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }

        String categoryFilter = (category != null && !category.isBlank()) ? category : null;

        List<Question> questions = questionRepository.findByRecordIdWithFilters(recordId, categoryFilter, difficultyEnum);

        return questions.stream()
                .map(QuestionResponse::from)
                .collect(Collectors.toList());
    }
}
