package top.zenyoung.graphics.service;

import javax.annotation.Nonnull;
import java.time.Duration;

/**
 * 验证码存储-服务接口
 *
 * @author young
 */
public interface CaptchaStorageService {

    /**
     * 验证码存储-添加
     *
     * @param id     存储ID
     * @param code   验证码
     * @param expire 有效期
     */
    void addCaptcha(@Nonnull final Long id, @Nonnull final String code, @Nonnull final Duration expire);

    /**
     * 验证码存储-加载
     *
     * @param id 存储ID
     * @return 加载验证码
     */
    String getCaptcha(@Nonnull final Long id);

    /**
     * 验证码存储-删除
     *
     * @param id 存储ID
     */
    void clearCaptcha(@Nonnull final Long id);
}
