package top.zenyoung.segment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 分段ID定义
 *
 * @author young
 */
@Getter
@RequiredArgsConstructor
public class SegmentIdDefinition {
    private final String namespace;
    private final int safeDistance;
    private final long offset;
    private final long step;
}
