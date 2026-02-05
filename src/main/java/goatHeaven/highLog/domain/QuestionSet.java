package goatHeaven.highLog.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "question_sets", indexes = {
        @Index(name = "idx_qsets_record_id", columnList = "record_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private StudentRecord record;

    @Column(name = "target_school", nullable = false, length = 100)
    private String targetSchool;

    @Column(name = "target_major", nullable = false, length = 100)
    private String targetMajor;

    @Column(name = "interview_type", nullable = false, length = 50)
    private String interviewType;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "questionSet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public QuestionSet(StudentRecord record, String targetSchool, String targetMajor,
                       String interviewType, String title) {
        this.record = record;
        this.targetSchool = targetSchool;
        this.targetMajor = targetMajor;
        this.interviewType = interviewType;
        this.title = title;
    }

    public boolean isOwner(Long userId) {
        return this.record.getUser().getId().equals(userId);
    }
}
