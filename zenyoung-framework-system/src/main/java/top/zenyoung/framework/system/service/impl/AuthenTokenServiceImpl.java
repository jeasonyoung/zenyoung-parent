package top.zenyoung.framework.system.service.impl;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.zenyoung.framework.auth.AuthUser;
import top.zenyoung.framework.auth.UserInfo;
import top.zenyoung.framework.system.Constants;
import top.zenyoung.framework.system.dao.repository.UserRepository;
import top.zenyoung.framework.system.dto.UserDTO;
import top.zenyoung.framework.system.service.AuthenTokenService;
import top.zenyoung.service.impl.BaseServiceImpl;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * 认证令牌服务接口实现
 *
 * @author young
 */
@Service
@RequiredArgsConstructor
public class AuthenTokenServiceImpl extends BaseServiceImpl implements AuthenTokenService {
    private final UserRepository repository;

    @Override
    @Cached(name = Constants.CACHE_PREFIX + "user" + Constants.CACHE_SEP, key = "#account", cacheType = CacheType.BOTH, expire = Constants.CACHE_EXPIRE)
    public AuthUser findByAccount(@Nonnull final String account) {
        return repository.findByAccount(account);
    }

    @Override
    @Cached(name = Constants.CACHE_PREFIX + "user", key = "#userId", cacheType = CacheType.BOTH, expire = Constants.CACHE_EXPIRE)
    public UserInfo getUserInfo(@Nonnull final Long userId) {
        final UserDTO data = repository.getById(userId);
        if (Objects.nonNull(data)) {
            return mapping(data, UserInfo.class);
        }
        return null;
    }
}
