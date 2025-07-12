package com.sessionbuilder.core.utils;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import jakarta.persistence.EntityManagerFactory;
import java.util.Map;

public class TestEntityManagerFactoryModule extends AbstractModule {
	private final Map<String, String> jdbcProperties;
	private final String persistenceUnit;

	public TestEntityManagerFactoryModule(Map<String, String> jdbcProperties, String persistenceUnit) {
		this.jdbcProperties = jdbcProperties;
		this.persistenceUnit = persistenceUnit;
	}

	@Provides
	@Singleton
	EntityManagerFactory provideEntityManagerFactory() {
		return EmfFactory.createEntityManagerFactory(this.persistenceUnit, this.jdbcProperties);
	}
}
