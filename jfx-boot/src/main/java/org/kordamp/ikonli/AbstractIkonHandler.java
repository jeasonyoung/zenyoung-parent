package org.kordamp.ikonli;

import lombok.Data;

/**
 * IkonHandler-抽象类
 *
 * @author young
 */
@Data
public abstract class AbstractIkonHandler implements IkonHandler {
    private Object font;
}
