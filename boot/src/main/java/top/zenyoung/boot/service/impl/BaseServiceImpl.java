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
    private BeanMappingService mappingService;

    /**
     * 设置BeanMappingService
     *
     * @param mappingService BeanMappingService对象
     */
    protected final void setMappingService(@Nonnull final BeanMappingService mappingService) {
        this.mappingService = mappingService;
    }

    /**
     * 获取BeanMappingService
     *
     * @return BeanMappingService对象
     */
    protected BeanMappingService getMappingService() {
        return this.mappingService;
    }

    private <T, R> R mappingHandler(@Nonnull final Function<BeanMappingService, R> handler) {
        return Optional.ofNullable(getMappingService())
                .map(handler)
                .orElse(null);
    }

    @Override
    public <T, MT> MT mapping(@Nullable final T data, @Nonnull final Class<MT> mtClass) {
        return mappingHandler(bms -> bms.mapping(data, mtClass));
    }

    @Override
    public <T, MT> List<MT> mapping(@Nullable final List<T> items, @Nonnull final Class<MT> mtClass) {
        return mappingHandler(bms -> bms.mapping(items, mtClass));
    }

    @Override
    public <T, MT> PageList<MT> mapping(@Nullable final PageList<T> pageList, @Nonnull final Class<MT> mtClass) {
        return mappingHandler(bms -> bms.mapping(pageList, mtClass));
    }

    protected <T, R> R validData(@Nullable final ResultVO<? extends T> ret, @Nonnull final Function<T, R> handler) {
        return Optional.ofNullable(ret)
                .map(t -> {
                    final boolean ok = t.getCode() == ResultCode.Success.getVal();
                    if (!ok) {
                        throw new ServiceException(t.getCode(), t.getMessage());
                    }
                    return handler.apply(t.getData());
                })
                .orElse(null);
    }

    protected <T> T validData(@Nullable final ResultVO<? extends T> ret) {
        return validData(ret, t -> t);
    }

    protected <T, R> R ejectionData(@Nullable final ResultVO<? extends T> ret, @Nonnull final Function<T, R> handler) {
        return Optional.ofNullable(ret)
                .map(t -> {
                    if (t.getCode() == ResultCode.Success.getVal()) {
                        return handler.apply(t.getData());
                    }
                    log.warn("ejectionData(ret: {})", t);
                    return null;
                })
                .orElse(null);
    }

    protected <T> T ejectionData(@Nullable final ResultVO<? extends T> ret) {
        return ejectionData(ret, t -> t);
    }
}
