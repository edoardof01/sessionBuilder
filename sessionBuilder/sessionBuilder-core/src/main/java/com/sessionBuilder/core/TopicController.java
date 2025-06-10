package com.sessionBuilder.core;

import java.util.ArrayList;


import com.google.inject.Inject;

public class TopicController {
	
	@Inject
	private TopicServiceInterface service;
	
	@Inject
	private TopicViewCallback viewCallback;
	
	public void setViewCallback(TopicViewCallback callback) {
		this.viewCallback = callback;
	}

	public Topic handleGetTopicById(long topicId) {
		try {
			return service.getTopicById(topicId);
		} catch(Exception e) {
			if(viewCallback != null) {
				viewCallback.onTopicError("Topic non trovato: " + e.getMessage());
			}
			throw e;
		}
	}

	public Topic handleCreateTopic(String name, String description, int difficulty, ArrayList<StudySession> sessionList) {
		try {
			Topic topic = service.createTopic(name, description, difficulty, sessionList);
			if (viewCallback != null) {
				viewCallback.onTopicAdded(topic);
			}
			return topic;
		} catch(Exception e) {
			if(viewCallback != null) {
				viewCallback.onTopicError("Topic non trovato: " + e.getMessage());
			}
			throw e;
		}
		
	}
	
	public void handleDeleteTopic(long topicId) {
		try {
			Topic topic = service.getTopicById(topicId);
			service.deleteTopic(topicId);
			if (viewCallback != null) {
				viewCallback.onTopicRemoved(topic);
			}
		} catch(Exception e) {
			if(viewCallback != null) {
				viewCallback.onTopicError("Topic non trovato: " + e.getMessage());
			}
		}
		
	}

	public void handleAddSessionToTopic(long topicId, long sessionId) {
		try {
			service.addSessionToTopic(topicId, sessionId);
		} catch(Exception e) {
			if(viewCallback != null) {
				viewCallback.onTopicError("Topic non trovato: " + e.getMessage());
			}
		}
	}
	
	public void handleRemoveSessionFromTopic(long topicId, long sessionId) {
		try {
			service.removeSessionFromTopic(topicId, sessionId);
		} catch(Exception e) {
			if (viewCallback != null) {
				viewCallback.onTopicError("Errore nella rimozione della sessione: " + e.getMessage());
			}
		}
	}
	
	public int handleTotalTime(long topicId) {
		try {
			Integer totalTime = service.calculateTotalTime(topicId);
			if (viewCallback != null) {
				viewCallback.onTotalTimeCalculated(totalTime);
			}
			return totalTime;
		} catch(Exception e) {
			if(viewCallback != null) {
				viewCallback.onTopicError("Topic non trovato: " + e.getMessage());
			}
			throw e;
		}
	}
	
	public int handlePercentageOfCompletion(long topicId) {
		try {
			Integer percentage = service.calculatePercentageOfCompletion(topicId);
			if (viewCallback != null) {
				viewCallback.onPercentageCalculated(percentage);
			}
			return percentage;
		} catch(Exception e) {
			if(viewCallback != null) {
				viewCallback.onTopicError("Topic non trovato: " + e.getMessage());
			}
			throw e;
		}
	}

	public TopicViewCallback getViewCallback() {
		return viewCallback;
	}

}
