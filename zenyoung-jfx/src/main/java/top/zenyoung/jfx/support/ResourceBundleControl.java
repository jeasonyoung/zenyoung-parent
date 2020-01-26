package top.zenyoung.jfx.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Objects;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Control that uses a custom {@link Charset} when reading resource bundles
 * compared to the default charset which is ISO-8859-1.
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/1/23 6:39 下午
 **/
public final class ResourceBundleControl extends ResourceBundle.Control {
    private final Charset charset;

    public ResourceBundleControl(final Charset charset) {
        this.charset = Objects.requireNonNull(charset);
    }

    @Override
    public ResourceBundle newBundle(final String baseName, final Locale locale, final String format, final ClassLoader loader, final boolean reload) throws IllegalAccessException, InstantiationException, IOException {
        final String bundleName = toBundleName(baseName, locale);
        final String resourceName = toResourceName(bundleName, "properties");

        ResourceBundle bundle = null;
        InputStream stream = null;
        if (reload) {
            final URL url = loader.getResource(resourceName);
            if (url != null) {
                final URLConnection connection = url.openConnection();
                if (connection != null) {
                    connection.setUseCaches(false);
                    stream = connection.getInputStream();
                }
            }
        } else {
            stream = loader.getResourceAsStream(resourceName);
        }
        if (stream != null) {
            try {
                bundle = new PropertyResourceBundle(new InputStreamReader(stream, charset));
            } finally {
                stream.close();
            }
        }
        return bundle;
    }
}
