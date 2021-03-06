package top.zenyoung.common.exception;

/**
 * 异常-签名错误
 *
 * @author yangyong
 * @version 1.0
 **/
public class SignErrorException extends RuntimeException {

    /**
     * 构造函数
     *
     * @param message 异常消息
     */
    public SignErrorException(String message) {
        super(message);
    }
}
