package top.zenyoung.boot.filter;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import top.zenyoung.boot.util.HttpUtils;

import javax.annotation.Nonnull;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * 请求只读包装器
 *
 * @author young
 */
@Slf4j
@Getter
public class RequestBodyReaderWrapper extends HttpServletRequestWrapper {
    private final byte[] body;

    public RequestBodyReaderWrapper(final HttpServletRequest request) {
        super(request);
        this.body = this.readBody(request);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        final InputStreamReader streamReader = new InputStreamReader(getInputStream());
        return IOUtils.toBufferedReader(streamReader);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (Objects.isNull(this.body) || this.body.length == 0) {
            return super.getInputStream();
        }
        final ByteArrayInputStream bis = new ByteArrayInputStream(this.body);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(final ReadListener listener) {

            }

            @Override
            public int read() throws IOException {
                return bis.read();
            }
        };
    }

    @Nonnull
    protected List<MediaType> includeMediaTypes() {
        return Lists.newArrayList(
                MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_FORM_URLENCODED
        );
    }

    protected byte[] readBody(@Nonnull final HttpServletRequest request) {
        final MediaType mediaType = HttpUtils.getContentType(request);
        if (Objects.nonNull(mediaType)) {
            final List<MediaType> includes = this.includeMediaTypes();
            if (!CollectionUtils.isEmpty(includes) && includes.contains(mediaType)) {
                final Charset charset = StandardCharsets.UTF_8;
                try (final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                     final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(bos, charset))) {
                    final InputStream is = request.getInputStream();
                    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset))) {
                        final char[] buf = new char[1024];
                        int count;
                        while ((count = reader.read(buf, 0, buf.length)) != -1) {
                            writer.write(buf, 0, count);
                        }
                    }
                    return bos.toByteArray();
                } catch (IOException e) {
                    log.error("读取Body失败: {}", e.getMessage());
                }
            }
        }
        return new byte[0];
    }
}
