package com.sessionBuilder.core;

import java.time.LocalDate;
import java.util.ArrayList;

import com.google.inject.Inject;

public class StudySessionController {
	
	@Inject
	private StudySessionService service;

	public StudySession handleCreateSession(LocalDate date, int duration, String note, ArrayList<Topic> topics) {
		return service.createSession(date, duration, note, topics);
	}

	public StudySession handleGetSession(long sessionId) {
		return service.getSessionById(sessionId);
	}

	public void handleAddTopic(long sessionId, long topicId) {
		service.addTopic(sessionId, topicId);
	}

	public void handleRemoveTopic(long sessionId, long topicId) {
		service.removeTopic(sessionId, topicId);
	}

	public void handleCompleteSession(long sessionId) {
		service.completeSession(sessionId);
	}

	public void handleDeleteSession(long sessionId) {
		service.deleteSession(sessionId);
		
	}
	
	

}
