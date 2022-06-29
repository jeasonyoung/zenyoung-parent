package top.zenyoung.generator.db;

import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.CollectionUtils;
import top.zenyoung.generator.type.ColumnType;
import top.zenyoung.generator.vo.TableVO;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 数据表
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Table extends TableVO {
    /**
     * 表字段集合
     */
    private List<TableField> fields;
    /**
     * 需要导入的包
     */
    private List<String> importPackages;

    /**
     * 初始化导包处理
     */
    public void init() {
        if (!CollectionUtils.isEmpty(this.fields)) {
            this.importPackages = fields.stream()
                    .map(field -> {
                        final ColumnType columnType = field.getColumnType();
                        final String pkg;
                        if (Objects.nonNull(columnType) && !Strings.isNullOrEmpty(pkg = columnType.getPkg())) {
                            return pkg;
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
        }
    }
}
