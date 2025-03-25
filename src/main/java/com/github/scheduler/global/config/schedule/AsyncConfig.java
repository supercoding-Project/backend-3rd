package com.github.scheduler.global.config.schedule;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig  {

    @Bean(name = "scheduleCreateExecutor")
    public Executor scheduleCreateExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 단일 스레드로 구성: 오직 하나의 스레드만 작업을 수행함.
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("ScheduleCreateExecutor-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "TodoCreateExecutor")
    public Executor todoCreateExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("TodoCreateExecutor-");
        executor.initialize();
        return executor;
    }

}
