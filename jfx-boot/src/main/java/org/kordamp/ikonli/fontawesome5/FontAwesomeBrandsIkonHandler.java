package org.kordamp.ikonli.fontawesome5;

import com.google.common.base.Strings;
import org.kordamp.ikonli.AbstractIkonHandler;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.IkonHandler;
import org.kordamp.jipsy.ServiceProviderFor;

import java.io.InputStream;
import java.net.URL;

/**
 * FontAwesomeBrandsIkonHandler
 *
 * @author young
 */
@ServiceProviderFor(IkonHandler.class)
public class FontAwesomeBrandsIkonHandler extends AbstractIkonHandler {
    private static final String FONT_RESOURCE = "/META-INF/fontawesome5/5.13.0/fonts/fa-brands-400.ttf";
    @Override
    public boolean supports(final String description) {
        return !Strings.isNullOrEmpty(description) && description.startsWith("fab-");
    }

    @Override
    public Ikon resolve(final String description) {
        return FontAwesomeBrands.findByDescription(description);
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
        return "Font Awesome 5 Brands Regular";
    }
}
