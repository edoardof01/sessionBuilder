package com.sessionbuilder.core.it;

import com.google.inject.AbstractModule;
import com.sessionbuilder.core.backend.SessionViewCallback;


public class TestSessionViewCallbackModule extends AbstractModule {
	private final SessionViewCallback mockSessionViewCallback;

	public TestSessionViewCallbackModule(SessionViewCallback mockSessionViewCallback) {
		this.mockSessionViewCallback = mockSessionViewCallback;
	}

	@Override
	protected void configure() {
		bind(SessionViewCallback.class).toInstance(mockSessionViewCallback);
	}
}

