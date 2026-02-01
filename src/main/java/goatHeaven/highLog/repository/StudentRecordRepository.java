package goatHeaven.highLog.repository;

import goatHeaven.highLog.enums.RecordStatus;
import goatHeaven.highLog.domain.StudentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRecordRepository extends JpaRepository<StudentRecord, Long> {

    List<StudentRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<StudentRecord> findByUserId(Long userId);

    @Query("SELECT sr FROM StudentRecord sr JOIN FETCH sr.user WHERE sr.id = :recordId")
    Optional<StudentRecord> findByIdWithUser(@Param("recordId") Long recordId);
    List<StudentRecord> findByUserIdAndStatus(Long userId, RecordStatus status);

    @Query("SELECT sr FROM StudentRecord sr WHERE sr.user.id = :userId AND sr.id = :recordId")
    Optional<StudentRecord> findByUserIdAndId(@Param("userId") Long userId, @Param("recordId") Long recordId);
    Optional<StudentRecord> findByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);
}
