package top.zenyoung.web.util;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
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

    private void renderLogContent(@Nonnull final StringBuilder builder, @Nullable final String prefix, @Nonnull final Map<?, ?> map) {
        if (!CollectionUtils.isEmpty(map)) {
            map.forEach((k, v) -> {
                renderPrefix(builder, prefix).append(k).append(":");
                if (v instanceof Map) {
                    builder.append("{").append("\n");
                    renderLogContent(builder, (Strings.isNullOrEmpty(prefix) ? "" : prefix) + "\t", (Map<?, ?>) v);
                    renderPrefix(builder, prefix).append("}").append("\n");
                } else if (v instanceof List) {
                    builder.append("[");
                    renderLogContent(builder, prefix, (List<?>) v);
                    builder.append("]").append("\n");
                } else {
                    if (v instanceof Number) {
                        builder.append(v);
                    } else if (v instanceof Boolean) {
                        builder.append(v);
                    } else {
                        builder.append("\"").append(v).append("\"");
                    }
                    builder.append("\n");
                }
            });
        }
    }

    private StringBuilder renderPrefix(@Nonnull final StringBuilder builder, @Nullable final String prefix) {
        if (!Strings.isNullOrEmpty(prefix)) {
            builder.append(prefix);
        }
        return builder;
    }

    private void renderLogContent(@Nonnull final StringBuilder builder, @Nullable final String prefix, @Nonnull final List<?> list) {
        if (!CollectionUtils.isEmpty(list)) {
            final List<String> vals = Lists.newLinkedList();
            int i = 0;
            for (Object obj : list) {
                if (obj == null) {
                    continue;
                }
                if (obj instanceof Map) {
                    if (i++ > 1) {
                        renderPrefix(builder, prefix).append(",").append("\n");
                    }
                    renderLogContent(builder, prefix, (Map<?, ?>) obj);
                    continue;
                }
                vals.add(String.valueOf(obj));
            }
            if (!CollectionUtils.isEmpty(vals)) {
                builder.append("\n");
                renderPrefix(builder, prefix).append(Joiner.on(",\n").skipNulls().join(vals));
                builder.append("\n");
            }
        }
    }

    @Override
    public CharSequence outputLogs() {
        final StringBuilder builder = new StringBuilder();
        renderMark(builder, true);
        renderLogContent(builder, null, logMaps);
        builder.append("\n").append("耗时: ").append(System.currentTimeMillis() - startStamp).append("ms");
        renderMark(builder, false);
        return builder.toString();
    }

    private void renderMark(@Nonnull final StringBuilder builder, @Nonnull final Boolean start) {
        final String mark = (start ? ">" : "<").repeat(80);
        builder.append("\n");
        if (start) {
            builder.append("[start]");
        }
        builder.append(mark);
        if (!start) {
            builder.append("[end]");
        }
        builder.append("\n");
    }
}
