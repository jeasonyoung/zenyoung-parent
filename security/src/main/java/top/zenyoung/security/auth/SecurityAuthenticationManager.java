package top.zenyoung.security.auth;

import org.springframework.security.core.Authentication;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

/**
 * 安全认证管理器接口
 *
 * @author young
 */
public interface SecurityAuthenticationManager {
    /**
     * 获取认证白名单
     *
     * @return 认证白名单
     */
    String[] getWhiteUrls();

    /**
     * 获取登录地址
     *
     * @return 登录地址
     */
    String[] getLoginUrls();

    /**
     * 解析认证
     *
     * @param request 请求数据
     * @return 认证结果
     */
    Authentication parseAuthenticationToken(@Nonnull final HttpServletRequest request);
}
