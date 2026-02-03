package goatHeaven.highLog.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookmarkRequest {

    @NotNull(message = "질문 ID는 필수입니다.")
    private Long questionId;
}
