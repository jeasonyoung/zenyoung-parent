package org.kordamp.ikonli;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Ikonli-枚举
 *
 * @author young
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Ikonli implements Ikon {
    /**
     * NONE
     */
    NONE("ikn-none", '\ue600');

    private final String description;
    private final int code;

    public static Ikonli findByDescription(final String description) {
        for (final Ikonli font : values()) {
            if (font.getDescription().equalsIgnoreCase(description)) {
                return font;
            }
        }
        throw new IllegalArgumentException("Icon description '" + description + "' is invalid!");
    }
}
