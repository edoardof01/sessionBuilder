package com.sessionBuilder.core;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;


public class AppModule extends AbstractModule {
	
	@Override
	public void configure() {
		bind(StudySessionRepositoryInterface.class).to(StudySessionRepository.class).in(Singleton.class);
		bind(TopicRepositoryInterface.class).to(TopicRepository.class).in(Singleton.class);
		bind(TransactionManager.class).to(TransactionManagerImpl.class).in(Singleton.class);
	}
	
	@Provides
	@Singleton
	EntityManagerFactory provideEntityManagerFactory() {
		return Persistence.createEntityManagerFactory("sessionbuilder-test");
	}
}

