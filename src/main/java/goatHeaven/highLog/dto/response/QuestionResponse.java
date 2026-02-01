package goatHeaven.highLog.dto.response;

import goatHeaven.highLog.domain.Question;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuestionResponse {

    private Long questionId;
    private String category;
    private String content;
    private Question.Difficulty difficulty;
    private Boolean isBookmarked;
    private String modelAnswer;
    private String questionPurpose;
    private String answerPoints;

    public static QuestionResponse from(Question question) {
        return QuestionResponse.builder()
                .questionId(question.getId())
                .category(question.getCategory())
                .content(question.getContent())
                .difficulty(question.getDifficulty())
                .isBookmarked(question.getIsBookmarked())
                .modelAnswer(question.getModelAnswer())
                .questionPurpose(question.getQuestionPurpose())
                .answerPoints(question.getAnswerPoints())
                .build();
    }
}
