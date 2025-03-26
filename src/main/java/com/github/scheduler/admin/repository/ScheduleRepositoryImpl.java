package com.github.scheduler.admin.repository;

import com.github.scheduler.admin.dto.schedule.ResponseUserScheduleListDTO;
import com.github.scheduler.schedule.entity.ScheduleEntity;
import com.github.scheduler.schedule.entity.ScheduleStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ScheduleRepositoryImpl implements ScheduleRepositoryCustom {

    private final EntityManager em;

    @Override
    public Page<ResponseUserScheduleListDTO> findAllUserSchedules(String keyword, LocalDate start, LocalDate end, Pageable pageable) {
        Map<String, Object> params = createParams(keyword, start, end);

        // Content Query (정렬 포함)
        String contentQueryStr = createContentQuery(keyword, start, end, pageable);
        TypedQuery<ScheduleEntity> contentQuery = em.createQuery(contentQueryStr, ScheduleEntity.class);
        params.forEach(contentQuery::setParameter);
        contentQuery.setFirstResult((int) pageable.getOffset());
        contentQuery.setMaxResults(pageable.getPageSize());
        List<ScheduleEntity> resultList = contentQuery.getResultList();

        // Count Query (정렬 제외)
        String countQueryStr = createCountQuery(keyword, start, end);
        TypedQuery<Long> countQuery = em.createQuery(countQueryStr, Long.class);
        params.forEach(countQuery::setParameter);
        Long total = countQuery.getSingleResult();

        // DTO 매핑
        List<ResponseUserScheduleListDTO> dtoList = resultList.stream()
                .map(schedule -> ResponseUserScheduleListDTO.from(schedule.getCreateUserId(), List.of(schedule)))
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, total);
    }

    private Map<String, Object> createParams(String keyword, LocalDate start, LocalDate end) {
        Map<String, Object> params = new HashMap<>();
        params.put("status", ScheduleStatus.DELETED);

        if (keyword != null && !keyword.isBlank()) {
            params.put("keyword", "%" + keyword + "%");
        }
        if (start != null) {
            params.put("start", start.atStartOfDay());
        }
        if (end != null) {
            params.put("end", end.atTime(23, 59, 59));
        }

        return params;
    }

    private String createContentQuery(String keyword, LocalDate start, LocalDate end, Pageable pageable) {
        StringBuilder sb = new StringBuilder("SELECT s FROM ScheduleEntity s JOIN FETCH s.createUserId u WHERE s.scheduleStatus != :status");

        if (keyword != null && !keyword.isBlank()) {
            sb.append(" AND (u.email LIKE :keyword OR u.username LIKE :keyword)");
        }
        if (start != null) {
            sb.append(" AND s.createdAt >= :start");
        }
        if (end != null) {
            sb.append(" AND s.createdAt <= :end");
        }

        // 정렬 처리
        String sort = pageable.getSort().stream()
                .map(order -> "s." + order.getProperty() + " " + order.getDirection())
                .collect(Collectors.joining(", "));
        if (!sort.isBlank()) {
            sb.append(" ORDER BY ").append(sort);
        }

        return sb.toString();
    }

    private String createCountQuery(String keyword, LocalDate start, LocalDate end) {
        StringBuilder sb = new StringBuilder("SELECT COUNT(s) FROM ScheduleEntity s JOIN s.createUserId u WHERE s.scheduleStatus != :status");

        if (keyword != null && !keyword.isBlank()) {
            sb.append(" AND (u.email LIKE :keyword OR u.username LIKE :keyword)");
        }
        if (start != null) {
            sb.append(" AND s.createdAt >= :start");
        }
        if (end != null) {
            sb.append(" AND s.createdAt <= :end");
        }

        return sb.toString();
    }
}
