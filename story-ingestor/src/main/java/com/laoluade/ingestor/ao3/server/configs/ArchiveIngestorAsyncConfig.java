package com.laoluade.ingestor.ao3.server.configs;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class ArchiveIngestorAsyncConfig implements AsyncConfigurer {
    @Value("${archiveIngestor.async.executor.corePoolSize:1}")
    private Integer corePoolSize;

    @Value("${archiveIngestor.async.executor.maxPoolSize:2}")
    private Integer maxPoolSize;

    @Value("${archiveIngestor.async.executor.queueCapacity:10}")
    private Integer queueCapacity;

    @Value("${archiveIngestor.async.executor.threadNamePrefix:archiveIngestorAsyncThread-}")
    private String threadNamePrefix;

    @Bean(name = "archiveIngestorAsyncExecutor")
    public Executor getAsyncExecutor()  {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(this.corePoolSize);
        executor.setMaxPoolSize(this.maxPoolSize);
        executor.setQueueCapacity(this.queueCapacity);
        executor.setThreadNamePrefix(this.threadNamePrefix);
        executor.initialize();
        return executor;
    }

    @Bean
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new ArchiveIngestorAsyncExceptionHandler();
    }
}
