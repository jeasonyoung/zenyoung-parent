package top.zenyoung.boot.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import top.zenyoung.common.model.EnumValue;

import javax.annotation.Nonnull;

/**
 * 异常基类
 *
 * @author young
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public abstract class BaseException extends RuntimeException implements EnumValue {
    /**
     * 错误代码
     */
    private final Integer code;

    @Override
    public int getVal() {
        return this.code;
    }

    @Override
    public String getTitle() {
        return super.getMessage();
    }

    /**
     * 构造函数
     *
     * @param code    异常代码
     * @param message 异常消息
     */
    public BaseException(final Integer code, final String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造函数
     *
     * @param ev 枚举异常
     */
    public BaseException(@Nonnull final EnumValue ev) {
        this(ev.getVal(), ev.getTitle());
    }
}
