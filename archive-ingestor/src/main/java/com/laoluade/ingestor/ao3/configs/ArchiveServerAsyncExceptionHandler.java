package com.laoluade.ingestor.ao3.configs;

import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

/**
 * <p>This class defines a custom {@link AsyncUncaughtExceptionHandler} for Archive Server application.</p>
 * <p>This class uses a {@link Logger} from the SLF4J API to print exception details to console.</p>
 */
public class ArchiveServerAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    /**
     * <p>This attribute is the logger used to record error messages when async exceptions happen.</p>
     */
    private static final Logger AIAEHLogger = LoggerFactory.getLogger(ArchiveServerAsyncExceptionHandler.class);

    /**
     * <p>This method defines the logic for the asynchronous uncaught exception handler.</p>
     * @param throwable The thrown exception from the crashed task.
     * @param method The method where the exception originated from.
     * @param params The parameters entered into the method where the exception originated from.
     */
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
