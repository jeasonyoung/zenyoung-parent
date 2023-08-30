package org.kordamp.ikonli.fontawesome5;

import com.google.common.base.Strings;
import org.kordamp.ikonli.AbstractIkonHandler;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.IkonHandler;
import org.kordamp.jipsy.ServiceProviderFor;

import java.io.InputStream;
import java.net.URL;

/**
 * FontAwesomeRegularIkonHandler
 *
 * @author young
 */
@ServiceProviderFor(IkonHandler.class)
public class FontAwesomeRegularIkonHandler extends AbstractIkonHandler {
    private static final String FONT_RESOURCE = "/META-INF/fontawesome5/5.13.0/fonts/fa-regular-400.ttf";

    @Override
    public boolean supports(final String description) {
        return !Strings.isNullOrEmpty(description) && description.startsWith("far-");
    }

    @Override
    public Ikon resolve(final String description) {
        return FontAwesomeRegular.findByDescription(description);
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
        return "Font Awesome 5 Free Regular";
    }
}
