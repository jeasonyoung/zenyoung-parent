package top.zenyoung.web.controller.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.common.util.JsonUtils;
import top.zenyoung.web.ParamHandler;
import top.zenyoung.web.vo.ReqPagingQuery;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
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

    public static String getExceptionError(@Nullable final Throwable ex) {
        if (ex != null) {
            String error = ex.getMessage();
            if (!Strings.isNullOrEmpty(error)) {
                return error;
            }
            if (Strings.isNullOrEmpty(error)) {
                final Throwable cause = ex.getCause();
                if (cause != null) {
                    return getExceptionError(cause);
                }
                final StackTraceElement[] traces = ex.getStackTrace();
                if (traces != null && traces.length > 0) {
                    return JsonUtils.toJson(new ObjectMapper(), traces);
                }
            }
        }
        return "未知错误";
    }

    public static Map<String, String[]> getReqParams() {
        final HttpServletRequest request = HttpUtils.getWebRequest();
        if(request != null){
            return request.getParameterMap();
        }
        return null;
    }

    public static <Req extends Serializable> Req parseReq(@Nonnull final Class<Req> reqClass, @Nonnull final ParamHandler handler) {
        //获取当前请求
        final Map<String, String[]> params = getReqParams();
        if (!CollectionUtils.isEmpty(params)) {
            return parse(params, reqClass, handler);
        }
        return null;
    }

    public static <ReqQry extends Serializable> ReqQry parseQuery(@Nonnull final Class<ReqQry> reqQueryClass, @Nonnull final ParamHandler handler) {
        return parseReq(reqQueryClass, handler);
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
                final Map<String, Serializable> reqParams = Maps.newLinkedHashMap();
                params.forEach((key, vals) -> {
                    if (!Strings.isNullOrEmpty(key) && vals != null && vals.length > 0) {
                        if (vals.length > 1) {
                            reqParams.put(key, Lists.newArrayList(vals));
                        } else {
                            reqParams.put(key, vals[0]);
                        }
                    }
                });
                final String json = handler.serialize(reqParams);
                if (!Strings.isNullOrEmpty(json)) {
                    log.debug("parse(params: {},dataClass: {},handler: {})=>\n {}", params, dataClass, handler, json);
                    final T req = handler.deserialize(json, dataClass);
                    if (req != null) {
                        //校验处理
                        handler.paramValidator(req);
                        //返回数据
                        return req;
                    }
                }
            }
        } catch (Throwable ex) {
            log.warn("parse(params: {},dataClass: {},handler: {})-exp: {}", params, dataClass, handler, ex.getMessage());
        }
        return null;
    }
}
