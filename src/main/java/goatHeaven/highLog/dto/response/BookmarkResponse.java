package goatHeaven.highLog.dto.response;

import goatHeaven.highLog.domain.Question;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BookmarkResponse {

    private Long questionId;
    private String recordTitle;
    private String category;
    private String content;
    private Question.Difficulty difficulty;
    private LocalDateTime createdAt;

    public static BookmarkResponse from(Question question) {
        return BookmarkResponse.builder()
                .questionId(question.getId())
                .recordTitle(question.getRecord().getTitle())
                .category(question.getCategory())
                .content(question.getContent())
                .difficulty(question.getDifficulty())
                .createdAt(question.getCreatedAt())
                .build();
    }
}
