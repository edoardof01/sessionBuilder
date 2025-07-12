package com.sessionbuilder.core.it;

import com.google.inject.AbstractModule;
import com.sessionbuilder.core.backend.TopicViewCallback;

public class TestTopicViewCallbackModule extends AbstractModule {
	private final TopicViewCallback mockTopicViewCallback;

	public TestTopicViewCallbackModule(TopicViewCallback mockTopicViewCallback) {
		this.mockTopicViewCallback = mockTopicViewCallback;
	}

	@Override
	protected void configure() {
		bind(TopicViewCallback.class).toInstance(mockTopicViewCallback);
	}
}
