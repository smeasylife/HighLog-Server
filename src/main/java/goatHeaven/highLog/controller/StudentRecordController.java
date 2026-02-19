package goatHeaven.highLog.controller;

import goatHeaven.highLog.dto.response.MessageResponse;
import goatHeaven.highLog.dto.response.PresignedUrlResponse;
import goatHeaven.highLog.dto.response.StudentRecordResponse;
import goatHeaven.highLog.dto.response.StudentRecordDetailResponse;
import goatHeaven.highLog.dto.response.StudentRecordPageResponse;
import goatHeaven.highLog.security.CustomUserPrincipal;
import goatHeaven.highLog.service.S3Service;
import goatHeaven.highLog.service.StudentRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class StudentRecordController {

    private final StudentRecordService studentRecordService;
    private final S3Service s3Service;

    @GetMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponse> getPresignedUrl(
            @RequestParam String fileName,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        PresignedUrlResponse response = s3Service.generatePresignedUrl(fileName, principal.getUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<StudentRecordPageResponse> getRecords(
            @RequestParam(defaultValue = "1") int page,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        StudentRecordPageResponse response = studentRecordService.getRecordsWithPagination(
                principal.getUserId(),
                page
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{recordId}")
    public ResponseEntity<StudentRecordDetailResponse> getRecord(
            @PathVariable Long recordId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        StudentRecordDetailResponse response = studentRecordService.getRecord(recordId, principal.getUserId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{recordId}")
    public ResponseEntity<MessageResponse> deleteRecord(
            @PathVariable Long recordId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        studentRecordService.deleteRecord(recordId, principal.getUserId());
        return ResponseEntity.ok(MessageResponse.of("생기부가 삭제되었습니다."));
    }
}
