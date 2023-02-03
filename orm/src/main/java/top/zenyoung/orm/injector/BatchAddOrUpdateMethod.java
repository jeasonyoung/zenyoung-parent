package top.zenyoung.orm.injector;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import com.google.common.base.Joiner;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import javax.annotation.Nonnull;
import java.util.List;
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
        final List<String> cols = tableInfo.getFieldList().stream()
                .map(TableFieldInfo::getColumn)
                .collect(Collectors.toList());
        cols.add(0, tableInfo.getKeyColumn());
        //
        final String colScript = buildColSql(cols);
        final String valScript = buildValSql(cols) + buildUpdateSql(cols);
        final String sql = String.format(SqlMethod.INSERT_ONE.getSql(), tableInfo.getTableName(), colScript, valScript);
        final SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return this.addInsertMappedStatement(mapperClass, modelClass, sqlSource, null, null, null);
    }

    private String buildColSql(@Nonnull final List<String> cols) {
        return LEFT_BRACKET + Joiner.on(COMMA).join(cols) + RIGHT_BRACKET;
    }

    private String buildValSql(@Nonnull final List<String> cols) {
        final String valScript = LEFT_BRACKET + cols.stream().map(field -> "#{item." + field + "}").collect(Collectors.joining(COMMA)) + RIGHT_BRACKET;
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
