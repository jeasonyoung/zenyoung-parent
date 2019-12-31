package top.zenyoung.common.response;

import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 结果状态枚举
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/10/21 9:15 下午
 **/
@Getter
@ToString
public enum ResultCode implements Serializable {
    /**
     * 成功
     */
    Success(200, "成功"),
    /**
     * 失败
     */
    Fail(500, "失败");

    /**
     * 枚举值
     */
    private final int val;
    /**
     * 枚举标题
     */
    private final String title;

    /**
     * 构造函数
     *
     * @param val   枚举值
     * @param title 枚举标题
     */
    ResultCode(final int val, final String title) {
        this.val = val;
        this.title = title;
    }
}
