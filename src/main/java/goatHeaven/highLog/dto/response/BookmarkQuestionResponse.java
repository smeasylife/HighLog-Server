package goatHeaven.highLog.dto.response;

import goatHeaven.highLog.domain.Question;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BookmarkQuestionResponse {

    private final Long bookmarkId;
    private final Long questionId;
    private final String recordTitle;
    private final String category;
    private final String content;
    private final String difficulty;
    private final LocalDateTime createdAt;

    public BookmarkQuestionResponse(Question question) {
        this.bookmarkId = question.getId();
        this.questionId = question.getId();
        this.recordTitle = question.getRecord().getTitle();
        this.category = question.getCategory();
        this.content = question.getContent();
        this.difficulty = question.getDifficulty().name();
        this.createdAt = question.getCreatedAt();
    }
}
