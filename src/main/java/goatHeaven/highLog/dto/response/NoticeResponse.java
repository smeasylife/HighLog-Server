package goatHeaven.highLog.dto.response;

import goatHeaven.highLog.jooq.tables.pojos.Notices;

import java.time.LocalDateTime;

public record NoticeResponse(
        Long id,
        String title,
        String content,
        Boolean isPinned,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static NoticeResponse from(Notices notice) {
        return new NoticeResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getIsPinned(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }
}
