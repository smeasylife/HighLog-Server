package goatHeaven.highLog.dto.response;

import goatHeaven.highLog.jooq.tables.pojos.Questions;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuestionResponse {

    private Long questionId;
    private String category;
    private String content;
    private String difficulty;
    private Boolean isBookmarked;
    private String modelAnswer;
    private String questionPurpose;
    private String answerPoints;

    public static QuestionResponse from(Questions question) {
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
