package top.zenyoung.sms.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 查询结果VO
 *
 * @param <T> 数据类型
 * @author young
 */
@Data
public abstract class BaseQueryVO<T> implements Serializable {
    /**
     * 请求id
     */
    private String requestId;
    /**
     * 状态码
     */
    private String code;
    /**
     * 返回消息
     */
    private String msg;
    /**
     * 状态
     */
    private Boolean status;

    /**
     * 总数据
     */
    private Long total;
    /**
     * 数据集合
     */
    private List<T> list;

    /**
     * 结果是否成功
     *
     * @return 是否成功
     */
    public boolean isSucceed() {
        return status;
    }

    /**
     * 结果是否失败
     *
     * @return 是否失败
     */
    public boolean isFailed() {
        return !status;
    }
}
