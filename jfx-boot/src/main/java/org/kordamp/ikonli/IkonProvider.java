package org.kordamp.ikonli;

/**
 * IkonProvider
 *
 * @param <K> 类型
 */
public interface IkonProvider<K extends Ikon> {
    /**
     * 获取Ikon对象
     *
     * @return Ikon对象
     */
    Class<K> getIkon();
}
