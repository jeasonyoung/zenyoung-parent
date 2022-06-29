package top.zenyoung.generator.db;

import javax.annotation.Nonnull;

/**
 * 查询配置
 *
 * @author young
 */
public interface QueryInfo {

    /**
     * 查询数据库集合SQL
     *
     * @return 查询数据库SQL
     */
    String databasesSql();

    /**
     * 查询表集合SQL
     *
     * @return 查询表SQL
     */
    String tablesSql();

    /**
     * 查询表字段SQL
     *
     * @param tableName 表名称
     * @return 表字段SQL
     */
    String tableFieldsSql(@Nonnull final String tableName);

    /**
     * 获取表名
     *
     * @return 表名
     */
    String tableName();

    /**
     * 获取表名注释
     *
     * @return 表名注释
     */
    String tableComment();

    /**
     * 获取字段名
     *
     * @return 字段名
     */
    String fieldName();

    /**
     * 获取字段类型
     *
     * @return 字段类型
     */
    String fieldType();

    /**
     * 获取字段注释
     *
     * @return 字段注释
     */
    String fieldComment();

    /**
     * 获取字段键类型
     *
     * @return 字段键类型
     */
    String fieldKey();
}
