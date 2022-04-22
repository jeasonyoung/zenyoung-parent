package top.zenyoung.web.controller;

import top.zenyoung.common.model.UserPrincipal;

import javax.annotation.Nullable;

/**
 * 认证用户-控制器-基类
 *
 * @author young
 */
public abstract class BaseAuthController<A extends UserPrincipal> extends BaseController {

    /**
     * 认证用户数据转换
     *
     * @param principal 认证用户
     * @return 转换数据
     */
    protected abstract A convert(@Nullable final UserPrincipal principal);

    /**
     * 检查认证用户
     *
     * @param principal 认证用户
     */
    protected abstract void checkAuth(@Nullable final A principal);
}
