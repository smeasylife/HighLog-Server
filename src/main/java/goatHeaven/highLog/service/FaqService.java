package goatHeaven.highLog.service;

import goatHeaven.highLog.domain.Faq;
import goatHeaven.highLog.dto.request.FaqRequest;
import goatHeaven.highLog.dto.response.FaqResponse;
import goatHeaven.highLog.exception.CustomException;
import goatHeaven.highLog.exception.ErrorCode;
import goatHeaven.highLog.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaqService {

    private final FaqRepository faqRepository;

    public List<FaqResponse> getFaqs(String category) {
        List<Faq> faqs;
        if (category != null && !category.isBlank()) {
            faqs = faqRepository.findByCategoryOrderByDisplayOrderAsc(category);
        } else {
            faqs = faqRepository.findAllByOrderByDisplayOrderAsc();
        }
        return faqs.stream()
                .map(FaqResponse::from)
                .toList();
    }

    @Transactional
    public FaqResponse createFaq(FaqRequest request) {
        Faq faq = Faq.builder()
                .category(request.getCategory())
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .displayOrder(request.getDisplayOrder())
                .build();

        Faq savedFaq = faqRepository.save(faq);
        return FaqResponse.from(savedFaq);
    }

    @Transactional
    public FaqResponse updateFaq(Long id, FaqRequest request) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.FAQ_NOT_FOUND));

        faq.update(request.getCategory(), request.getQuestion(),
                request.getAnswer(), request.getDisplayOrder());
        return FaqResponse.from(faq);
    }

    @Transactional
    public void deleteFaq(Long id) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.FAQ_NOT_FOUND));
        faqRepository.delete(faq);
    }
}
