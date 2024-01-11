package top.zenyoung.data.r2dbc.querydsl;

import com.google.common.collect.Maps;
import io.r2dbc.spi.Parameters;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.binding.BindMarkersFactory;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.model.EnumValue;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * Querydsl桥接器
 *
 * @author young
 */
@RequiredArgsConstructor(staticName = "of")
public class QuerydslParameterBinder {
    private final BindMarkersFactory bindMarkersFactory;

    public DatabaseClient.GenericExecuteSpec bind(@Nonnull final DatabaseClient client, @Nonnull final List<Object> bindings, @Nonnull final String sql) {
        var parameterNameToParameterValue = parameterNameToParameterValue(bindings);
        var sqlWithParameterNames = getSqlWithParameterNames(parameterNameToParameterValue, sql);
        var spec = client.sql(sqlWithParameterNames);
        if (!CollectionUtils.isEmpty(parameterNameToParameterValue)) {
            for (Map.Entry<String, Object> entry : parameterNameToParameterValue.entrySet()) {
                spec = spec.bind(entry.getKey(), Parameters.in(entry.getValue()));
            }
        }
        return spec;
    }

    private Map<String, Object> parameterNameToParameterValue(@Nonnull final List<Object> bindings) {
        final var parameterNameToParameterValue = Maps.<String, Object>newLinkedHashMap();
        if (!CollectionUtils.isEmpty(bindings)) {
            final var bindMarkers = bindMarkersFactory.create();
            bindings.forEach(param -> {
                final var marker = bindMarkers.next();
                Object val = param;
                if (param instanceof EnumValue p) {
                    val = p.getVal();
                }
                parameterNameToParameterValue.put(marker.getPlaceholder(), val);
            });
        }
        return parameterNameToParameterValue;
    }

    private String getSqlWithParameterNames(@Nonnull final Map<String, Object> parameterNameToParameterValue, @Nonnull final String sql) {
        var sqlWithParameterNames = sql;
        for (String parameterName : parameterNameToParameterValue.keySet()) {
            String paramName = escape(parameterName);
            sqlWithParameterNames = sqlWithParameterNames.replaceFirst("\\?", paramName);
        }
        return sqlWithParameterNames;
    }

    private String escape(@Nonnull final String parameterName) {
        if (parameterName.startsWith("$")) {
            return parameterName.replace("$", "\\$");
        }
        return parameterName;
    }
}
