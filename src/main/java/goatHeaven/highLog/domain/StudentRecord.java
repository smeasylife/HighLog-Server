package goatHeaven.highLog.domain;

import goatHeaven.highLog.enums.RecordStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "s3_key", nullable = false, length = 512)
    private String s3Key;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordStatus status = RecordStatus.PENDING;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionSet> questionSets = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public StudentRecord(User user, String title, String s3Key, RecordStatus status) {
        this.user = user;
        this.title = title;
        this.s3Key = s3Key;
        this.status = status != null ? status : RecordStatus.PENDING;
    }

    public void updateStatus(RecordStatus status) {
        this.status = status;
    }

    public boolean isOwner(Long userId) {
        return this.user.getId().equals(userId);
    }
}
