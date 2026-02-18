package goatHeaven.highLog.service;

import goatHeaven.highLog.jooq.tables.pojos.Questions;
import goatHeaven.highLog.dto.response.BookmarkResponse;
import goatHeaven.highLog.dto.response.BookmarkToggleResponse;
import goatHeaven.highLog.exception.CustomException;
import goatHeaven.highLog.exception.ErrorCode;
import goatHeaven.highLog.repository.QuestionRepository;
import goatHeaven.highLog.repository.QuestionRepository.BookmarkedQuestionWithRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

    private final QuestionRepository questionRepository;

    @Transactional
    public BookmarkToggleResponse toggleBookmark(Long userId, Long questionId) {
        Questions question = questionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_NOT_FOUND));

        // 소유자 확인
        if (!questionRepository.isOwner(questionId, userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 북마크 토글
        Boolean newBookmarkStatus = !Boolean.TRUE.equals(question.getIsBookmarked());
        questionRepository.updateBookmark(questionId, newBookmarkStatus);

        return BookmarkToggleResponse.of(questionId, newBookmarkStatus);
    }

    public List<BookmarkResponse> getBookmarks(Long userId, Long recordId) {
        List<BookmarkedQuestionWithRecord> bookmarkedQuestions;

        if (recordId != null) {
            bookmarkedQuestions = questionRepository.findBookmarkedWithRecordByUserIdAndRecordId(userId, recordId);
        } else {
            bookmarkedQuestions = questionRepository.findBookmarkedWithRecordByUserId(userId);
        }

        return bookmarkedQuestions.stream()
                .map(BookmarkResponse::from)
                .collect(Collectors.toList());
    }
}
