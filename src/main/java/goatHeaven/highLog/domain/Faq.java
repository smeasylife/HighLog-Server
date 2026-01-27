package goatHeaven.highLog.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "faqs", indexes = {
        @Index(name = "idx_faqs_category", columnList = "category"),
        @Index(name = "idx_faqs_order", columnList = "display_order")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Faq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public Faq(String category, String question, String answer, Integer displayOrder) {
        this.category = category;
        this.question = question;
        this.answer = answer;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
    }

    public void update(String category, String question, String answer, Integer displayOrder) {
        this.category = category;
        this.question = question;
        this.answer = answer;
        this.displayOrder = displayOrder != null ? displayOrder : this.displayOrder;
    }
}
