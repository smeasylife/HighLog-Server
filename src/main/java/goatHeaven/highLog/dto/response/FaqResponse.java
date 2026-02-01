package goatHeaven.highLog.dto.response;

import goatHeaven.highLog.domain.Faq;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FaqResponse {

    private Long id;
    private String category;
    private String question;
    private String answer;
    private Integer displayOrder;
    private LocalDateTime createdAt;

    public static FaqResponse from(Faq faq) {
        return FaqResponse.builder()
                .id(faq.getId())
                .category(faq.getCategory())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .displayOrder(faq.getDisplayOrder())
                .createdAt(faq.getCreatedAt())
                .build();
    }
}
