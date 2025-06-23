package com.sessionBuilder.core;


import java.util.List;

public interface TopicServiceInterface {
	Topic createTopic(String name, String description, int difficulty, List<StudySession> sessionList);
	Topic getTopicById(long id);
	List<Topic> getAllTopics();
	void addSessionToTopic(long topicId, long sessionId);
	void deleteTopic(long topicId);
	void removeSessionFromTopic(long topicId, long sessionId);
	int calculateTotalTime(long topicId);
	int calculatePercentageOfCompletion(long topicId);
}
