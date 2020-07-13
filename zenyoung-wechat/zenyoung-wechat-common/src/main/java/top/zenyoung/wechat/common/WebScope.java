package top.zenyoung.wechat.common;

import com.google.common.base.Strings;
import lombok.Getter;
import top.zenyoung.common.model.EnumValue;

import javax.annotation.Nullable;

/**
 * Web授权范围-枚举
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/7/12 1:23 下午
 **/
@Getter
public enum WebScope implements EnumValue {
    /**
     * 基础授权
     */
    Base(0, "snsapi_base"),
    /**
     * 用户基本信息
     */
    Userinfo(1, "snsapi_userinfo");

    private final int val;
    private final String title;

    WebScope(final int val, final String title) {
        this.val = val;
        this.title = title;
    }

    /**
     * 枚举转换
     *
     * @param title 枚举标题
     * @return 枚举数据
     */
    public static WebScope ofTitle(@Nullable final String title) {
        if (!Strings.isNullOrEmpty(title)) {
            for (WebScope scope : WebScope.values()) {
                if (title.equalsIgnoreCase(scope.getTitle())) {
                    return scope;
                }
            }
        }
        return Base;
    }
}
