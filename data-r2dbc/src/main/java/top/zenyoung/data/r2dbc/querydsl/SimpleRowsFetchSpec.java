package top.zenyoung.data.r2dbc.querydsl;

import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

@RequiredArgsConstructor(staticName = "of")
public class SimpleRowsFetchSpec<T> implements RowsFetchSpec<T> {
    private final RowsFetchSpec<T> rowsFetchSpec;

    @Nonnull
    @Override
    public Mono<T> one() {
        return rowsFetchSpec.one();
    }

    @Nonnull
    @Override
    public Mono<T> first() {
        return rowsFetchSpec.first();
    }

    @Nonnull
    @Override
    public Flux<T> all() {
        return rowsFetchSpec.all();
    }
}
