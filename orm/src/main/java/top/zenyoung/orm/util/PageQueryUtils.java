package top.zenyoung.orm.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import top.zenyoung.common.dto.BasePageDTO;
import top.zenyoung.common.paging.DataResult;
import top.zenyoung.common.paging.PageList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * 分页查询工具类
 *
 * @author young
 */
@UtilityClass
public class PageQueryUtils {
    private static final int PAGE_IDX = BasePageDTO.DEF_PAGE_INDEX, PAGE_SIZE = BasePageDTO.DEF_PAGE_SIZE;

    public static <T, R> PageList<R> wrapper(@Nullable final Integer pageNum, @Nullable final Integer pageSize,
                                             @Nonnull final UnaryOperator<IPage<T>> queryHandler,
                                             @Nonnull final Function<T, R> convert) {
        final long idx = pageNum == null || pageNum <= 0 ? PAGE_IDX : pageNum;
        final long size = pageSize == null || pageSize <= 0 ? PAGE_SIZE : pageSize;
        IPage<T> page = new Page<>(idx, size);
        page = queryHandler.apply(page);
        if (page != null) {
            final List<R> items = page.getRecords().stream()
                    .filter(Objects::nonNull)
                    .map(convert)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            return DataResult.of(page.getTotal(), items);
        }
        return DataResult.of(Lists.newArrayList());
    }

    public static <T> PageList<T> wrapper(@Nullable final Integer pageNum, @Nullable final Integer pageSize,
                                          @Nonnull final UnaryOperator<IPage<T>> queryHandler) {
        return wrapper(pageNum, pageSize, queryHandler, Function.identity());
    }

    public static <T, P extends BasePageDTO> PageList<T> wrapper(@Nullable final P dto,
                                                                 @Nonnull final UnaryOperator<IPage<T>> queryHandler) {
        if (Objects.isNull(dto)) {
            return wrapper(null, null, queryHandler);
        }
        return wrapper(dto.getPageIndex(), dto.getPageSize(), queryHandler);
    }
}
