package top.zenyoung.orm.injector;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 重复键更新方法
 *
 * @author young
 */
public class BatchAddOrUpdateMethod extends AbstractMethod {

    public BatchAddOrUpdateMethod() {
        super("batchAddOrUpdate");
    }

    @Override
    public MappedStatement injectMappedStatement(final Class<?> mapperClass, final Class<?> modelClass, final TableInfo tableInfo) {
        final Map<String, String> colPropMaps = tableInfo.getFieldList().stream()
                .collect(Collectors.toMap(TableFieldInfo::getColumn, TableFieldInfo::getProperty, (o, n) -> n));
        colPropMaps.put(tableInfo.getKeyColumn(), tableInfo.getKeyProperty());
        //
        final List<String> cols = Lists.newArrayList(colPropMaps.keySet());
        final String colScript = buildColSql(cols);
        final String valScript = buildValSql(cols, colPropMaps) + buildUpdateSql(cols);
        final String sql = String.format(SqlMethod.INSERT_ONE.getSql(), tableInfo.getTableName(), colScript, valScript);
        final SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return this.addInsertMappedStatement(mapperClass, modelClass, sqlSource, NoKeyGenerator.INSTANCE, null, null);
    }

    private String buildColSql(@Nonnull final List<String> cols) {
        return LEFT_BRACKET + Joiner.on(COMMA).join(cols) + RIGHT_BRACKET;
    }

    private String buildValSql(@Nonnull final List<String> cols, @Nonnull final Map<String, String> colPropMaps) {
        final String valScript = LEFT_BRACKET + cols.stream()
                .map(col -> {
                    final String prop = colPropMaps.getOrDefault(col, null);
                    if (!Strings.isNullOrEmpty(prop)) {
                        return "#{item." + prop + "}";
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining(COMMA)) + RIGHT_BRACKET;
        return SqlScriptUtils.convertForeach(valScript, Constants.COLL, null, "item", COMMA);
    }

    private String buildUpdateSql(@Nonnull final List<String> cols) {
        final String sql = "\n ON DUPLICATE KEY UPDATE\n%s\n";
        final String updateScript = cols.stream()
                .map(col -> col + "=values(" + col + ")")
                .collect(Collectors.joining(COMMA + NEWLINE));
        return String.format(sql, updateScript);
    }
}
