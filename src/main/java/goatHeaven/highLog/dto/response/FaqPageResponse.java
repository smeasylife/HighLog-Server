package goatHeaven.highLog.dto.response;

import java.util.List;

public record FaqPageResponse(
        List<FaqResponse> faqs,
        int currentPage,
        int totalPages,
        long totalElements,
        boolean hasNext,
        boolean hasPrevious
) {
    public static FaqPageResponse of(List<FaqResponse> faqs, int currentPage, int totalPages, long totalElements) {
        return new FaqPageResponse(
                faqs,
                currentPage,
                totalPages,
                totalElements,
                currentPage < totalPages - 1,
                currentPage > 0
        );
    }
}
