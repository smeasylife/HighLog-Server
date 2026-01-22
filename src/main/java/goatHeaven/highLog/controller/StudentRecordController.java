package goatHeaven.highLog.controller;

import goatHeaven.highLog.domain.Question;
import goatHeaven.highLog.dto.request.StudentRecordRequest;
import goatHeaven.highLog.dto.response.MessageResponse;
import goatHeaven.highLog.dto.response.StudentRecordResponse;
import goatHeaven.highLog.service.StudentRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class StudentRecordController {

    private final StudentRecordService studentRecordService;

    @PostMapping
    public ResponseEntity<StudentRecordResponse> saveRecord(
            @Valid @RequestBody StudentRecordRequest request,
            Authentication authentication) {
        StudentRecordResponse response = studentRecordService.saveRecord(authentication, request);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * 생기부 목록 조회
     * GET /api/records
     */
    @GetMapping
    public ResponseEntity<List<StudentRecordResponse>> getRecords(Authentication authentication) {
        List<StudentRecordResponse> responses = studentRecordService.getRecords(authentication);
        return ResponseEntity.ok(responses);
    }

    /**
     * 특정 생기부 조회
     * GET /api/records/{recordId}
     */
    @GetMapping("/{recordId}")
    public ResponseEntity<StudentRecordResponse> getRecord(
            @PathVariable Long recordId,
            Authentication authentication) {
        StudentRecordResponse response = studentRecordService.getRecord(recordId, authentication);
        return ResponseEntity.ok(response);
    }

    /**
     * 생기부 삭제
     * DELETE /api/records/{recordId}
     */
    @DeleteMapping("/{recordId}")
    public ResponseEntity<MessageResponse> deleteRecord(
            @PathVariable Long recordId,
            Authentication authentication) {
        studentRecordService.deleteRecord(recordId, authentication);
        return ResponseEntity.ok(MessageResponse.of("생기부가 삭제되었습니다."));
    }

    /**
     * 생기부 질문 목록 조회
     * GET /api/records/{recordId}/questions
     */
    @GetMapping("/{recordId}/questions")
    public ResponseEntity<List<Question>> getQuestions(
            @PathVariable Long recordId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Question.Difficulty difficulty,
            Authentication authentication) {
        List<Question> questions = studentRecordService.getQuestions(
                recordId, category, difficulty, authentication);
        return ResponseEntity.ok(questions);
    }
}
