package top.zenyoung.orm.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.google.common.collect.Maps;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 重复键更新方法
 *
 * @author young
 */
@SuppressWarnings({"all"})
public class BatchAddOrUpdateMethod extends AbstractMethod {
    private static final Map<Class<?>, List<String>> classColumnsMap = Maps.newConcurrentMap();

    @Override
    public MappedStatement injectMappedStatement(final Class<?> mapperClass, final Class<?> modelClass, final TableInfo tableInfo) {
        //脚本模板
        final String sql = "<script>\nINSERT INTO %s (%s)\nVALUES\n" +
                "<foreach collection=\"list\" index=\"index\" item=\"item\" separator=\",\">\n %s \n</foreach>\n" +
                "ON DUPLICATE KEY UPDATE\n%s\n</script>";
        //字段list（除了主键）
        final List<TableFieldInfo> fields = tableInfo.getFieldList();
        //全部字段
        final List<String> allFieldName = fields.stream()
                .map(TableFieldInfo::getColumn)
                .collect(Collectors.toList());
        allFieldName.add(0, tableInfo.getKeyColumn());
        //生成所需的SQL片段
        final String fieldNames = String.join(COMMA, allFieldName);
        final String insertFields = buildInsertFields(tableInfo);
        final String updateFields = fields.stream()
                .map(field -> {
                    final String col = field.getColumn();
                    return col + "=values(" + col + ")";
                })
                .collect(Collectors.joining(COMMA + NEWLINE));
        //生成SQL
        final String sqlResult = String.format(sql, tableInfo.getTableName(), fieldNames, insertFields, updateFields);
        final SqlSource sqlSource = languageDriver.createSqlSource(configuration, sqlResult, modelClass);
        return this.addUpdateMappedStatement(mapperClass, modelClass, "batchAddOrUpdate", sqlSource);
    }

    private String buildInsertFields(@Nonnull final TableInfo tableInfo) {
        final List<String> allFieldNames = tableInfo.getFieldList().stream()
                .map(field -> field.getField().getName())
                .collect(Collectors.toList());
        allFieldNames.add(0, tableInfo.getKeyProperty());
        final String insertFields = allFieldNames.stream()
                .map(name -> "#{item." + name + "}")
                .collect(Collectors.joining(COMMA));
        return LEFT_BRACKET + insertFields + RIGHT_BRACKET;
    }
}
