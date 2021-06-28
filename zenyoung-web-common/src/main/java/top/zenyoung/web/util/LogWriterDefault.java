package top.zenyoung.web.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
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
    private final ObjectMapper objectMapper;

    public LogWriterDefault() {
        logMaps = Maps.newLinkedHashMap();
        startStamp = System.currentTimeMillis();
        objectMapper = new ObjectMapper();
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

    @SneakyThrows
    private void renderLogContent(@Nonnull final StringBuilder builder, @Nonnull final Map<?, ?> map) {
        if (!CollectionUtils.isEmpty(map)) {
            builder.append(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(map));
        }
    }
    
    @Override
    public CharSequence outputLogs() {
        final StringBuilder builder = new StringBuilder();
        renderMark(builder, true);
        try {
            renderLogContent(builder, logMaps);
        } catch (Throwable ex) {
            log.warn("outputLogs-exp: {}", ex.getMessage());
        } finally {
            builder.append("\n").append("耗时: ").append(System.currentTimeMillis() - startStamp).append("ms");
        }
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
