package com.sessionBuilder.core;

public interface StudySessionRepositoryInterface {

	StudySession findById(long id);
	void save(StudySession session);
	void update(StudySession session);
	void delete(long id);
}
