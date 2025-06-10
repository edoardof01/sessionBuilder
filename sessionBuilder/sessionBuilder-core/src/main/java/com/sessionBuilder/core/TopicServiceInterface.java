package com.sessionBuilder.core;

import java.util.ArrayList;

public interface TopicServiceInterface {
	Topic createTopic(String name, String description, int difficulty, ArrayList<StudySession> sessionList);
	Topic getTopicById(long id);
	void addSessionToTopic(long topicId, long sessionId);
	void deleteTopic(long topicId);
	void removeSessionFromTopic(long topicId, long sessionId);
	int calculateTotalTime(long topicId);
	int calculatePercentageOfCompletion(long topicId);
}
