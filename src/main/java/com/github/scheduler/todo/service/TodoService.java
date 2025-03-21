package com.github.scheduler.todo.service;

import com.github.scheduler.calendar.entity.CalendarEntity;
import com.github.scheduler.calendar.entity.CalendarType;
import com.github.scheduler.calendar.repository.CalendarRepository;
import com.github.scheduler.calendar.repository.UserCalendarRepository;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import com.github.scheduler.schedule.dto.RepeatScheduleDto;
import com.github.scheduler.schedule.entity.RepeatType;
import com.github.scheduler.todo.dto.TodoCreateDto;
import com.github.scheduler.todo.dto.TodoDeleteDto;
import com.github.scheduler.todo.dto.TodoResponseDto;
import com.github.scheduler.todo.dto.TodoUpdateDto;
import com.github.scheduler.todo.entity.TodoEntity;
import com.github.scheduler.todo.event.TodoDeleteEvent;
import com.github.scheduler.todo.event.TodoUpdateEvent;
import com.github.scheduler.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final CalendarRepository calendarRepository;
    private final UserCalendarRepository userCalendarRepository;
    private final ApplicationEventPublisher eventPublisher;


    //할 일 조회
    @Transactional
    public List<TodoResponseDto> getTodo(CustomUserDetails customUserDetails, String view, String date, Long calendarId) {
        // 인증된 사용자 확인
        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_FOUND_USER, ErrorCode.NOT_FOUND_USER.getMessage());
        }

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

        // calendarId를 기준으로 할 일 조회
        List<TodoEntity> todoEntities = todoRepository.findByCalendarCalendarIdAndTodoDateBetween(calendarId, startDate, endDate);
        List<TodoResponseDto> todoResponseDto = new ArrayList<>();
        for (TodoEntity todo : todoEntities){
            TodoResponseDto dto = convertTodoEntityToDto(todo);
            todoResponseDto.add(dto);
        }

        todoResponseDto.sort(Comparator.comparing(TodoResponseDto::getTodoDate));
        return todoResponseDto;
    }

    private TodoResponseDto convertTodoEntityToDto(TodoEntity todoEntity){
        return TodoResponseDto.builder()
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
                .calendarId(todoEntity.getCalendar() != null ? todoEntity.getCalendar().getCalendarId().toString() : null)
                .build();

    }

    //할 일 등록
    @Transactional
    public List<TodoCreateDto> createTodo(CustomUserDetails customUserDetails, TodoCreateDto todoCreateDto, Long calendarId) {
        // 인증된 사용자 확인
        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_FOUND_USER, ErrorCode.NOT_FOUND_USER.getMessage());
        }

        CalendarEntity calendarEntity = calendarRepository.findById(calendarId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_CALENDAR,ErrorCode.NOT_FOUND_CALENDAR.getMessage()));

        if (!(calendarEntity.getCalendarType().equals(CalendarType.PERSONAL)
                || calendarEntity.getCalendarType().equals(CalendarType.SHARED)
                || calendarEntity.getCalendarType().equals(CalendarType.TODO))) {
            throw new AppException(ErrorCode.INVALID_CALENDAR_TYPE, ErrorCode.INVALID_CALENDAR_TYPE.getMessage());
        }

        // 반복 설정 처리
        RepeatType repeatType = RepeatType.NONE;
        int repeatInterval = 0;
        LocalDate repeatEndDate = null;
        if (todoCreateDto.getRepeatSchedule() != null &&
                todoCreateDto.getRepeatSchedule().getRepeatType() != null &&
                !todoCreateDto.getRepeatSchedule().getRepeatType().isEmpty()) {
            repeatType = RepeatType.valueOf(todoCreateDto.getRepeatSchedule().getRepeatType().toUpperCase());
            repeatInterval = todoCreateDto.getRepeatSchedule().getRepeatInterval();
            repeatEndDate = todoCreateDto.getRepeatSchedule().getRepeatEndDate();
        }

        TodoEntity todoEntity = TodoEntity.builder()
                .createUser(customUserDetails.getUserEntity())
                .todoContent(todoCreateDto.getTodoContent())
                .todoDate(todoCreateDto.getTodoDate())
                .memo(todoCreateDto.getMemo())
                .repeatType(repeatType)
                .repeatInterval(repeatInterval)
                .repeatEndDate(repeatEndDate)
                .completed(false)
                .calendar(calendarEntity)
                .build();

        TodoEntity savedTodo = todoRepository.save(todoEntity);
        TodoCreateDto saveTodoCreateDto = convertTodoEntityToTodoCreateDto(savedTodo);
        return Collections.singletonList(saveTodoCreateDto);
    }

    private TodoCreateDto convertTodoEntityToTodoCreateDto(TodoEntity todoEntity) {
        return TodoCreateDto.builder()
                .todoId(todoEntity.getTodoId())
                .todoContent(todoEntity.getTodoContent())
                .todoDate(todoEntity.getTodoDate())
                .memo(todoEntity.getMemo())
                .repeatSchedule(RepeatScheduleDto.builder()
                        .repeatType(todoEntity.getRepeatType().name())
                        .repeatInterval(todoEntity.getRepeatInterval())
                        .repeatEndDate(todoEntity.getRepeatEndDate())
                        .build())
                .calendarId(todoEntity.getCalendar() != null ?
                        Long.valueOf(todoEntity.getCalendar().getCalendarId().toString()) : null)
                .build();
    }

    //할 일 수정
    @Transactional
    public List<TodoUpdateDto> updateTodo(CustomUserDetails customUserDetails, TodoUpdateDto todoUpdateDto,
                                          Long todoId, Long calendarId){
        // 인증된 사용자 확인
        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_FOUND_USER, ErrorCode.NOT_FOUND_USER.getMessage());
        }

        TodoEntity todoEntity = todoRepository.findById(todoId)
                .orElseThrow(() -> new AppException(ErrorCode.TODO_NOT_FOUND, ErrorCode.TODO_NOT_FOUND.getMessage()));

        CalendarEntity calendarEntity = todoEntity.getCalendar();
        if (calendarEntity == null || !calendarEntity.getCalendarId().equals(calendarId)) {
            throw new AppException(ErrorCode.INVALID_CALENDAR_ID, ErrorCode.INVALID_CALENDAR_ID.getMessage());
        }

        Long currentUserId = customUserDetails.getUserEntity().getUserId();

        // 캘린더 타입에 따른 수정 권한 검증
        if (calendarEntity.getCalendarType().equals(CalendarType.PERSONAL)) {
            //PERSONAL 경우 작성자만 수정 가능
            if (!todoEntity.getCreateUser().getUserId().equals(currentUserId)) {
                throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS, ErrorCode.UNAUTHORIZED_ACCESS.getMessage());
            }
        } else if (calendarEntity.getCalendarType().equals(CalendarType.SHARED) || calendarEntity.getCalendarType().equals(CalendarType.TODO)) {
            //SHARED 또는 할 일 경우 캘린더에 포함되어있는 사용자일 경우 수정 가능
            if (!userCalendarRepository.existsByCalendarEntityCalendarIdAndUserEntityUserId(
                    calendarEntity.getCalendarId(), currentUserId)) {
                throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS, ErrorCode.UNAUTHORIZED_ACCESS.getMessage());
            }
        } else {
            throw new AppException(ErrorCode.INVALID_CALENDAR_TYPE, ErrorCode.INVALID_CALENDAR_TYPE.getMessage());
        }

        //수정 할 필드
        todoEntity.setTodoContent(todoUpdateDto.getTodoContent());
        todoEntity.setTodoDate(todoUpdateDto.getTodoDate());
        todoEntity.setMemo(todoUpdateDto.getMemo());
        todoEntity.setCompleted(todoUpdateDto.getCompleted());
        //반복 설정 수정
        if (todoUpdateDto.getRepeatSchedule() != null &&
                todoUpdateDto.getRepeatSchedule().getRepeatType() != null &&
                !todoUpdateDto.getRepeatSchedule().getRepeatType().isEmpty()) {
            RepeatType newRepeatType = RepeatType.valueOf(todoUpdateDto.getRepeatSchedule().getRepeatType().toUpperCase());
            todoEntity.setRepeatType(newRepeatType);
            todoEntity.setRepeatInterval(todoUpdateDto.getRepeatSchedule().getRepeatInterval());
            todoEntity.setRepeatEndDate(todoUpdateDto.getRepeatSchedule().getRepeatEndDate());
        }

        try {
            eventPublisher.publishEvent(new TodoUpdateEvent(todoId, "할 일이 성공적으로 업데이트되었습니다.", true));
            todoRepository.flush();
            return Collections.singletonList(TodoUpdateDto.builder()
                    .todoId(todoEntity.getTodoId())
                    .todoContent(todoEntity.getTodoContent())
                    .todoDate(todoEntity.getTodoDate())
                    .memo(todoEntity.getMemo())
                    .completed(todoEntity.getCompleted())
                    .repeatSchedule(RepeatScheduleDto.builder()
                            .repeatType(todoEntity.getRepeatType().name())
                            .repeatInterval(todoEntity.getRepeatInterval())
                            .repeatEndDate(todoEntity.getRepeatEndDate())
                            .build())
                    .build());
        } catch (OptimisticLockingFailureException ex) {
            eventPublisher.publishEvent(new TodoUpdateEvent(todoId, "동시 수정 충돌로 인해 업데이트에 실패했습니다.", false));
            throw new AppException(ErrorCode.NOT_UPDATE, ErrorCode.NOT_UPDATE.getMessage());
        }
    }

    //할 일 삭제(수정 필요)
    @Transactional
    public TodoDeleteDto deleteTodo(CustomUserDetails customUserDetails, Long todoId, Long calendarId) {
        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_AUTHORIZED_USER, ErrorCode.NOT_AUTHORIZED_USER.getMessage());
        }

        TodoEntity todoEntity = todoRepository.findById(todoId)
                .orElseThrow(() -> new AppException(ErrorCode.TODO_NOT_FOUND, ErrorCode.TODO_NOT_FOUND.getMessage()));

        CalendarEntity calendarEntity = todoEntity.getCalendar();
        if (calendarEntity == null || !calendarEntity.getCalendarId().equals(calendarId)) {
            throw new AppException(ErrorCode.INVALID_CALENDAR_ID, ErrorCode.INVALID_CALENDAR_ID.getMessage());
        }

        Long currentUserId = customUserDetails.getUserEntity().getUserId();
        boolean canDelete = calendarEntity.getCalendarType().equals(CalendarType.PERSONAL)
                ? todoEntity.getCreateUser().getUserId().equals(currentUserId)
                : userCalendarRepository.existsByCalendarEntityCalendarIdAndUserEntityUserId(calendarId, currentUserId);

        if (!canDelete) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS, ErrorCode.UNAUTHORIZED_ACCESS.getMessage());
        }

        try {
            todoRepository.delete(todoEntity);
            todoRepository.flush();
            eventPublisher.publishEvent(new TodoDeleteEvent(todoId, "할 일이 성공적으로 삭제되었습니다.", true));

            return TodoDeleteDto.builder()
                    .todoId(todoId)
                    .message("할 일이 성공적으로 삭제되었습니다.")
                    .build();
        } catch (OptimisticLockingFailureException ex) {
            eventPublisher.publishEvent(new TodoDeleteEvent(todoId, "동시 삭제 충돌로 인해 삭제 실패하였습니다.", false));
            throw new AppException(ErrorCode.NOT_DELETE, ErrorCode.NOT_DELETE.getMessage());
        }
    }
}

