package top.zenyoung.generator.db.mysql;

import top.zenyoung.generator.type.ColumnType;
import top.zenyoung.generator.type.DbTypeConvert;

import javax.annotation.Nonnull;

import static top.zenyoung.generator.type.DbColumnType.*;

/**
 * Mysql类型转换接口
 *
 * @author young
 */
public class MysqlTypeConvert implements DbTypeConvert {

    @Override
    public ColumnType processTypeConvert(@Nonnull final String fieldType) {
        return ConvertRunner.of(fieldType)
                .test("char", "text", "json", "enum").then(STRING)
                .test("tinyint(1)", "bit(1)", "int").then(INTEGER)
                .test("bigint").then(LONG)
                .test("bit").then(BYTE)
                .test("decimal").then(BIG_DECIMAL)
                .test("clob").then(CLOB)
                .test("blob").then(BLOB)
                .test("binary").then(BYTE_ARRAY)
                .test("float").then(FLOAT)
                .test("double").then(DOUBLE)
                .test("date", "time", "year").then(DATE)
                .or(STRING);
    }
}
