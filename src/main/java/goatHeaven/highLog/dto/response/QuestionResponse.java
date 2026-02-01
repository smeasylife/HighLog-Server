package goatHeaven.highLog.dto.response;

import goatHeaven.highLog.domain.Question;
import goatHeaven.highLog.domain.QuestionDifficulty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuestionResponse {

    private Long questionId;
    private String category;
    private String content;
    private QuestionDifficulty difficulty;
    private Boolean isBookmarked;
    private String modelAnswer;

    public static QuestionResponse from(Question question) {
        return QuestionResponse.builder()
                .questionId(question.getId())
                .category(question.getCategory())
                .content(question.getContent())
                .difficulty(question.getDifficulty())
                .isBookmarked(question.getIsBookmarked())
                .modelAnswer(question.getModelAnswer())
                .build();
    }
}
