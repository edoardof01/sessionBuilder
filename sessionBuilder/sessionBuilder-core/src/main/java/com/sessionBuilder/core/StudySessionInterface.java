package com.sessionBuilder.core;

import java.time.LocalDate;
import java.util.ArrayList;

public interface StudySessionInterface {
	StudySession getSessionById(long id);
	StudySession createSession(LocalDate date, int duration, String note, ArrayList<Topic> topicList);
	void completeSession(long sessionId);
	void addTopic(long sessionId, long topicId);
	void removeTopic(long sessionId, long topicId);
	void deleteSession(long sessionId);
}
