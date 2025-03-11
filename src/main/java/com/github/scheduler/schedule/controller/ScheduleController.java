package com.github.scheduler.schedule.controller;

import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.schedule.dto.CreateScheduleDto;
import com.github.scheduler.schedule.dto.ScheduleDto;
import com.github.scheduler.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    //TODO:일정 조회(monthly,weekly,daily)
    @Operation(summary = "일정 조회: 월별, 주별, 일별 조회")
    @GetMapping
    public ResponseEntity<List<ScheduleDto>> getSchedules(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(name = "view", defaultValue = "monthly") String view,
            @RequestParam(name = "date") String date) {

    }

    //TODO:일정 등록
    @Operation(summary = "개인 또는 팀 일정 등록")
    @PostMapping("/{scheduleId}")
    public ResponseEntity<ScheduleDto> createSchedule(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody CreateScheduleDto createScheduleDto){

    }

    //TODO:일정 수정
    @Operation(summary = "개인 또는 팀 일정 수정")
    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleDto> updateSchedule(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody ScheduleDto scheduleDto){

    }

    //TODO:일정 삭제
    @Operation(summary = "개인 또는 팀 일정 삭제")
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable("scheduleId") Long scheduleId){

    }

}
