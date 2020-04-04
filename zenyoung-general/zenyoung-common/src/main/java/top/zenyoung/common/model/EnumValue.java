package top.zenyoung.common.model;

import java.io.Serializable;

/**
 * 枚举值接口
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/3/22 11:22 下午
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
}
