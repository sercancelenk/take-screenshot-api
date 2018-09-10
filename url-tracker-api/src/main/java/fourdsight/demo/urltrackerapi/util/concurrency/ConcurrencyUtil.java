package fourdsight.demo.urltrackerapi.util.concurrency;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Sercan CELENK created by IntelliJ
 */

public class ConcurrencyUtil {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrencyUtil.class);

    public ConcurrencyUtil() {
        logger.info("ConcurrencyUtil initialized successfully.");
    }

    public static ExecutorService createExecutor(int threadCount) {
        return Executors.newFixedThreadPool(threadCount);
    }

    public static void shutdownExecutor(ExecutorService executor, Integer executorTimeoutSeconds) {
        try {
            executor.shutdown();
            if (Objects.isNull(executorTimeoutSeconds)) executorTimeoutSeconds = 6;
            executor.awaitTermination(executorTimeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Task interrupted", e);
            Thread.currentThread().interrupt();
        } finally {
            if (!executor.isTerminated()) {
                logger.error("Cancel non-finished tasks");
            }
            executor.shutdownNow();
            executor = null;
        }
    }

    public static void runAsync(ExecutorService executor, AsyncTask task, Boolean isLast, Integer executorTimeoutSeconds) {
        try {
            executor.submit(() -> {
                try {
                    task.run();
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            });
        } finally {
            if (BooleanUtils.isTrue(isLast))
                shutdownExecutor(executor, executorTimeoutSeconds);
        }
    }

}
