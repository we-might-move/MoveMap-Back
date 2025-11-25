package org.wemightmove.movemap.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    // 푸시 전용 스레드 풀
    /**
     * t2.micro 기준
     * - corePoolSize : 2 (평상 시)
     * - maxPoolSize : 5 (최대)
     * - queueCapacity : 100 (대기열)
     */
    @Bean(name = "pushExecutor")
    public Executor pushExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("push-");
        executor.setRejectedExecutionHandler((r, e) -> {
            log.error("푸시 큐 초과, 요청 버림");
        });
        executor.initialize();
        return executor;
    }
}
