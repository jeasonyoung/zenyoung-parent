package top.zenyoung.framework.runtime.service.impl;

import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.service.BeanMappingService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * javaBean对象内容复制服务接口实现
 *
 * @author young
 */
@Service
public class BeanMappingServiceImpl implements BeanMappingService {
    private final ModelMapper modelMapper = new ModelMapper();

    @PostConstruct
    private void init() {
        final Configuration configuration = this.modelMapper.getConfiguration();
        configuration.setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setDeepCopyEnabled(true)
                .setFullTypeMatchingRequired(true);
    }

    @Override
    public <T, MT> MT mapping(@Nullable final T data, @Nonnull final Class<MT> tClass) {
        if (Objects.isNull(data)) {
            return null;
        }
        return this.modelMapper.map(data, tClass);
    }

    @Override
    public <T, MT> List<MT> mapping(@Nullable final List<T> ts, @Nonnull final Class<MT> tClass) {
        if (CollectionUtils.isEmpty(ts)) {
            return null;
        }
        return ts.stream()
                .map(item -> this.mapping(item, tClass))
                .collect(Collectors.toList());
    }

    @Override
    public <T extends Serializable, MT extends Serializable> PagingResult<MT> mapping(@Nullable final PagingResult<T> pageList,
                                                                                      @Nonnull final Class<MT> tClass) {
        if (Objects.isNull(pageList)) {
            return null;
        }
        final Long total = pageList.getTotal();
        final List<T> items = pageList.getRows();
        final List<MT> rows = this.mapping(items, tClass);
        return new PagingResult<MT>() {
            @Override
            public Long getTotal() {
                return total;
            }

            @Override
            public List<MT> getRows() {
                return rows;
            }
        };
    }
}