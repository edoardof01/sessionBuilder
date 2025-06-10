package com.sessionBuilder.core;


public interface RepositoryContext {
	TopicRepositoryInterface getTopicRepository();
	StudySessionRepositoryInterface getSessionRepository();
}
