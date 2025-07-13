package com.sessionbuilder.core.backend;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sessionbuilder.core.utils.EmfFactory;
import com.sessionbuilder.core.utils.TestEntityManagerFactoryModule;

import jakarta.persistence.EntityManagerFactory;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestEntityManagerFactoryModuleTest {

	@Mock
	private EntityManagerFactory mockEmf;

	@Test
	public void testProvideEntityManagerFactoryUsesConstructorArgumentsAndIsSingleton() {
		String expectedPersistenceUnit = "test-persistence-unit";
		Map<String, String> expectedJdbcProperties = Collections.singletonMap("hibernate.hbm2ddl.auto", "none");

		try (MockedStatic<EmfFactory> mockedEmfFactory = mockStatic(EmfFactory.class)) {
			mockedEmfFactory
				.when(() -> EmfFactory.createEntityManagerFactory(eq(expectedPersistenceUnit), eq(expectedJdbcProperties)))
				.thenReturn(mockEmf);

			TestEntityManagerFactoryModule module = new TestEntityManagerFactoryModule(expectedJdbcProperties, expectedPersistenceUnit);
			Injector injector = Guice.createInjector(module);
			EntityManagerFactory emfInstance1 = injector.getInstance(EntityManagerFactory.class);
			EntityManagerFactory emfInstance2 = injector.getInstance(EntityManagerFactory.class);

			mockedEmfFactory.verify(() -> EmfFactory.createEntityManagerFactory(eq(expectedPersistenceUnit), eq(expectedJdbcProperties)), times(1));

			assertNotNull("L'EntityManagerFactory iniettata non dovrebbe essere null", emfInstance1);
			assertSame("L'EntityManagerFactory dovrebbe essere un singleton (stessa istanza)", emfInstance1, emfInstance2);
		}
	}
}
