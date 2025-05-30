package com.sessionBuilder.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;



public class AppModuleTest {

    private Injector injector;

    @Before
    public void setUp() {
        AppModule appModule = new AppModule();
        injector = Guice.createInjector(appModule);
    }

    @Test
    public void testStudySessionRepositoryBinding() {
        StudySessionRepositoryInterface sessionRepoInstance1 = injector.getInstance(StudySessionRepositoryInterface.class);
        assertNotNull(sessionRepoInstance1);
        assertTrue(sessionRepoInstance1 instanceof StudySessionRepository);
        StudySessionRepositoryInterface sessionRepoInstance2 = injector.getInstance(StudySessionRepositoryInterface.class);
        assertSame(sessionRepoInstance1, sessionRepoInstance2);
    }

    @Test
    public void testTopicRepositoryBinding() {
        TopicRepositoryInterface topicRepoInstance1 = injector.getInstance(TopicRepositoryInterface.class);
        assertNotNull(topicRepoInstance1);
        assertTrue(topicRepoInstance1 instanceof TopicRepository);
        TopicRepositoryInterface topicRepoInstance2 = injector.getInstance(TopicRepositoryInterface.class);
        assertSame(topicRepoInstance1, topicRepoInstance2);
    }

    @Test
    public void appModuleCanBeInstantiated() {
        AppModule module = new AppModule();
        assertNotNull(module);
    }
}
