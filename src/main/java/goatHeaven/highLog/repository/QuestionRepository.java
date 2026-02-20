package goatHeaven.highLog.repository;

import goatHeaven.highLog.jooq.tables.JQuestions;
import goatHeaven.highLog.jooq.tables.JQuestionSets;
import goatHeaven.highLog.jooq.tables.JStudentRecords;
import goatHeaven.highLog.jooq.tables.daos.QuestionsDao;
import goatHeaven.highLog.jooq.tables.pojos.Questions;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class QuestionRepository {

    private final DSLContext dsl;
    private final QuestionsDao dao;
    private static final JQuestions QUESTIONS = JQuestions.QUESTIONS;
    private static final JQuestionSets QUESTION_SETS = JQuestionSets.QUESTION_SETS;
    private static final JStudentRecords STUDENT_RECORDS = JStudentRecords.STUDENT_RECORDS;

    public QuestionRepository(Configuration configuration, DSLContext dsl) {
        this.dao = new QuestionsDao(configuration);
        this.dsl = dsl;
    }

    public Optional<Questions> findById(Long id) {
        return dao.fetchOptionalById(id);
    }

    public List<Questions> findBySetIdWithFilters(Long setId, Long userId, String category, String difficulty) {
        Condition condition = QUESTIONS.SET_ID.eq(setId);

        if (category != null) {
            condition = condition.and(QUESTIONS.CATEGORY.eq(category));
        }
        if (difficulty != null) {
            condition = condition.and(QUESTIONS.DIFFICULTY.eq(difficulty));
        }

        return dsl.select(QUESTIONS)
                .from(QUESTIONS)
                .join(QUESTION_SETS).on(QUESTIONS.SET_ID.eq(QUESTION_SETS.ID))
                .join(STUDENT_RECORDS).on(QUESTION_SETS.RECORD_ID.eq(STUDENT_RECORDS.ID))
                .where(condition)
                .and(STUDENT_RECORDS.USER_ID.eq(userId))
                .fetchInto(Questions.class);
    }


    public int countBookmarkedByUserId(Long userId) {
        return dsl.fetchCount(
                dsl.selectFrom(QUESTIONS)
                        .where(QUESTIONS.SET_ID.in(
                                dsl.select(QUESTION_SETS.ID)
                                        .from(QUESTION_SETS)
                                        .join(STUDENT_RECORDS).on(QUESTION_SETS.RECORD_ID.eq(STUDENT_RECORDS.ID))
                                        .where(STUDENT_RECORDS.USER_ID.eq(userId))
                        ))
                        .and(QUESTIONS.IS_BOOKMARKED.eq(true))
        );
    }


    /**
     * Question의 소유자(userId)를 확인합니다.
     * Question → QuestionSet → StudentRecord → User 관계를 통해 확인
     */
    public boolean isOwner(Long questionId, Long userId) {
        return dsl.fetchExists(
                dsl.select()
                        .from(QUESTIONS)
                        .join(QUESTION_SETS).on(QUESTIONS.SET_ID.eq(QUESTION_SETS.ID))
                        .join(STUDENT_RECORDS).on(QUESTION_SETS.RECORD_ID.eq(STUDENT_RECORDS.ID))
                        .where(QUESTIONS.ID.eq(questionId))
                        .and(STUDENT_RECORDS.USER_ID.eq(userId))
        );
    }

    public void updateBookmark(Long questionId, Boolean isBookmarked) {
        dsl.update(QUESTIONS)
                .set(QUESTIONS.IS_BOOKMARKED, isBookmarked)
                .where(QUESTIONS.ID.eq(questionId))
                .execute();
    }

    /**
     * 북마크된 질문 조회 (recordTitle 포함)
     * Question + StudentRecord.title 조인해서 반환
     */
    public List<BookmarkedQuestionWithRecord> findBookmarkedWithRecordByUserId(Long userId) {
        return dsl.select(
                        QUESTIONS.ID,
                        QUESTIONS.CATEGORY,
                        QUESTIONS.CONTENT,
                        QUESTIONS.DIFFICULTY,
                        QUESTIONS.CREATED_AT,
                        STUDENT_RECORDS.TITLE.as("recordTitle")
                )
                .from(QUESTIONS)
                .join(QUESTION_SETS).on(QUESTIONS.SET_ID.eq(QUESTION_SETS.ID))
                .join(STUDENT_RECORDS).on(QUESTION_SETS.RECORD_ID.eq(STUDENT_RECORDS.ID))
                .where(STUDENT_RECORDS.USER_ID.eq(userId))
                .and(QUESTIONS.IS_BOOKMARKED.eq(true))
                .orderBy(QUESTIONS.CREATED_AT.desc())
                .fetchInto(BookmarkedQuestionWithRecord.class);
    }

    public List<BookmarkedQuestionWithRecord> findBookmarkedWithRecordByUserIdAndRecordId(Long userId, Long recordId) {
        return dsl.select(
                        QUESTIONS.ID,
                        QUESTIONS.CATEGORY,
                        QUESTIONS.CONTENT,
                        QUESTIONS.DIFFICULTY,
                        QUESTIONS.CREATED_AT,
                        STUDENT_RECORDS.TITLE.as("recordTitle")
                )
                .from(QUESTIONS)
                .join(QUESTION_SETS).on(QUESTIONS.SET_ID.eq(QUESTION_SETS.ID))
                .join(STUDENT_RECORDS).on(QUESTION_SETS.RECORD_ID.eq(STUDENT_RECORDS.ID))
                .where(STUDENT_RECORDS.USER_ID.eq(userId))
                .and(STUDENT_RECORDS.ID.eq(recordId))
                .and(QUESTIONS.IS_BOOKMARKED.eq(true))
                .orderBy(QUESTIONS.CREATED_AT.desc())
                .fetchInto(BookmarkedQuestionWithRecord.class);
    }

    // JOIN 결과를 담을 DTO
    public record BookmarkedQuestionWithRecord(
            Long id,
            String category,
            String content,
            String difficulty,
            java.time.LocalDateTime createdAt,
            String recordTitle
    ) {}


}
