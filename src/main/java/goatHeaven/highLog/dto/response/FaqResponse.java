package goatHeaven.highLog.dto.response;

import goatHeaven.highLog.jooq.tables.pojos.Faqs;

import java.time.LocalDateTime;

public record FaqResponse(
        Long id,
        String category,
        String question,
        String answer,
        Integer displayOrder,
        LocalDateTime createdAt
) {
    public static FaqResponse from(Faqs faq) {
        return new FaqResponse(
                faq.getId(),
                faq.getCategory(),
                faq.getQuestion(),
                faq.getAnswer(),
                faq.getDisplayOrder(),
                faq.getCreatedAt()
        );
    }
}
