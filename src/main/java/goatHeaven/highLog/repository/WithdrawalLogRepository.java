package goatHeaven.highLog.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Repository
@RequiredArgsConstructor
public class WithdrawalLogRepository {

    private final DSLContext dsl;

    public void insert(String reason, int membershipDays) {
        dsl.insertInto(table("withdrawal_logs"))
                .set(field("reason"), reason)
                .set(field("membership_days"), membershipDays)
                .set(field("created_at"), LocalDateTime.now())
                .execute();
    }
}
