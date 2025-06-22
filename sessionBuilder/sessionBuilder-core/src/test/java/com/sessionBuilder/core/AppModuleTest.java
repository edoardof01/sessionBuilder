package com.sessionBuilder.core;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import jakarta.persistence.EntityManagerFactory;

@RunWith(MockitoJUnitRunner.class)
public class AppModuleTest {
   
   @Mock
   private EntityManagerFactory emf;
   
   private Injector injector;
   private String originalEnvironment;
   
   @Before
   public void setUp() {
   	originalEnvironment = System.getProperty("app.environment");
   	AbstractModule testModule = new AbstractModule() {
   		@Override
   		protected void configure() {
   			bind(TopicRepositoryInterface.class).to(TopicRepository.class).in(Singleton.class);
   			bind(StudySessionRepositoryInterface.class).to(StudySessionRepository.class).in(Singleton.class);
   			bind(TransactionManager.class).to(TransactionManagerImpl.class).in(Singleton.class);
   			bind(EntityManagerFactory.class).toInstance(emf);
   		}
   	};
   	injector = Guice.createInjector(testModule);
   }
   
   @After
   public void tearDown() {
   	if (originalEnvironment != null) {
   		System.setProperty("app.environment", originalEnvironment);
   	} else {
   		System.clearProperty("app.environment");
   	}
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
   
   @Test
   public void testDeterminePersistenceUnitDefaultsToProduction() {
   	System.clearProperty("app.environment");
   	AppModule module = new AppModule();
   	String persistenceUnit = module.determinePersistenceUnit();
   	assertEquals("sessionbuilder-prod", persistenceUnit);
   }
   
   @Test
   public void testDeterminePersistenceUnitWithTestEnvironment() {
   	System.setProperty("app.environment", "test");
   	AppModule module = new AppModule();
   	String persistenceUnit = module.determinePersistenceUnit();
   	assertEquals("sessionbuilder-test", persistenceUnit);
   }
}