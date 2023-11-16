package com.querydsl.r2dbc.core.dml;

import reactor.core.publisher.Mono;

public interface DMLClause<C extends DMLClause<C>> {
    Mono<Long> execute();
}
