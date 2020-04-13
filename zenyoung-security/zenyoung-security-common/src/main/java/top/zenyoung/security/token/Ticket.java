package top.zenyoung.security.token;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import top.zenyoung.security.model.UserPrincipal;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 令牌票据
 *
 * @author yangyong
 * @version 1.0
 *  2020/3/19 4:06 下午
 **/
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Ticket extends UserPrincipal {
    private static final String KEY_ID = "id";
    private static final String KEY_ACCOUNT = "account";
    private static final String KEY_ROLES = "roles";

    private static final String SEP_ROLE = ",";

    public Ticket(@Nonnull final UserPrincipal principal) {
        //用户ID
        setId(principal.getId());
        //用户账号
        setAccount(principal.getAccount());
        //用户角色集合
        setRoles(getRoles());
    }

    public Map<String, Object> toClaims() {
        return new HashMap<String, Object>(3) {
            {
                //用户ID
                put(KEY_ID, getId());
                //用户账号
                put(KEY_ACCOUNT, getAccount());
                //用户角色集合
                final List<String> roles = getRoles();
                put(KEY_ROLES, (roles == null || roles.size() == 0) ? null : Joiner.on(SEP_ROLE).skipNulls().join(roles));
            }
        };
    }

    public static Ticket create(@Nonnull final Map<String, Object> claims) {
        final Ticket ticket = new Ticket();
        if (claims.size() > 0) {
            //用户ID
            ticket.setId(convert(KEY_ID, claims, Object::toString));
            //用户账号
            ticket.setAccount(convert(KEY_ACCOUNT, claims, Object::toString));
            //用户角色集合
            ticket.setRoles(convert(KEY_ROLES, claims, obj -> {
                final String roles = obj.toString();
                if (!Strings.isNullOrEmpty(roles)) {
                    return Splitter.on(SEP_ROLE).omitEmptyStrings().trimResults().splitToList(roles);
                }
                return null;
            }));
        }
        return ticket;
    }

    private static <T> T convert(@Nonnull final String key, @Nonnull final Map<String, Object> data, @Nonnull final Function<Object, T> handler) {
        if (!Strings.isNullOrEmpty(key)) {
            final Object obj = data.getOrDefault(key, null);
            if (obj != null) {
                return handler.apply(obj);
            }
        }
        return null;
    }
}
