package top.zenyoung.generator.service;

import top.zenyoung.generator.domain.Column;
import top.zenyoung.generator.domain.Table;
import top.zenyoung.generator.model.DatabaseConnect;

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
     *
     * @param connect 数据库连接参数
     */
    void testDatabaseConnect(@Nonnull final DatabaseConnect connect);

    /**
     * 表查询
     *
     * @param connect        数据库链接
     * @param queryTableName 查询条件-表名
     * @return 查询结果
     */
    List<Table> queryTables(@Nonnull final DatabaseConnect connect, @Nullable final String queryTableName);

    /**
     * 获取表数据
     *
     * @param connect   数据库链接
     * @param tableName 数据库表名称
     * @return 表数据
     */
    Table getTable(@Nonnull final DatabaseConnect connect, @Nonnull final String tableName);

    /**
     * 获取表列集合
     *
     * @param connect   数据库链接
     * @param tableName 数据库表名称
     * @return 列集合
     */
    List<Column> getColumns(@Nonnull final DatabaseConnect connect, @Nonnull final String tableName);
}
