package top.zenyoung.security.webflux.model;

import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;
import top.zenyoung.common.model.Status;
import top.zenyoung.security.model.UserPrincipal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 令牌用户认证数据
 *
 * @author yangyong
 * @version 1.0
 * 2020/3/19 4:25 下午
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class TokenUserDetails extends UserPrincipal implements UserDetails {
    /**
     * 密码
     */
    private String password;
    /**
     * 状态
     */
    private Status status = Status.Enable;

    /**
     * 构造函数
     *
     * @param principal 用户数据
     * @param password  用户密码
     * @param status    用户状态
     */
    public TokenUserDetails(@Nonnull final UserPrincipal principal, @Nullable final String password, @Nullable final Status status) {
        super(principal);
        this.password = password;
        this.status = status;
    }

    /**
     * 构造函数
     *
     * @param principal 用户数据
     */
    public TokenUserDetails(@Nonnull final UserPrincipal principal) {
        this(principal, null, null);
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
        return getAccount();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return getStatus() != null && Status.Enable == this.getStatus();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * 构建用户登录认证数据
     *
     * @param principal 用户数据
     * @param password  用户密码
     * @param status    用户状态
     * @return 认证数据
     */
    public static Mono<UserDetails> build(@Nonnull final UserPrincipal principal, @Nullable final String password, @Nullable final Status status) {
        return Mono.just(new TokenUserDetails(principal, password, status));
    }
}
