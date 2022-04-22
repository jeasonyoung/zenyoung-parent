package top.zenyoung.web.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.zenyoung.common.model.EnumValue;

/**
 * 结果状态-枚举
 *
 * @author young
 **/
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ResultCode implements EnumValue {
    /**
     * 成功
     */
    Success(0, "成功"),
    /**
     * 失败
     */
    Fail(-1, "失败"),
    /**
     * 错误
     */
    Error(500, "错误");

    private final int val;
    private final String title;
}
