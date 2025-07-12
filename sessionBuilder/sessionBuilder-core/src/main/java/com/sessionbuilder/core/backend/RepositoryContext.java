package com.sessionbuilder.core.backend;


public interface RepositoryContext {
	TopicRepositoryInterface getTopicRepository();
	StudySessionRepositoryInterface getSessionRepository();
}
