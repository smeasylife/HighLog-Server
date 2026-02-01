package goatHeaven.highLog.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_sessions", indexes = {
        @Index(name = "idx_sessions_user_id", columnList = "user_id"),
        @Index(name = "idx_sessions_thread_id", columnList = "thread_id"),
        @Index(name = "idx_sessions_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewSession {

    @Id
    @Column(length = 100)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private StudentRecord record;

    @Column(name = "thread_id", nullable = false)
    private String threadId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Question.Difficulty intensity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InterviewMode mode;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private InterviewStatus status = InterviewStatus.IN_PROGRESS;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "interview_logs", columnDefinition = "jsonb")
    private String interviewLogs;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "final_report", columnDefinition = "jsonb")
    private String finalReport;

    @Column(name = "started_at", updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "limit_time_seconds")
    private Integer limitTimeSeconds = 900;

    @PrePersist
    protected void onCreate() {
        this.startedAt = LocalDateTime.now();
    }

    @Builder
    public InterviewSession(String id, User user, StudentRecord record, String threadId,
                            Question.Difficulty intensity, InterviewMode mode, Integer limitTimeSeconds) {
        this.id = id;
        this.user = user;
        this.record = record;
        this.threadId = threadId;
        this.intensity = intensity;
        this.mode = mode;
        this.status = InterviewStatus.IN_PROGRESS;
        this.limitTimeSeconds = limitTimeSeconds != null ? limitTimeSeconds : 900;
    }

    public void updateStatus(InterviewStatus status) {
        this.status = status;
        if (status == InterviewStatus.COMPLETED) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public void updateInterviewLogs(String interviewLogs) {
        this.interviewLogs = interviewLogs;
    }

    public void updateFinalReport(String finalReport) {
        this.finalReport = finalReport;
    }
}
