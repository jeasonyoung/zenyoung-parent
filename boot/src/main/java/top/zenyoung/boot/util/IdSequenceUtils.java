package top.zenyoung.boot.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import top.zenyoung.boot.config.IdSequenceProperties;
import top.zenyoung.common.sequence.IdSequence;
import top.zenyoung.common.sequence.SnowFlake;
import top.zenyoung.common.util.RandomUtils;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * IdSequence工具类
 *
 * @author young
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IdSequenceUtils {
    private static final int CPUS;

    static {
        CPUS = Math.max(Runtime.getRuntime().availableProcessors(), 1);
    }

    public static IdSequence create(@Nullable final IdSequenceProperties properties) {
        final int max = 10;
        final int workerId = (Objects.isNull(properties) || Objects.isNull(properties.getWorkerId()) || properties.getWorkerId() < 0) ?
                (CPUS & max) : Math.min((int) SnowFlake.MAX_WORKER_ID, properties.getWorkerId());
        final int dataCenterId = (Objects.isNull(properties) || Objects.isNull(properties.getDataCenterId()) || properties.getDataCenterId() < 0) ?
                ((CPUS * 2) & max) : Math.min((int) SnowFlake.MAX_DATA_CENTER_ID, properties.getDataCenterId());
        final int sequence = (Objects.isNull(properties) || Objects.isNull(properties.getSequence()) || properties.getSequence() < 0) ?
                RandomUtils.randomInt(0, (int) SnowFlake.MAX_SEQUENCE) : Math.min((int) SnowFlake.MAX_SEQUENCE, properties.getSequence());
        return SnowFlake.getInstance(workerId, dataCenterId, sequence);
    }
}
