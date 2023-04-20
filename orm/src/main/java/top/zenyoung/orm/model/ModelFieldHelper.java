package top.zenyoung.orm.model;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import top.zenyoung.orm.annotation.PoField;
import top.zenyoung.orm.enums.DbField;
import top.zenyoung.orm.enums.PoConstant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 实体字段注解工具类
 *
 * @param <PO> 实体主键类型
 * @author young
 */
@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class ModelFieldHelper<PO> {
    private static final Map<Class<?>, Object> LOCKS = Maps.newConcurrentMap();
    private final Map<PoConstant, Field> fieldMap = Maps.newHashMap();
    private final Map<PoConstant, String> colMap = Maps.newHashMap();
    private final AtomicBoolean refInited = new AtomicBoolean(false);
    private final Class<PO> poCls;

    /**
     * 实体类型初始化
     */
    public void init() {
        if (Objects.isNull(poCls) || refInited.get()) {
            return;
        }
        synchronized (LOCKS.computeIfAbsent(poCls, k -> new Object())) {
            try {
                //实体类字段
                final List<Field> fields = ReflectionKit.getFieldList(poCls);
                if (CollectionUtils.isEmpty(fields)) {
                    return;
                }
                //实体数据表字段
                final Map<String, ColumnCache> columnCacheMap = LambdaUtils.getColumnMap(poCls);
                fields.stream()
                        .filter(Objects::nonNull)
                        .forEach(field -> {
                            final String nameUpper = field.getName().toUpperCase(Locale.ENGLISH);
                            final ColumnCache colCache = columnCacheMap.getOrDefault(nameUpper, null);
                            final String colName = Objects.isNull(colCache) ? null : colCache.getColumn();
                            //默认字段处理
                            this.initDefaultFieldHandler(field, colName);
                            //注解处理
                            if (field.isAnnotationPresent(PoField.class) && !Strings.isNullOrEmpty(colName)) {
                                this.initPoFieldAnnoHandler(field, colName);
                            }
                        });
                //检测初始化数据结果
                if (!CollectionUtils.isEmpty(fieldMap) && !CollectionUtils.isEmpty(colMap)) {
                    refInited.set(true);
                }
            } finally {
                LOCKS.remove(poCls);
            }
        }
    }

    private void initDefaultFieldHandler(@Nonnull final Field field, @Nullable final String col) {
        PoConstant pc;
        //检测是否有逻辑删除注解
        if (field.isAnnotationPresent(TableLogic.class)) {
            pc = PoConstant.DeletedAt;
        } else {
            pc = PoConstant.byFieldName(field.getName());
            if (pc == PoConstant.DeletedAt) {
                return;
            }
        }
        if (Objects.nonNull(pc)) {
            fieldMap.put(pc, field);
            if (!Strings.isNullOrEmpty(col)) {
                colMap.put(pc, col);
            }
        }
    }

    private void initPoFieldAnnoHandler(@Nonnull final Field field, @Nonnull final String col) {
        final PoField pf = field.getAnnotation(PoField.class);
        if (Objects.nonNull(pf) && pf.fill() != DbField.Default && !Strings.isNullOrEmpty(col)) {
            final PoConstant pc = PoConstant.byPoField(pf);
            if (Objects.nonNull(pc)) {
                fieldMap.put(pc, field);
                colMap.put(pc, col);
            }
        }
    }

    /**
     * 获取实体字段
     *
     * @param pc 实体字段枚举
     * @return 实体字段
     */
    public final Field getField(@Nonnull final PoConstant pc) {
        return fieldMap.computeIfAbsent(pc, p -> {
            init();
            return fieldMap.getOrDefault(p, null);
        });
    }

    /**
     * 获取表字段名
     *
     * @param pc 实体字段枚举
     * @return 表字段名
     */
    public final String getColumn(@Nonnull final PoConstant pc) {
        return colMap.computeIfAbsent(pc, p -> {
            init();
            return colMap.getOrDefault(p, null);
        });
    }

    /**
     * 获取表字段集合
     *
     * @return 字段集合
     */
    public List<String> getAllCols() {
        return Arrays.stream(PoConstant.values())
                .filter(Objects::nonNull)
                .map(this::getColumn)
                .filter(col -> !Strings.isNullOrEmpty(col))
                .distinct()
                .collect(Collectors.toList());
    }
}
