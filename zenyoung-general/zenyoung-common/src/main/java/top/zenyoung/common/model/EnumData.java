package top.zenyoung.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;

/**
 * 枚举值-数据
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/3/22 11:19 下午
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnumData implements EnumValue {
    /**
     * 枚举值
     */
    private int val;
    /**
     * 枚举标题
     */
    private String title;

    /**
     * 构造函数
     *
     * @param ev 枚举值接口
     * @return 枚举值对象
     */
    public static EnumData parse(@Nullable final EnumValue ev) {
        if (ev != null) {
            return new EnumData(ev.getVal(), ev.getTitle());
        }
        return null;
    }
}
