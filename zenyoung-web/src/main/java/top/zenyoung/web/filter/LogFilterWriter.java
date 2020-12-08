package top.zenyoung.web.filter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * 日志记录器-接口
 *
 * @author yangyong
 * @version 1.0
 * date 2020/8/2 9:29 上午
 **/
public interface LogFilterWriter extends Serializable {
    /**
     * 写入日志
     *
     * @param logContent 日志内容
     * @return 记录器接口
     */
    LogFilterWriter writer(@Nullable final CharSequence logContent);

    /**
     * 获取日志内容
     *
     * @return 日志内容
     */
    @Nonnull
    @Override
    String toString();

    /**
     * 获取输出日志处理
     *
     * @return 输出日志
     */
    CharSequence outputLogs();
}
