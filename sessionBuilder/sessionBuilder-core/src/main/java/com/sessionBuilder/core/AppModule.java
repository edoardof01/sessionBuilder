package com.sessionBuilder.core;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;


public class AppModule extends AbstractModule {
	
	@Override
	public void configure() {
		bind(StudySessionRepositoryInterface.class).to(StudySessionRepository.class).in(Scopes.SINGLETON);;
		bind(TopicRepositoryInterface.class).to(TopicRepository.class).in(Scopes.SINGLETON);;
	}
}

