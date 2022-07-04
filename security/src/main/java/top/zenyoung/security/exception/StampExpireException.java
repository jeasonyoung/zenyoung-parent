package top.zenyoung.security.exception;

import top.zenyoung.boot.exception.ServiceException;

/**
 * 异常-时间戳过期
 *
 * @author yangyong
 * @version 1.0
 **/
public class StampExpireException extends ServiceException {

    /**
     * 构造函数
     */
    public StampExpireException() {
        super("时间戳过期");
    }
}
