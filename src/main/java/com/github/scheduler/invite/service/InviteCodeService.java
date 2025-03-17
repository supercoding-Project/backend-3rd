package com.github.scheduler.invite.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class InviteCodeService {
    private final StringRedisTemplate redisTemplate;

    private static final long INVITE_CODE_EXPIRE_TIME = 60; // 초대코드 유효기간: 1시간

    public String generateAndSaveInviteCode(Long calendarId) {
        String inviteCode = generateInviteCode();
        log.info("📢 초대 코드 생성 완료: {}", inviteCode);

        try {
            redisTemplate.opsForValue().set(
                    "invite:" + inviteCode,
                    String.valueOf(calendarId),
                    INVITE_CODE_EXPIRE_TIME,
                    TimeUnit.MINUTES
            );
            log.info("✅ Redis에 초대 코드 저장 완료: invite:{} -> {}", inviteCode, calendarId);

            // 저장 후 값 확인
            String storedValue = redisTemplate.opsForValue().get("invite:" + inviteCode);
            log.info("🔍 Redis 저장 값 확인: invite:{} -> {}", inviteCode, storedValue);

        } catch (Exception e) {
            log.error("❌ Redis 저장 실패: {}", e.getMessage());
        }

        return inviteCode;
    }

    // 초대 코드 조회
    public String getInviteCode(Long calendarId) {
        return redisTemplate.opsForValue().get("invite:" + calendarId);
    }

    public Long validateInviteCode(String inviteCode) {
        String key = "invite:" + inviteCode;
        String calendarId = redisTemplate.opsForValue().get(key);

        if (calendarId == null) {
            throw new IllegalArgumentException("INVALID_INVITE_CODE: 유효하지 않은 초대 코드입니다.");
        }

        return Long.parseLong(calendarId);
    }

    // 초대 코드 삭제
    public void deleteInviteCode(String inviteCode) {
        redisTemplate.delete("invite:" + inviteCode);
    }


    // 8자리 랜덤 초대 코드 생성
    private String generateInviteCode() {
        return java.util.UUID.randomUUID().toString().substring(0, 8);
    }
}