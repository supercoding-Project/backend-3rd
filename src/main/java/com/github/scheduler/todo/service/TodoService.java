package com.github.scheduler.todo.service;

import com.github.scheduler.calendar.entity.CalendarType;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import com.github.scheduler.schedule.dto.RepeatScheduleDto;
import com.github.scheduler.todo.dto.TodoResponseDto;
import com.github.scheduler.todo.entity.TodoEntity;
import com.github.scheduler.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;

    //할 일 조회
    @Transactional
    public List<TodoResponseDto> getTodo(CustomUserDetails customUserDetails, String view, String date, CalendarType calendarType) {
        // 인증된 사용자 확인
        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_FOUND_USER, ErrorCode.NOT_FOUND_USER.getMessage());
        }

        Long userId = customUserDetails.getUserEntity().getUserId();
        LocalDate targetDate = LocalDate.parse(date);
        LocalDate startDate;
        LocalDate endDate;

        if(view.equalsIgnoreCase("WEEKLY")) {
            startDate = targetDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
            endDate = targetDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
        } else if(view.equalsIgnoreCase("DAILY")) {
            startDate = targetDate;
            endDate = targetDate;
        } else if(view.equalsIgnoreCase("MONTHLY")) {
            startDate = targetDate.withDayOfMonth(1);
            endDate = targetDate.withDayOfMonth(targetDate.lengthOfMonth());
        } else {
            throw new AppException(ErrorCode.DATE_FORMAT_INCORRECT, ErrorCode.DATE_FORMAT_INCORRECT.getMessage());
        }

        return todoRepository.findTodosByCreateUser_UserIdAndTodoDateBetween(userId, startDate, endDate)
                .stream()
                .map(todoEntity -> TodoResponseDto.builder()
                        .todoId(todoEntity.getTodoId())
                        .createUserId(todoEntity.getCreateUser().getUserId())
                        .todoContent(todoEntity.getTodoContent())
                        .todoDate(todoEntity.getTodoDate())
                        .repeatSchedule(RepeatScheduleDto.builder()
                                .repeatType(todoEntity.getRepeatType() != null ? todoEntity.getRepeatType().name() : "NONE")
                                .repeatInterval(todoEntity.getRepeatInterval())
                                .repeatEndDate(todoEntity.getRepeatEndDate())
                                .build())
                        .memo(todoEntity.getMemo())
                        .calendarId(todoEntity.getCalendarId() != null ? todoEntity.getCalendarId().getCalendarId().toString() : null)
                        .build())
                .collect(Collectors.toList());

    }
}

