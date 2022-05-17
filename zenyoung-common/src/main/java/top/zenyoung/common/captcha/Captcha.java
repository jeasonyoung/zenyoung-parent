package top.zenyoung.common.captcha;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 验证码接口
 *
 * @author young
 */
public interface Captcha {

    /**
     * 创建验证码，实现类需同时生成随机验证码字符串和验证码图片
     */
    void createCode();

    /**
     * 获取验证码的文字内容
     *
     * @return 验证码文字内容
     */
    String getCode();

    /**
     * 验证验证码是否正确，建议忽略大小写
     *
     * @param userInputCode 用户输入的验证码
     * @return 是否与生成的一直
     */
    boolean verify(final String userInputCode);

    /**
     * 将验证码写出到目标流中
     *
     * @param out 目标流
     * @throws IOException 异常
     */
    void write(final OutputStream out) throws IOException;
}
