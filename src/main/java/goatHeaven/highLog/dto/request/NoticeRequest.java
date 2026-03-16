package goatHeaven.highLog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoticeRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 255, message = "제목은 255자 이하여야 합니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        String content,

        Boolean isPinned
) {
    public NoticeRequest {
        if (isPinned == null) {
            isPinned = false;
        }
    }
}
