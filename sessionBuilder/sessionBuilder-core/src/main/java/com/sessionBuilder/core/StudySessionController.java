package com.sessionBuilder.core;

import java.time.LocalDate;
import java.util.ArrayList;


import com.google.inject.Inject;

public class StudySessionController {
	
	@Inject
	private StudySessionInterface service;
	
	@Inject
	private SessionViewCallback viewCallBack;
	

	public StudySession handleCreateSession(LocalDate date, int duration, String note, ArrayList<Topic> topics) {
		try {
			StudySession session = service.createSession(date, duration, note, topics);
			if (viewCallBack != null) {
				viewCallBack.onSessionAdded(session);
			}
			return session;
		} catch(Exception e) {
			if (viewCallBack != null) {
				viewCallBack.onSessionError("Errore: " + e.getMessage());
			}
			throw e;
		}	
	}

	public StudySession handleGetSession(long sessionId) {
		try {
			return service.getSessionById(sessionId);
		} catch(Exception e) {
			if (viewCallBack != null) {
				viewCallBack.onSessionError("Errore: " + e.getMessage());
			}
			throw e;
		}
		
	}

	public void handleAddTopic(long sessionId, long topicId) {
		try {
			service.addTopic(sessionId, topicId);
		}catch(Exception e) {
			if (viewCallBack != null) {
				viewCallBack.onSessionError("Errore: " + e.getMessage());
			}
		}
	}

	public void handleRemoveTopic(long sessionId, long topicId) {
		try {
			service.removeTopic(sessionId, topicId);
		}catch(Exception e) {
			if (viewCallBack != null) {
				viewCallBack.onSessionError("Errore: " + e.getMessage());
			}
		}
		
	}

	public void handleCompleteSession(long sessionId) {
		try {
			service.completeSession(sessionId);
		}catch (Exception e){
			if(viewCallBack != null) {
				viewCallBack.onSessionError("Errore: " + e.getMessage());
			}
		}
	}
		

	public void handleDeleteSession(long sessionId) {
		try {
			StudySession session = service.getSessionById(sessionId);
			service.deleteSession(session.getId());
			if(viewCallBack != null) {
				viewCallBack.onSessionRemoved(session);
			}
		} catch(Exception e) {
			if(viewCallBack != null) {
				viewCallBack.onSessionError("Errore: " + e.getMessage());
			}
		}
		
	}

	public void setViewCallBack(SessionViewCallback viewCallBack) {
		this.viewCallBack = viewCallBack;
		
	}

	public SessionViewCallback getViewCallback() {
		return viewCallBack;
	}
	
	

}
