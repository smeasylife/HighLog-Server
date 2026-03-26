package goatHeaven.highLog.repository;

import goatHeaven.highLog.jooq.tables.JFaqs;
import goatHeaven.highLog.jooq.tables.daos.FaqsDao;
import goatHeaven.highLog.jooq.tables.pojos.Faqs;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class FaqRepository {

    private final DSLContext dsl;
    private final FaqsDao dao;
    private static final JFaqs FAQS = JFaqs.FAQS;

    public FaqRepository(Configuration configuration, DSLContext dsl) {
        this.dao = new FaqsDao(configuration);
        this.dsl = dsl;
    }

    public List<Faqs> findAll() {
        return dsl.selectFrom(FAQS)
                .orderBy(FAQS.DISPLAY_ORDER.asc(), FAQS.CREATED_AT.desc())
                .fetchInto(Faqs.class);
    }

    public List<Faqs> findAllWithPaging(int page, int size) {
        return dsl.selectFrom(FAQS)
                .orderBy(FAQS.DISPLAY_ORDER.asc(), FAQS.CREATED_AT.desc())
                .limit(size)
                .offset(page * size)
                .fetchInto(Faqs.class);
    }

    public List<Faqs> findByCategory(String category) {
        return dsl.selectFrom(FAQS)
                .where(FAQS.CATEGORY.eq(category))
                .orderBy(FAQS.DISPLAY_ORDER.asc(), FAQS.CREATED_AT.desc())
                .fetchInto(Faqs.class);
    }

    public List<Faqs> findByCategoryWithPaging(String category, int page, int size) {
        return dsl.selectFrom(FAQS)
                .where(FAQS.CATEGORY.eq(category))
                .orderBy(FAQS.DISPLAY_ORDER.asc(), FAQS.CREATED_AT.desc())
                .limit(size)
                .offset(page * size)
                .fetchInto(Faqs.class);
    }

    public Optional<Faqs> findById(Long id) {
        return dao.fetchOptionalById(id);
    }

    public Faqs save(Faqs faq) {
        if (faq.getCreatedAt() == null) {
            faq.setCreatedAt(LocalDateTime.now());
        }

        dsl.insertInto(FAQS)
                .set(FAQS.CATEGORY, faq.getCategory())
                .set(FAQS.QUESTION, faq.getQuestion())
                .set(FAQS.ANSWER, faq.getAnswer())
                .set(FAQS.DISPLAY_ORDER, faq.getDisplayOrder())
                .set(FAQS.CREATED_AT, faq.getCreatedAt())
                .execute();

        return dsl.selectFrom(FAQS)
                .orderBy(FAQS.ID.desc())
                .limit(1)
                .fetchOneInto(Faqs.class);
    }

    public Faqs update(Long id, Faqs faq) {
        dsl.update(FAQS)
                .set(FAQS.CATEGORY, faq.getCategory())
                .set(FAQS.QUESTION, faq.getQuestion())
                .set(FAQS.ANSWER, faq.getAnswer())
                .set(FAQS.DISPLAY_ORDER, faq.getDisplayOrder())
                .where(FAQS.ID.eq(id))
                .execute();

        return findById(id).orElse(null);
    }

    public void deleteById(Long id) {
        dsl.deleteFrom(FAQS)
                .where(FAQS.ID.eq(id))
                .execute();
    }

    public boolean existsById(Long id) {
        return dsl.fetchExists(
                dsl.selectFrom(FAQS)
                        .where(FAQS.ID.eq(id))
        );
    }

    public long count() {
        return dsl.fetchCount(FAQS);
    }

    public long countByCategory(String category) {
        return dsl.fetchCount(
                dsl.selectFrom(FAQS)
                        .where(FAQS.CATEGORY.eq(category))
        );
    }
}
