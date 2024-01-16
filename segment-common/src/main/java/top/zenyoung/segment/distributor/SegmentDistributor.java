package top.zenyoung.segment.distributor;

/**
 * 分布式分段接口
 */
public interface SegmentDistributor {
    int DEFAULT_SEGMENTS = 1;

    /**
     * 获取命名空间
     *
     * @return 命名空间
     */
    String getNamespace();

    /**
     * 获取步长
     *
     * @return 步长
     */
    long getStep();

    /**
     * 获取步长
     *
     * @param segments 分段集合
     * @return 步长值
     */
    default long getStep(final int segments) {
        return Math.multiplyExact(getStep(), Math.max(0, segments));
    }
}
