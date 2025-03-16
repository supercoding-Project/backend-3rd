package com.github.scheduler.invite.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class InviteCodeService {
    private final StringRedisTemplate redisTemplate;
    private static final long INVITE_CODE_EXPIRE_TIME = 60; // 초대 코드 유효 시간 (1시간)


    //초대 코드 생성 및 저장 (Redis)
    public String generateAndSaveInviteCode(Long calendarId) {
        String inviteCode = generateInviteCode();
        redisTemplate.opsForValue().set(
                "invite:" + calendarId,
                inviteCode,
                INVITE_CODE_EXPIRE_TIME,
                TimeUnit.MINUTES
        );
        return inviteCode;
    }

    // 초대 코드 조회
    public String getInviteCode(Long calendarId) {
        return redisTemplate.opsForValue().get("invite:" + calendarId);
    }

    // 초대 코드 삭제
    public void deleteInviteCode(Long calendarId) {
        redisTemplate.delete("invite:" + calendarId);
    }


    // 8자리 랜덤 초대 코드 생성
    private String generateInviteCode() {
        return java.util.UUID.randomUUID().toString().substring(0, 8);
    }
}