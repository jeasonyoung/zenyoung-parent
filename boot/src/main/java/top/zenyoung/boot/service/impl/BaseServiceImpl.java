package top.zenyoung.boot.service.impl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import top.zenyoung.boot.service.BaseService;
import top.zenyoung.boot.service.BeanMappingService;
import top.zenyoung.common.exception.ServiceException;
import top.zenyoung.common.mapping.BeanMapping;
import top.zenyoung.common.model.ResultCode;
import top.zenyoung.common.paging.PageList;
import top.zenyoung.common.vo.ResultVO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * 服务基础接口实现类
 *
 * @author young
 */
@Slf4j
public class BaseServiceImpl implements BaseService, BeanMapping {
    @Autowired(required = false)
    @Getter(value = AccessLevel.PROTECTED)
    @Setter(value = AccessLevel.PROTECTED)
    private BeanMappingService mappingService;

    private <R> R mappingHandler(@Nonnull final Function<BeanMappingService, R> handler) {
        return Optional.ofNullable(getMappingService())
                .map(handler)
                .orElse(null);
    }

    @Override
    public <T, R> R mapping(@Nullable final T data, @Nonnull final Class<R> rClass) {
        return mappingHandler(bms -> bms.mapping(data, rClass));
    }

    @Override
    public <T, R> List<R> mapping(@Nullable final List<T> items, @Nonnull final Class<R> rClass) {
        return mappingHandler(bms -> bms.mapping(items, rClass));
    }

    @Override
    public <T extends Serializable, R extends Serializable> PageList<R> mapping(@Nullable final PageList<T> pageList,
                                                                                @Nonnull final Class<R> rClass) {
        return mappingHandler(bms -> bms.mapping(pageList, rClass));
    }

    protected <T extends Serializable, R extends Serializable> R validData(@Nullable final ResultVO<? extends T> ret,
                                                                           @Nonnull final Function<T, R> handler) {
        return Optional.ofNullable(ret)
                .map(t -> {
                    final boolean ok = t.getCode() == ResultCode.SUCCESS.getVal();
                    if (!ok) {
                        throw new ServiceException(t.getCode(), t.getMessage());
                    }
                    return handler.apply(t.getData());
                })
                .orElse(null);
    }

    protected <T extends Serializable> T validData(@Nullable final ResultVO<? extends T> ret) {
        return validData(ret, t -> t);
    }

    protected <T extends Serializable, R extends Serializable> R ejectionData(@Nullable final ResultVO<? extends T> ret,
                                                                              @Nonnull final Function<T, R> handler) {
        return Optional.ofNullable(ret)
                .map(t -> {
                    if (t.getCode() == ResultCode.SUCCESS.getVal()) {
                        return handler.apply(t.getData());
                    }
                    log.warn("ejectionData(ret: {})", t);
                    return null;
                })
                .orElse(null);
    }

    protected <T extends Serializable> T ejectionData(@Nullable final ResultVO<? extends T> ret) {
        return ejectionData(ret, t -> t);
    }
}
