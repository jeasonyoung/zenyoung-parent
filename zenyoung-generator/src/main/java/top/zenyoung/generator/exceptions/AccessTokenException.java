package top.zenyoung.generator.exceptions;

import top.zenyoung.common.exception.BaseException;

/**
 * 访问令牌异常
 *
 * @author young
 */
public class AccessTokenException extends BaseException {
    /**
     * 构造函数
     */
    public AccessTokenException() {
        super(401, "访问令牌不存在或不合法!");
    }
}
