package goatHeaven.highLog.service;

import goatHeaven.highLog.domain.Role;
import goatHeaven.highLog.dto.request.NoticeRequest;
import goatHeaven.highLog.dto.response.NoticePageResponse;
import goatHeaven.highLog.dto.response.NoticeResponse;
import goatHeaven.highLog.exception.CustomException;
import goatHeaven.highLog.exception.ErrorCode;
import goatHeaven.highLog.jooq.tables.pojos.Notices;
import goatHeaven.highLog.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    private static final int DEFAULT_PAGE_SIZE = 7;

    public List<NoticeResponse> getAllNotices() {
        return noticeRepository.findAll().stream()
                .map(NoticeResponse::from)
                .collect(Collectors.toList());
    }

    public NoticePageResponse getNoticesWithPaging(int page, Integer size) {
        int pageSize = (size != null && size > 0) ? size : DEFAULT_PAGE_SIZE;

        List<NoticeResponse> notices = noticeRepository.findAllWithPaging(page, pageSize).stream()
                .map(NoticeResponse::from)
                .collect(Collectors.toList());

        long totalElements = noticeRepository.count();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        return NoticePageResponse.of(notices, page, totalPages, totalElements);
    }

    public NoticeResponse getNoticeById(Long id) {
        Notices notice = noticeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));
        return NoticeResponse.from(notice);
    }

    @Transactional
    public NoticeResponse createNotice(Role role, NoticeRequest request) {
        validateAdminRole(role);

        Notices notice = new Notices();
        notice.setTitle(request.title());
        notice.setContent(request.content());
        notice.setIsPinned(request.isPinned());

        Notices savedNotice = noticeRepository.save(notice);
        return NoticeResponse.from(savedNotice);
    }

    @Transactional
    public NoticeResponse updateNotice(Role role, Long id, NoticeRequest request) {
        validateAdminRole(role);

        if (!noticeRepository.existsById(id)) {
            throw new CustomException(ErrorCode.NOTICE_NOT_FOUND);
        }

        Notices notice = new Notices();
        notice.setTitle(request.title());
        notice.setContent(request.content());
        notice.setIsPinned(request.isPinned());

        Notices updatedNotice = noticeRepository.update(id, notice);
        return NoticeResponse.from(updatedNotice);
    }

    @Transactional
    public void deleteNotice(Role role, Long id) {
        validateAdminRole(role);

        if (!noticeRepository.existsById(id)) {
            throw new CustomException(ErrorCode.NOTICE_NOT_FOUND);
        }

        noticeRepository.deleteById(id);
    }

    private void validateAdminRole(Role role) {
        if (role != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }
    }
}
