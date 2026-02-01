package goatHeaven.highLog.service;

import goatHeaven.highLog.domain.*;
import goatHeaven.highLog.dto.response.QuestionResponse;
import goatHeaven.highLog.exception.CustomException;
import goatHeaven.highLog.exception.ErrorCode;
import goatHeaven.highLog.repository.QuestionRepository;
import goatHeaven.highLog.repository.StudentRecordRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @InjectMocks
    private QuestionService questionService;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private StudentRecordRepository studentRecordRepository;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_RECORD_ID = 10L;
    private static final Long TEST_QUESTION_ID = 100L;

    @Nested
    @DisplayName("getQuestionsByRecordId 메서드")
    class GetQuestionsByRecordIdTest {

        @Test
        @DisplayName("성공 - 필터 없이 전체 질문 조회")
        void getQuestions_Success_NoFilter() {
            // given
            User user = createUser(TEST_USER_ID);
            StudentRecord record = createRecord(TEST_RECORD_ID, user, RecordStatus.READY);
            List<Question> questions = List.of(
                    createQuestion(100L, record, "인성", QuestionDifficulty.BASIC, false),
                    createQuestion(101L, record, "전공적합성", QuestionDifficulty.DEEP, true)
            );

            given(studentRecordRepository.findById(TEST_RECORD_ID)).willReturn(Optional.of(record));
            given(questionRepository.findByRecordIdWithFilters(eq(TEST_RECORD_ID), any(), any()))
                    .willReturn(questions);

            // when
            List<QuestionResponse> responses = questionService.getQuestionsByRecordId(
                    TEST_USER_ID, TEST_RECORD_ID, null, null);

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getQuestionId()).isEqualTo(100L);
            assertThat(responses.get(0).getCategory()).isEqualTo("인성");
            assertThat(responses.get(0).getIsBookmarked()).isFalse();
            assertThat(responses.get(1).getQuestionId()).isEqualTo(101L);
            assertThat(responses.get(1).getIsBookmarked()).isTrue();
        }

        @Test
        @DisplayName("성공 - 카테고리 필터 적용")
        void getQuestions_Success_WithCategoryFilter() {
            // given
            User user = createUser(TEST_USER_ID);
            StudentRecord record = createRecord(TEST_RECORD_ID, user, RecordStatus.READY);
            List<Question> questions = List.of(
                    createQuestion(100L, record, "인성", QuestionDifficulty.BASIC, false)
            );

            given(studentRecordRepository.findById(TEST_RECORD_ID)).willReturn(Optional.of(record));
            given(questionRepository.findByRecordIdWithFilters(TEST_RECORD_ID, "인성", null))
                    .willReturn(questions);

            // when
            List<QuestionResponse> responses = questionService.getQuestionsByRecordId(
                    TEST_USER_ID, TEST_RECORD_ID, "인성", null);

            // then
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getCategory()).isEqualTo("인성");
        }

        @Test
        @DisplayName("성공 - 난이도 필터 적용")
        void getQuestions_Success_WithDifficultyFilter() {
            // given
            User user = createUser(TEST_USER_ID);
            StudentRecord record = createRecord(TEST_RECORD_ID, user, RecordStatus.READY);
            List<Question> questions = List.of(
                    createQuestion(100L, record, "인성", QuestionDifficulty.DEEP, false)
            );

            given(studentRecordRepository.findById(TEST_RECORD_ID)).willReturn(Optional.of(record));
            given(questionRepository.findByRecordIdWithFilters(TEST_RECORD_ID, null, QuestionDifficulty.DEEP))
                    .willReturn(questions);

            // when
            List<QuestionResponse> responses = questionService.getQuestionsByRecordId(
                    TEST_USER_ID, TEST_RECORD_ID, null, "DEEP");

            // then
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getDifficulty()).isEqualTo(QuestionDifficulty.DEEP);
        }

        @Test
        @DisplayName("실패 - 생기부를 찾을 수 없음")
        void getQuestions_RecordNotFound() {
            // given
            given(studentRecordRepository.findById(TEST_RECORD_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> questionService.getQuestionsByRecordId(
                    TEST_USER_ID, TEST_RECORD_ID, null, null))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.RECORD_NOT_FOUND));
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 생기부에 접근")
        void getQuestions_AccessDenied() {
            // given
            User otherUser = createUser(999L);
            StudentRecord record = createRecord(TEST_RECORD_ID, otherUser, RecordStatus.READY);

            given(studentRecordRepository.findById(TEST_RECORD_ID)).willReturn(Optional.of(record));

            // when & then
            assertThatThrownBy(() -> questionService.getQuestionsByRecordId(
                    TEST_USER_ID, TEST_RECORD_ID, null, null))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.RECORD_ACCESS_DENIED));
        }

        @Test
        @DisplayName("실패 - 생기부 분석이 완료되지 않음")
        void getQuestions_RecordNotReady() {
            // given
            User user = createUser(TEST_USER_ID);
            StudentRecord record = createRecord(TEST_RECORD_ID, user, RecordStatus.ANALYZING);

            given(studentRecordRepository.findById(TEST_RECORD_ID)).willReturn(Optional.of(record));

            // when & then
            assertThatThrownBy(() -> questionService.getQuestionsByRecordId(
                    TEST_USER_ID, TEST_RECORD_ID, null, null))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.RECORD_NOT_READY));
        }

        @Test
        @DisplayName("실패 - 잘못된 난이도 값")
        void getQuestions_InvalidDifficulty() {
            // given
            User user = createUser(TEST_USER_ID);
            StudentRecord record = createRecord(TEST_RECORD_ID, user, RecordStatus.READY);

            given(studentRecordRepository.findById(TEST_RECORD_ID)).willReturn(Optional.of(record));

            // when & then
            assertThatThrownBy(() -> questionService.getQuestionsByRecordId(
                    TEST_USER_ID, TEST_RECORD_ID, null, "INVALID"))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_INPUT_VALUE));
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

    private StudentRecord createRecord(Long id, User user, RecordStatus status) {
        StudentRecord record = StudentRecord.builder()
                .user(user)
                .title("2025학년도 생기부")
                .s3Key("users/1/records/test.pdf")
                .targetSchool("한국대학교")
                .targetMajor("컴퓨터공학과")
                .interviewType("종합전형")
                .status(status)
                .build();
        ReflectionTestUtils.setField(record, "id", id);
        return record;
    }

    private Question createQuestion(Long id, StudentRecord record, String category,
                                    QuestionDifficulty difficulty, boolean isBookmarked) {
        Question question = Question.builder()
                .record(record)
                .category(category)
                .content("테스트 질문입니다.")
                .difficulty(difficulty)
                .modelAnswer("모범 답안입니다.")
                .build();
        ReflectionTestUtils.setField(question, "id", id);
        ReflectionTestUtils.setField(question, "isBookmarked", isBookmarked);
        return question;
    }
}
