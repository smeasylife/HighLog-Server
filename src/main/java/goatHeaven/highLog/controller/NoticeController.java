package goatHeaven.highLog.controller;

import goatHeaven.highLog.dto.request.NoticeRequest;
import goatHeaven.highLog.dto.response.NoticePageResponse;
import goatHeaven.highLog.dto.response.NoticeResponse;
import goatHeaven.highLog.security.CustomUserPrincipal;
import goatHeaven.highLog.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public ResponseEntity<NoticePageResponse> getAllNotices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(noticeService.getNoticesWithPaging(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoticeResponse> getNoticeById(@PathVariable Long id) {
        return ResponseEntity.ok(noticeService.getNoticeById(id));
    }

    @PostMapping
    public ResponseEntity<NoticeResponse> createNotice(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody NoticeRequest request) {
        NoticeResponse response = noticeService.createNotice(principal.getRole(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoticeResponse> updateNotice(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody NoticeRequest request) {
        return ResponseEntity.ok(noticeService.updateNotice(principal.getRole(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id) {
        noticeService.deleteNotice(principal.getRole(), id);
        return ResponseEntity.noContent().build();
    }
}
