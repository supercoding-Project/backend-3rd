package com.github.scheduler.admin.service;

import com.github.scheduler.admin.dto.faq.FaqDetailResponseDTO;
import com.github.scheduler.admin.dto.faq.FaqListResponseDTO;
import com.github.scheduler.admin.dto.faq.FaqRequestDTO;
import com.github.scheduler.admin.entity.FaqCategory;
import com.github.scheduler.admin.entity.FaqEntity;
import com.github.scheduler.admin.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqRepository faqRepository;


    public List<FaqListResponseDTO> getAllFaqs() {
        return faqRepository.findAll().stream()
                .map(FaqListResponseDTO::from)
                .collect(Collectors.toList());
    }

    public List<FaqListResponseDTO> getFaqsByCategory(FaqCategory category) {
        return faqRepository.findByCategory(category).stream()
                .map(FaqListResponseDTO::from)
                .collect(Collectors.toList());
    }

    public FaqDetailResponseDTO getFaq(long id) {
        FaqEntity faq =  faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("faq not found"));

        return FaqDetailResponseDTO.from(faq);
    }

    public void createFaq(FaqRequestDTO dto) {
        FaqEntity faq = FaqEntity.builder()
                .question(dto.getQuestion())
                .answer(dto.getAnswer())
                .category(dto.getCategory())
                .build();
        faqRepository.save(faq);
    }

    public void updateFaq(long id, FaqRequestDTO dto) {
        FaqEntity faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("faq not found"));

        faq.update(dto.getQuestion(),dto.getAnswer(),dto.getCategory());
        faqRepository.save(faq);
    }

    public void deleteFaq(long id) {
        if (!faqRepository.existsById(id)) {
            throw new RuntimeException("faq not found");
        }
        faqRepository.deleteById(id);
    }
}
