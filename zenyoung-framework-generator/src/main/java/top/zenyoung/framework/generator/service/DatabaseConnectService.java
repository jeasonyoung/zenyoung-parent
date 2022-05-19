package top.zenyoung.framework.generator.service;

import top.zenyoung.framework.generator.domain.Column;
import top.zenyoung.framework.generator.domain.Table;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 数据库连接-服务接口
 *
 * @author young
 */
public interface DatabaseConnectService {
    /**
     * 测试数据库链接
     */
    void testDatabaseConnect();

    /**
     * 表查询
     *
     * @param queryTableName 查询条件-表名
     * @return 查询结果
     */
    List<Table> queryTables(@Nullable final String queryTableName);

    /**
     * 获取表数据
     *
     * @param tableName 数据库表名称
     * @return 表数据
     */
    Table getTable(@Nonnull final String tableName);

    /**
     * 获取表列集合
     *
     * @param tableName 数据库表名称
     * @return 列集合
     */
    List<Column> getColumns(@Nonnull final String tableName);
}
