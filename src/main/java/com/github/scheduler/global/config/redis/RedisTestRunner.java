package com.github.scheduler.global.config.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisTestRunner implements CommandLineRunner {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            redisTemplate.opsForValue().set("testKey", "testValue", 5);
            String value = redisTemplate.opsForValue().get("testKey");
            log.info("✅ Redis 연결 확인: testKey -> {}", value);
        } catch (Exception e) {
            log.error("❌ Redis 연결 실패: {}", e.getMessage());
        }
    }
}
