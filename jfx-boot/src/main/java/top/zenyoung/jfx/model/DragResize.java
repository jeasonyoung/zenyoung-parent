package top.zenyoung.jfx.model;

import lombok.Data;

/**
 * 拖拽重置尺寸
 *
 * @author young
 */
@Data
public class DragResize {
    /**
     * 判定是否为调整窗口状态的范围与边界距离
     */
    public static final int RESIZE_WIDTH = 5;
    /**
     * 窗口最小宽度
     */
    public static final int MIN_WIDTH = 300;
    /**
     * 窗口最小高度
     */
    public static final int MIN_HEIGHT = 250;

    /**
     * 是否处于右边调整状态
     */
    private boolean right;
    /**
     * 是否处于右下角调整状态
     */
    private boolean bottomRight;
    /**
     * 是否处于下边调整状态
     */
    private boolean bottom;

    public void clear() {
        right = bottomRight = bottom = false;
    }

    public boolean isResize() {
        return right || bottomRight || bottom;
    }
}
