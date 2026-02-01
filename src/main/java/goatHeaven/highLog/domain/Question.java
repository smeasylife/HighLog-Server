package goatHeaven.highLog.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "questions", indexes = {
        @Index(name = "idx_questions_record_id", columnList = "record_id"),
        @Index(name = "idx_questions_category", columnList = "category"),
        @Index(name = "idx_questions_difficulty", columnList = "difficulty")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private StudentRecord record;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionDifficulty difficulty;

    @Column(name = "is_bookmarked")
    private Boolean isBookmarked = false;

    @Column(name = "model_answer", columnDefinition = "TEXT")
    private String modelAnswer;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public Question(StudentRecord record, String category, String content,
                    QuestionDifficulty difficulty, String modelAnswer) {
        this.record = record;
        this.category = category;
        this.content = content;
        this.difficulty = difficulty;
        this.modelAnswer = modelAnswer;
        this.isBookmarked = false;
    }

    public void toggleBookmark() {
        this.isBookmarked = !this.isBookmarked;
    }
}
