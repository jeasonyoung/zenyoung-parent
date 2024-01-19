package top.zenyoung.data.r2dbc.querydsl;

import com.google.common.collect.Maps;
import io.r2dbc.spi.Parameter;
import io.r2dbc.spi.Parameters;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.binding.BindMarkersFactory;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.model.EnumValue;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Querydsl桥接器
 *
 * @author young
 */
@RequiredArgsConstructor(staticName = "of")
public class QuerydslParameterBinder {
    private static final String PLACEHOLDER = "?";
    private final BindMarkersFactory bindMarkersFactory;

    public DatabaseClient.GenericExecuteSpec bind(@Nonnull final DatabaseClient client, @Nonnull final List<Object> bindings, @Nonnull final String sql) {
        var paramVals = buildParamValues(bindings);
        var sqlWithParams = buildSqlWithParamNames(paramVals, sql);
        var spec = client.sql(sqlWithParams);
        if (!CollectionUtils.isEmpty(paramVals)) {
            final AtomicInteger refIdx = new AtomicInteger(0);
            for (Map.Entry<String, Object> entry : paramVals.entrySet()) {
                final String name = entry.getKey();
                final Parameter inParam = Parameters.in(entry.getValue());
                if (name.startsWith(PLACEHOLDER)) {
                    spec = spec.bind(refIdx.incrementAndGet() + "", inParam);
                } else {
                    spec = spec.bind(entry.getKey(), inParam);
                }
            }
        }
        return spec;
    }

    private Map<String, Object> buildParamValues(@Nonnull final List<Object> bindings) {
        final var parameterNameToParameterValue = Maps.<String, Object>newLinkedHashMap();
        if (!CollectionUtils.isEmpty(bindings)) {
            final var bindMarkers = bindMarkersFactory.create();
            for (int i = 0; i < bindings.size(); i++) {
                final var marker = bindMarkers.next();
                Object val = bindings.get(i);
                if (val instanceof EnumValue ev) {
                    val = ev.getVal();
                }
                parameterNameToParameterValue.put(marker.getPlaceholder() + i, val);
            }
        }
        return parameterNameToParameterValue;
    }

    private String buildSqlWithParamNames(@Nonnull final Map<String, Object> paramVals, @Nonnull final String sql) {
        var sqlWithParames = sql;
        final AtomicInteger refIdx = new AtomicInteger(0);
        for (String param : paramVals.keySet()) {
            if (param.startsWith(PLACEHOLDER)) {
                sqlWithParames = sqlWithParames.replaceFirst("\\?", ":{" + refIdx.incrementAndGet() + "}");
            }
        }
        return sqlWithParames;
    }
}
