package top.zenyoung.web.vo;

import lombok.Getter;
import lombok.ToString;
import top.zenyoung.common.model.EnumValue;

/**
 * 结果状态-枚举
 *
 * @author yangyong
 * @version 1.0
 **/
@Getter
@ToString
public enum ResultCode implements EnumValue {
    /**
     * 成功
     */
    Success(0, "成功"),
    /**
     * 失败
     */
    Fail(-1, "失败");

    private final int val;
    private final String title;

    ResultCode(final int val, final String title) {
        this.val = val;
        this.title = title;
    }
}
