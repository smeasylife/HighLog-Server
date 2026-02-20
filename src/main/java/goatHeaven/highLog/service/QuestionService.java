package goatHeaven.highLog.service;

import goatHeaven.highLog.jooq.tables.pojos.Questions;
import goatHeaven.highLog.dto.response.QuestionResponse;
import goatHeaven.highLog.exception.CustomException;
import goatHeaven.highLog.exception.ErrorCode;
import goatHeaven.highLog.repository.QuestionRepository;
import goatHeaven.highLog.repository.QuestionSetRepository;
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
    private final QuestionSetRepository questionSetRepository;

    public List<QuestionResponse> getQuestionsByQuestionSetId(Long userId, Long questionSetId, String category, String difficulty) {
        // QuestionSet 존재 여부 확인
        questionSetRepository.findById(questionSetId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));

        // 소유자 확인
        if (!questionSetRepository.isOwner(questionSetId, userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        String difficultyFilter = null;
        if (difficulty != null && !difficulty.isBlank()) {
            difficultyFilter = difficulty.toUpperCase();
        }

        String categoryFilter = (category != null && !category.isBlank()) ? category : null;

        List<Questions> questions = questionRepository.findBySetIdWithFilters(questionSetId, userId, categoryFilter, difficultyFilter);

        return questions.stream()
                .map(QuestionResponse::from)
                .collect(Collectors.toList());
    }
}
