package com.sessionBuilder.it;

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
import com.google.inject.Module;
import com.sessionBuilder.core.StudySession;
import com.sessionBuilder.core.StudySessionRepository;
import com.sessionBuilder.core.StudySessionRepositoryInterface;
import com.sessionBuilder.core.Topic;
import com.sessionBuilder.core.TopicRepository;
import com.sessionBuilder.core.TopicRepositoryInterface;
import com.sessionBuilder.core.TransactionManager;
import com.sessionBuilder.core.TransactionManagerImpl;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class StudySessionRepositoryIt {
	
	private StudySessionRepositoryInterface sessionRepository;
	private TopicRepositoryInterface topicRepository;
	private EntityManagerFactory emf;

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
		
		emf = Persistence.createEntityManagerFactory("sessionbuilder-test", properties);
		Module module = new AbstractModule() {
			@Override
			protected void configure() {
				bind(EntityManagerFactory.class).toInstance(emf);
				bind(StudySessionRepositoryInterface.class).to(StudySessionRepository.class);
				bind(TopicRepositoryInterface.class).to(TopicRepository.class);
				bind(TransactionManager.class).to(TransactionManagerImpl.class);
			}
		};
		Injector injector = Guice.createInjector(module);
		sessionRepository = injector.getInstance(StudySessionRepositoryInterface.class);
		topicRepository = injector.getInstance(TopicRepositoryInterface.class);
	}
	
	@After
	public void tearDown() {
		if (emf != null && emf.isOpen()) {
			emf.close();
		}
	}

	@Test
	public void testFindByIdFailureIt() {
		long id = 10000000;
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> sessionRepository.findById(id));
		assertThat(e.getMessage()).isEqualTo("non esiste una session con tale id");
	}
	
	@Test 
	public void testSaveAndFindSessionIt() {
		Topic topic = new Topic("Corsa", "allena lo scatto", 1, new ArrayList<>());
		topicRepository.save(topic);
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>());
		sessionRepository.save(session);
		long sessionId = session.getId();
		assertThat(sessionId).isPositive();
		StudySession retrievedSession = sessionRepository.findById(sessionId);
		assertThat(retrievedSession).isNotNull();
		assertThat(retrievedSession.getNote()).isEqualTo("una nota");
		assertThat(retrievedSession.getDuration()).isEqualTo(60);
	}
	
	@Test
	public void testUpdateSessionIt() {
		Topic topic = new Topic("Corsa", "allena lo scatto", 1, new ArrayList<>());
		topicRepository.save(topic);
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>());
		sessionRepository.save(session);
		long sessionId = session.getId();
		assertThat(sessionRepository.findById(sessionId).getDuration()).isEqualTo(60);
		session.setDuration(90);
		sessionRepository.update(session);
		assertThat(sessionRepository.findById(sessionId).getDuration()).isEqualTo(90);
	}
	
	@Test
	public void testDeleteSessionIt() {
		Topic topic = new Topic("Corsa", "allena lo scatto", 1, new ArrayList<>());
		topicRepository.save(topic);
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>());
		sessionRepository.save(session);
		long sessionId = session.getId();
		assertThat(sessionRepository.findById(sessionId)).isNotNull();
		sessionRepository.delete(sessionId);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
			sessionRepository.findById(sessionId);
		});
		assertThat(e.getMessage()).isEqualTo("non esiste una session con tale id");
	}
}