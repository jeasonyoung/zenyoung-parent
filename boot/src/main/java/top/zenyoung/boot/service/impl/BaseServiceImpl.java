package top.zenyoung.boot.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import top.zenyoung.boot.exception.ServiceException;
import top.zenyoung.boot.service.BaseService;
import top.zenyoung.boot.service.BeanMappingService;
import top.zenyoung.common.mapping.BeanMapping;
import top.zenyoung.common.model.ResultCode;
import top.zenyoung.common.paging.PageList;
import top.zenyoung.common.vo.ResultVO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * 服务基础接口实现类
 *
 * @author young
 */
@Slf4j
public class BaseServiceImpl implements BaseService, BeanMapping {

    @Autowired
    private BeanMappingService mappingService;

    @Override
    public <T, MT> MT mapping(@Nullable final T data, @Nonnull final Class<MT> mtClass) {
        return mappingService.mapping(data, mtClass);
    }

    @Override
    public <T, MT> List<MT> mapping(@Nullable final List<T> items, @Nonnull final Class<MT> mtClass) {
        return mappingService.mapping(items, mtClass);
    }

    @Override
    public <T, MT> PageList<MT> mapping(@Nullable final PageList<T> pageList, @Nonnull final Class<MT> mtClass) {
        return mappingService.mapping(pageList, mtClass);
    }

    protected <T, R> R validData(@Nullable final ResultVO<? extends T> ret, @Nonnull final Function<T, R> handler) {
        if (Objects.nonNull(ret)) {
            final boolean ok = ret.getCode() == ResultCode.Success.getVal();
            if (!ok) {
                throw new ServiceException(ret.getCode(), ret.getMessage());
            }
            return handler.apply(ret.getData());
        }
        return null;
    }

    protected <T> T validData(@Nullable final ResultVO<? extends T> ret) {
        return validData(ret, t -> t);
    }

    protected <T, R> R ejectionData(@Nullable final ResultVO<? extends T> ret, @Nonnull final Function<T, R> handler) {
        if (Objects.nonNull(ret)) {
            if (ret.getCode() == ResultCode.Success.getVal()) {
                return handler.apply(ret.getData());
            }
            log.warn("ejectionData(ret: {})", ret);
        }
        return null;
    }

    protected <T> T ejectionData(@Nullable final ResultVO<? extends T> ret) {
        return ejectionData(ret, t -> t);
    }

}
