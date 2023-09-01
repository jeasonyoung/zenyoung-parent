package top.zenyoung.jfx.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 视图尺寸大小
 *
 * @author young
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViewSize {
    /**
     * 宽度
     */
    private Double width;
    /**
     * 高度
     */
    private Double height;
}
