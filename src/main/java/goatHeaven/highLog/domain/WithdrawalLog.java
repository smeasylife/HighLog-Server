package goatHeaven.highLog.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "withdrawal_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WithdrawalLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "membership_days")
    private Integer membershipDays;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public WithdrawalLog(String reason, Integer membershipDays) {
        this.reason = reason;
        this.membershipDays = membershipDays;
    }
}
