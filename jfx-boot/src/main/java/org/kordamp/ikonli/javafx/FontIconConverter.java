package org.kordamp.ikonli.javafx;

import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.scene.text.Font;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.kordamp.ikonli.Ikon;

/**
 * FontIconConverter
 *
 * @author young
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FontIconConverter extends StyleConverter<String, Ikon> {
    private static class Holder {
        private static final FontIconConverter INSTANCE = new FontIconConverter();
        private static final SequenceConverter SEQUENCE_INSTANCE = new SequenceConverter();
    }

    public static StyleConverter<String, Ikon> getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public Ikon convert(final ParsedValue<String, Ikon> value,final Font font) {
        final String description = value.getValue().trim();
        return IkonResolver.getInstance().resolve(description).resolve(description);
    }

    @Override
    public String toString() {
        return "FontIconConverter";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class SequenceConverter extends StyleConverter<String, Ikon[]> {

        public static SequenceConverter getInstance() {
            return Holder.SEQUENCE_INSTANCE;
        }

        @Override
        public Ikon[] convert(final ParsedValue<String, Ikon[]> value,final Font font) {
            final String[] descriptions = value.getValue().split(",");
            final Ikon[] ikons = new Ikon[descriptions.length];
            for (int i = 0; i < descriptions.length; i++) {
                final String description = descriptions[i].trim();
                ikons[i] = IkonResolver.getInstance().resolve(description).resolve(description);
            }
            return ikons;
        }

        @Override
        public String toString() {
            return "FontIcon.SequenceConverter";
        }
    }
}
