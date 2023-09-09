package top.zenyoung.retrofit.util;

import com.google.common.base.Strings;
import lombok.experimental.UtilityClass;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import top.zenyoung.retrofit.annotation.RetrofitClient;
import top.zenyoung.retrofit.exception.ReadResponseBodyException;

import javax.annotation.Nonnull;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

/**
 * Retrofit 工具类
 *
 * @author young
 */
@UtilityClass
public class RetrofitUtils {
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    public static final String CONTENT_ENCODING = "Content-Encoding";
    public static final String GZIP = "gzip";
    public static final String IDENTITY = "identity";
    private static final String SUFFIX = "/";

    public static String readResponseBody(@Nonnull final Response res) throws ReadResponseBodyException {
        try {
            final Headers headers = res.headers();
            if (bodyHasUnknownEncoding(headers)) {
                return null;
            }
            final ResponseBody resBody = res.body();
            if (Objects.isNull(resBody)) {
                return null;
            }
            final long contentLength = resBody.contentLength();
            final BufferedSource source = resBody.source();
            source.request(Long.MAX_VALUE);
            Buffer buf = source.getBuffer();
            if (GZIP.equalsIgnoreCase(headers.get(CONTENT_ENCODING))) {
                try (final GzipSource gzippedResBody = new GzipSource(buf.clone());
                     final Buffer buffer = new Buffer()) {
                    buffer.writeAll(gzippedResBody);
                    buf = buffer;
                }
            }
            Charset charset = UTF8;
            final MediaType contentType = resBody.contentType();
            if (Objects.nonNull(contentType)) {
                charset = contentType.charset(UTF8);
            }
            if (contentLength > 0) {
                final Charset utf8 = Optional.ofNullable(charset).orElse(UTF8);
                return Optional.ofNullable(buf.clone())
                        .map(b -> b.readString(utf8))
                        .orElse(null);
            }
            return null;
        } catch (Exception e) {
            throw new ReadResponseBodyException(e);
        }
    }

    private static boolean bodyHasUnknownEncoding(@Nonnull final Headers headers) {
        final String contentEncoding = headers.get(CONTENT_ENCODING);
        return !Strings.isNullOrEmpty(contentEncoding)
                && !IDENTITY.equalsIgnoreCase(contentEncoding)
                && !GZIP.equalsIgnoreCase(contentEncoding);
    }

    public static String convertBaseUrl(@Nonnull final RetrofitClient client, @Nonnull final Environment env) {
        String baseUrl = client.baseUrl();
        if (StringUtils.hasText(baseUrl)) {
            baseUrl = env.resolveRequiredPlaceholders(baseUrl);
            // 解析baseUrl占位符
            if (!baseUrl.endsWith(SUFFIX)) {
                baseUrl += SUFFIX;
            }
        } else {
            final String serviceId = client.serviceId();
            if (StringUtils.hasText(serviceId)) {
                baseUrl = env.resolveRequiredPlaceholders(serviceId);
            }
        }
        //统一路径前缀
        String path;
        if (!Strings.isNullOrEmpty(path = client.path())) {
            if (path.startsWith(SUFFIX)) {
                path = path.substring(1);
            }
            if (path.endsWith(SUFFIX)) {
                final int len = path.length();
                if (len > 1) {
                    path = path.substring(0, path.length() - 1);
                } else {
                    path = "";
                }
            }
            if (!Strings.isNullOrEmpty(path)) {
                if (baseUrl.endsWith(SUFFIX)) {
                    baseUrl += path;
                } else {
                    baseUrl += SUFFIX + path;
                }
            }
        }
        return baseUrl;
    }
}
