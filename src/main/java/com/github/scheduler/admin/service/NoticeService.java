package com.github.scheduler.admin.service;

import com.github.scheduler.admin.dto.notice.NoticeDetailResponseDTO;
import com.github.scheduler.admin.dto.notice.NoticeRequestDTO;
import com.github.scheduler.admin.dto.notice.NoticeResponseDTO;
import com.github.scheduler.admin.entity.NoticeEntity;
import com.github.scheduler.admin.repository.NoticeRepository;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public Page<NoticeResponseDTO> getAllNotices(String keyword, Pageable pageable) {
        return noticeRepository.findKeyword(keyword,pageable)
                .map(NoticeResponseDTO::from);
    }


    public NoticeDetailResponseDTO getNotice(long id) {
        NoticeEntity notice = noticeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOTICE_NOT_FOUND,ErrorCode.NOTICE_NOT_FOUND.getMessage()));
        return NoticeDetailResponseDTO.from(notice);
    }


    public void createNotice(NoticeRequestDTO dto) {
        NoticeEntity notice = NoticeEntity.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .build();
        noticeRepository.save(notice);
    }


    public void updateNotice(long id , NoticeRequestDTO dto) {
        NoticeEntity notice = noticeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOTICE_NOT_FOUND,ErrorCode.NOTICE_NOT_FOUND.getMessage()));
        notice.update(dto.getTitle(),dto.getContent());
        noticeRepository.save(notice);
    }

    public void deleteNotice(long id) {
        if (!noticeRepository.existsById(id)) {
            throw new AppException(ErrorCode.NOTICE_NOT_FOUND,ErrorCode.NOTICE_NOT_FOUND.getMessage());
        }
        noticeRepository.deleteById(id);
    }

}
