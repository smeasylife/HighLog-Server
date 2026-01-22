package goatHeaven.highLog.controller;

import goatHeaven.highLog.dto.request.BookmarkRequest;
import goatHeaven.highLog.dto.response.BookmarkQuestionResponse;
import goatHeaven.highLog.dto.response.BookmarkResponse;
import goatHeaven.highLog.dto.response.QuestionGenerationEvent;
import goatHeaven.highLog.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping(value = "/questions/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<QuestionGenerationEvent> generateQuestions(
            @RequestParam Long recordId,
            Authentication authentication) {
        return questionService.generateQuestions(recordId, authentication);
    }

    /**
     * 질문 즐겨찾기 토글
     * POST /api/bookmarks
     */
    @PostMapping("/bookmarks")
    public ResponseEntity<BookmarkResponse> toggleBookmark(
            @Valid @RequestBody BookmarkRequest request,
            Authentication authentication) {
        BookmarkResponse response = questionService.toggleBookmark(
                request.getQuestionId(), authentication);
        return ResponseEntity.ok(response);
    }

    /**
     * 즐겨찾기 질문 목록 조회
     * GET /api/bookmarks
     */
    @GetMapping("/bookmarks")
    public ResponseEntity<List<BookmarkQuestionResponse>> getBookmarks(
            Authentication authentication) {
        List<BookmarkQuestionResponse> responses = questionService.getBookmarks(authentication);
        return ResponseEntity.ok(responses);
    }
}
