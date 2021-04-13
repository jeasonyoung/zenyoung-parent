package top.zenyoung.web.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Map;

/**
 * 日志记录器-接口
 *
 * @author yangyong
 * @version 1.0
 * date 2020/8/2 9:29 上午
 **/
public interface LogWriter extends Serializable {
    /**
     * 记录日志
     *
     * @param key    日志键
     * @param values 日志内容
     */
    void writer(@Nonnull final String key, @Nullable final Map<String, Serializable> values);

    /**
     * 记录日志
     *
     * @param key   日志键
     * @param value 日志内容
     */
    void writer(@Nonnull final String key, @Nullable final String value);

    /**
     * 获取输出日志处理
     *
     * @return 输出日志
     */
    CharSequence outputLogs();
}
