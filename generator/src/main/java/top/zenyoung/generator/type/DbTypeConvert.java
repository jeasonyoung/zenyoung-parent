package top.zenyoung.generator.type;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Set;

/**
 * 数据库类型转换
 *
 * @author young
 */
public interface DbTypeConvert {
    /**
     * 执行类型转换
     *
     * @param fieldType 字段类型
     * @return 转换结果类型
     */
    ColumnType processTypeConvert(@Nonnull final String fieldType);

    /**
     * 转换处理
     */
    @RequiredArgsConstructor(staticName = "of")
    class ConvertRunner {
        private final String fieldType;
        private boolean selected = false;
        private ColumnType ret = null;

        public ConvertRunner test(@Nonnull final String... contains) {
            if (this.selected) {
                return this;
            }
            if (!Strings.isNullOrEmpty(this.fieldType) && contains.length > 0) {
                final Set<String> items = Sets.newHashSet(contains);
                this.selected = items.contains(this.fieldType);
            }
            return this;
        }

        public ConvertRunner then(@Nonnull final ColumnType ret) {
            if (this.selected) {
                this.ret = ret;
            }
            return this;
        }

        public ColumnType or(final ColumnType colType) {
            return Optional.ofNullable(this.ret).orElse(colType);
        }
    }
}
