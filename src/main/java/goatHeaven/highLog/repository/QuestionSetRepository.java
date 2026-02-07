package goatHeaven.highLog.repository;

import goatHeaven.highLog.domain.QuestionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionSetRepository extends JpaRepository<QuestionSet, Long> {

    // 특정 생기부에 속한 모든 질문 세트 조회
    List<QuestionSet> findByRecordId(Long recordId);

    // 특정 생기부에 속한 질문 세트를 ID로 조회
    @Query("SELECT qs FROM QuestionSet qs WHERE qs.id = :setId AND qs.record.id = :recordId")
    Optional<QuestionSet> findByIdAndRecordId(@Param("setId") Long setId, @Param("recordId") Long recordId);
}
