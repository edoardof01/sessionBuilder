package com.sessionbuilder.core;

import java.util.function.Function;

@FunctionalInterface
public interface TopicTransactionCode<T> extends Function<TopicRepositoryInterface,T>{
}