package top.zenyoung.orm.constant;

/**
 * 实体常量
 *
 * @author young
 */
public interface PoConstants {
    /**
     * 状态字段(0:禁用,1:启用)
     */
    String STATUS = "status";
    /**
     * 逻辑删除字段(0:未删除,1:逻辑删除)
     */
    String LOGIC_DEL = "logicDel";

    /**
     * 创建用户字段
     */
    String CREATE_BY = "createBy";
    /**
     * 创建时间字段
     */
    String CREATE_AT = "createAt";

    /**
     * 更新用户字段
     */
    String UPDATE_BY = "updateBy";
    /**
     * 更新时间字段
     */
    String UPDATE_AT = "updateAt";
}
