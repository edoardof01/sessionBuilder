package com.sessionbuilder.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@RunWith(MockitoJUnitRunner.class)
public class AppModuleTest {

	@Mock
	private EntityManagerFactory mockEmf;

	@Test
	public void testTopicRepositoryInterfaceBindingIsSingleton() {
		Injector injector = Guice.createInjector(
			Modules.override(new AppModule("test-unit", Collections.emptyMap())).with(new AbstractModule() {
				@Override
				protected void configure() {
					bind(EntityManagerFactory.class).toInstance(mockEmf);
				}
			})
		);

		TopicRepositoryInterface instance1 = injector.getInstance(TopicRepositoryInterface.class);
		TopicRepositoryInterface instance2 = injector.getInstance(TopicRepositoryInterface.class);

		assertNotNull(instance1);
		assertSame("TopicRepositoryInterface deve essere singleton", instance1, instance2);
	}

	@Test
	public void testStudySessionRepositoryInterfaceBindingIsSingleton() {
		Injector injector = Guice.createInjector(
			Modules.override(new AppModule("test-unit", Collections.emptyMap())).with(new AbstractModule() {
				@Override
				protected void configure() {
					bind(EntityManagerFactory.class).toInstance(mockEmf);
				}
			})
		);

		StudySessionRepositoryInterface instance1 = injector.getInstance(StudySessionRepositoryInterface.class);
		StudySessionRepositoryInterface instance2 = injector.getInstance(StudySessionRepositoryInterface.class);

		assertNotNull(instance1);
		assertSame("StudySessionRepositoryInterface deve essere singleton", instance1, instance2);
	}

	@Test
	public void testTransactionManagerBindingIsSingleton() {
		Injector injector = Guice.createInjector(
			Modules.override(new AppModule("test-unit", Collections.emptyMap())).with(new AbstractModule() {
				@Override
				protected void configure() {
					bind(EntityManagerFactory.class).toInstance(mockEmf);
				}
			})
		);

		TransactionManager instance1 = injector.getInstance(TransactionManager.class);
		TransactionManager instance2 = injector.getInstance(TransactionManager.class);

		assertNotNull(instance1);
		assertSame("TransactionManager deve essere singleton", instance1, instance2);
	}

	@Test
	public void testTopicServiceInterfaceBindingIsSingleton() {
		Injector injector = Guice.createInjector(
			Modules.override(new AppModule("test-unit", Collections.emptyMap())).with(new AbstractModule() {
				@Override
				protected void configure() {
					bind(EntityManagerFactory.class).toInstance(mockEmf);
				}
			})
		);

		TopicServiceInterface instance1 = injector.getInstance(TopicServiceInterface.class);
		TopicServiceInterface instance2 = injector.getInstance(TopicServiceInterface.class);

		assertNotNull(instance1);
		assertSame("TopicServiceInterface deve essere singleton", instance1, instance2);
	}

	@Test
	public void testStudySessionInterfaceBindingIsSingleton() {
		Injector injector = Guice.createInjector(
			Modules.override(new AppModule("test-unit", Collections.emptyMap())).with(new AbstractModule() {
				@Override
				protected void configure() {
					bind(EntityManagerFactory.class).toInstance(mockEmf);
				}
			})
		);

		StudySessionInterface instance1 = injector.getInstance(StudySessionInterface.class);
		StudySessionInterface instance2 = injector.getInstance(StudySessionInterface.class);

		assertNotNull(instance1);
		assertSame("StudySessionInterface deve essere singleton", instance1, instance2);
	}

	@Test
	public void testProvideEntityManagerFactoryUsesConstructorArgumentsAndIsSingleton() {
		String expectedPersistenceUnit = "my-persistence-unit";
		Map<String, String> expectedProperties = Collections.singletonMap("key", "value");

		try (MockedStatic<Persistence> mockedPersistence = Mockito.mockStatic(Persistence.class)) {
			mockedPersistence
				.when(() -> Persistence.createEntityManagerFactory(expectedPersistenceUnit, expectedProperties))
				.thenReturn(mockEmf);

			AppModule module = new AppModule(expectedPersistenceUnit, expectedProperties);
			Injector injector = Guice.createInjector(module);

			EntityManagerFactory emfInstance1 = injector.getInstance(EntityManagerFactory.class);
			EntityManagerFactory emfInstance2 = injector.getInstance(EntityManagerFactory.class);
			
			mockedPersistence.verify(() -> Persistence.createEntityManagerFactory(expectedPersistenceUnit, expectedProperties));

			assertNotNull(emfInstance1);
			assertSame("L'EntityManagerFactory dovrebbe essere un singleton", emfInstance1, emfInstance2);
		}
	}
}