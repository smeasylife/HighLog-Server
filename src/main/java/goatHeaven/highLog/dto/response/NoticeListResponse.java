package goatHeaven.highLog.dto.response;

import goatHeaven.highLog.domain.Notice;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class NoticeListResponse {

    private List<NoticeItem> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;

    @Getter
    @Builder
    public static class NoticeItem {
        private Long id;
        private String title;
        private Boolean isPinned;
        private LocalDateTime createdAt;

        public static NoticeItem from(Notice notice) {
            return NoticeItem.builder()
                    .id(notice.getId())
                    .title(notice.getTitle())
                    .isPinned(notice.getIsPinned())
                    .createdAt(notice.getCreatedAt())
                    .build();
        }
    }

    public static NoticeListResponse from(Page<Notice> noticePage) {
        return NoticeListResponse.builder()
                .content(noticePage.getContent().stream()
                        .map(NoticeItem::from)
                        .toList())
                .totalElements(noticePage.getTotalElements())
                .totalPages(noticePage.getTotalPages())
                .currentPage(noticePage.getNumber())
                .build();
    }
}
