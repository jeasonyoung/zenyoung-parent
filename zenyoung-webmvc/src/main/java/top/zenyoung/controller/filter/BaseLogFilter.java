package top.zenyoung.controller.filter;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import top.zenyoung.web.filter.LogFilterWriter;
import top.zenyoung.web.filter.LogFilterWriterDefault;

import javax.annotation.Nonnull;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 日志过滤器
 *
 * @author young
 */
@Slf4j
public abstract class BaseLogFilter implements Filter, Ordered {

    private LogFilterWriter logFilterWriter;

    @Override
    public int getOrder() {
        return -10;
    }

    @Nonnull
    protected LogFilterWriter getLogWriter() {
        return new LogFilterWriterDefault();
    }

    @Override
    public void init(final FilterConfig filterConfig) {
        this.logFilterWriter = getLogWriter();
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        log.debug("doFilter()...");
        final HttpServletRequest req = (HttpServletRequest) request;

    }

    @Override
    public void destroy() {

    }

    protected static class RepeatedlyReadRequestWrapper extends HttpServletRequestWrapper {
        private final byte[] body;

        @SneakyThrows
        public RepeatedlyReadRequestWrapper(final HttpServletRequest request) {
            super(request);
            body = readBytes(request.getReader());
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(getInputStream()));
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
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
                return builder.toString().getBytes(StandardCharsets.UTF_8);
            }
            return null;
        }
    }

    protected static class RepeatedlyReadResponseWrapper extends HttpServletResponseWrapper {

        public RepeatedlyReadResponseWrapper(final HttpServletResponse response) {
            super(response);
        }
    }
}
