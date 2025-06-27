package com.sessionbuilder.core;

import java.util.function.Function;

@FunctionalInterface
public interface MultiRepositoryTransactionCode<T> extends Function<RepositoryContext, T> {
}