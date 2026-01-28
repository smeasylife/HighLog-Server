package goatHeaven.highLog.controller;

import goatHeaven.highLog.dto.response.MessageResponse;
import goatHeaven.highLog.dto.response.PresignedUrlResponse;
import goatHeaven.highLog.dto.response.StudentRecordResponse;
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
    public ResponseEntity<List<StudentRecordResponse>> getRecords(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        List<StudentRecordResponse> responses = studentRecordService.getRecords(principal.getUserId());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{recordId}")
    public ResponseEntity<StudentRecordResponse> getRecord(
            @PathVariable Long recordId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        StudentRecordResponse response = studentRecordService.getRecord(recordId, principal.getUserId());
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
