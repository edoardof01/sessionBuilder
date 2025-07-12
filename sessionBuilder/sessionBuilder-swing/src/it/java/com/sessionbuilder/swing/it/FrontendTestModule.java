package com.sessionbuilder.swing.it;

import com.google.inject.AbstractModule;
import com.sessionbuilder.core.backend.SessionViewCallback;
import com.sessionbuilder.core.backend.TopicViewCallback;
import com.sessionbuilder.swing.TopicAndSessionManager;

public class FrontendTestModule extends AbstractModule {

	private final TopicAndSessionManager managerView;

	public FrontendTestModule(TopicAndSessionManager managerView) {
		this.managerView = managerView;
	}

	@Override
	protected void configure() {
		bind(TopicAndSessionManager.class).toInstance(managerView);
		bind(TopicViewCallback.class).toInstance(managerView);
		bind(SessionViewCallback.class).toInstance(managerView);
	}
}

