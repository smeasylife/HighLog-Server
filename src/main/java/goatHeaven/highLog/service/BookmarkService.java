package goatHeaven.highLog.service;

import goatHeaven.highLog.domain.Question;
import goatHeaven.highLog.dto.response.BookmarkResponse;
import goatHeaven.highLog.dto.response.BookmarkToggleResponse;
import goatHeaven.highLog.exception.CustomException;
import goatHeaven.highLog.exception.ErrorCode;
import goatHeaven.highLog.repository.QuestionRepository;
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
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_NOT_FOUND));

        if (!question.getRecord().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.RECORD_ACCESS_DENIED);
        }

        question.toggleBookmark();

        return BookmarkToggleResponse.of(questionId, question.getIsBookmarked());
    }

    public List<BookmarkResponse> getBookmarks(Long userId) {
        List<Question> bookmarkedQuestions = questionRepository.findBookmarkedByUserId(userId);

        return bookmarkedQuestions.stream()
                .map(BookmarkResponse::from)
                .collect(Collectors.toList());
    }
}
