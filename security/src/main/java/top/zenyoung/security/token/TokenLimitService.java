package top.zenyoung.security.token;

import top.zenyoung.security.auth.Ticket;

import javax.annotation.Nonnull;

/**
 * 令牌限制-服务接口
 *
 * @author young
 */
public interface TokenLimitService {

    /**
     * 限制令牌处理
     *
     * @param ticket        用户票据
     * @param maxTokenCount 最大令牌数量
     * @param accessToken   访问令牌
     */
    void limit(@Nonnull final Ticket ticket, @Nonnull final Integer maxTokenCount, @Nonnull final String accessToken);

    /**
     * 令牌入列
     *
     * @param ticket      用户票据
     * @param accessToken 令牌串
     */
    void limitIn(@Nonnull final Ticket ticket, @Nonnull final String accessToken);

    /**
     * 令牌出列
     *
     * @param ticket        用户票据
     * @param maxTokenCount 最大令牌数量
     */
    void limitOut(@Nonnull final Ticket ticket, @Nonnull final Integer maxTokenCount);

    /**
     * 移除用户令牌
     *
     * @param ticket      用户票据
     * @param accessToken 令牌串
     */
    void remove(@Nonnull final Ticket ticket, @Nonnull final String accessToken);

    /**
     * 判断令牌串是否在队列中存在
     *
     * @param ticket      用户票据
     * @param accessToken 令牌串
     * @return 是否存在
     */
    boolean isExists(@Nonnull final Ticket ticket, @Nonnull final String accessToken);
}
