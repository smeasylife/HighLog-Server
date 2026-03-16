package goatHeaven.highLog.repository;

import goatHeaven.highLog.jooq.tables.JNotices;
import goatHeaven.highLog.jooq.tables.daos.NoticesDao;
import goatHeaven.highLog.jooq.tables.pojos.Notices;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class NoticeRepository {

    private final DSLContext dsl;
    private final NoticesDao dao;
    private static final JNotices NOTICES = JNotices.NOTICES;

    public NoticeRepository(Configuration configuration, DSLContext dsl) {
        this.dao = new NoticesDao(configuration);
        this.dsl = dsl;
    }

    public List<Notices> findAll() {
        return dsl.selectFrom(NOTICES)
                .orderBy(NOTICES.IS_PINNED.desc(), NOTICES.CREATED_AT.desc())
                .fetchInto(Notices.class);
    }

    public List<Notices> findAllWithPaging(int page, int size) {
        return dsl.selectFrom(NOTICES)
                .orderBy(NOTICES.IS_PINNED.desc(), NOTICES.CREATED_AT.desc())
                .limit(size)
                .offset(page * size)
                .fetchInto(Notices.class);
    }

    public long count() {
        return dsl.fetchCount(NOTICES);
    }

    public Optional<Notices> findById(Long id) {
        return dao.fetchOptionalById(id);
    }

    public Notices save(Notices notice) {
        if (notice.getCreatedAt() == null) {
            notice.setCreatedAt(LocalDateTime.now());
        }
        notice.setUpdatedAt(LocalDateTime.now());

        dsl.insertInto(NOTICES)
                .set(NOTICES.TITLE, notice.getTitle())
                .set(NOTICES.CONTENT, notice.getContent())
                .set(NOTICES.IS_PINNED, notice.getIsPinned())
                .set(NOTICES.CREATED_AT, notice.getCreatedAt())
                .set(NOTICES.UPDATED_AT, notice.getUpdatedAt())
                .returning()
                .fetchOne();

        return dsl.selectFrom(NOTICES)
                .orderBy(NOTICES.ID.desc())
                .limit(1)
                .fetchOneInto(Notices.class);
    }

    public Notices update(Long id, Notices notice) {
        dsl.update(NOTICES)
                .set(NOTICES.TITLE, notice.getTitle())
                .set(NOTICES.CONTENT, notice.getContent())
                .set(NOTICES.IS_PINNED, notice.getIsPinned())
                .set(NOTICES.UPDATED_AT, LocalDateTime.now())
                .where(NOTICES.ID.eq(id))
                .execute();

        return findById(id).orElse(null);
    }

    public void deleteById(Long id) {
        dsl.deleteFrom(NOTICES)
                .where(NOTICES.ID.eq(id))
                .execute();
    }

    public boolean existsById(Long id) {
        return dsl.fetchExists(
                dsl.selectFrom(NOTICES)
                        .where(NOTICES.ID.eq(id))
        );
    }
}
