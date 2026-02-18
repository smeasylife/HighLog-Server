package goatHeaven.highLog.dto.response;

import goatHeaven.highLog.repository.QuestionRepository.BookmarkedQuestionWithRecord;
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
    private String difficulty;
    private LocalDateTime createdAt;

    public static BookmarkResponse from(BookmarkedQuestionWithRecord question) {
        return BookmarkResponse.builder()
                .questionId(question.id())
                .recordTitle(question.recordTitle())
                .category(question.category())
                .content(question.content())
                .difficulty(question.difficulty())
                .createdAt(question.createdAt())
                .build();
    }
}
