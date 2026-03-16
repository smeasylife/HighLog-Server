package goatHeaven.highLog.service;

import goatHeaven.highLog.domain.Role;
import goatHeaven.highLog.dto.request.FaqRequest;
import goatHeaven.highLog.dto.response.FaqPageResponse;
import goatHeaven.highLog.dto.response.FaqResponse;
import goatHeaven.highLog.exception.CustomException;
import goatHeaven.highLog.exception.ErrorCode;
import goatHeaven.highLog.jooq.tables.pojos.Faqs;
import goatHeaven.highLog.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaqService {

    private final FaqRepository faqRepository;
    private static final int DEFAULT_PAGE_SIZE = 6;

    public List<FaqResponse> getAllFaqs() {
        return faqRepository.findAll().stream()
                .map(FaqResponse::from)
                .collect(Collectors.toList());
    }

    public FaqPageResponse getFaqsWithPaging(String category, int page, Integer size) {
        int pageSize = (size != null && size > 0) ? size : DEFAULT_PAGE_SIZE;

        List<FaqResponse> faqs;
        long totalElements;

        if (category != null && !category.isBlank()) {
            faqs = faqRepository.findByCategoryWithPaging(category, page, pageSize).stream()
                    .map(FaqResponse::from)
                    .collect(Collectors.toList());
            totalElements = faqRepository.countByCategory(category);
        } else {
            faqs = faqRepository.findAllWithPaging(page, pageSize).stream()
                    .map(FaqResponse::from)
                    .collect(Collectors.toList());
            totalElements = faqRepository.count();
        }

        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        return FaqPageResponse.of(faqs, page, totalPages, totalElements);
    }

    public FaqResponse getFaqById(Long id) {
        Faqs faq = faqRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.FAQ_NOT_FOUND));
        return FaqResponse.from(faq);
    }

    @Transactional
    public FaqResponse createFaq(Role role, FaqRequest request) {
        validateAdminRole(role);

        Faqs faq = new Faqs();
        faq.setCategory(request.category());
        faq.setQuestion(request.question());
        faq.setAnswer(request.answer());
        faq.setDisplayOrder(request.displayOrder());

        Faqs savedFaq = faqRepository.save(faq);
        return FaqResponse.from(savedFaq);
    }

    @Transactional
    public FaqResponse updateFaq(Role role, Long id, FaqRequest request) {
        validateAdminRole(role);

        if (!faqRepository.existsById(id)) {
            throw new CustomException(ErrorCode.FAQ_NOT_FOUND);
        }

        Faqs faq = new Faqs();
        faq.setCategory(request.category());
        faq.setQuestion(request.question());
        faq.setAnswer(request.answer());
        faq.setDisplayOrder(request.displayOrder());

        Faqs updatedFaq = faqRepository.update(id, faq);
        return FaqResponse.from(updatedFaq);
    }

    @Transactional
    public void deleteFaq(Role role, Long id) {
        validateAdminRole(role);

        if (!faqRepository.existsById(id)) {
            throw new CustomException(ErrorCode.FAQ_NOT_FOUND);
        }

        faqRepository.deleteById(id);
    }

    private void validateAdminRole(Role role) {
        if (role != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }
    }
}
