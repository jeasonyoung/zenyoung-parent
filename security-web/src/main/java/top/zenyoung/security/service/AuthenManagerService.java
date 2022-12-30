package top.zenyoung.security.service;

import org.springframework.security.core.Authentication;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

/**
 * 安全认证管理-服务接口
 *
 * @author young
 */
public interface AuthenManagerService {
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
    Authentication parseAuthenToken(@Nonnull final HttpServletRequest request);
}
