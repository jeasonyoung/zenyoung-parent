package top.zenyoung.data.r2dbc.querydsl;

import com.google.common.collect.Maps;
import io.r2dbc.spi.Parameters;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.binding.BindMarkersFactory;

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
        for (Map.Entry<String, Object> entry : parameterNameToParameterValue.entrySet()) {
            spec = spec.bind(entry.getKey(), Parameters.in(entry.getValue()));
        }
        return spec;
    }

    private Map<String, Object> parameterNameToParameterValue(@Nonnull final List<Object> bindings) {
        var bindMarkers = bindMarkersFactory.create();
        var parameterNameToParameterValue = Maps.<String, Object>newLinkedHashMap();
        for (int i = 0; i < bindings.size(); i++) {
            var marker = bindMarkers.next(String.valueOf(i));
            parameterNameToParameterValue.put(marker.getPlaceholder(), bindings.get(i));
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
