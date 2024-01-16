package top.zenyoung.segment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * 分段ID定义
 *
 * @author young
 */
@Getter
@RequiredArgsConstructor(staticName = "of")
public class SegmentIdDefinition implements Serializable {
    /**
     * 命名空间
     */
    private final String namespace;
    /**
     * 安全距离
     */
    private final int safeDistance;
    /**
     * 偏移量
     */
    private final long offset;
    /**
     * 步长
     */
    private final long step;
}
