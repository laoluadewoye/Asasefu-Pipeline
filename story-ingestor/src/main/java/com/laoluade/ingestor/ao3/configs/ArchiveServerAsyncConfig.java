package com.laoluade.ingestor.ao3.configs;

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
public class ArchiveServerAsyncConfig implements AsyncConfigurer {
    @Value("${archiveIngestor.async.executor.corePoolSize:2}")
    private Integer corePoolSize;

    @Value("${archiveIngestor.async.executor.maxPoolSize:4}")
    private Integer maxPoolSize;

    @Value("${archiveIngestor.async.executor.queueCapacity:10}")
    private Integer queueCapacity;

    @Value("${archiveIngestor.async.executor.threadNamePrefix:archiveIngestorAsyncThread-}")
    private String threadNamePrefix;

    @Bean(name = "archiveIngestorAsyncExecutor")
    public Executor getAsyncExecutor()  {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(this.corePoolSize);
        executor.setMaxPoolSize(this.maxPoolSize+1); // One additional thread for the session monitoring loop
        executor.setQueueCapacity(this.queueCapacity);
        executor.setThreadNamePrefix(this.threadNamePrefix);
        executor.initialize();
        return executor;
    }

    @Bean
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new ArchiveServerAsyncExceptionHandler();
    }
}
