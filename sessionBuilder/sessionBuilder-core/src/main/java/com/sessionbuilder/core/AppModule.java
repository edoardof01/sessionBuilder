package com.sessionbuilder.core;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import jakarta.persistence.EntityManagerFactory;
import java.util.Map;

public class AppModule extends AbstractModule {

	private final String persistenceUnit;
	private final Map<String, String> dbProperties;

	public AppModule(String persistenceUnit, Map<String, String> dbProperties) {
		this.persistenceUnit = persistenceUnit;
		this.dbProperties = dbProperties;
	}

	@Override
	public void configure() {
		bind(StudySessionRepositoryInterface.class).to(StudySessionRepository.class).in(Singleton.class);
		bind(TopicRepositoryInterface.class).to(TopicRepository.class).in(Singleton.class);
		bind(TransactionManager.class).to(TransactionManagerImpl.class).in(Singleton.class);
		bind(TopicServiceInterface.class).to(TopicService.class).in(Singleton.class);
		bind(StudySessionInterface.class).to(StudySessionService.class).in(Singleton.class);
	}

	@Provides
	@Singleton
	EntityManagerFactory provideEntityManagerFactory() {
		return EmfFactory.createEntityManagerFactory(this.persistenceUnit, this.dbProperties);
	}
}
