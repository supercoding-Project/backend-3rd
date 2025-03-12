package com.github.scheduler.alarm.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/alarm")
@RequiredArgsConstructor
public class AlarmController {

    @Operation(summary = "스케줄 알람 정보 조회", description = "")
    @GetMapping("/schedule")
    public void myScheduleAlarm() {

    }
//    @Operation(summary = "Q&A 알람 정보 조회", description = "")
//    @GetMapping("/qna")
//    public void myQnAlarm() {
//
//    }
//    @Operation(summary = "공지사항 알람 정보 조회", description = "")
//    @GetMapping("/notice")
//    public void myNoticeAlarm() {

//    }
    @Operation(summary = "읽지않은 알람 정보 조회", description = "")
    @GetMapping
    public void myAlarmCount() {

    }
}
