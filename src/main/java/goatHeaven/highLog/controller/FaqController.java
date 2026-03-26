package goatHeaven.highLog.controller;

import goatHeaven.highLog.dto.request.FaqRequest;
import goatHeaven.highLog.dto.response.FaqPageResponse;
import goatHeaven.highLog.dto.response.FaqResponse;
import goatHeaven.highLog.security.CustomUserPrincipal;
import goatHeaven.highLog.service.FaqService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/faqs")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    @GetMapping
    public ResponseEntity<FaqPageResponse> getAllFaqs(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(faqService.getFaqsWithPaging(category, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FaqResponse> getFaqById(@PathVariable Long id) {
        return ResponseEntity.ok(faqService.getFaqById(id));
    }

    @PostMapping
    public ResponseEntity<FaqResponse> createFaq(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody FaqRequest request) {
        FaqResponse response = faqService.createFaq(principal.getRole(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FaqResponse> updateFaq(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody FaqRequest request) {
        return ResponseEntity.ok(faqService.updateFaq(principal.getRole(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFaq(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id) {
        faqService.deleteFaq(principal.getRole(), id);
        return ResponseEntity.noContent().build();
    }
}
