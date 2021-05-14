package top.zenyoung.web.controller.util;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.web.ParamHandler;
import top.zenyoung.web.vo.ReqPagingQuery;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Map;

/**
 * 请求处理工具类
 *
 * @author young
 */
@Slf4j
public class ReqUtils {
    private static final String PAGING_QUERY_BY_INDEX = "index";
    private static final String PAGING_QUERY_BY_ROWS = "rows";

    public static Map<String, String[]> getReqParams() {
        final ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            return attrs.getRequest().getParameterMap();
        }
        return null;
    }

    public static <ReqQry extends Serializable> ReqQry parseQuery(@Nonnull final Class<ReqQry> reqQueryClass, @Nonnull final ParamHandler handler) {
        //获取当前请求
        final Map<String, String[]> params = getReqParams();
        if (!CollectionUtils.isEmpty(params)) {
            return parse(params, reqQueryClass, handler);
        }
        return null;
    }

    @Nonnull
    public static <ReqQry extends Serializable> PagingQuery<ReqQry> parsePagingQuery(@Nonnull final Class<ReqQry> reqPagingQueryClass, @Nonnull final ParamHandler handler) {
        //获取当前请求
        final Map<String, String[]> params = getReqParams();
        if (!CollectionUtils.isEmpty(params)) {
            final ReqPagingQuery<ReqQry> pagingQuery = new ReqPagingQuery<>();
            //获取页码
            pagingQuery.setIndex(getIntValue(params, PAGING_QUERY_BY_INDEX));
            //获取每页数据量
            pagingQuery.setRows(getIntValue(params, PAGING_QUERY_BY_ROWS));
            //查询条件
            pagingQuery.setQuery(parseQueryBody(params, reqPagingQueryClass, handler));
            //返回对象
            return pagingQuery;
        }
        return new ReqPagingQuery<>();
    }

    private static Integer getIntValue(@Nonnull final Map<String, String[]> params, @Nonnull final String paramName) {
        if (!CollectionUtils.isEmpty(params) && !Strings.isNullOrEmpty(paramName)) {
            final String[] vals = params.getOrDefault(paramName, null);
            if (vals != null && vals.length > 0) {
                try {
                    return Integer.parseInt(vals[0]);
                } catch (Throwable ex) {
                    log.warn("getIntValue[paramName: {}]-exp: {}", paramName, ex.getMessage());
                }
            }
        }
        return 0;
    }

    private static <ReqQry extends Serializable> ReqQry parseQueryBody(@Nonnull final Map<String, String[]> params, @Nonnull final Class<ReqQry> reqPagingQueryClass, @Nonnull final ParamHandler handler) {
        if (!CollectionUtils.isEmpty(params)) {
            try {
                final Map<String, String[]> out = Maps.filterEntries(params, entry -> {
                    final String key = entry.getKey();
                    if (!Strings.isNullOrEmpty(key)) {
                        return !key.equalsIgnoreCase(PAGING_QUERY_BY_INDEX) && !key.equalsIgnoreCase(PAGING_QUERY_BY_ROWS);
                    }
                    return false;
                });
                return parse(out, reqPagingQueryClass, handler);
            } catch (Throwable ex) {
                log.warn("parseBody-exp: {}", ex.getMessage());
            }
        }
        return null;
    }

    public static <T extends Serializable> T parse(@Nonnull final Map<String, String[]> params, @Nonnull final Class<T> dataClass, @Nonnull final ParamHandler handler) {
        log.debug("parse(params: {},dataClass: {},handler: {})...", params, dataClass, handler);
        try {
            if (!CollectionUtils.isEmpty(params)) {
                final String json = handler.serialize(params);
                if (!Strings.isNullOrEmpty(json)) {
                    return handler.deserialize(json, dataClass);
                }
            }
        } catch (Throwable ex) {
            log.warn("parse(params: {},dataClass: {},handler: {})-exp: {}", params, dataClass, handler, ex.getMessage());
        }
        return null;
    }
}
