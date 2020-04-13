package top.zenyoung.common.exception;

/**
 * 异常-时间戳过期
 *
 * @author yangyong
 * @version 1.0
 **/
public class StampExpireException extends RuntimeException {

    /**
     * 构造函数
     */
    public StampExpireException() {
        super("时间戳过期");
    }
}
