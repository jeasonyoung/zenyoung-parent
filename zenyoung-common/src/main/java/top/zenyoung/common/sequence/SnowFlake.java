package top.zenyoung.common.sequence;

import lombok.Getter;

/**
 * 雪花数算法工具类
 *
 * @author young
 */
public class SnowFlake implements Sequence<Long>, IdSequence {
    /**
     * 时间初始值 2^41 - 1
     */
    private static final long TOWEPOCH = 1465978576000L;
    /**
     * 5位机器ID长度
     */
    private static final int WORKER_ID_BITS = 5;
    /**
     * 5位机房ID长度
     */
    private static final int DATA_CENTER_ID_BITS = 5;
    /**
     * 12位每毫秒内产生的ID长度
     */
    private static final int SEQUENCE_BITS = 12;

    /**
     * 最大机器ID
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    /**
     * 最大机房ID
     */
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);
    /**
     * 最大序号
     */
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    private static final int WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final int DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final int TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

    private long lastTimeStamp = -1L;

    /**
     * 机器ID 2进制5位
     */
    @Getter
    private final long workerId;
    /**
     * 机房ID 2进制5位
     */
    @Getter
    private final long dataCenterId;
    /**
     * 代表1毫秒内生成多个id的最新序号 2进制12位, 4095个
     */
    private long sequence;

    /**
     * 获取实例
     *
     * @param workerId     机器ID
     * @param dataCenterId 机房ID
     * @param sequence     顺序号
     * @return 实例
     */
    public static SnowFlake getInstance(final int workerId, final int dataCenterId, final int sequence) {
        return new SnowFlake(workerId, dataCenterId, sequence);
    }

    /**
     * 获取实例
     *
     * @param workerId     机器ID
     * @param dataCenterId 机房ID
     * @return 实例
     */
    public static SnowFlake getInstance(final int workerId, final int dataCenterId) {
        return new SnowFlake(workerId, dataCenterId, 0);
    }

    /**
     * 获取实例
     *
     * @return 实例
     */
    public static SnowFlake getInstance() {
        return new SnowFlake(
                (int) ((long) ((Math.random() + 1) * System.currentTimeMillis()) & MAX_WORKER_ID),
                (int) (((long) (Math.random() + System.currentTimeMillis())) & MAX_DATA_CENTER_ID),
                0
        );
    }

    /**
     * 构造函数
     *
     * @param workerId     机器ID
     * @param dataCenterId 机房ID
     * @param sequence     顺序号
     */
    private SnowFlake(final long workerId, final long dataCenterId, final long sequence) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", MAX_WORKER_ID));
        }
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException(String.format("dataCenterId Id can't be greater than %d or less than 0", MAX_DATA_CENTER_ID));
        }
        if (sequence > MAX_SEQUENCE || sequence < 0) {
            throw new IllegalArgumentException(String.format("sequence Id can't be greater than %d or less than 0", MAX_SEQUENCE));
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
        this.sequence = sequence;
    }

    /**
     * 生成雪花ID
     *
     * @return 雪花ID
     */
    @Override
    public synchronized Long nextId() {
        //当前时间戳
        long timestamp = timeGen();
        if (timestamp < lastTimeStamp) {
            throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds.", (lastTimeStamp - timestamp)));
        }
        //同一毫秒内，序号递增
        if (lastTimeStamp == timestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = nextMills(lastTimeStamp);
            }
        } else {
            sequence = 0;
        }
        lastTimeStamp = timestamp;
        //合成雪花数
        return ((timestamp - TOWEPOCH) << TIMESTAMP_LEFT_SHIFT) | (dataCenterId << DATA_CENTER_ID_SHIFT) | (workerId << WORKER_ID_SHIFT) | sequence;
    }

    /**
     * 当某一毫秒的时间,产生的Id数，超过上限(4095),系统会进入等待，直到下一毫秒,系统继续产生ID
     *
     * @param lastTimeStamp 比较时间戳
     * @return 新时间戳
     */
    private long nextMills(final long lastTimeStamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimeStamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }
}
