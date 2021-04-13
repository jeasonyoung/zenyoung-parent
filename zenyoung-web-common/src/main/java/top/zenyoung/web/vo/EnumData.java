package top.zenyoung.web.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.zenyoung.common.model.EnumValue;

import javax.annotation.Nullable;

/**
 * 枚举值-数据
 *
 * @author yangyong
 * @version 1.0
 **/
@Getter
@RequiredArgsConstructor(staticName = "of")
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
     * @param data 枚举数据接口
     * @return 枚举数据
     */
    public static EnumData of(@Nullable final EnumValue data) {
        if (data != null) {
            return new EnumData(data.getVal(), data.getTitle());
        }
        return null;
    }
}
