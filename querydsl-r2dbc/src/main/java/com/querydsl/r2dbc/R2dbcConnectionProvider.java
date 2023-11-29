package com.querydsl.r2dbc;

import io.r2dbc.spi.Connection;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface R2dbcConnectionProvider {
    Mono<Connection> getConnection();
}
