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

}
