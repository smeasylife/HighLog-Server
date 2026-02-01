package goatHeaven.highLog.repository;

import goatHeaven.highLog.domain.RecordStatus;
import goatHeaven.highLog.domain.StudentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRecordRepository extends JpaRepository<StudentRecord, Long> {

    List<StudentRecord> findByUserId(Long userId);

    List<StudentRecord> findByUserIdAndStatus(Long userId, RecordStatus status);

    Optional<StudentRecord> findByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);
}
