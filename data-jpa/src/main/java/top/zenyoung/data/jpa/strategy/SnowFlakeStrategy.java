package top.zenyoung.data.jpa.strategy;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import top.zenyoung.common.sequence.IdSequence;
import top.zenyoung.common.sequence.IdSequenceProperties;
import top.zenyoung.common.sequence.SnowFlake;
import top.zenyoung.data.jpa.util.SpringContextUtils;

import java.io.Serializable;
import java.util.Optional;
import java.util.Properties;

/**
 * 雪花数主键生成策略
 *
 * @author young
 */
public class SnowFlakeStrategy implements IdentifierGenerator {
    private IdSequence idSequence;

    @Override
    public void configure(final Type type, final Properties params, final ServiceRegistry serviceRegistry) throws MappingException {
        final IdSequenceProperties properties = SpringContextUtils.getBean(IdSequenceProperties.class);
        this.idSequence = SnowFlake.create(properties);
    }

    @Override
    public Serializable generate(final SharedSessionContractImplementor s, final Object obj) throws HibernateException {
        final Serializable id = s.getEntityPersister(null, obj).getClassMetadata().getIdentifier(obj, s);
        return Optional.ofNullable(id)
                .orElseGet(() -> idSequence.nextId());
    }
}
