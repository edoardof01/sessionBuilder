package com.sessionbuilder.it;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sessionbuilder.core.EmfFactory;
import com.sessionbuilder.core.StudySession;
import com.sessionbuilder.core.StudySessionRepository;
import com.sessionbuilder.core.StudySessionRepositoryInterface;
import com.sessionbuilder.core.Topic;
import com.sessionbuilder.core.TopicRepository;
import com.sessionbuilder.core.TopicRepositoryInterface;
import com.sessionbuilder.core.TransactionManager;
import com.sessionbuilder.core.TransactionManagerImpl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

public class StudySessionRepositoryIT {
	
	private EntityManagerFactory emf;
	private TransactionManager transactionManager;

	@SuppressWarnings("resource")
	@ClassRule
	public static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
			.withDatabaseName(System.getenv().getOrDefault("POSTGRES_TEST_DB", "test"))
			.withUsername(System.getenv().getOrDefault("POSTGRES_TEST_USER", "test"))
			.withPassword(System.getenv().getOrDefault("POSTGRES_TEST_PASSWORD", "test"));
	
	@BeforeClass
	public static void setUpContainer() {
		postgres.start();
	}
	
	private void cleanDatabase() {
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
	
	@Before
	public void setup() {
		Map<String, String> properties = new HashMap<>();
		properties.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
		properties.put("jakarta.persistence.jdbc.url", postgres.getJdbcUrl());
		properties.put("jakarta.persistence.jdbc.user", postgres.getUsername());
		properties.put("jakarta.persistence.jdbc.password", postgres.getPassword());
		properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
		properties.put("hibernate.hbm2ddl.auto", "create-drop");
		properties.put("hibernate.show_sql", "true");
		properties.put("hibernate.format_sql", "true");
		
		emf = EmfFactory.createEntityManagerFactory("sessionbuilder-test", properties);
		cleanDatabase();
		AbstractModule module = new AbstractModule() {
			@Override
			protected void configure() {
				bind(EntityManagerFactory.class).toInstance(emf);
				bind(StudySessionRepositoryInterface.class).to(StudySessionRepository.class);
				bind(TopicRepositoryInterface.class).to(TopicRepository.class);
				bind(TransactionManager.class).to(TransactionManagerImpl.class);
			}
		};
		Injector injector = Guice.createInjector(module);
		injector.getInstance(StudySessionRepositoryInterface.class);
		injector.getInstance(TopicRepositoryInterface.class);
		transactionManager = injector.getInstance(TransactionManager.class);
	}
	
	@After
	public void tearDown() {
		if (emf != null && emf.isOpen()) {
			emf.close();
		}
		transactionManager.getEmHolder().remove();
	}

	@Test
	public void testFindByIdFailureIt() {
		long id = 10000000;
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> transactionManager.doInSessionTransaction(repo -> repo.findById(id)));
		assertThat(e.getMessage()).isEqualTo("non esiste una session con tale id");
	}
	
	@Test 
	public void testSaveAndFindSessionIt() {
		Topic topic = new Topic("Corsa", "allena lo scatto", 1, new ArrayList<>());
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(topic);
			return null;
		});
		
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>(java.util.List.of(topic)));
		transactionManager.doInSessionTransaction(repo -> {
			repo.save(session);
			return null;
		});
		
		long sessionId = session.getId();
		assertThat(sessionId).isPositive();
		
		StudySession retrievedSession = transactionManager.doInSessionTransaction(repo -> repo.findById(sessionId));
		
		assertThat(retrievedSession).isNotNull();
		assertThat(retrievedSession.getNote()).isEqualTo("una nota");
		assertThat(retrievedSession.getDuration()).isEqualTo(60);
	}
	
	@Test
	public void testUpdateSessionIt() {
		Topic topic = new Topic("Corsa", "allena lo scatto", 1, new ArrayList<>());
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(topic);
			return null;
		});
		
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>(java.util.List.of(topic)));
		transactionManager.doInSessionTransaction(repo -> {
			repo.save(session);
			return null;
		});
		
		long sessionId = session.getId();
		
		StudySession retrievedSession = transactionManager.doInSessionTransaction(repo -> repo.findById(sessionId));
		assertThat(retrievedSession.getDuration()).isEqualTo(60);
		
		retrievedSession.setDuration(90);
		transactionManager.doInSessionTransaction(repo -> {
			repo.update(retrievedSession);
			return null;
		});
		
		StudySession updatedSession = transactionManager.doInSessionTransaction(repo -> repo.findById(sessionId));
		assertThat(updatedSession.getDuration()).isEqualTo(90);
	}
	
	@Test
	public void testDeleteSessionIt() {
		Topic topic = new Topic("Corsa", "allena lo scatto", 1, new ArrayList<>());
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(topic);
			return null;
		});
		
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>(java.util.List.of(topic)));
		transactionManager.doInSessionTransaction(repo -> {
			repo.save(session);
			return null;
		});
		
		long sessionId = session.getId();
		
		StudySession retrievedSession = transactionManager.doInSessionTransaction(repo -> repo.findById(sessionId));
		assertThat(retrievedSession).isNotNull();
		
		transactionManager.doInSessionTransaction(repo -> {
			repo.delete(sessionId);
			return null;
		});
		
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
			transactionManager.doInSessionTransaction(repo -> repo.findById(sessionId));
		});
		assertThat(e.getMessage()).isEqualTo("non esiste una session con tale id");
	}
}