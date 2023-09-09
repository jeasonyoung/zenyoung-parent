package top.zenyoung.retrofit.retry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 重试机制
 *
 * @author young
 */
@Slf4j
@Getter
@AllArgsConstructor
public class RetryStrategy {
    private int maxRetries;
    private final int intervalMs;

    public boolean shouldRetry() {
        return this.maxRetries > 0;
    }

    public void retry() {
        this.maxRetries--;
        waitUntilNextTry();
    }

    private void waitUntilNextTry() {
        if (intervalMs > 0) {
            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("waitUntilNextTry[intervalMs: {}]-exp", intervalMs, e);
            }
        }
    }
}
