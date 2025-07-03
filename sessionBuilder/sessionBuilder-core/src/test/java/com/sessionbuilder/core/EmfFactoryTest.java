package com.sessionbuilder.core;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;

import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class EmfFactoryTest {

	private static final String TEST_PERSISTENCE_UNIT = "test-pu";

	@Test
	public void testCreateEntityManagerFactory_Success() {
		Map<String, String> properties = new HashMap<>();
		properties.put("jakarta.persistence.jdbc.url", "jdbc:h2:mem:testdb");
		properties.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
		properties.put("jakarta.persistence.schema-generation.database.action", "drop-and-create");

		try (MockedStatic<Persistence> mockedPersistence = Mockito.mockStatic(Persistence.class)) {
			EntityManagerFactory mockEmf = Mockito.mock(EntityManagerFactory.class);
			mockedPersistence.when(() -> Persistence.createEntityManagerFactory(TEST_PERSISTENCE_UNIT, properties))
				.thenReturn(mockEmf);

			EntityManagerFactory result = EmfFactory.createEntityManagerFactory(TEST_PERSISTENCE_UNIT, properties);

			assertThat(result).isEqualTo(mockEmf);

			mockedPersistence.verify(() -> Persistence.createEntityManagerFactory(TEST_PERSISTENCE_UNIT, properties));
		}
	}

	@Test
	public void testCreateEntityManagerFactory_WithEmptyProperties() {
		Map<String, String> properties = Collections.emptyMap();

		try (MockedStatic<Persistence> mockedPersistence = Mockito.mockStatic(Persistence.class)) {
			EntityManagerFactory mockEmf = Mockito.mock(EntityManagerFactory.class);
			mockedPersistence.when(() -> Persistence.createEntityManagerFactory(TEST_PERSISTENCE_UNIT, properties))
				.thenReturn(mockEmf);

			EntityManagerFactory result = EmfFactory.createEntityManagerFactory(TEST_PERSISTENCE_UNIT, properties);

			assertThat(result).isEqualTo(mockEmf);
			mockedPersistence.verify(() -> Persistence.createEntityManagerFactory(TEST_PERSISTENCE_UNIT, properties));
		}
	}

	@Test
	public void testCreateEntityManagerFactory_PersistenceException() {
		Map<String, String> properties = new HashMap<>();
		properties.put("invalid.property", "value");

		try (MockedStatic<Persistence> mockedPersistence = Mockito.mockStatic(Persistence.class)) {
			mockedPersistence.when(() -> Persistence.createEntityManagerFactory(TEST_PERSISTENCE_UNIT, properties))
				.thenThrow(new PersistenceException("Failed to create EMF due to configuration error"));

			PersistenceException thrown = assertThrows(PersistenceException.class, () -> {
				EmfFactory.createEntityManagerFactory(TEST_PERSISTENCE_UNIT, properties);
			});

			assertThat(thrown.getMessage()).contains("Failed to create EMF due to configuration error");
			mockedPersistence.verify(() -> Persistence.createEntityManagerFactory(TEST_PERSISTENCE_UNIT, properties));
		}
	}
	
	@Test
	public void testPrivateConstructorCoverage() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		Constructor<EmfFactory> constructor = EmfFactory.class.getDeclaredConstructor();
		
		constructor.setAccessible(true);
		
		EmfFactory instance = constructor.newInstance();
		
		assertThat(instance).isNotNull();
	}
}