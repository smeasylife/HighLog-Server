package goatHeaven.highLog.repository;

import goatHeaven.highLog.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByQuestionSetId(Long questionSetId);

    List<Question> findByQuestionSetIdAndCategory(Long questionSetId, String category);

    List<Question> findByQuestionSetIdAndDifficulty(Long questionSetId, Question.Difficulty difficulty);

    List<Question> findByQuestionSetIdAndCategoryAndDifficulty(Long questionSetId, String category, Question.Difficulty difficulty);

    @Query("SELECT q FROM Question q WHERE q.questionSet.id = :questionSetId " +
           "AND (:category IS NULL OR q.category = :category) " +
           "AND (:difficulty IS NULL OR q.difficulty = :difficulty)")
    List<Question> findByQuestionSetIdWithFilters(
            @Param("questionSetId") Long questionSetId,
            @Param("category") String category,
            @Param("difficulty") Question.Difficulty difficulty
    );

    @Query("SELECT q FROM Question q JOIN FETCH q.questionSet qs JOIN FETCH qs.record r WHERE r.user.id = :userId " +
           "AND q.isBookmarked = true ORDER BY q.createdAt DESC")
    List<Question> findBookmarkedQuestionsByUserId(@Param("userId") Long userId);

    boolean existsByQuestionSetIdAndId(Long questionSetId, Long questionId);
}
