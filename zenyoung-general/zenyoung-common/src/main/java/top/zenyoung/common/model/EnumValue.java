package top.zenyoung.common.model;

import java.io.Serializable;
import java.util.HashMap;
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
        return new HashMap<String, Serializable>(2) {
            {
                //枚举值
                put("val", getVal());
                //枚举标题
                put("title", getTitle());
            }
        };
    }
}
