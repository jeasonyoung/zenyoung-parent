package top.zenyoung.security.exception;

import top.zenyoung.boot.exception.ServiceException;

/**
 * 异常-签名错误
 *
 * @author yangyong
 * @version 1.0
 **/
public class SignErrorException extends ServiceException {

    /**
     * 构造函数
     *
     * @param message 异常消息
     */
    public SignErrorException(String message) {
        super(message);
    }
}
