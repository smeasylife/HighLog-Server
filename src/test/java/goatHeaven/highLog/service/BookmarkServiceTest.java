package goatHeaven.highLog.service;

import goatHeaven.highLog.domain.Question;
import goatHeaven.highLog.domain.QuestionSet;
import goatHeaven.highLog.domain.RecordStatus;
import goatHeaven.highLog.domain.StudentRecord;
import goatHeaven.highLog.domain.User;
import goatHeaven.highLog.dto.response.BookmarkResponse;
import goatHeaven.highLog.dto.response.BookmarkToggleResponse;
import goatHeaven.highLog.exception.CustomException;
import goatHeaven.highLog.exception.ErrorCode;
import goatHeaven.highLog.repository.QuestionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @InjectMocks
    private BookmarkService bookmarkService;

    @Mock
    private QuestionRepository questionRepository;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_RECORD_ID = 10L;
    private static final Long TEST_QUESTION_ID = 100L;

    @Nested
    @DisplayName("toggleBookmark 메서드")
    class ToggleBookmarkTest {

        @Test
        @DisplayName("성공 - 북마크 등록 (false → true)")
        void toggleBookmark_Success_AddBookmark() {
            // given
            User user = createUser(TEST_USER_ID);
            StudentRecord record = createRecord(TEST_RECORD_ID, user);
            Question question = createQuestion(TEST_QUESTION_ID, record, false);

            given(questionRepository.findById(TEST_QUESTION_ID)).willReturn(Optional.of(question));

            // when
            BookmarkToggleResponse response = bookmarkService.toggleBookmark(TEST_USER_ID, TEST_QUESTION_ID);

            // then
            assertThat(response.getQuestionId()).isEqualTo(TEST_QUESTION_ID);
            assertThat(response.getIsBookmarked()).isTrue();
            assertThat(question.getIsBookmarked()).isTrue();
        }

        @Test
        @DisplayName("성공 - 북마크 해제 (true → false)")
        void toggleBookmark_Success_RemoveBookmark() {
            // given
            User user = createUser(TEST_USER_ID);
            StudentRecord record = createRecord(TEST_RECORD_ID, user);
            Question question = createQuestion(TEST_QUESTION_ID, record, true);

            given(questionRepository.findById(TEST_QUESTION_ID)).willReturn(Optional.of(question));

            // when
            BookmarkToggleResponse response = bookmarkService.toggleBookmark(TEST_USER_ID, TEST_QUESTION_ID);

            // then
            assertThat(response.getQuestionId()).isEqualTo(TEST_QUESTION_ID);
            assertThat(response.getIsBookmarked()).isFalse();
            assertThat(question.getIsBookmarked()).isFalse();
        }

        @Test
        @DisplayName("실패 - 질문을 찾을 수 없음")
        void toggleBookmark_QuestionNotFound() {
            // given
            given(questionRepository.findById(TEST_QUESTION_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> bookmarkService.toggleBookmark(TEST_USER_ID, TEST_QUESTION_ID))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.QUESTION_NOT_FOUND));
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 질문에 접근")
        void toggleBookmark_AccessDenied() {
            // given
            User otherUser = createUser(999L);
            StudentRecord record = createRecord(TEST_RECORD_ID, otherUser);
            Question question = createQuestion(TEST_QUESTION_ID, record, false);

            given(questionRepository.findById(TEST_QUESTION_ID)).willReturn(Optional.of(question));

            // when & then
            assertThatThrownBy(() -> bookmarkService.toggleBookmark(TEST_USER_ID, TEST_QUESTION_ID))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.RECORD_ACCESS_DENIED));
        }
    }

    @Nested
    @DisplayName("getBookmarks 메서드")
    class GetBookmarksTest {

        @Test
        @DisplayName("성공 - 전체 북마크된 질문 목록 조회 (recordId 없음)")
        void getBookmarks_Success() {
            // given
            User user = createUser(TEST_USER_ID);
            StudentRecord record = createRecord(TEST_RECORD_ID, user);
            List<Question> bookmarkedQuestions = List.of(
                    createQuestion(100L, record, true),
                    createQuestion(101L, record, true)
            );

            given(questionRepository.findBookmarkedQuestionsByUserId(TEST_USER_ID)).willReturn(bookmarkedQuestions);

            // when
            List<BookmarkResponse> responses = bookmarkService.getBookmarks(TEST_USER_ID, null);

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getQuestionId()).isEqualTo(100L);
            assertThat(responses.get(0).getRecordTitle()).isEqualTo("2025학년도 생기부");
            assertThat(responses.get(1).getQuestionId()).isEqualTo(101L);
        }

        @Test
        @DisplayName("성공 - 특정 생기부의 북마크된 질문 목록 조회 (recordId 있음)")
        void getBookmarks_ByRecordId_Success() {
            // given
            User user = createUser(TEST_USER_ID);
            StudentRecord record = createRecord(TEST_RECORD_ID, user);
            List<Question> bookmarkedQuestions = List.of(
                    createQuestion(100L, record, true)
            );

            given(questionRepository.findBookmarkedQuestionsByUserIdAndRecordId(TEST_USER_ID, TEST_RECORD_ID))
                    .willReturn(bookmarkedQuestions);

            // when
            List<BookmarkResponse> responses = bookmarkService.getBookmarks(TEST_USER_ID, TEST_RECORD_ID);

            // then
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getQuestionId()).isEqualTo(100L);
            assertThat(responses.get(0).getRecordTitle()).isEqualTo("2025학년도 생기부");
        }

        @Test
        @DisplayName("성공 - 북마크된 질문이 없는 경우 빈 리스트 반환")
        void getBookmarks_Empty() {
            // given
            given(questionRepository.findBookmarkedQuestionsByUserId(TEST_USER_ID)).willReturn(List.of());

            // when
            List<BookmarkResponse> responses = bookmarkService.getBookmarks(TEST_USER_ID, null);

            // then
            assertThat(responses).isEmpty();
        }
    }

    // 테스트 객체 생성 헬퍼 메서드
    private User createUser(Long id) {
        User user = User.builder()
                .email("test@gmail.com")
                .password("password")
                .name("테스트")
                .marketingAgreement(true)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private StudentRecord createRecord(Long id, User user) {
        StudentRecord record = StudentRecord.builder()
                .user(user)
                .title("2025학년도 생기부")
                .s3Key("users/1/records/test.pdf")
                .targetSchool("한국대학교")
                .targetMajor("컴퓨터공학과")
                .interviewType("종합전형")
                .status(RecordStatus.READY)
                .build();
        ReflectionTestUtils.setField(record, "id", id);
        return record;
    }

    private QuestionSet createQuestionSet(Long id, StudentRecord record) {
        QuestionSet questionSet = QuestionSet.builder()
                .record(record)
                .targetSchool("한국대학교")
                .targetMajor("컴퓨터공학과")
                .interviewType("종합전형")
                .title("테스트 질문 세트")
                .build();
        ReflectionTestUtils.setField(questionSet, "id", id);
        return questionSet;
    }

    private Question createQuestion(Long id, StudentRecord record, boolean isBookmarked) {
        QuestionSet questionSet = createQuestionSet(id + 1000, record);
        Question question = Question.builder()
                .category("인성")
                .content("테스트 질문입니다.")
                .difficulty(Question.Difficulty.BASIC)
                .modelAnswer("모범 답안입니다.")
                .build();
        question.setQuestionSet(questionSet);
        ReflectionTestUtils.setField(question, "id", id);
        ReflectionTestUtils.setField(question, "isBookmarked", isBookmarked);
        return question;
    }
}
