package com.sessionBuilder.it;

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
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sessionbuilder.core.StudySession;
import com.sessionbuilder.core.StudySessionInterface;
import com.sessionbuilder.core.StudySessionRepository;
import com.sessionbuilder.core.StudySessionRepositoryInterface;
import com.sessionbuilder.core.StudySessionService;
import com.sessionbuilder.core.Topic;
import com.sessionbuilder.core.TopicRepository;
import com.sessionbuilder.core.TopicRepositoryInterface;
import com.sessionbuilder.core.TransactionManager;
import com.sessionbuilder.core.TransactionManagerImpl;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class StudySessionServiceIT {
	
	private EntityManagerFactory emf;
	private StudySessionInterface sessionService;
	private TopicRepositoryInterface topicRepository;

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
		AbstractModule module = new AbstractModule() {
			@Override
			protected void configure() {
				bind(EntityManagerFactory.class).toInstance(emf);
				bind(StudySessionRepositoryInterface.class).to(StudySessionRepository.class);
				bind(TopicRepositoryInterface.class).to(TopicRepository.class);
				bind(TransactionManager.class).to(TransactionManagerImpl.class);
				bind(StudySessionInterface.class).to(StudySessionService.class);
			}
		};
		Injector injector = Guice.createInjector(module);
		sessionService = injector.getInstance(StudySessionInterface.class);
		topicRepository = injector.getInstance(TopicRepositoryInterface.class);
	}
	
	@After
	public void tearDown() {
		if (emf != null && emf.isOpen()) {
			emf.close();
		}
	}
	
	@Test
	public void testCreateAndGetSessionIt() {
		Topic topic = new Topic("Math", "Algebra", 3, new ArrayList<>());
		topicRepository.save(topic);
		
		StudySession createdSession = sessionService.createSession(LocalDate.now().plusDays(1), 60, "Test session", new ArrayList<>());
		
		long sessionId = createdSession.getId();
		assertThat(sessionId).isPositive();
		
		StudySession retrievedSession = sessionService.getSessionById(sessionId);
		
		assertThat(createdSession).isNotNull();
		assertThat(retrievedSession).isNotNull();
		assertThat(retrievedSession.getDuration()).isEqualTo(60);
		assertThat(retrievedSession.getNote()).isEqualTo("Test session");
		assertThat(retrievedSession.isComplete()).isFalse();
	}
	
	@Test
	public void testGetSessionByIdNotFoundIt() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
			() -> sessionService.getSessionById(99999L));
		assertThat(exception.getMessage()).isEqualTo("la sessione cercata non esiste");
	}
	
	@Test
	public void testCompleteSessionIt() {
		Topic topic = new Topic("Physics", "Mechanics", 4, new ArrayList<>());
		topicRepository.save(topic);
		
		StudySession session = sessionService.createSession(LocalDate.now().plusDays(1), 90, "Physics session", new ArrayList<>());
		long sessionId = session.getId();
		
		assertThat(session.isComplete()).isFalse();
		
		sessionService.completeSession(sessionId);
		
		StudySession completedSession = sessionService.getSessionById(sessionId);
		assertThat(completedSession.isComplete()).isTrue();
	}
	

	@Test
	public void testAddTopicToSessionIt() {
		Topic topic = new Topic("Chemistry", "Organic Chemistry", 5, new ArrayList<>());
		topicRepository.save(topic);
		long topicId = topic.getId();
		
		StudySession session = sessionService.createSession(LocalDate.now().plusDays(1), 120, "Chemistry session", new ArrayList<>());
		long sessionId = session.getId();
		
		sessionService.addTopic(sessionId, topicId);
		
		StudySession updatedSession = sessionService.getSessionById(sessionId);
		assertThat(updatedSession.getTopicList()).hasSize(1);
		assertThat(updatedSession.getTopicList().get(0).getName()).isEqualTo("Chemistry");
	}

	
	@Test
	public void testRemoveTopicFromSessionIt() {
		Topic topic = new Topic("History", "World War II", 4, new ArrayList<>());
		topicRepository.save(topic);
		long topicId = topic.getId();
		
		Topic topic2 = new Topic("Geography", "Europe", 2, new ArrayList<>());
		topicRepository.save(topic2);
		long topic2Id = topic2.getId();
		
		StudySession session = sessionService.createSession(LocalDate.now().plusDays(1), 90, "History session", new ArrayList<>());
		long sessionId = session.getId();
		
		sessionService.addTopic(sessionId, topicId);
		sessionService.addTopic(sessionId, topic2Id);
		
		StudySession sessionWithTopics = sessionService.getSessionById(sessionId);
		assertThat(sessionWithTopics.getTopicList()).hasSize(2);
		
		sessionService.removeTopic(sessionId, topicId);
		
		StudySession sessionWithoutTopic = sessionService.getSessionById(sessionId);
		assertThat(sessionWithoutTopic.getTopicList()).hasSize(1);
		assertThat(sessionWithoutTopic.getTopicList().get(0).getName()).isEqualTo("Geography");
	}

	
	@Test
	public void testDeleteSessionIt() {
		Topic topic = new Topic("Literature", "Shakespeare", 4, new ArrayList<>());
		topicRepository.save(topic);
		
		StudySession session = sessionService.createSession(LocalDate.now().plusDays(1), 75, "Literature session", new ArrayList<>());
		long sessionId = session.getId();
		
		StudySession existingSession = sessionService.getSessionById(sessionId);
		assertThat(existingSession).isNotNull();
		
		sessionService.deleteSession(sessionId);
		
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
			() -> sessionService.getSessionById(sessionId));
		assertThat(exception.getMessage()).isEqualTo("la sessione cercata non esiste");
	}

}