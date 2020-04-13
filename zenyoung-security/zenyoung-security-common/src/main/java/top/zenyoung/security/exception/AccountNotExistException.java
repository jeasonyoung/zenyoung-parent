package top.zenyoung.security.exception;

/**
 * 异常-账号不存在
 *
 * @author yangyong
 * @version 1.0
 *  2020/3/14 5:54 下午
 **/
public class AccountNotExistException extends RuntimeException {

    /**
     * 构造函数
     *
     * @param message 异常消息
     */
    public AccountNotExistException(final String message) {
        super(message);
    }
}
