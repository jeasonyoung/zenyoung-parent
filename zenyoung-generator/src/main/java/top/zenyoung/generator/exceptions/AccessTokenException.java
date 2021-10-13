package top.zenyoung.generator.exceptions;

/**
 * 访问令牌异常
 *
 * @author young
 */
public class AccessTokenException extends RuntimeException {

    /**
     * 构造函数
     */
    public AccessTokenException() {
        super("访问令牌不存在或不合法!");
    }
}
