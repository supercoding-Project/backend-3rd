package com.github.scheduler.admin.service;

import com.github.scheduler.admin.dto.inquiry.*;
import com.github.scheduler.admin.entity.InquiryEntity;
import com.github.scheduler.admin.repository.InquiryRepository;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.repository.UserRepository;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
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
                .orElseThrow(() -> new AppException(ErrorCode.INQUIRY_NOT_FOUND,ErrorCode.INQUIRY_NOT_FOUND.getMessage()));

        if (inquiry.isPrivate()) {
            throw new AppException(ErrorCode.INQUIRY_PRIVATE_POST,ErrorCode.INQUIRY_PRIVATE_POST.getMessage());
        }
        return InquiryDetailResponseDTO.from(inquiry);
    }

    public Boolean verifyPassword(Long id, PasswordVerifyDTO dto) {
        InquiryEntity inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INQUIRY_NOT_FOUND,ErrorCode.INQUIRY_NOT_FOUND.getMessage()));
        if (!inquiry.getPassword().equals(dto.getPassword())) {
            throw new AppException(ErrorCode.INQUIRY_PASSWORD_NOT_MATCH,ErrorCode.INQUIRY_PASSWORD_NOT_MATCH.getMessage());
        }
        return true;
    }

    public void createInquiry(InquiryRequestDTO dto, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.ADMIN_USER_NOT_FOUND, ErrorCode.ADMIN_USER_NOT_FOUND.getMessage()));

        InquiryEntity inquiry = InquiryEntity.create(dto, user);
        inquiryRepository.save(inquiry);
    }

    public void updateInquiry(Long id, InquiryModifyRequestDTO dto, Long userId) {
        InquiryEntity inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INQUIRY_NOT_FOUND,ErrorCode.INQUIRY_NOT_FOUND.getMessage()));
        if (!inquiry.getUser().getUserId().equals(userId)) {
            throw new AppException(ErrorCode.INQUIRY_NOT_OWNER,ErrorCode.INQUIRY_NOT_OWNER.getMessage());
        }

        inquiry.update(dto.getTitle(), dto.getContent(), dto.getCategory(), dto.isPrivate(), dto.getPassword());
        inquiryRepository.save(inquiry);
    }

    public void deleteInquiry(Long id, Long userId) {
        InquiryEntity inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INQUIRY_NOT_FOUND, ErrorCode.INQUIRY_NOT_FOUND.getMessage()));

        if (!inquiry.getUser().getUserId().equals(userId)) {
            throw new AppException(ErrorCode.INQUIRY_NOT_OWNER,ErrorCode.INQUIRY_NOT_OWNER.getMessage());
        }
        inquiryRepository.delete(inquiry);
    }

}
