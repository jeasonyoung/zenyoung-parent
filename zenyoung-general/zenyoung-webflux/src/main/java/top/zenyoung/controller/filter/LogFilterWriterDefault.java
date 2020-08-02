package top.zenyoung.controller.filter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 日志记录器-默认实现
 *
 * @author yangyong
 * @version 1.0
 * date 2020/8/2 9:34 上午
 **/
class LogFilterWriterDefault implements LogFilterWriter {
    private final StringBuilder logBudiler;
    private final long startStamp;

    public LogFilterWriterDefault() {
        this.logBudiler = new StringBuilder();
        this.startStamp = System.currentTimeMillis();
    }

    @Override
    public LogFilterWriter writer(@Nullable final CharSequence logContent) {
        if (logContent != null) {
            logBudiler.append(logContent);
        }
        return this;
    }

    @Nonnull
    @Override
    public String toString() {
        return logBudiler.toString();
    }

    @Override
    public CharSequence outputLogs() {
        return logBudiler.append("\n").append("耗时: ").append(System.currentTimeMillis() - startStamp).append("ms");
    }
}
