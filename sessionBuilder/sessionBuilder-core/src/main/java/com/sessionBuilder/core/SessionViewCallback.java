package com.sessionBuilder.core;

public interface SessionViewCallback {
	void onSessionAdded(StudySession session);
	void onSessionRemoved(StudySession session);
	void onSessionError(String message);
	void onSessionUpdated(StudySession session);
}
