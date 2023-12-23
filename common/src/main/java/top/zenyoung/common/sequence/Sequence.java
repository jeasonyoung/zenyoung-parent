package top.zenyoung.common.sequence;

/**
 * 序号接口
 *
 * @author young
 */
public interface Sequence {
    /**
     * 生成序号数据
     *
     * @return 序号数据
     */
    long nextId();
}
