package top.zenyoung.data.r2dbc.querydsl;

import com.querydsl.core.types.Expression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 参数和表达式Pair
 *
 * @author young
 */
@Getter
@RequiredArgsConstructor(staticName = "of")
public class ParameterAndExpressionPair {
    /**
     * 参数类型
     */
    private final Class<?> parameterType;
    /**
     * 表达式
     */
    private final Expression<?> expression;
}
