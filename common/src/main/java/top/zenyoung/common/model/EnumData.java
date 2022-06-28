package top.zenyoung.common.model;

import lombok.Getter;

import javax.annotation.Nullable;

/**
 * 枚举值-数据
 *
 * @author yangyong
 * @version 1.0
 **/
@Getter
public class EnumData implements EnumValue {
    /**
     * 枚举值
     */
    private final int val;
    /**
     * 枚举标题
     */
    private final String title;

    /**
     * 构造函数
     *
     * @param val   枚举值
     * @param title 枚举标题
     */
    public EnumData(final int val, final String title) {
        this.val = val;
        this.title = title;
    }

    /**
     * 枚举数据转换
     *
     * @param data 枚举数据接口
     * @return 枚举数据
     */
    public static EnumData parse(@Nullable final EnumValue data) {
        return EnumData.of(data);
    }

    /**
     * 静态构建枚举数据
     *
     * @param val   枚举值
     * @param title 枚举标题
     * @return 枚举数据
     */
    public static EnumData of(final int val, final String title) {
        return new EnumData(val, title);
    }

    /**
     * 静态构建枚举数据
     *
     * @param data 枚举数据接口
     * @return 枚举数据
     */
    public static EnumData of(@Nullable final EnumValue data) {
        if (data != null) {
            return EnumData.of(data.getVal(), data.getTitle());
        }
        return null;
    }
}
