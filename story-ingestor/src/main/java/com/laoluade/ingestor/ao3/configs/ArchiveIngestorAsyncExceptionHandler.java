package com.laoluade.ingestor.ao3.configs;

import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

public class ArchiveIngestorAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    private static final Logger AIAEHLogger = LoggerFactory.getLogger(ArchiveIngestorAsyncExceptionHandler.class);

    public void handleUncaughtException(Throwable throwable, Method method, @Nullable Object... params) {
        if (AIAEHLogger.isErrorEnabled()) {
            // Log the method name
            AIAEHLogger.error("Exception in async method {}", method.getName());

            // Log the parameters
            if (params != null) {
                String paramString = "Params used in aforementioned async method: ";
                for (Object param : params) {
                    paramString = paramString + param.toString() + ", ";
                }
                AIAEHLogger.error(paramString);
            }

            // Log the exception
            AIAEHLogger.error("Error from aforementioned async method: {}", throwable.getMessage());
        }
    }
}
