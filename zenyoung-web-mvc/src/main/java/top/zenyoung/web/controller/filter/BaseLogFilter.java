package top.zenyoung.web.controller.filter;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
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
import java.util.Iterator;
import java.util.Map;

/**
 * 日志过滤器
 *
 * @author young
 */
@Slf4j
public abstract class BaseLogFilter implements Filter, Ordered {
    private static final Charset CHARSET = StandardCharsets.UTF_8;

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

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        log.debug("doFilter()...");
        final LogWriter logWriter = getLogWriter();
        try {
            final HttpServletRequest req = (HttpServletRequest) request;
            final HttpServletResponse resp = (HttpServletResponse) response;
            //请求消息
            logWriter.writer("url", req.getRequestURI());
            logWriter.writer("method", req.getMethod());
            logWriter.writer("clientIpAddr", HttpUtils.getClientIpAddr(req));
            logWriter.writer("headers", getHeaders(req));
            logWriter.writer("params", getParams(req.getParameterMap()));
            //请求类型
            final String contentType = req.getContentType();
            if (!Strings.isNullOrEmpty(contentType) && contentType.toLowerCase().contains(MediaType.APPLICATION_JSON_VALUE)) {
                //application/json
                final RequestWrapper reqWrap = new RequestWrapper(req);
                final ResponseWrapper respWrap = new ResponseWrapper(resp);
                //业务处理
                chain.doFilter(reqWrap, respWrap);
                //
                logWriter.writer("req-body", ":\n" + reqWrap.getBody());
                logWriter.writer("resp-body", ":\n" + respWrap.getBody());
            } else {
                chain.doFilter(req, resp);
            }
        } finally {
            log.info(logWriter.outputLogs() + "");
        }
    }

    private Map<String, Serializable> getHeaders(@Nonnull final HttpServletRequest req) {
        final Map<String, Serializable> headerVals = Maps.newLinkedHashMap();
        final Iterator<String> names = req.getHeaderNames().asIterator();
        while (names.hasNext()) {
            final String name = names.next();
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
                    paramVals.put(key, Joiner.on(",").join(value));
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
            return new String(body, CHARSET);
        }

    }

    protected static class ResponseWrapper extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream outputStream;
        private final PrintWriter writer;

        public ResponseWrapper(final HttpServletResponse response) {
            super(response);
            outputStream = new ByteArrayOutputStream();
            writer = new PrintWriter(outputStream);
        }

        @Override
        public ServletOutputStream getOutputStream() {
            return new ServletOutputStream() {
                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setWriteListener(WriteListener listener) {

                }

                @Override
                public void write(final int b) {
                    outputStream.write(b);
                }
            };
        }

        @Override
        public PrintWriter getWriter() {
            return writer;
        }

        @SneakyThrows
        public void flush() {
            writer.flush();
            writer.close();
            outputStream.flush();
            outputStream.close();
        }

        public String getBody() {
            flush();
            return outputStream.toString(CHARSET);
        }
    }
}
