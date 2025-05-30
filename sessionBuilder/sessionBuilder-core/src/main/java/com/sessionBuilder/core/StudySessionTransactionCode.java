package com.sessionBuilder.core;

import java.util.function.Function;

@FunctionalInterface
public interface StudySessionTransactionCode<T> extends Function<StudySessionRepositoryInterface,T>{

}
