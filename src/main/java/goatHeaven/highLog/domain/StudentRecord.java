package goatHeaven.highLog.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_records", indexes = {
        @Index(name = "idx_records_user_id", columnList = "user_id"),
        @Index(name = "idx_records_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(name = "s3_key", nullable = false, length = 512)
    private String s3Key;

    @Column(name = "target_school", length = 100)
    private String targetSchool;

    @Column(name = "target_major", length = 100)
    private String targetMajor;

    @Column(name = "interview_type", length = 50)
    private String interviewType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RecordStatus status = RecordStatus.PENDING;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public StudentRecord(User user, String title, String s3Key, String targetSchool,
                         String targetMajor, String interviewType, RecordStatus status) {
        this.user = user;
        this.title = title;
        this.s3Key = s3Key;
        this.targetSchool = targetSchool;
        this.targetMajor = targetMajor;
        this.interviewType = interviewType;
        this.status = status != null ? status : RecordStatus.PENDING;
    }

    public void updateStatus(RecordStatus status) {
        this.status = status;
        if (status == RecordStatus.READY) {
            this.analyzedAt = LocalDateTime.now();
        }
    }
}
