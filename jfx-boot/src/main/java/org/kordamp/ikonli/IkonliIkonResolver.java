package org.kordamp.ikonli;

import com.google.common.base.Strings;
import org.kordamp.jipsy.ServiceProviderFor;

import java.io.InputStream;
import java.net.URL;

/**
 * IkonliIkonResolver
 *
 * @author young
 */
@ServiceProviderFor(IkonHandler.class)
public class IkonliIkonResolver extends AbstractIkonHandler {
    private static final String FONT_RESOURCE = "/META-INF/ikonli/0.0.0/fonts/ikonli.ttf";

    @Override
    public boolean supports(final String description) {
        return !Strings.isNullOrEmpty(description) && description.startsWith("ikn-");
    }

    @Override
    public Ikon resolve(final String description) {
        return Ikonli.findByDescription(description);
    }

    @Override
    public URL getFontResource() {
        return getClass().getResource(FONT_RESOURCE);
    }

    @Override
    public InputStream getFontResourceAsStream() {
        return getClass().getResourceAsStream(FONT_RESOURCE);
    }

    @Override
    public String getFontFamily() {
        return "Ikonli";
    }
}
