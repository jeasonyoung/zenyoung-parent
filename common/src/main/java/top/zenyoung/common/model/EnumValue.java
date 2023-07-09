package top.zenyoung.common.model;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Map;

/**
 * 枚举值接口
 *
 * @author yangyong
 * @version 1.0
 **/
public interface EnumValue extends Serializable {

    /**
     * 获取枚举值
     *
     * @return 枚举值
     */
    @JsonValue
    int getVal();

    /**
     * 获取枚举标题
     *
     * @return 枚举标题
     */
    String getTitle();

    /**
     * 转换为Map对象
     *
     * @return Map对象
     */
    default Map<String, Serializable> toMap() {
        final Map<String, Serializable> vals = Maps.newHashMap();
        //枚举值
        vals.put("val", getVal());
        //枚举标题
        vals.put("title", getTitle());
        //返回
        return vals;
    }
}
