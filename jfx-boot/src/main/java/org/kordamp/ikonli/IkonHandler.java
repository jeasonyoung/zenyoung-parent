package org.kordamp.ikonli;

import java.io.InputStream;
import java.net.URL;

/**
 * Ikon处理器
 *
 * @author young
 */
public interface IkonHandler {
    /**
     * 是否支持描述信息
     *
     * @param description 描述信息
     * @return 是否支持
     */
    boolean supports(final String description);

    /**
     * 根据描述信息处理
     *
     * @param description 描述信息
     * @return Ikon对象
     */
    Ikon resolve(final String description);

    /**
     * 获取Font资源URL
     *
     * @return 资源URL
     */
    URL getFontResource();

    /**
     * 获取Font资源流
     *
     * @return 资源流
     */
    InputStream getFontResourceAsStream();

    /**
     * 获取FontFamily
     *
     * @return FontFamily
     */
    String getFontFamily();

    /**
     * 获取Font对象
     *
     * @return Font对象
     */
    Object getFont();

    /**
     * 设置Font对象
     *
     * @param font Font对象
     */
    void setFont(final Object font);
}
