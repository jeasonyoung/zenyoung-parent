package top.zenyoung.common.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 计算工具类
 *
 * @author young
 */
@UtilityClass
public class CalcUtils {

    public static <T, R> List<R> split(@Nullable final Collection<T> items, @Nonnull final Function<T, R> convert) {
        if (Objects.nonNull(items) && !items.isEmpty()) {
            return items.stream()
                    .map(convert)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    public static <T, R> Set<R> set(@Nullable final Collection<T> items, @Nonnull final Function<T, R> convert) {
        if (Objects.nonNull(items) && !items.isEmpty()) {
            return items.stream()
                    .map(convert)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
        return Sets.newHashSet();
    }

    public static <T, K, V> Map<K, V> map(@Nullable final Collection<T> items,
                                          @Nonnull final Function<T, K> keyConvert,
                                          @Nonnull final Function<T, V> valConvert) {
        if (Objects.nonNull(items) && !items.isEmpty()) {
            return items.stream()
                    .filter(t -> Objects.nonNull(keyConvert.apply(t)) && Objects.nonNull(valConvert.apply(t)))
                    .collect(Collectors.toMap(keyConvert, valConvert, (o, n) -> n));
        }
        return Maps.newHashMap();
    }

    public static <T, K, V> Map<K, List<V>> group(@Nullable final Collection<T> items,
                                                  @Nonnull final Function<T, K> keyConvert,
                                                  @Nonnull final Function<T, V> valConvert) {
        if (Objects.nonNull(items) && !items.isEmpty()) {
            return items.stream()
                    .filter(t -> Objects.nonNull(keyConvert.apply(t)) && Objects.nonNull(valConvert.apply(t)))
                    .collect(Collectors.groupingBy(keyConvert, Collectors.mapping(valConvert, Collectors.toList())));
        }
        return Maps.newHashMap();
    }

    public static <K, V> void assign(@Nonnull final Supplier<K> keyHandler,
                                     @Nonnull final Map<K, V> valMap,
                                     @Nonnull final Consumer<V> assignHandler) {
        Optional.ofNullable(keyHandler.get())
                .filter(key -> {
                    if (key instanceof String) {
                        return !Strings.isNullOrEmpty((String) key);
                    }
                    return true;
                })
                .map(key -> valMap.getOrDefault(key, null))
                .ifPresent(assignHandler);
    }

    public static <R> CompletableFuture<R> async(@Nonnull final Supplier<R> valHandler) {
        return CompletableFuture.supplyAsync(valHandler);
    }

    public static <R> CompletableFuture<Void> async(@Nonnull final Supplier<R> valHandler, @Nonnull final Consumer<R> assignHandler) {
        return async(valHandler).thenAccept(assignHandler);
    }

    public static void syncJoin(@Nonnull final List<CompletableFuture<?>> futures) {
        //检查是否存在
        if (futures.isEmpty()) {
            return;
        }
        //同步等待
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
}
