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
import com.github.scheduler.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final CalendarRepository calendarRepository;
    private final UserCalendarRepository userCalendarRepository;
    private final RedissonClient redissonClient;


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

        // 캘린더 타입 조건으로 필터링하여 할 일 조회
        List<TodoEntity> todoEntities = todoRepository.findByCreateUser_UserIdAndTodoDateBetweenAndCalendar_CalendarType(
                userId, startDate, endDate, calendarType);

        return todoEntities.stream()
                .map(todo -> TodoResponseDto.builder()
                        .todoId(todo.getTodoId())
                        .createUserId(todo.getCreateUser().getUserId())
                        .todoContent(todo.getTodoContent())
                        .todoDate(todo.getTodoDate())
                        .repeatSchedule(RepeatScheduleDto.builder()
                                .repeatType(todo.getRepeatType() != null ? todo.getRepeatType().name() : "NONE")
                                .repeatInterval(todo.getRepeatInterval())
                                .repeatEndDate(todo.getRepeatEndDate())
                                .build())
                        .memo(todo.getMemo())
                        .calendarId(todo.getCalendar() != null ? todo.getCalendar().getCalendarId().toString() : null)
                        .build())
                .collect(Collectors.toList());

    }

    //할 일 등록
    @Transactional
    public List<TodoCreateDto> createTodo(CustomUserDetails customUserDetails, TodoCreateDto todoCreateDto,
                                          CalendarType calendarType, Long calendarId) {
        // 인증된 사용자 확인
        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_FOUND_USER, ErrorCode.NOT_FOUND_USER.getMessage());
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

        CalendarEntity calendarEntity = calendarRepository.findById(calendarId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_CALENDAR, ErrorCode.NOT_FOUND_CALENDAR.getMessage()));

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
        return Collections.singletonList(
                TodoCreateDto.builder()
                        .todoId(savedTodo.getTodoId())
                        .todoContent(savedTodo.getTodoContent())
                        .todoDate(savedTodo.getTodoDate())
                        .memo(savedTodo.getMemo())
                        .repeatSchedule(RepeatScheduleDto.builder()
                                .repeatType(savedTodo.getRepeatType().name())
                                .repeatInterval(savedTodo.getRepeatInterval())
                                .repeatEndDate(savedTodo.getRepeatEndDate())
                                .build())
                        .calendarId(Long.valueOf(savedTodo.getCalendar().getCalendarId().toString()))
                        .build()
        );
    }

    //할 일 수정
    @Transactional
    public List<TodoUpdateDto> updateTodo(CustomUserDetails customUserDetails, TodoUpdateDto todoUpdateDto,
                                          Long todoId, CalendarType calendarType){
        // 인증된 사용자 확인
        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_FOUND_USER, ErrorCode.NOT_FOUND_USER.getMessage());
        }
        RLock lock = redissonClient.getLock("todo: " + todoId);
        boolean acquired = false;

        try{
            acquired = lock.tryLock(5, 5, TimeUnit.SECONDS);
            if (!acquired) {
                throw new AppException(ErrorCode.NOT_OBTAIN_LOCK, ErrorCode.NOT_OBTAIN_LOCK.getMessage());
            }
            //할 일 수정 코드
            TodoEntity todoEntity = todoRepository.findById(todoId)
                    .orElseThrow(() -> new AppException(ErrorCode.TODO_NOT_FOUND, ErrorCode.TODO_NOT_FOUND.getMessage()));
            Long currentUserId = customUserDetails.getUserEntity().getUserId();
            boolean canUpdate = false;
            CalendarEntity calendarEntity = todoEntity.getCalendar();

            if (calendarEntity.getCalendarType().equals(CalendarType.PERSONAL)) {
                canUpdate = todoEntity.getCreateUser().getUserId().equals(currentUserId);
            } else if (calendarEntity.getCalendarType().equals(CalendarType.SHARED) || calendarEntity.getCalendarType().equals(CalendarType.TODO)) {
                if (userCalendarRepository.existsByCalendarEntityCalendarIdAndUserEntityUserId(
                        calendarEntity.getCalendarId(), currentUserId)) {
                    canUpdate = true;
                }
            }

            if (!canUpdate) {
                throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS, ErrorCode.UNAUTHORIZED_ACCESS.getMessage());
            }

            //수정 할 필드
            todoEntity.setTodoContent(todoUpdateDto.getTodoContent());
            todoEntity.setTodoDate(todoUpdateDto.getTodoDate());
            todoEntity.setMemo(todoUpdateDto.getMemo());
            //반복 설정 수정
            if (todoUpdateDto.getRepeatSchedule() != null &&
                    todoUpdateDto.getRepeatSchedule().getRepeatType() != null &&
                    !todoUpdateDto.getRepeatSchedule().getRepeatType().isEmpty()) {
                RepeatType newRepeatType = RepeatType.valueOf(todoUpdateDto.getRepeatSchedule().getRepeatType().toUpperCase());
                todoEntity.setRepeatType(newRepeatType);
                todoEntity.setRepeatInterval(todoUpdateDto.getRepeatSchedule().getRepeatInterval());
                todoEntity.setRepeatEndDate(todoUpdateDto.getRepeatSchedule().getRepeatEndDate());
            }

            TodoEntity savedTodo = todoRepository.save(todoEntity);
            return Collections.singletonList(
                    TodoUpdateDto.builder()
                            .todoId(savedTodo.getTodoId())
                            .todoContent(savedTodo.getTodoContent())
                            .todoDate(savedTodo.getTodoDate())
                            .memo(savedTodo.getMemo())
                            .repeatSchedule(RepeatScheduleDto.builder()
                                    .repeatType(savedTodo.getRepeatType().name())
                                    .repeatInterval(savedTodo.getRepeatInterval())
                                    .repeatEndDate(savedTodo.getRepeatEndDate())
                                    .build())
                            .build()
            );

        }  catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException(ErrorCode.NOT_OBTAIN_LOCK, ErrorCode.NOT_OBTAIN_LOCK.getMessage());
        } finally {
            if (acquired) {
                lock.unlock();
            }
        }
    }

    //할 일 삭제
    @Transactional
    public TodoDeleteDto deleteTodo(CustomUserDetails customUserDetails, Long todoId, CalendarType calendarType) {
        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_AUTHORIZED_USER, ErrorCode.NOT_AUTHORIZED_USER.getMessage());
        }
        RLock lock = redissonClient.getLock("todo:" + todoId);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(5, 5, TimeUnit.SECONDS);
            if (!acquired) {
                throw new AppException(ErrorCode.NOT_OBTAIN_LOCK, ErrorCode.NOT_OBTAIN_LOCK.getMessage());
            }
            TodoEntity todoEntity = todoRepository.findById(todoId)
                    .orElseThrow(() -> new AppException(ErrorCode.TODO_NOT_FOUND, ErrorCode.TODO_NOT_FOUND.getMessage()));
            Long currentUserId = customUserDetails.getUserEntity().getUserId();
            boolean canDelete = false;
            CalendarEntity calendarEntity = todoEntity.getCalendar();
            //스캐쥴 타입 확인
            if (calendarEntity.getCalendarType().equals(CalendarType.PERSONAL)) {
                canDelete = todoEntity.getCreateUser().getUserId().equals(currentUserId);
            } else if (calendarEntity.getCalendarType().equals(CalendarType.SHARED)
                    || calendarEntity.getCalendarType().equals(CalendarType.TODO)) {
                if (userCalendarRepository.existsByCalendarEntityCalendarIdAndUserEntityUserId(
                        calendarEntity.getCalendarId(), currentUserId)) {
                    canDelete = true;
                }
            }
            if (!canDelete) {
                throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS, ErrorCode.UNAUTHORIZED_ACCESS.getMessage());
            }
            todoRepository.delete(todoEntity);
            return TodoDeleteDto.builder()
                    .todoId(todoId)
                    .message("할 일이 성공적으로 삭제되었습니다.")
                    .build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException(ErrorCode.NOT_OBTAIN_LOCK, ErrorCode.NOT_OBTAIN_LOCK.getMessage());
        } finally {
            if (acquired) {
                lock.unlock();
            }
        }
    }
}

