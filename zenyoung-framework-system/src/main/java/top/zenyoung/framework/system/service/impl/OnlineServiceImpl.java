package top.zenyoung.framework.system.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.paging.DataResult;
import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.common.util.JsonUtils;
import top.zenyoung.framework.Constants;
import top.zenyoung.framework.service.RedisEnhancedService;
import top.zenyoung.framework.system.dto.OnlineDTO;
import top.zenyoung.framework.system.dto.OnlineQueryDTO;
import top.zenyoung.framework.system.service.OnlineService;
import top.zenyoung.security.token.Ticket;
import top.zenyoung.security.token.TokenService;
import top.zenyoung.service.impl.BaseServiceImpl;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 用户在线-服务接口实现
 *
 * @author young
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OnlineServiceImpl extends BaseServiceImpl implements OnlineService {
    private final ObjectMapper objMapper;
    private final StringRedisTemplate redisTemplate;
    private final RedisEnhancedService enhancedService;

    private final TokenService tokenService;

    @Override
    public PagingResult<OnlineDTO> query(@Nonnull final OnlineQueryDTO dto) {
        return enhancedService.redisHandler(() -> {
            final String redisTicketKeyPrefix = Constants.getAuthTicketWithRefreshKey("*");
            final Set<String> keys = redisTemplate.keys(redisTicketKeyPrefix);
            if (!CollectionUtils.isEmpty(keys)) {
                final int pIdx = Math.max(dto.getPageIndex(), OnlineQueryDTO.DEF_PAGE_INDEX),
                        pSize = Math.max(dto.getPageSize(), OnlineQueryDTO.DEF_PAGE_SIZE);
                final int idx = Math.max(pIdx - 1, 0) * pSize;
                if (idx < keys.size()) {
                    final List<OnlineDTO> items = keys.parallelStream()
                            .skip(idx).limit(pSize)
                            .map(key -> {
                                final OnlineDTO data = getValByTicketKey(key);
                                if (Objects.nonNull(data) && key.endsWith(Constants.SEP_REDIS)) {
                                    final int index = key.lastIndexOf(Constants.SEP_REDIS);
                                    if (index > 0) {
                                        data.setKey(key.substring(index + 1));
                                    }
                                }
                                return data;
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    return DataResult.of((long) keys.size(), items);
                }
            }
            return null;
        });
    }

    private OnlineDTO getValByTicketKey(final String redisTicketKey) {
        if (!Strings.isNullOrEmpty(redisTicketKey)) {
            return enhancedService.redisHandler(() -> {
                final String json = redisTemplate.opsForValue().get(redisTicketKey);
                if (!Strings.isNullOrEmpty(json)) {
                    final Ticket ticket = JsonUtils.fromJson(objMapper, json, Ticket.class);
                    if (Objects.nonNull(ticket)) {
                        return mapping(ticket, OnlineDTO.class);
                    }
                }
                return null;
            });
        }
        return null;
    }

    @Override
    public OnlineDTO getByKey(@Nonnull final String key) {
        if (!Strings.isNullOrEmpty(key)) {
            final String redisTicketKey = Constants.getAuthTicketWithRefreshKey(key);
            return getValByTicketKey(redisTicketKey);
        }
        return null;
    }

    @Override
    public boolean batchForceExitByKeys(@Nonnull final String[] keys) {
        if (keys.length > 0) {
            return enhancedService.redisHandler(() -> {
                final List<String> delKeys = Stream.of(keys).parallel()
                        .filter(key -> !Strings.isNullOrEmpty(key))
                        .map(key -> new ArrayList<String>(4) {
                            {
                                //访问令牌缓存键
                                final String tokenKey = Constants.getAuthAccessWithRefreshKey(key);
                                if (!Strings.isNullOrEmpty(tokenKey)) {
                                    add(tokenKey);
                                    final String accessToken = enhancedService.redisHandler(() -> redisTemplate.opsForValue().get(tokenKey));
                                    if (!Strings.isNullOrEmpty(accessToken)) {
                                        //刷新令牌缓存键
                                        add(Constants.getAuthRefreshWithAccessKey(accessToken));
                                        //解析用户令牌
                                        final Ticket ticket = tokenService.validToken(accessToken);
                                        if (Objects.nonNull(ticket)) {
                                            final String refreshKey = Constants.getAuthRefreshWithTicketKey(ticket);
                                            add(refreshKey);
                                        }
                                    }
                                }
                                //票据缓存键
                                add(Constants.getAuthTicketWithRefreshKey(key));
                            }
                        })
                        .flatMap(Collection::stream)
                        .distinct()
                        .collect(Collectors.toList());
                //删除键
                final Long ret = redisTemplate.delete(delKeys);
                return Objects.nonNull(ret) && ret > 0;
            });
        }
        return false;
    }
}
