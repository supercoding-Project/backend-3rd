package com.github.scheduler.todo.controller;

import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.dto.ApiResponse;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import com.github.scheduler.todo.dto.TodoCreateDto;
import com.github.scheduler.todo.dto.TodoDeleteDto;
import com.github.scheduler.todo.dto.TodoResponseDto;
import com.github.scheduler.todo.dto.TodoUpdateDto;
import com.github.scheduler.todo.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/todo")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    // 할 일 조회: 월별, 주별, 일별 조회
    @Operation(summary = "할 일 조회: 월별, 주별, 일별 조회", description = "사용자의 할 일을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TodoResponseDto>>> getTodo(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(name = "view", defaultValue = "MONTHLY") String view,
            @RequestParam(name = "date", required = false,  defaultValue = "") String date,
            @RequestParam(name = "calendarId") List<Long> calendarId) {

        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_AUTHORIZED_USER, ErrorCode.NOT_AUTHORIZED_USER.getMessage());
        }

        if (calendarId == null) {
            throw new AppException(ErrorCode.NOT_FOUND_CALENDAR, ErrorCode.NOT_FOUND_CALENDAR.getMessage());
        }

        if (date == null || date.isEmpty()) {
            date = LocalDate.now().toString();
        }

        List<TodoResponseDto> todoResponse = todoService.getTodo(customUserDetails, view, date, calendarId);
        return ResponseEntity.ok(ApiResponse.success(todoResponse));
    }

    // 할 일 등록
    @Operation(summary = "할 일 등록", description = "새로운 할 일을 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<List<TodoCreateDto>>> createTodo(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody TodoCreateDto todoCreateDto,
            @RequestParam(name = "calendarId") Long calendarId) {

        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_AUTHORIZED_USER, ErrorCode.NOT_AUTHORIZED_USER.getMessage());
        }

        if (calendarId == null) {
            throw new AppException(ErrorCode.NOT_FOUND_CALENDAR, ErrorCode.NOT_FOUND_CALENDAR.getMessage());
        }

        List<TodoCreateDto> createdTodo = todoService.createTodo(customUserDetails, todoCreateDto, calendarId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdTodo));
    }

    // 할 일 수정
    @Operation(summary = "할 일 수정", description = "할 일을 수정합니다.")
    @PutMapping("/{todoId}")
    public ResponseEntity<ApiResponse<List<TodoUpdateDto>>> updateTodo(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody TodoUpdateDto todoUpdateDto,
            @PathVariable("todoId") Long todoId,
            @RequestParam(name = "calendarId") Long calendarId) {

        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_AUTHORIZED_USER, ErrorCode.NOT_AUTHORIZED_USER.getMessage());
        }

        if (calendarId == null) {
            throw new AppException(ErrorCode.NOT_FOUND_CALENDAR, ErrorCode.NOT_FOUND_CALENDAR.getMessage());
        }

        List<TodoUpdateDto> updatedTodo = todoService.updateTodo(customUserDetails, todoUpdateDto, todoId, calendarId);
        return ResponseEntity.ok(ApiResponse.success(updatedTodo));
    }

    // 할 일 삭제
    @Operation(summary = "할 일 삭제", description = "할 일을 삭제합니다.")
    @DeleteMapping("/{todoId}")
    public ResponseEntity<ApiResponse<TodoDeleteDto>> deleteTodo(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable("todoId") Long todoId,
            @RequestParam(name = "calendarId") Long calendarId) {

        if (customUserDetails == null) {
            throw new AppException(ErrorCode.NOT_AUTHORIZED_USER, ErrorCode.NOT_AUTHORIZED_USER.getMessage());
        }

        if (calendarId == null) {
            throw new AppException(ErrorCode.NOT_FOUND_CALENDAR, ErrorCode.NOT_FOUND_CALENDAR.getMessage());
        }

        TodoDeleteDto todoDelete = todoService.deleteTodo(customUserDetails, todoId, calendarId);
        return ResponseEntity.ok(ApiResponse.success(todoDelete));
    }
}

