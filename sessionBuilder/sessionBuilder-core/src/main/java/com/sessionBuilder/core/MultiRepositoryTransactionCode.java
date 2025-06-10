package com.sessionBuilder.core;

import java.util.function.Function;

@FunctionalInterface
public interface MultiRepositoryTransactionCode<T> extends Function<RepositoryContext, T> {
}