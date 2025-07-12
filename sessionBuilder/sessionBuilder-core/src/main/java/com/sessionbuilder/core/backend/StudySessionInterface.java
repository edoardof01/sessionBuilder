package com.sessionbuilder.core.backend;

import java.time.LocalDate;
import java.util.List;

public interface StudySessionInterface {
	StudySession getSessionById(long id);
	List<StudySession> getAllSessions();
	StudySession createSession(LocalDate date, int duration, String note, List<Long> topicIds);
	StudySession completeSession(long sessionId);
	void addTopic(long sessionId, long topicId);
	void removeTopic(long sessionId, long topicId);
	void deleteSession(long sessionId);
}
