package com.sessionbuilder.core.backend;

public interface TopicViewCallback {
	void onTopicAdded(Topic topic);
	void onTopicRemoved(Topic topic);
	void onTopicError(String message);
	void onTotalTimeCalculated(Integer totalTime);
	void onPercentageCalculated(Integer percentage);
}
