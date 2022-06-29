package top.zenyoung.generator.db.mysql;

import top.zenyoung.generator.db.QueryInfo;

import javax.annotation.Nonnull;

/**
 * MySql-查询配置
 *
 * @author young
 */
public class MysqlQueryInfo implements QueryInfo {

    @Override
    public String databasesSql() {
        return "show databases";
    }

    @Override
    public String tablesSql() {
        return "show table status";
    }

    @Override
    public String tableFieldsSql(@Nonnull final String tableName) {
        return String.format("show full fields from `%s`", tableName);
    }

    @Override
    public String tableName() {
        return "NAME";
    }

    @Override
    public String tableComment() {
        return "COMMENT";
    }

    @Override
    public String fieldName() {
        return "FIELD";
    }

    @Override
    public String fieldType() {
        return "TYPE";
    }

    @Override
    public String fieldComment() {
        return "COMMENT";
    }

    @Override
    public String fieldKey() {
        return "KEY";
    }
}
