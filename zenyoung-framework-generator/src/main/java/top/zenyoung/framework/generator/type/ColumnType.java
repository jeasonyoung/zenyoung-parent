package top.zenyoung.framework.generator.type;

/**
 * 数据行类型接口
 *
 * @author young
 */
public interface ColumnType {
    /**
     * 获取字段类型
     *
     * @return 字段类型
     */
    String getType();

    /**
     * 获取字段类型完整名
     *
     * @return 字段类型完整名
     */
    String getPkg();
}
