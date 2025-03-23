package com.github.scheduler.admin.service;

import com.github.scheduler.admin.dto.inquiryAnswer.AnswerRequestDTO;
import com.github.scheduler.admin.entity.InquiryAnswerEntity;
import com.github.scheduler.admin.entity.InquiryEntity;
import com.github.scheduler.admin.repository.InquiryAnswerRepository;
import com.github.scheduler.admin.repository.InquiryRepository;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InquiryAnswerService {

    private final InquiryAnswerRepository inquiryAnswerRepository;
    private final InquiryRepository inquiryRepository;

    public void createAnswer(long inquiryId, AnswerRequestDTO dto, UserEntity admin) {
        InquiryEntity inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new AppException(ErrorCode.INQUIRY_NOT_FOUND,ErrorCode.INQUIRY_NOT_FOUND.getMessage()));

        if (inquiry.isAnswered()) {
            throw new AppException(ErrorCode.INQUIRY_ALREADY_ANSWERED,ErrorCode.INQUIRY_ALREADY_ANSWERED.getMessage());
        }

        InquiryAnswerEntity answer = InquiryAnswerEntity.builder()
                .inquiry(inquiry)
                .content(dto.getContent())
                .answeredAt(LocalDateTime.now())
                .admin(admin)
                .build();

        inquiry.markAsAnswered();  // 답변 등록으로 상태 변경

        inquiryAnswerRepository.save(answer);


    }

    public void updateAnswer(long answerId, AnswerRequestDTO dto) {
        InquiryAnswerEntity answer = inquiryAnswerRepository.findById(answerId)
                .orElseThrow(() -> new AppException(ErrorCode.INQUIRY_ANSWER_NOT_FOUND, ErrorCode.INQUIRY_ANSWER_NOT_FOUND.getMessage()));

        answer.updateContent(dto.getContent());
        inquiryAnswerRepository.save(answer);
    }

    @Transactional
    public void deleteAnswer(long answerId) {
        InquiryAnswerEntity answer = inquiryAnswerRepository.findById(answerId)
                .orElseThrow(() -> new AppException(ErrorCode.INQUIRY_ANSWER_NOT_FOUND, ErrorCode.INQUIRY_ANSWER_NOT_FOUND.getMessage()));

        InquiryEntity inquiry = answer.getInquiry();

        inquiryAnswerRepository.delete(answer);

        // 답변이 하나도 안 남았으면 answered = false
        boolean hasOtherAnswers = inquiryAnswerRepository.existsByInquiry(inquiry);
        if (!hasOtherAnswers) {
            inquiry.markAsUnanswered();
        }
    }
}
