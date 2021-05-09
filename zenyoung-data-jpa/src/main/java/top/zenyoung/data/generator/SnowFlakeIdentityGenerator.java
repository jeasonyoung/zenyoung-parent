package top.zenyoung.data.generator;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import top.zenyoung.common.sequence.Sequence;
import top.zenyoung.common.sequence.SnowFlake;

import java.io.Serializable;

/**
 * 雪花数主键生成策略
 *
 * @author young
 */
public class SnowFlakeIdentityGenerator implements IdentifierGenerator {
    private static final Sequence<Long> GENERATOR;

    static {
        final int max = 31;
        final int cpus = Runtime.getRuntime().availableProcessors();
        GENERATOR = SnowFlake.getInstance((int) (cpus * (Math.random() + 1)) & max, cpus & max);
    }

    @Override
    public Serializable generate(final SharedSessionContractImplementor s, final Object obj) {
        final Serializable id = s.getEntityPersister(null, obj).getClassMetadata().getIdentifier(obj, s);
        return id != null ? id : GENERATOR.nextId();
    }
}
