package top.zenyoung.common.exception;

/**
 * 异常-时间戳过期
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/3/14 6:14 下午
 **/
public class StampExpireException extends RuntimeException {

    /**
     * 构造函数
     */
    public StampExpireException() {
        super("时间戳过期");
    }
}
