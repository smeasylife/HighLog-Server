package goatHeaven.highLog.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookmarkToggleResponse {

    private Long questionId;
    private Boolean isBookmarked;

    public static BookmarkToggleResponse of(Long questionId, boolean isBookmarked) {
        return BookmarkToggleResponse.builder()
                .questionId(questionId)
                .isBookmarked(isBookmarked)
                .build();
    }
}
