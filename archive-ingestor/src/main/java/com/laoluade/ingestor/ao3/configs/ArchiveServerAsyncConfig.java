package com.laoluade.ingestor.ao3.configs;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * <p>This class is the asynchronous configuration for the Spring Boot backend server.</p>
 * <p>
 *     This class implements {@link org.springframework.scheduling.annotation.AsyncConfigurer}
 *     to standardize async execution settings and async exception handling settings.
 * </p>
 * <p>This class uses the following settings from the application.properties file to configure itself:</p>
 * <ul>
 *     <li>archiveServer.async.executor.corePoolSize</li>
 *     <li>archiveServer.async.executor.maxPoolSize</li>
 *     <li>archiveServer.async.executor.queueCapacity</li>
 *     <li>archiveServer.async.executor.threadNamePrefix</li>
 * </ul>
 * <p>All class attributes correspond to their <code>archiveServer.async.executor</code> counterpart.</p>
 */
@Configuration
@EnableAsync
public class ArchiveServerAsyncConfig implements AsyncConfigurer {
    /**
     * <p>This attribute specifies the primary number of cores to run before placing new tasks in queue.</p>
     */
    @Value("${archiveServer.async.executor.corePoolSize}")
    private Integer corePoolSize;

    /**
     * <p>This attribute specifies the maximum number of cores that can be run if the async task queue becomes full.</p>
     * <p>If this number is reached and queue capacity is filled, then new tasks are simply dropped.</p>
     */
    @Value("${archiveServer.async.executor.maxPoolSize}")
    private Integer maxPoolSize;

    /**
     * <p>This attribute specifies the capacity of the async task waiting queue.</p>
     */
    @Value("${archiveServer.async.executor.queueCapacity}")
    private Integer queueCapacity;

    /**
     * <p>This attribute specifies the thread name prefix to use for naming async threads.</p>
     */
    @Value("${archiveServer.async.executor.threadNamePrefix}")
    private String threadNamePrefix;

    /**
     * <p>
     *     This method returns a {@link ThreadPoolTaskExecutor} singleton bean
     *     that is customized using class attributes.
     * </p>
     * @return {@link ThreadPoolTaskExecutor} with custom configuration.
     */
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

    /**
     * <p>
     *     This method returns a custom {@link AsyncUncaughtExceptionHandler} made for the archive server.
     *     See documentation for {@link ArchiveServerAsyncExceptionHandler} for more details.
     * </p>
     * @return Archive Server's {@link AsyncUncaughtExceptionHandler} implementation.
     */
    @Bean
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new ArchiveServerAsyncExceptionHandler();
    }
}
