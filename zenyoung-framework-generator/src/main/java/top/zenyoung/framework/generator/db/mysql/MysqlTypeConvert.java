package top.zenyoung.framework.generator.db.mysql;

import top.zenyoung.common.convert.TypeConvertRunner;
import top.zenyoung.framework.generator.type.ColumnType;
import top.zenyoung.framework.generator.type.DbTypeConvert;

import javax.annotation.Nonnull;

import static top.zenyoung.framework.generator.type.DbColumnType.*;

/**
 * Mysql类型转换接口
 *
 * @author young
 */
public class MysqlTypeConvert implements DbTypeConvert {

    @Override
    public ColumnType processTypeConvert(@Nonnull final String fieldType) {
        final TypeConvertRunner<ColumnType> runner = new TypeConvertRunner<>();
        return runner.use(fieldType)
                .test(runner.containsAny("char", "text", "json", "enum").then(STRING))
                .test(runner.contains("bigint").then(LONG))
                .test(runner.containsAny("tinyint(1)", "bit(1)").then(BOOLEAN))
                .test(runner.contains("bit").then(BYTE))
                .test(runner.contains("int").then(INTEGER))
                .test(runner.contains("decimal").then(BIG_DECIMAL))
                .test(runner.contains("clob").then(CLOB))
                .test(runner.contains("blob").then(BLOB))
                .test(runner.contains("binary").then(BYTE_ARRAY))
                .test(runner.contains("float").then(FLOAT))
                .test(runner.contains("double").then(DOUBLE))
                .test(runner.containsAny("date", "time", "year").then(DATE))
                .or(STRING);
    }
}
