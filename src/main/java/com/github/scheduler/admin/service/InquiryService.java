package com.github.scheduler.admin.service;

import com.github.scheduler.admin.dto.inquiry.*;
import com.github.scheduler.admin.entity.InquiryEntity;
import com.github.scheduler.admin.repository.InquiryRepository;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    public List<InquiryListResponseDTO> getAllInquiries() {
        List<InquiryEntity> inquiries = inquiryRepository.findAll();

        return inquiries.stream()
                .map(inquiry -> {
                    if (inquiry.isPrivate()) {
                        return InquiryListResponseDTO.ofHidden(inquiry);
                    }
                    return InquiryListResponseDTO.of(inquiry);
                })
                .toList();
    }

    public InquiryDetailResponseDTO getInquiry(Long id) {
        InquiryEntity inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 문의글을 찾을 수 없습니다."));

        if (inquiry.isPrivate()) {
            throw new RuntimeException("비공개 글입니다. 비밀번호 확인이 필요합니다.");
        }
        return InquiryDetailResponseDTO.from(inquiry);
    }

    public Boolean verifyPassword(Long id, PasswordVerifyDTO dto) {
        InquiryEntity inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 문의글을 찾을 수 없습니다."));
        if (!inquiry.getPassword().equals(dto.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        return true;
    }

    public void createInquiry(InquiryRequestDTO dto, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        InquiryEntity inquiry = InquiryEntity.create(dto, user);
        inquiryRepository.save(inquiry);
    }

    public void updateInquiry(Long id, InquiryModifyRequestDTO dto, Long userId) {
        InquiryEntity inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 문의글을 찾을 수 없습니다."));
        if (!inquiry.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("본인의 문의글만 수정할 수 있습니다.");
        }

        inquiry.update(dto.getTitle(), dto.getContent(), dto.getCategory(), dto.isPrivate(), dto.getPassword());
        inquiryRepository.save(inquiry);
    }

    public void deleteInquiry(Long id, Long userId) {
        InquiryEntity inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 문의글을 찾을 수 없습니다."));

        if (!inquiry.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("본인의 문의글만 삭제할 수 있습니다.");
        }
        inquiryRepository.delete(inquiry);
    }

}
