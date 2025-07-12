package com.sessionbuilder.core.backend;

import java.time.LocalDate;
import java.util.List;

import com.google.inject.Inject;

public class StudySessionController {
	
	@Inject
	private StudySessionInterface service;
	
	@Inject
	private SessionViewCallback viewCallBack;
	
	private static final String STRING_ERROR = "Error: ";
	

	public StudySession handleCreateSession(LocalDate date, int duration, String note, List<Long> topicIds) {
		try {
			StudySession session = service.createSession(date, duration, note, topicIds);
			if (viewCallBack != null) {
				viewCallBack.onSessionAdded(session);
			}
			return session;
		} catch(Exception e) {
			if (viewCallBack != null) {
				viewCallBack.onSessionError(STRING_ERROR + e.getMessage());
			}
			throw e;
		}	
	}

	public StudySession handleGetSession(long sessionId) {
		try {
			return service.getSessionById(sessionId);
		} catch(Exception e) {
			if (viewCallBack != null) {
				viewCallBack.onSessionError(STRING_ERROR + e.getMessage());
			}
			throw e;
		}
		
	}
	
	public List<StudySession> handleGetAllSessions() {
		try {
			return service.getAllSessions();
		} catch(Exception e) {
			if(viewCallBack != null) {
				viewCallBack.onSessionError("Errore nel caricamento delle session");
			}
			throw e;
		}
	}
	

	public void handleAddTopic(long sessionId, long topicId) {
		try {
			service.addTopic(sessionId, topicId);
		}catch(Exception e) {
			if (viewCallBack != null) {
				viewCallBack.onSessionError(STRING_ERROR + e.getMessage());
			}
		}
	}

	public void handleRemoveTopic(long sessionId, long topicId) {
		try {
			service.removeTopic(sessionId, topicId);
		}catch(Exception e) {
			if (viewCallBack != null) {
				viewCallBack.onSessionError(STRING_ERROR + e.getMessage());
			}
		}
		
	}

	public void handleCompleteSession(long sessionId) {
		try {
			StudySession updatedSession = service.completeSession(sessionId);
			if(viewCallBack != null) {
				viewCallBack.onSessionUpdated(updatedSession);
			}
		}catch (Exception e){
			if(viewCallBack != null) {
				viewCallBack.onSessionError(STRING_ERROR + e.getMessage());
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
				viewCallBack.onSessionError(STRING_ERROR + e.getMessage());
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
