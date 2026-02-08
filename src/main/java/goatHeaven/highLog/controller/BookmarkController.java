package goatHeaven.highLog.controller;

import goatHeaven.highLog.dto.request.BookmarkRequest;
import goatHeaven.highLog.dto.response.BookmarkResponse;
import goatHeaven.highLog.dto.response.BookmarkToggleResponse;
import goatHeaven.highLog.security.CustomUserPrincipal;
import goatHeaven.highLog.service.BookmarkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping
    public ResponseEntity<BookmarkToggleResponse> toggleBookmark(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody BookmarkRequest request) {

        BookmarkToggleResponse response = bookmarkService.toggleBookmark(
                principal.getUserId(), request.getQuestionId());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<BookmarkResponse>> getBookmarks(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false) Long recordId) {

        List<BookmarkResponse> bookmarks = bookmarkService.getBookmarks(principal.getUserId(), recordId);

        return ResponseEntity.ok(bookmarks);
    }
}
