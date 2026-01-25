package goatHeaven.highLog.service;

import goatHeaven.highLog.domain.Question;
import goatHeaven.highLog.domain.StudentRecord;
import goatHeaven.highLog.dto.response.BookmarkQuestionResponse;
import goatHeaven.highLog.dto.response.BookmarkResponse;
import goatHeaven.highLog.dto.response.QuestionGenerationEvent;
import goatHeaven.highLog.enums.RecordStatus;
import goatHeaven.highLog.exception.CustomException;
import goatHeaven.highLog.exception.ErrorCode;
import goatHeaven.highLog.repository.QuestionRepository;
import goatHeaven.highLog.repository.StudentRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final StudentRecordRepository studentRecordRepository;
    private final AiService aiService;

    /**
     * 질문 생성을 시작하고 SSE 스트림을 반환합니다.
     */
    public Flux<QuestionGenerationEvent> generateQuestions(Long recordId, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        StudentRecord record = studentRecordRepository.findByUserIdAndId(userId, recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));

        if (record.getStatus() != RecordStatus.READY) {
            return Flux.error(new CustomException(ErrorCode.RECORD_NOT_READY));
        }

        // 이미 질문이 존재하는지 확인
        List<Question> existingQuestions = questionRepository.findByRecordId(recordId);
        if (!existingQuestions.isEmpty()) {
            return Flux.error(new CustomException(ErrorCode.QUESTIONS_ALREADY_EXIST));
        }

        // AI 서버에 질문 생성 요청
        return aiService.generateQuestions(recordId)
                .doOnNext(event -> {
                    if ("complete".equals(event.getType())) {
                        log.info("Question generation completed for record: {}", recordId);
                    }
                })
                .doOnError(error -> {
                    log.error("Question generation failed for record: {}", recordId, error);
                });
    }

    /**
     * 생성된 질문을 DB에 저장합니다.
     */
    @Transactional
    public void saveQuestions(Long recordId, List<Question> questions) {
        StudentRecord record = studentRecordRepository.findById(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));

        for (Question question : questions) {
            question.setRecord(record);
            questionRepository.save(question);
        }
    }

    /**
     * 질문을 즐겨찾기에 추가하거나 제거합니다.
     */
    @Transactional
    public BookmarkResponse toggleBookmark(Long questionId, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_NOT_FOUND));

        // 사용자 검증 (질문이 속한 생기부의 소유자인지 확인)
        if (!question.getRecord().isOwner(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        question.toggleBookmark();
        questionRepository.save(question);

        return BookmarkResponse.builder()
                .questionId(questionId)
                .isBookmarked(question.getIsBookmarked())
                .build();
    }

    /**
     * 즐겨찾기한 모든 질문을 조회합니다.
     */
    public List<BookmarkQuestionResponse> getBookmarks(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return questionRepository.findBookmarkedQuestionsByUserId(userId)
                .stream()
                .map(BookmarkQuestionResponse::new)
                .toList();
    }
}
