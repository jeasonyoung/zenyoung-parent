package top.zenyoung.sms.exption;

import top.zenyoung.common.exception.BaseException;

/**
 * SMS 异常
 *
 * @author hekang
 */
public class SmsException extends BaseException {

    public SmsException(final String message) {
        super(501, message);
    }
}
