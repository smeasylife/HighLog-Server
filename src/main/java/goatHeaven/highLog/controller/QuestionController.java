package goatHeaven.highLog.controller;

import goatHeaven.highLog.dto.response.QuestionResponse;
import goatHeaven.highLog.security.CustomUserPrincipal;
import goatHeaven.highLog.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/{recordId}/questions")
    public ResponseEntity<List<QuestionResponse>> getQuestions(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long recordId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty) {

        List<QuestionResponse> questions = questionService.getQuestionsByRecordId(
                principal.getUserId(), recordId, category, difficulty);

        return ResponseEntity.ok(questions);
    }
}
