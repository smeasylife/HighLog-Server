package goatHeaven.highLog.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookmarkResponse {

    private Long questionId;
    private Boolean isBookmarked;
}
