package goatHeaven.highLog.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "questions", indexes = {
        @Index(name = "idx_questions_set_id", columnList = "set_id"),
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
    @JoinColumn(name = "set_id", nullable = false)
    @Setter
    private QuestionSet questionSet;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty;

    @Column(name = "is_bookmarked")
    private Boolean isBookmarked = false;

    @Column(name = "model_answer", columnDefinition = "TEXT")
    private String modelAnswer;

    @Column(name = "model_answer_criteria", columnDefinition = "TEXT")
    private String modelAnswerCriteria;

    @Column(name = "question_purpose", columnDefinition = "TEXT")
    private String questionPurpose;

    @Column(name = "answer_points", columnDefinition = "TEXT")
    private String answerPoints;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public Question(StudentRecord record, String category, String content,
                   Difficulty difficulty, String modelAnswer, String modelAnswerCriteria,
                   String questionPurpose, String answerPoints) {
        this.category = category;
        this.content = content;
        this.difficulty = difficulty;
        this.modelAnswer = modelAnswer;
        this.modelAnswerCriteria = modelAnswerCriteria;
        this.questionPurpose = questionPurpose;
        this.answerPoints = answerPoints;
        this.isBookmarked = false;
    }

    public void toggleBookmark() {
        this.isBookmarked = !this.isBookmarked;
    }

    public enum Difficulty {
        BASIC,
        PRESSURE,
        DEEP
    }
}
