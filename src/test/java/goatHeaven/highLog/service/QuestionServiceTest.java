//package goatHeaven.highLog.service;
//
//import goatHeaven.highLog.domain.Question;
//import goatHeaven.highLog.domain.QuestionSet;
//import goatHeaven.highLog.domain.Role;
//import goatHeaven.highLog.domain.StudentRecord;
//import goatHeaven.highLog.domain.User;
//import goatHeaven.highLog.dto.response.QuestionResponse;
//import goatHeaven.highLog.enums.RecordStatus;
//import goatHeaven.highLog.exception.CustomException;
//import goatHeaven.highLog.exception.ErrorCode;
//import goatHeaven.highLog.repository.QuestionRepository;
//import goatHeaven.highLog.repository.QuestionSetRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.BDDMockito.given;
//
//@ExtendWith(MockitoExtension.class)
//class QuestionServiceTest {
//
//    @InjectMocks
//    private QuestionService questionService;
//
//    @Mock
//    private QuestionRepository questionRepository;
//
//    @Mock
//    private QuestionSetRepository questionSetRepository;
//
//    private static final Long TEST_USER_ID = 1L;
//    private static final Long TEST_RECORD_ID = 10L;
//    private static final Long TEST_QUESTION_SET_ID = 20L;
//    private static final Long TEST_QUESTION_ID = 100L;
//
//    @Nested
//    @DisplayName("getQuestionsByQuestionSetId 메서드")
//    class GetQuestionsByQuestionSetIdTest {
//
//        @Test
//        @DisplayName("성공 - 필터 없이 전체 질문 조회")
//        void getQuestions_Success_NoFilter() {
//            // given
//            User user = createUser(TEST_USER_ID);
//            StudentRecord record = createRecord(TEST_RECORD_ID, user);
//            QuestionSet questionSet = createQuestionSet(TEST_QUESTION_SET_ID, record);
//            List<Question> questions = List.of(
//                    createQuestion(100L, questionSet, "인성", Question.Difficulty.기본, false),
//                    createQuestion(101L, questionSet, "전공적합성", Question.Difficulty.심화, true)
//            );
//
//            given(questionSetRepository.findById(TEST_QUESTION_SET_ID)).willReturn(Optional.of(questionSet));
//            given(questionRepository.findByQuestionSetIdWithFilters(eq(TEST_QUESTION_SET_ID), any(), any()))
//                    .willReturn(questions);
//
//            // when
//            List<QuestionResponse> responses = questionService.getQuestionsByQuestionSetId(
//                    TEST_USER_ID, TEST_QUESTION_SET_ID, null, null);
//
//            // then
//            assertThat(responses).hasSize(2);
//            assertThat(responses.get(0).getQuestionId()).isEqualTo(100L);
//            assertThat(responses.get(0).getCategory()).isEqualTo("인성");
//            assertThat(responses.get(0).getIsBookmarked()).isFalse();
//            assertThat(responses.get(1).getQuestionId()).isEqualTo(101L);
//            assertThat(responses.get(1).getIsBookmarked()).isTrue();
//        }
//
//        @Test
//        @DisplayName("성공 - 카테고리 필터 적용")
//        void getQuestions_Success_WithCategoryFilter() {
//            // given
//            User user = createUser(TEST_USER_ID);
//            StudentRecord record = createRecord(TEST_RECORD_ID, user);
//            QuestionSet questionSet = createQuestionSet(TEST_QUESTION_SET_ID, record);
//            List<Question> questions = List.of(
//                    createQuestion(100L, questionSet, "인성", Question.Difficulty.기본, false)
//            );
//
//            given(questionSetRepository.findById(TEST_QUESTION_SET_ID)).willReturn(Optional.of(questionSet));
//            given(questionRepository.findByQuestionSetIdWithFilters(TEST_QUESTION_SET_ID, "인성", null))
//                    .willReturn(questions);
//
//            // when
//            List<QuestionResponse> responses = questionService.getQuestionsByQuestionSetId(
//                    TEST_USER_ID, TEST_QUESTION_SET_ID, "인성", null);
//
//            // then
//            assertThat(responses).hasSize(1);
//            assertThat(responses.get(0).getCategory()).isEqualTo("인성");
//        }
//
//        @Test
//        @DisplayName("성공 - 난이도 필터 적용")
//        void getQuestions_Success_WithDifficultyFilter() {
//            // given
//            User user = createUser(TEST_USER_ID);
//            StudentRecord record = createRecord(TEST_RECORD_ID, user);
//            QuestionSet questionSet = createQuestionSet(TEST_QUESTION_SET_ID, record);
//            List<Question> questions = List.of(
//                    createQuestion(100L, questionSet, "인성", Question.Difficulty.심화, false)
//            );
//
//            given(questionSetRepository.findById(TEST_QUESTION_SET_ID)).willReturn(Optional.of(questionSet));
//            given(questionRepository.findByQuestionSetIdWithFilters(TEST_QUESTION_SET_ID, null, Question.Difficulty.심화))
//                    .willReturn(questions);
//
//            // when
//            List<QuestionResponse> responses = questionService.getQuestionsByQuestionSetId(
//                    TEST_USER_ID, TEST_QUESTION_SET_ID, null, "심화");
//
//            // then
//            assertThat(responses).hasSize(1);
//            assertThat(responses.get(0).getDifficulty()).isEqualTo(Question.Difficulty.심화);
//        }
//
//        @Test
//        @DisplayName("실패 - 질문 세트를 찾을 수 없음")
//        void getQuestions_QuestionSetNotFound() {
//            // given
//            given(questionSetRepository.findById(TEST_QUESTION_SET_ID)).willReturn(Optional.empty());
//
//            // when & then
//            assertThatThrownBy(() -> questionService.getQuestionsByQuestionSetId(
//                    TEST_USER_ID, TEST_QUESTION_SET_ID, null, null))
//                    .isInstanceOf(CustomException.class)
//                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
//                            .isEqualTo(ErrorCode.RECORD_NOT_FOUND));
//        }
//
//        @Test
//        @DisplayName("실패 - 다른 사용자의 질문 세트에 접근")
//        void getQuestions_AccessDenied() {
//            // given
//            User otherUser = createUser(999L);
//            StudentRecord record = createRecord(TEST_RECORD_ID, otherUser);
//            QuestionSet questionSet = createQuestionSet(TEST_QUESTION_SET_ID, record);
//
//            given(questionSetRepository.findById(TEST_QUESTION_SET_ID)).willReturn(Optional.of(questionSet));
//
//            // when & then
//            assertThatThrownBy(() -> questionService.getQuestionsByQuestionSetId(
//                    TEST_USER_ID, TEST_QUESTION_SET_ID, null, null))
//                    .isInstanceOf(CustomException.class)
//                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
//                            .isEqualTo(ErrorCode.FORBIDDEN));
//        }
//
//        @Test
//        @DisplayName("실패 - 잘못된 난이도 값")
//        void getQuestions_InvalidDifficulty() {
//            // given
//            User user = createUser(TEST_USER_ID);
//            StudentRecord record = createRecord(TEST_RECORD_ID, user);
//            QuestionSet questionSet = createQuestionSet(TEST_QUESTION_SET_ID, record);
//
//            given(questionSetRepository.findById(TEST_QUESTION_SET_ID)).willReturn(Optional.of(questionSet));
//
//            // when & then
//            assertThatThrownBy(() -> questionService.getQuestionsByQuestionSetId(
//                    TEST_USER_ID, TEST_QUESTION_SET_ID, null, "INVALID"))
//                    .isInstanceOf(CustomException.class)
//                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
//                            .isEqualTo(ErrorCode.INVALID_INPUT_VALUE));
//        }
//    }
//
//    // 테스트 객체 생성 헬퍼 메서드
//    private User createUser(Long id) {
//        User user = User.builder()
//                .email("test@gmail.com")
//                .password("password")
//                .name("테스트")
//                .marketingAgreement(true)
//                .build();
//        ReflectionTestUtils.setField(user, "id", id);
//        return user;
//    }
//
//    private StudentRecord createRecord(Long id, User user) {
//        StudentRecord record = StudentRecord.builder()
//                .user(user)
//                .title("2025학년도 생기부")
//                .s3Key("users/1/records/test.pdf")
//                .status(RecordStatus.READY)
//                .build();
//        ReflectionTestUtils.setField(record, "id", id);
//        return record;
//    }
//
//    private QuestionSet createQuestionSet(Long id, StudentRecord record) {
//        QuestionSet questionSet = QuestionSet.builder()
//                .record(record)
//                .targetSchool("서울대학교")
//                .targetMajor("컴퓨터공학부")
//                .interviewType("학생부종합전형")
//                .title("서울대 컴공")
//                .build();
//        ReflectionTestUtils.setField(questionSet, "id", id);
//        return questionSet;
//    }
//
//    private Question createQuestion(Long id, QuestionSet questionSet, String category,
//                                    Question.Difficulty difficulty, boolean isBookmarked) {
//        Question question = Question.builder()
//                .category(category)
//                .content("테스트 질문입니다.")
//                .difficulty(difficulty)
//                .modelAnswer("모범 답안입니다.")
//                .evaluationCriteria("평가 기준입니다.")
//                .purpose("질문 목적입니다.")
//                .answerPoints("답변 포인트입니다.")
//                .build();
//        question.setQuestionSet(questionSet);
//        ReflectionTestUtils.setField(question, "id", id);
//        ReflectionTestUtils.setField(question, "isBookmarked", isBookmarked);
//        return question;
//    }
//}
