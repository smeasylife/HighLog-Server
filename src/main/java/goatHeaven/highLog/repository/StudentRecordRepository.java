package goatHeaven.highLog.repository;

import goatHeaven.highLog.jooq.tables.JStudentRecords;
import goatHeaven.highLog.jooq.tables.JQuestionSets;
import goatHeaven.highLog.jooq.tables.JQuestions;
import goatHeaven.highLog.jooq.tables.daos.StudentRecordsDao;
import goatHeaven.highLog.jooq.tables.pojos.StudentRecords;
import goatHeaven.highLog.jooq.tables.pojos.QuestionSets;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class StudentRecordRepository {

    private final DSLContext dsl;
    private final StudentRecordsDao dao;
    private static final JStudentRecords STUDENT_RECORDS = JStudentRecords.STUDENT_RECORDS;
    private static final JQuestionSets QUESTION_SETS = JQuestionSets.QUESTION_SETS;
    private static final JQuestions QUESTIONS = JQuestions.QUESTIONS;

    public StudentRecordRepository(Configuration configuration, DSLContext dsl) {
        this.dao = new StudentRecordsDao(configuration);
        this.dsl = dsl;
    }

    public Optional<StudentRecords> findById(Long id) {
        return dao.fetchOptionalById(id);
    }

    public List<StudentRecords> findByUserId(Long userId) {
        return dao.fetchByUserId(userId);
    }

    public List<StudentRecords> findByUserIdOrderByCreatedAtDesc(Long userId) {
        return dsl.selectFrom(STUDENT_RECORDS)
                .where(STUDENT_RECORDS.USER_ID.eq(userId))
                .orderBy(STUDENT_RECORDS.CREATED_AT.desc())
                .fetchInto(StudentRecords.class);
    }

    public StudentRecords insert(StudentRecords record) {
        record.setCreatedAt(LocalDateTime.now());
        if (record.getStatus() == null) {
            record.setStatus("PENDING");
        }
        dao.insert(record);
        return record;
    }

    public void updateStatus(Long recordId, String status) {
        dsl.update(STUDENT_RECORDS)
                .set(STUDENT_RECORDS.STATUS, status)
                .where(STUDENT_RECORDS.ID.eq(recordId))
                .execute();
    }

    public void delete(StudentRecords record) {
        dao.delete(record);
    }

    public List<QuestionSets> findQuestionSetsByRecordId(Long recordId) {
        return dsl.selectFrom(QUESTION_SETS)
                .where(QUESTION_SETS.RECORD_ID.eq(recordId))
                .fetchInto(QuestionSets.class);
    }

    /**
     * 사용자의 모든 StudentRecord와 하위 데이터를 삭제합니다.
     * 삭제 순서: Questions → QuestionSets → StudentRecords
     */
    public void deleteAllByUserId(Long userId) {
        // 1. 해당 사용자의 모든 record_id 조회
        List<Long> recordIds = dsl.select(STUDENT_RECORDS.ID)
                .from(STUDENT_RECORDS)
                .where(STUDENT_RECORDS.USER_ID.eq(userId))
                .fetchInto(Long.class);

        if (recordIds.isEmpty()) {
            return;
        }

        // 2. 해당 record들의 모든 question_set_id 조회
        List<Long> questionSetIds = dsl.select(QUESTION_SETS.ID)
                .from(QUESTION_SETS)
                .where(QUESTION_SETS.RECORD_ID.in(recordIds))
                .fetchInto(Long.class);

        // 3. Questions 삭제 (자식)
        if (!questionSetIds.isEmpty()) {
            dsl.deleteFrom(QUESTIONS)
                    .where(QUESTIONS.SET_ID.in(questionSetIds))
                    .execute();
        }

        // 4. QuestionSets 삭제
        dsl.deleteFrom(QUESTION_SETS)
                .where(QUESTION_SETS.RECORD_ID.in(recordIds))
                .execute();

        // 5. StudentRecords 삭제
        dsl.deleteFrom(STUDENT_RECORDS)
                .where(STUDENT_RECORDS.USER_ID.eq(userId))
                .execute();
    }
}
