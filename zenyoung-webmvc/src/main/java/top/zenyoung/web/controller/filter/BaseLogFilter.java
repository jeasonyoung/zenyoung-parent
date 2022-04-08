package top.zenyoung.web.controller.filter;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.util.CollectionUtils;
import top.zenyoung.web.controller.util.HttpUtils;
import top.zenyoung.web.util.LogWriter;
import top.zenyoung.web.util.LogWriterDefault;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * 日志过滤器
 *
 * @author young
 */
@Slf4j
public abstract class BaseLogFilter implements Filter, Ordered {
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final List<MediaType> FILTER_CONTENT_TYPES = Lists.newArrayList(
            MediaType.APPLICATION_JSON,
            MediaType.APPLICATION_FORM_URLENCODED
    );

    @Override
    public int getOrder() {
        return -10;
    }

    @Nonnull
    protected LogWriter getLogWriter() {
        return new LogWriterDefault();
    }

    @Override
    public void init(final FilterConfig filterConfig) {
        log.info("init(config: {})...", filterConfig);
    }

    private void addLogBodyContentType() {
        final MediaType[] types = getLogBodyContentTypes();
        if (types != null && types.length > 0) {
            final List<MediaType> contentTypes = FILTER_CONTENT_TYPES;
            contentTypes.addAll(
                    Stream.of(types)
                            .filter(t -> t != null && !contentTypes.contains(t))
                            .collect(Collectors.toList())
            );
        }
    }

    /**
     * 添加打印日志的请求类型
     *
     * @return 请求类型
     */
    protected MediaType[] getLogBodyContentTypes() {
        return null;
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        log.debug("doFilter()...");
        final LogWriter logWriter = getLogWriter();
        try {
            final HttpServletRequest req = (HttpServletRequest) request;
            final HttpServletResponse resp = (HttpServletResponse) response;
            //请求消息
            logWriter.writer(req.getMethod(), req.getRequestURI());
            logWriter.writer("ip", HttpUtils.getClientIpAddr(req));
            logWriter.writer("headers", getHeaders(req));
            logWriter.writer("params", getParams(req.getParameterMap()));
            //添加请求类型集合
            addLogBodyContentType();
            //请求类型
            final ServletServerHttpRequest httpRequest = new ServletServerHttpRequest(req);
            final MediaType reqContentType = httpRequest.getHeaders().getContentType();
            //业务处理
            final RequestWrapper reqWrap = new RequestWrapper(req);
            final ResponseWrapper respWrap = new ResponseWrapper(resp);
            chain.doFilter(reqWrap, respWrap);
            //请求报文内容
            if (checkContentTypes(reqContentType)) {
                logWriter.writer("req-body", buildBodyToMap(reqWrap.getBody()));
            }
            //响应类型
            final ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(respWrap);
            final MediaType respContentType = httpResponse.getHeaders().getContentType();
            if (respContentType == null || checkContentTypes(respContentType)) {
                final String respBody = respWrap.getBody();
                if (!Strings.isNullOrEmpty(respBody)) {
                    final Map<String, Serializable> respBodyMap = buildBodyToMap(respBody);
                    if (respBodyMap != null) {
                        logWriter.writer("resp-body", respBodyMap);
                    } else {
                        logWriter.writer("resp-body", respBody);
                    }
                }
            }
        } finally {
            log.info(logWriter.outputLogs() + "");
        }
    }

    /**
     * 构建数据集合
     *
     * @param bodyJson json串
     * @return 集合数据
     */
    protected abstract Map<String, Serializable> buildBodyToMap(@Nonnull final String bodyJson);

    private boolean checkContentTypes(@Nullable final MediaType contentType) {
        if (contentType != null && !FILTER_CONTENT_TYPES.isEmpty()) {
            for (MediaType t : FILTER_CONTENT_TYPES) {
                if (t != null && t.isCompatibleWith(contentType)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Map<String, Serializable> getHeaders(@Nonnull final HttpServletRequest req) {
        final Map<String, Serializable> headerVals = Maps.newLinkedHashMap();
        final Enumeration<String> names = req.getHeaderNames();
        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            if (!Strings.isNullOrEmpty(name)) {
                final String val = req.getHeader(name);
                if (!Strings.isNullOrEmpty(val)) {
                    headerVals.put(name, val);
                }
            }
        }
        return headerVals;
    }

    private Map<String, Serializable> getParams(@Nullable final Map<String, String[]> params) {
        final Map<String, Serializable> paramVals = Maps.newLinkedHashMap();
        if (!CollectionUtils.isEmpty(params)) {
            params.forEach((key, value) -> {
                if (!Strings.isNullOrEmpty(key) && value != null) {
                    paramVals.put(key, Joiner.on(",").skipNulls().join(value));
                }
            });
        }
        return paramVals;
    }

    @Override
    public void destroy() {

    }

    protected static class RequestWrapper extends HttpServletRequestWrapper {
        private final byte[] body;

        @SneakyThrows
        public RequestWrapper(final HttpServletRequest request) {
            super(request);
            body = readBytes(request.getReader());
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream()));
        }

        @Override
        public ServletInputStream getInputStream() {
            final ByteArrayInputStream bais = new ByteArrayInputStream(body);
            return new ServletInputStream() {

                @Override
                public int read() {
                    return bais.read();
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(final ReadListener listener) {

                }

                @Override
                public boolean isFinished() {
                    return bais.available() == 0;
                }
            };
        }

        @SneakyThrows
        private byte[] readBytes(final BufferedReader br) {
            final StringBuilder builder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line);
            }
            if (builder.length() > 0) {
                return builder.toString().getBytes(CHARSET);
            }
            return null;
        }

        public String getBody() {
            return body == null ? null : new String(body, CHARSET);
        }
    }

    protected static class ResponseWrapper extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream output;
        private final PrintWriter cachedWriter;
        private final HttpServletResponse response;
        private ServletOutputStream filterOutput;

        public ResponseWrapper(final HttpServletResponse response) {
            super(response);
            this.response = response;
            output = new ByteArrayOutputStream();
            cachedWriter = new PrintWriter(output);
        }

        @Override
        public ServletOutputStream getOutputStream() {
            if (filterOutput == null) {
                filterOutput = new ServletOutputStream() {
                    @Override
                    public boolean isReady() {
                        return false;
                    }

                    @Override
                    public void setWriteListener(final WriteListener listener) {

                    }

                    @Override
                    public void write(int b) {
                        output.write(b);
                    }

                    @Override
                    public void flush() throws IOException {
                        if (!response.isCommitted()) {
                            final byte[] body = toByteArray();
                            final ServletOutputStream outputStream = response.getOutputStream();
                            outputStream.write(body);
                            outputStream.flush();
                        }
                    }
                };
            }
            return filterOutput;
        }

        @Override
        public PrintWriter getWriter() {
            return cachedWriter;
        }

        public byte[] toByteArray() {
            return output.toByteArray();
        }

        @SneakyThrows
        public String getBody() {
            final byte[] body = toByteArray();
            return body == null ? null : new String(body, CHARSET);
        }
    }
}
