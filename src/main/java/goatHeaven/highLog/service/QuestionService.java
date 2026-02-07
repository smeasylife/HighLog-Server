package goatHeaven.highLog.service;

import goatHeaven.highLog.domain.Question;
import goatHeaven.highLog.domain.Question;
import goatHeaven.highLog.domain.QuestionSet;
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
        QuestionSet questionSet = questionSetRepository.findById(questionSetId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));

        if (!questionSet.isOwner(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
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

        List<Question> questions = questionRepository.findByQuestionSetIdWithFilters(questionSetId, categoryFilter, difficultyEnum);

        return questions.stream()
                .map(QuestionResponse::from)
                .collect(Collectors.toList());
    }
}
