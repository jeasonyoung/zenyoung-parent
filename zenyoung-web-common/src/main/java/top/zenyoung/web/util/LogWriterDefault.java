package top.zenyoung.web.util;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 日志记录器-默认实现
 *
 * @author yangyong
 * @version 1.0
 * date 2020/8/2 9:34 上午
 **/
@Slf4j
public class LogWriterDefault implements LogWriter {
    private final Map<String, Serializable> logMaps;
    private final long startStamp;

    public LogWriterDefault() {
        logMaps = Maps.newLinkedHashMap();
        startStamp = System.currentTimeMillis();
    }

    @Override
    public void writer(@Nonnull final String key, @Nullable final Map<String, Serializable> values) {
        log.debug("writer(key: {},values: {})...", key, values);
        if (!Strings.isNullOrEmpty(key) && !CollectionUtils.isEmpty(values)) {
            logMaps.put(key, Maps.newLinkedHashMap(values));
        }
    }

    @Override
    public void writer(@Nonnull final String key, @Nullable final String value) {
        log.debug("writer(key: {},value: {})....", key, value);
        if (!Strings.isNullOrEmpty(key)) {
            logMaps.put(key, value);
        }
    }

    @SuppressWarnings({"unchecked"})
    private void buildLogContent(@Nonnull final StringBuilder builder, @Nullable final Map<String, Serializable> vals) {
        if (!CollectionUtils.isEmpty(vals)) {
            vals.forEach((k, v) -> {
                builder.append(k);
                if (v instanceof String) {
                    builder.append("=").append(v);
                } else if (v instanceof List) {
                    builder.append(":").append("{");
                    builder.append(Joiner.on(",").skipNulls().join((List<?>) v));
                    builder.append("}");
                } else if (v instanceof Map) {
                    builder.append(":").append("{");
                    buildLogContent(builder, (Map<String, Serializable>) v);
                    builder.append("}");
                } else {
                    builder.append(":").append("{");
                    builder.append(v.toString());
                    builder.append("}");
                }
                builder.append("\n");
            });
        }
    }

    @Override
    public CharSequence outputLogs() {
        final StringBuilder builder = new StringBuilder();
        buildLogContent(builder, logMaps);
        builder.append("\n").append("耗时: ").append(System.currentTimeMillis() - startStamp).append("ms");
        return builder.toString();
    }
}
