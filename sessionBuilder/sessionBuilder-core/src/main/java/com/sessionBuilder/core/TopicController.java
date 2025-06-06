package com.sessionBuilder.core;

import java.util.ArrayList;

import com.google.inject.Inject;

public class TopicController {
	
	@Inject
	TopicService service;

	public Topic handleGetTopicById(long topicId) {
		return service.getTopicById(topicId);
	}

	public Topic handleCreateTopic(String name, String description, int difficulty, ArrayList<StudySession> sessionList) {
		return service.createTopic(name, description, difficulty, sessionList);
	}
	
	public void handleDeleteTopic(long topicId) {
		service.deleteTopic(topicId);
	}

	public void handleAddSessionToTopic(long topicId, long sessionId) {
		service.addSessionToTopic(topicId, sessionId);
	}
	
	public void handleTotalTime(long topicId) {
		service.calculateTotalTime(topicId);
	}
	
	public void handlePercentageOfCompletion(long topicId) {
		service.calculatePercentageOfCompletion(topicId);
	}

	
	
	
	
 
	
	
	
	

}
