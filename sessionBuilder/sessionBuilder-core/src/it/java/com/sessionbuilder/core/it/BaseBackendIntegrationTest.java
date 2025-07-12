package com.sessionbuilder.core.it;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.sessionbuilder.core.utils.AppModule;
import com.sessionbuilder.core.backend.TransactionManager;
import com.sessionbuilder.core.utils.TestEntityManagerFactoryModule;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseBackendIntegrationTest {

	protected EntityManagerFactory emf;
	protected Injector injector;
	protected TransactionManager transactionManager;

	@SuppressWarnings("resource")
	@ClassRule
	public static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
		.withDatabaseName(System.getenv().getOrDefault("POSTGRES_DB", "test"))
		.withUsername(System.getenv().getOrDefault("POSTGRES_USER", "test"))
		.withPassword(System.getenv().getOrDefault("POSTGRES_PASSWORD", "test"));

	@BeforeClass
	public static void setUpContainer() {
		postgres.start();
	}

	protected void cleanDatabase() {
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			em.createNativeQuery("TRUNCATE TABLE topic_studysession CASCADE").executeUpdate();
			em.createNativeQuery("TRUNCATE TABLE studysession CASCADE").executeUpdate();
			em.createNativeQuery("TRUNCATE TABLE topic CASCADE").executeUpdate();
			tx.commit();
		} finally {
			if (tx.isActive()) tx.rollback();
			em.close();
		}
	}

	protected abstract AbstractModule getTestSpecificModule();

	@Before
	public void setupBase() {
		Map<String, String> jdbcProperties = new HashMap<>();
		jdbcProperties.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
		jdbcProperties.put("jakarta.persistence.jdbc.url", postgres.getJdbcUrl());
		jdbcProperties.put("jakarta.persistence.jdbc.user", postgres.getUsername());
		jdbcProperties.put("jakarta.persistence.jdbc.password", postgres.getPassword());
		jdbcProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
		jdbcProperties.put("hibernate.hbm2ddl.auto", "create-drop");
		jdbcProperties.put("hibernate.show_sql", "true");
		jdbcProperties.put("hibernate.format_sql", "true");

		injector = Guice.createInjector(
			Modules.override(new AppModule("dummy-persistence-unit", Collections.emptyMap()))
				.with(new TestEntityManagerFactoryModule(jdbcProperties, "sessionbuilder-test"),
					getTestSpecificModule())
		);
		emf = injector.getInstance(EntityManagerFactory.class);
		transactionManager = injector.getInstance(TransactionManager.class);
		cleanDatabase();
		onSetup();
	}

	protected void onSetup() {
	}

	@After
	public void tearDownBase() {
		if (emf != null && emf.isOpen()) {
			emf.close();
		}
		if (transactionManager != null && transactionManager.getEmHolder() != null) {
			transactionManager.getEmHolder().remove();
		}
	}
}
