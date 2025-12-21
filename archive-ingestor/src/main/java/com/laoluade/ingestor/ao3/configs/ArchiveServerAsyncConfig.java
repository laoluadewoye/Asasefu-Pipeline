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
    @Value("${archiveServer.async.executor.corePoolSize}")
    private Integer corePoolSize;

    @Value("${archiveServer.async.executor.maxPoolSize}")
    private Integer maxPoolSize;

    @Value("${archiveServer.async.executor.queueCapacity}")
    private Integer queueCapacity;

    @Value("${archiveServer.async.executor.threadNamePrefix}")
    private String threadNamePrefix;

    @Bean(name = "archiveServerAsyncExecutor")
    public Executor getAsyncExecutor()  {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(this.corePoolSize);
        // Two additional thread for the session monitoring loop and driver creation
        executor.setMaxPoolSize(this.maxPoolSize);
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
