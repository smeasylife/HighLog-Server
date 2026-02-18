package goatHeaven.highLog.repository;

import goatHeaven.highLog.jooq.tables.JQuestionSets;
import goatHeaven.highLog.jooq.tables.JStudentRecords;
import goatHeaven.highLog.jooq.tables.daos.QuestionSetsDao;
import goatHeaven.highLog.jooq.tables.pojos.QuestionSets;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class QuestionSetRepository {

    private final DSLContext dsl;
    private final QuestionSetsDao dao;
    private static final JQuestionSets QUESTION_SETS = JQuestionSets.QUESTION_SETS;
    private static final JStudentRecords STUDENT_RECORDS = JStudentRecords.STUDENT_RECORDS;

    public QuestionSetRepository(Configuration configuration, DSLContext dsl) {
        this.dao = new QuestionSetsDao(configuration);
        this.dsl = dsl;
    }

    public Optional<QuestionSets> findById(Long id) {
        return dao.fetchOptionalById(id);
    }

    public List<QuestionSets> findByRecordId(Long recordId) {
        return dao.fetchByRecordId(recordId);
    }

    public Optional<QuestionSets> findByIdAndRecordId(Long setId, Long recordId) {
        return dsl.selectFrom(QUESTION_SETS)
                .where(QUESTION_SETS.ID.eq(setId))
                .and(QUESTION_SETS.RECORD_ID.eq(recordId))
                .fetchOptionalInto(QuestionSets.class);
    }

    /**
     * QuestionSet의 소유자(userId)를 확인합니다.
     * QuestionSet → StudentRecord → User 관계를 통해 확인
     */
    public boolean isOwner(Long questionSetId, Long userId) {
        return dsl.fetchExists(
                dsl.select()
                        .from(QUESTION_SETS)
                        .join(STUDENT_RECORDS).on(QUESTION_SETS.RECORD_ID.eq(STUDENT_RECORDS.ID))
                        .where(QUESTION_SETS.ID.eq(questionSetId))
                        .and(STUDENT_RECORDS.USER_ID.eq(userId))
        );
    }

    /**
     * QuestionSet을 저장하고 생성된 ID를 반환합니다.
     */
    public Long save(QuestionSets questionSet) {
        return dsl.insertInto(QUESTION_SETS)
                .set(QUESTION_SETS.TITLE, questionSet.getTitle())
                .set(QUESTION_SETS.INTERVIEW_TYPE, questionSet.getInterviewType())
                .set(QUESTION_SETS.TARGET_SCHOOL, questionSet.getTargetSchool())
                .set(QUESTION_SETS.TARGET_MAJOR, questionSet.getTargetMajor())
                .set(QUESTION_SETS.RECORD_ID, questionSet.getRecordId())
                .set(QUESTION_SETS.CREATED_AT, java.time.LocalDateTime.now())
                .returning(QUESTION_SETS.ID)
                .fetchOne()
                .getId();
    }
}
