package goatHeaven.highLog.repository;

import goatHeaven.highLog.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByRecordId(Long recordId);

    @Query("SELECT q FROM Question q WHERE q.record.id = :recordId " +
           "AND (:category IS NULL OR q.category = :category) " +
           "AND (:difficulty IS NULL OR q.difficulty = :difficulty)")
    List<Question> findByRecordIdWithFilters(
            @Param("recordId") Long recordId,
            @Param("category") String category,
            @Param("difficulty") Question.Difficulty difficulty
    );

    @Query("SELECT q FROM Question q JOIN FETCH q.record r WHERE r.user.id = :userId " +
           "AND q.isBookmarked = true ORDER BY q.createdAt DESC")
    List<Question> findBookmarkedQuestionsByUserId(@Param("userId") Long userId);

    boolean existsByRecordIdAndId(Long recordId, Long questionId);
}
