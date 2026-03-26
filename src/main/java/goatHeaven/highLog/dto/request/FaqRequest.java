package goatHeaven.highLog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FaqRequest(
        @NotBlank(message = "카테고리는 필수입니다.")
        @Size(max = 50, message = "카테고리는 50자 이하여야 합니다.")
        String category,

        @NotBlank(message = "질문은 필수입니다.")
        @Size(max = 255, message = "질문은 255자 이하여야 합니다.")
        String question,

        @NotBlank(message = "답변은 필수입니다.")
        String answer,

        Integer displayOrder
) {
    public FaqRequest {
        if (displayOrder == null) {
            displayOrder = 0;
        }
    }
}
