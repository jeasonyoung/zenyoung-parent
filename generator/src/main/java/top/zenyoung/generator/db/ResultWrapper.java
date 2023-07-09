package top.zenyoung.generator.db;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

/**
 * 结果集包装器
 *
 * @author young
 */
@Slf4j
@Getter
@RequiredArgsConstructor(staticName = "of")
public class ResultWrapper {
    private final Map<String, Object> row;

    /**
     * 获取字段值
     *
     * @param colField 列字段
     * @return 字段值
     */
    public String getFieldValue(@Nonnull final String colField) {
        if (!Strings.isNullOrEmpty(colField) && !CollectionUtils.isEmpty(row)) {
            final Object val = row.get(colField.toUpperCase());
            if (Objects.nonNull(val)) {
                if (val instanceof String) {
                    return (String) val;
                }
                return val.toString();
            }
        }
        return "";
    }

    /**
     * 获取是否为主键
     *
     * @return 是否为主键
     */
    public boolean isPrimaryKey(@Nonnull final String colField) {
        final String key = this.getFieldValue(colField);
        return !Strings.isNullOrEmpty(key) && "PRI".equalsIgnoreCase(key);
    }

    /**
     * 获取字段注释
     *
     * @param colField 字段名
     * @return 字段注释
     */
    public String getComment(final String colField) {
        return Strings.isNullOrEmpty(colField) ? "" : formatComment(getFieldValue(colField));
    }

    /**
     * 格式化注释
     *
     * @param comment 格式化前注释
     * @return 格式化后注释
     */
    private String formatComment(final String comment) {
        return Strings.isNullOrEmpty(comment) ? "" : comment.replace("\r\n", "\t");
    }
}
