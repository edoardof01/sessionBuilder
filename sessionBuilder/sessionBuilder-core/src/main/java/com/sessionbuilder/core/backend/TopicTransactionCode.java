package com.sessionbuilder.core.backend;

import java.util.function.Function;

@FunctionalInterface
public interface TopicTransactionCode<T> extends Function<TopicRepositoryInterface,T>{
}