package top.zenyoung.security.exception;

/**
 * 异常-账号已禁用
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/3/14 6:07 下午
 **/
public class AccountForbiddenException extends RuntimeException {

    /**
     * 构造函数
     *
     * @param message 异常消息
     */
    public AccountForbiddenException(final String message) {
        super(message);
    }
}
