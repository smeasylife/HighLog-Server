package goatHeaven.highLog.dto.response;

import goatHeaven.highLog.domain.Question;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class QuestionResponse {

    private final Long questionId;
    private final String category;
    private final String content;
    private final String difficulty;
    private final Boolean isBookmarked;
    private final String modelAnswer;
    private final String modelAnswerCriteria;
    private final String questionPurpose;
    private final String answerPoints;
    private final LocalDateTime createdAt;

    public QuestionResponse(Question question) {
        this.questionId = question.getId();
        this.category = question.getCategory();
        this.content = question.getContent();
        this.difficulty = question.getDifficulty().name();
        this.isBookmarked = question.getIsBookmarked();
        this.modelAnswer = question.getModelAnswer();
        this.modelAnswerCriteria = question.getModelAnswerCriteria();
        this.questionPurpose = question.getQuestionPurpose();
        this.answerPoints = question.getAnswerPoints();
        this.createdAt = question.getCreatedAt();
    }
}
