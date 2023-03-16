package com.huawei.services.runtime;

import javax.annotation.Nonnull;

/**
 * 运行时日志接口
 *
 * @author young
 */
public interface RuntimeLogger {
    /**
     * 记录日志内容
     * @param log
     * 日志内容
     */
    void log(@Nonnull final String log);

    /**
     * 记录debug日志内容
     * @param log
     * 日志内容
     */
    void debug(@Nonnull final String log);

    /**
     * 记录info日志内容
     * @param log
     * 日志内容
     */
    void info(@Nonnull final String log);

    /**
     * 记录warn日志内容
     * @param log
     * 日志内容
     */
    void warn(@Nonnull final String log);

    /**
     * 记录error日志内容
     * @param log
     * 日志内容
     */
    void error(@Nonnull final String log);

    /**
     * 设置日志level
     * @param level
     * level
     */
    void setLevel(@Nonnull final String level);
}
