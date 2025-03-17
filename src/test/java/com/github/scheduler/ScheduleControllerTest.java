package com.github.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.calendar.entity.CalendarType;
import com.github.scheduler.global.config.auth.JwtTokenProvider;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.schedule.controller.ScheduleController;
import com.github.scheduler.schedule.dto.CreateScheduleDto;
import com.github.scheduler.schedule.dto.DeleteScheduleDto;
import com.github.scheduler.schedule.dto.ScheduleDto;
import com.github.scheduler.schedule.service.ScheduleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ScheduleController.class)
public class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScheduleService scheduleService;

    // JwtTokenProvider는 Security 관련 컴포넌트 로딩 때문에 모의 빈으로 등록합니다.
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    // JavaTimeModule 등록 ObjectMapper (Java 8 날짜/시간 직렬화를 위해)
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // 테스트용 CustomUserDetails 생성 헬퍼 메서드
    private CustomUserDetails createTestUser() {
        UserEntity userEntity = UserEntity.builder()
                .userId(1L)
                .username("testUser")
                .build();
        return new CustomUserDetails(userEntity);
    }

    // GET 엔드포인트 테스트 (일정 조회)
    @Test
    public void testGetSchedules() throws Exception {
        List<ScheduleDto> schedules = new ArrayList<>();
        ScheduleDto sampleDto = ScheduleDto.builder()
                .scheduleId(1L)
                .title("테스트 스케줄")
                .startTime(LocalDateTime.of(2025, 3, 20, 10, 0))
                .endTime(LocalDateTime.of(2025, 3, 20, 11, 0))
                .build();
        schedules.add(sampleDto);

        given(scheduleService.getSchedules(any(CustomUserDetails.class), anyString(), anyString(), anyList()))
                .willReturn(schedules);

        mockMvc.perform(get("/api/v1/schedules")
                        .param("view", "MONTHLY")
                        .param("date", "2025-03-20")
                        .param("calendarTypes", "PERSONAL")
                        .with(user(createTestUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data[0].title").value("테스트 스케줄"));
    }

    // POST 엔드포인트 테스트 (일정 등록)
    @Test
    public void testCreateSchedule() throws Exception {
        CreateScheduleDto createScheduleDto = CreateScheduleDto.builder()
                .title("새 스케줄")
                .location("회의실 A")
                .startTime(LocalDateTime.of(2025, 3, 21, 10, 0))
                .endTime(LocalDateTime.of(2025, 3, 21, 11, 0))
                .build();

        List<CreateScheduleDto> createdList = Collections.singletonList(createScheduleDto);
        given(scheduleService.createSchedule(any(CustomUserDetails.class),
                any(CreateScheduleDto.class),
                any(CalendarType.class),
                anyLong()))
                .willReturn(createdList);

        mockMvc.perform(post("/api/v1/schedules")
                        .param("calendarType", "PERSONAL")
                        .param("calendarId", "123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createScheduleDto))
                        .with(user(createTestUser()))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data[0].title").value("새 스케줄"));
    }

    // PUT 엔드포인트 테스트 (일정 수정)
    @Test
    public void testUpdateSchedule() throws Exception {
        CreateScheduleDto updateScheduleDto = CreateScheduleDto.builder()
                .title("수정된 스케줄")
                .location("회의실 B")
                .startTime(LocalDateTime.of(2025, 3, 21, 10, 30))
                .endTime(LocalDateTime.of(2025, 3, 21, 12, 0))
                .build();

        List<CreateScheduleDto> updatedList = Collections.singletonList(updateScheduleDto);
        given(scheduleService.updateSchedule(any(CustomUserDetails.class),
                any(CreateScheduleDto.class),
                anyLong(),
                any(CalendarType.class)))
                .willReturn(updatedList);

        mockMvc.perform(put("/api/v1/schedules/{scheduleId}", 1L)
                        .param("calendarType", "SHARED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateScheduleDto))
                        .with(user(createTestUser()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("수정된 스케줄"));
    }

    @Test
    public void testDeleteSchedule() throws Exception {
        // 삭제 요청에 필요한 DeleteScheduleDto (본문 내용이 필요하면 추가)
        DeleteScheduleDto deleteScheduleDto = DeleteScheduleDto.builder().build();

        // 예상 삭제 결과 DTO 생성
        DeleteScheduleDto expectedResponse = DeleteScheduleDto.builder()
                .scheduleId(1L)
                .message("일정이 성공적으로 삭제되었습니다.")
                .build();

        // 서비스에서 deleteSchedule 호출 시 예상 결과를 반환하도록 모킹
        given(scheduleService.deleteSchedule(
                any(CustomUserDetails.class),
                any(DeleteScheduleDto.class),
                anyLong(),
                any(CalendarType.class)))
                .willReturn(expectedResponse);

        // DELETE 요청 실행
        mockMvc.perform(delete("/api/v1/schedules/{scheduleId}", 1L)
                        .param("calendarType", "PERSONAL")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(deleteScheduleDto))
                        .with(user(createTestUser()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scheduleId").value(1L))
                .andExpect(jsonPath("$.data.message").value("일정이 성공적으로 삭제되었습니다."));
    }
}
