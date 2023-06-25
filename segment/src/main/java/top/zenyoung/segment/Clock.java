package top.zenyoung.segment;

import java.time.Duration;
import java.util.concurrent.locks.LockSupport;

/**
 * 时间包装
 *
 * @author young
 */
public interface Clock {

    Clock CACHE = new CacheClock();

    Clock SYSTEM = new SystemClock();

    long secondTime();

    static long getSystemSecondTime() {
        return System.currentTimeMillis() / 1000;
    }

    class SystemClock implements Clock {

        @Override
        public long secondTime() {
            return getSystemSecondTime();
        }
    }

    class CacheClock implements Clock, Runnable {
        public static final long ONE_SECOND_PERIOD = Duration.ofSeconds(1).toNanos();

        private final Thread thread;
        private volatile long lastTime;

        public CacheClock() {
            this.lastTime = getSystemSecondTime();
            this.thread = new Thread(this);
            this.thread.setName("CosId-CacheClock");
            this.thread.setDaemon(true);
            this.thread.start();
        }

        @Override
        public void run() {
            while (!thread.isInterrupted()) {
                this.lastTime = getSystemSecondTime();
                LockSupport.parkNanos(this, ONE_SECOND_PERIOD);
            }
        }

        @Override
        public long secondTime() {
            return lastTime;
        }
    }
}
