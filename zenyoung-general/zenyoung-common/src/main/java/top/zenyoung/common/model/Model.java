package top.zenyoung.common.model;

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Map;

/**
 * 数据模型接口
 *
 * @author young
 */
public interface Model extends Serializable {

    /**
     * 转换为数据Map
     *
     * @return 数据Map
     */
    default Map<String, Serializable> toMap() {
        return Maps.newLinkedHashMap();
    }
}
