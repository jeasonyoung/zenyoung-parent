package top.zenyoung.common.sequence;

import lombok.Data;

/**
 * 主键序号接口属性
 *
 * @author young
 */
@Data
public class IdSequenceProperties {
    /**
     * 机器ID
     */
    private Integer workerId;
    /**
     * 机房ID
     */
    private Integer dataCenterId;
    /**
     * 顺序号
     */
    private Integer sequence;
}
