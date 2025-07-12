package com.sessionbuilder.core.backend;

import java.util.function.Function;

import jakarta.persistence.EntityManager;

@FunctionalInterface
public interface TransactionCode<T> extends Function<EntityManager, T> {
}

