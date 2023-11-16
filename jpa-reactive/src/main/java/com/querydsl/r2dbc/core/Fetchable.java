package com.querydsl.r2dbc.core;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface Fetchable<T> {
    Flux<T> fetch();

    Mono<T> fetchFirst();

    Mono<T> fetchOne();
}
