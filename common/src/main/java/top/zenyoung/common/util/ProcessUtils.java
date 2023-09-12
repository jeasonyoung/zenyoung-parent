package top.zenyoung.common.util;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import top.zenyoung.common.exception.ServiceException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 进程工具类
 *
 * @author young
 */
@Slf4j
@UtilityClass
public class ProcessUtils {
    private static final Map<String, Object> LOCKS = Maps.newConcurrentMap();
    private static final String SEP = " ";

    private static List<String> buildArgments(@Nonnull final List<String> argments) {
        return argments.stream()
                .map(arg -> {
                    if (!Strings.isNullOrEmpty(arg)) {
                        return Splitter.on(SEP)
                                .trimResults()
                                .omitEmptyStrings()
                                .splitToList(arg);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(arg -> !Strings.isNullOrEmpty(arg))
                .collect(Collectors.toList());
    }

    private static String getProcessOutput(@Nullable final InputStream input) {
        if (Objects.nonNull(input)) {
            final StringBuilder builder = new StringBuilder();
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!Strings.isNullOrEmpty(line)) {
                        builder.append(line);
                    }
                }
            } catch (IOException e) {
                log.warn("getProcessOutput-exp: {}", e.getMessage());
            }
            return builder.toString().replace("\u0000", "");
        }
        return null;
    }

    /**
     * 进程执行
     *
     * @param argments 参数集合
     * @return 执行结果
     * @throws Exception 异常处理
     */
    public static String exec(@Nonnull final List<String> argments) throws Exception {
        final List<String> cmds = buildArgments(argments);
        if (cmds.isEmpty()) {
            throw new IllegalArgumentException("'argments'为空或不合法!");
        }
        final String key = Joiner.on(SEP).join(cmds);
        synchronized (LOCKS.computeIfAbsent(key, k -> new Object())) {
            try {
                //执行外部进程
                final Process p = Runtime.getRuntime().exec(cmds.toArray(new String[0]));
                //执行等待
                p.waitFor();
                //错误消息处理
                final String err = getProcessOutput(p.getErrorStream());
                if (!Strings.isNullOrEmpty(err)) {
                    log.error("exec(argment: {})-exp: {}", key, err);
                    throw new ServiceException(err);
                }
                //执行结果输出
                return getProcessOutput(p.getInputStream());
            } finally {
                LOCKS.remove(key);
            }
        }
    }
}
