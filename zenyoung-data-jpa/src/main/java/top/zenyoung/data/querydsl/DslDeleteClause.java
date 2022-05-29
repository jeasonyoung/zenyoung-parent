package top.zenyoung.data.querydsl;

import com.google.common.collect.Lists;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPADeleteClause;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * DSL删除处理
 *
 * @author young
 */
@RequiredArgsConstructor(staticName = "of")
public class DslDeleteClause {
    private final List<Predicate> predicates = Lists.newLinkedList();
    private final JPADeleteClause clause;

    /**
     * 添加删除条件
     *
     * @param condition 是否存在
     * @param where     删除条件
     * @return 链式对象
     */
    public DslDeleteClause add(final boolean condition, final Predicate where) {
        if (condition && Objects.nonNull(where)) {
            predicates.add(where);
        }
        return this;
    }

    /**
     * 添加删除条件
     *
     * @param where 删除条件
     * @return 链式对象
     */
    public DslDeleteClause add(final Predicate where) {
        return add(true, where);
    }

    /**
     * 添加删除条件
     *
     * @param condition 是否存在
     * @param handler   删除条件处理
     * @return 链式对象
     */
    public DslDeleteClause addFn(final boolean condition, final Supplier<Predicate> handler) {
        if (condition && Objects.nonNull(handler)) {
            return add(handler.get());
        }
        return this;
    }

    /**
     * 添加删除条件
     *
     * @param handler 删除条件处理
     * @return 链式对象
     */
    public DslDeleteClause addFn(final Supplier<Predicate> handler) {
        return addFn(true, handler);
    }

    /**
     * 执行删除处理
     *
     * @return 删除结果
     */
    public boolean execute() {
        if (Objects.nonNull(this.clause) && !CollectionUtils.isEmpty(this.predicates)) {
            final Predicate[] wheres = this.predicates.stream()
                    .filter(Objects::nonNull)
                    .toArray(Predicate[]::new);
            if (wheres.length > 0) {
                return clause.where(wheres).execute() > 0;
            }
        }
        return false;
    }
}
