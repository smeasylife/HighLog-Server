package goatHeaven.highLog.repository;

import goatHeaven.highLog.domain.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {

    List<Faq> findAllByOrderByDisplayOrderAsc();

    List<Faq> findByCategoryOrderByDisplayOrderAsc(String category);
}
