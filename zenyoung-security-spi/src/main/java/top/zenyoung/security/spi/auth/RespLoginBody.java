package top.zenyoung.security.spi.auth;

import com.google.common.collect.Lists;
import lombok.*;
import org.springframework.beans.BeanUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

/**
 * 登录-响应报文体
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/12/22 8:09 下午
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespLoginBody implements Serializable {

    /**
     * 登录令牌(jwt格式,有效期:30分钟)
     */
    private String token;
    /**
     * 刷新令牌(md5(uuid),存在Redis中,调用刷新令牌接口后自动延期)
     */
    private String refreshToken;
    /**
     * 用户信息
     */
    private User user;
    /**
     * 用户菜单集合
     */
    @Builder.Default
    private List<Menu> menus = Lists.newLinkedList();

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class User extends UserInfo implements Serializable {
        /**
         * 用户角色集合
         */
        private List<String> roles = Lists.newLinkedList();

        /**
         * 构造函数
         *
         * @param userInfo 用户信息
         * @param roles    角色集合
         */
        public User(@Nonnull final UserInfo userInfo, @Nullable final List<String> roles) {
            BeanUtils.copyProperties(userInfo, this);
            this.roles = roles;
        }
    }
}
