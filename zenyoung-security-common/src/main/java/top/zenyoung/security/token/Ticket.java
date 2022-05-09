package top.zenyoung.security.token;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.model.UserPrincipal;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 令牌票据
 *
 * @author yangyong
 * @version 1.0
 * 2020/3/19 4:06 下午
 **/
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Ticket extends UserPrincipal {
    private static final String KEY_ID = "id";
    private static final String KEY_ACCOUNT = "account";
    private static final String KEY_ROLES = "roles";
    private static final String KEY_DEVICE = "device";
    private static final String KEY_EXTS = "exts";

    private static final String SEP_ROLE = ",";

    public Ticket(@Nonnull final UserPrincipal principal) {
        super(principal);
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
                put(KEY_ROLES, CollectionUtils.isEmpty(roles) ? null : Joiner.on(SEP_ROLE).skipNulls().join(roles));
                //设备标识
                put(KEY_DEVICE, getDevice());
                //扩展数据
                final Map<String, Serializable> exts = getExts();
                put(KEY_EXTS, CollectionUtils.isEmpty(exts) ? null : exts);
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
            //设备标识
            ticket.setDevice(convert(KEY_DEVICE, claims, Object::toString));
            //扩展数据
            final Object objVal = claims.getOrDefault(KEY_EXTS, null);
            if (objVal instanceof Map) {
                ((Map<?, ?>) objVal).forEach((k, v) -> {
                    if ((k instanceof String) && (v instanceof Serializable)) {
                        final String key = (String) k;
                        if (!Strings.isNullOrEmpty(key)) {
                            ticket.add(key, (Serializable) v);
                        }
                    }
                });
            }
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
