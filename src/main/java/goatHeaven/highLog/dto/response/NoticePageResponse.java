package goatHeaven.highLog.dto.response;

import java.util.List;

public record NoticePageResponse(
        List<NoticeResponse> notices,
        int currentPage,
        int totalPages,
        long totalElements,
        boolean hasNext,
        boolean hasPrevious
) {
    public static NoticePageResponse of(List<NoticeResponse> notices, int currentPage, int totalPages, long totalElements) {
        return new NoticePageResponse(
                notices,
                currentPage,
                totalPages,
                totalElements,
                currentPage < totalPages - 1,
                currentPage > 0
        );
    }
}
