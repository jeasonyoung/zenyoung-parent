package top.zenyoung.security.spi;

import com.google.common.base.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;
import top.zenyoung.security.spi.auth.UserDetail;
import top.zenyoung.security.spi.token.TokenDetail;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证用户数据
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/12/22 7:51 下午
 **/
public class TokenUserDetail extends UserDetail implements UserDetails, TokenDetail {

    /**
     * 构造函数
     *
     * @param user 登录用户信息
     */
    public TokenUserDetail(@Nonnull final UserDetail user) {
        BeanUtils.copyProperties(user, this);
    }

    /**
     * 构造函数
     *
     * @param tokenDetail 令牌数据
     */
    TokenUserDetail(@Nonnull final TokenDetail tokenDetail) {
        //用户类型
        setType(tokenDetail.getType());
        //用户ID
        setUserId(tokenDetail.getUserId());
        //用户账号
        setAccount(tokenDetail.getAccount());
        if (Strings.isNullOrEmpty(getAccount())) {
            setAccount(getUserId());
        }
        //所属机构ID
        setOrgId(tokenDetail.getOrgId());
        //用户角色集合
        setRoles(tokenDetail.getRoles());
        //用户是否启用
        setEnabled(true);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        final List<String> roles = getRoles();
        if (!CollectionUtils.isEmpty(roles)) {
            return roles.stream()
                    .filter(role -> !Strings.isNullOrEmpty(role))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public String getUsername() {
        return getUserId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }
}
