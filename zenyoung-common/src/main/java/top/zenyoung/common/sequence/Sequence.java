package top.zenyoung.common.sequence;

import java.io.Serializable;

/**
 * 序号接口
 *
 * @author young
 */
public interface Sequence<T extends Serializable> {
    /**
     * 生成序号数据
     *
     * @return 序号数据
     */
    T nextId();
}
