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
import com.sessionbuilder.core.AppModule;
import com.sessionbuilder.core.StudySession;
import com.sessionbuilder.core.StudySessionRepositoryInterface;
import com.sessionbuilder.core.Topic;
import com.sessionbuilder.core.TopicServiceInterface;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class TopicServiceIT {
	
	private EntityManagerFactory emf;
	private TopicServiceInterface topicService;
	private StudySessionRepositoryInterface sessionRepository;
	
	private String name;
	private String description;
	private int difficulty;

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
		name = "Fumetti";
		description = "leggi topolino";
		difficulty = 1;
		
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
		AppModule module = new AppModule("sessionbuilder-test", properties);
		AbstractModule testModule = new AbstractModule() {
			@Override
			public void configure() {
				bind(EntityManagerFactory.class).toInstance(emf);
			}
		};
		Injector injector = Guice.createInjector(module, testModule);
		topicService = injector.getInstance(TopicServiceInterface.class);
		sessionRepository = injector.getInstance(StudySessionRepositoryInterface.class);
	}
	
	@After
	public void tearDown() {
		if (emf != null && emf.isOpen()) {
			emf.close();
		}
	}
	
	@Test
	public void testCreateAndGetTopicIt() {
		Topic createdTopic = topicService.createTopic(name, description, difficulty, new ArrayList<>());
		
		long realId = createdTopic.getId();
		assertThat(realId).isPositive();
		
		Topic retrievedTopic = topicService.getTopicById(realId);
		
		assertThat(createdTopic).isNotNull();
		assertThat(retrievedTopic).isNotNull();
		assertThat(retrievedTopic.getName()).isEqualTo(name);
		assertThat(retrievedTopic.getDescription()).isEqualTo(description);
		assertThat(retrievedTopic.getDifficulty()).isEqualTo(difficulty);
	}
	
	@Test
	public void testAddSessionToTopicIt() {
		Topic topic = topicService.createTopic("Documentari", "studio del nazismo", 1, new ArrayList<>());
		long topicId = topic.getId();
		
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>());
		sessionRepository.save(session);
		long sessionId = session.getId();
		
		topicService.addSessionToTopic(topicId, sessionId);
		
		Topic updatedTopic = topicService.getTopicById(topicId);
		assertThat(updatedTopic.getSessionList()).hasSize(1);
		assertThat(updatedTopic.getSessionList().get(0).getNote()).isEqualTo("una nota");
	}
	
	@Test
	public void testRemoveSessionFromTopicIt() {
		Topic topic = topicService.createTopic("Documentari", "studio del nazismo", 1, new ArrayList<>());
		long topicId = topic.getId();
		
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>());
		sessionRepository.save(session);
		long sessionId = session.getId();
		
		topicService.addSessionToTopic(topicId, sessionId);
		
		Topic topicWithSession = topicService.getTopicById(topicId);
		assertThat(topicWithSession.getSessionList()).hasSize(1);
		
		topicService.removeSessionFromTopic(topicId, sessionId);
		
		Topic topicWithoutSession = topicService.getTopicById(topicId);
		assertThat(topicWithoutSession.getSessionList()).isEmpty();
	}
	
	@Test
	public void testDeleteTopicIt() {
		Topic topic = topicService.createTopic("Documentari", "studio del nazismo", 1, new ArrayList<>());
		long topicId = topic.getId();
		
		Topic existingTopic = topicService.getTopicById(topicId);
		assertThat(existingTopic).isNotNull();
		
		topicService.deleteTopic(topicId);
		
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
			() -> topicService.getTopicById(topicId));
		assertThat(exception.getMessage()).isEqualTo("il topic cercato non esiste");
	}
	
	@Test
	public void testCalculateTotalTimeIt() {
		Topic topic = topicService.createTopic(name, description, difficulty, new ArrayList<>());
		long topicId = topic.getId();
		
		StudySession session1 = new StudySession(LocalDate.now().plusDays(1), 60, "session 1", new ArrayList<>());
		StudySession session2 = new StudySession(LocalDate.now().plusDays(2), 30, "session 2", new ArrayList<>());
		
		sessionRepository.save(session1);
		sessionRepository.save(session2);
		
		topicService.addSessionToTopic(topicId, session1.getId());
		topicService.addSessionToTopic(topicId, session2.getId());
		
		Integer totalTime = topicService.calculateTotalTime(topicId);
		assertThat(totalTime).isEqualTo(90);
	}
	
	@Test
	public void testCalculatePercentageOfCompletionIt() {
		Topic topic = topicService.createTopic(name, description, difficulty, new ArrayList<>());
		long topicId = topic.getId();
		
		StudySession uncompletedSession = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>());
		sessionRepository.save(uncompletedSession);
		
		StudySession completedSession = new StudySession(LocalDate.now().plusDays(2), 60, "un'altra nota", new ArrayList<>());
		completedSession.setIsComplete(true);
		sessionRepository.save(completedSession);
		
		topicService.addSessionToTopic(topicId, uncompletedSession.getId());
		topicService.addSessionToTopic(topicId, completedSession.getId());
		
		Integer percentage = topicService.calculatePercentageOfCompletion(topicId);
		assertThat(percentage).isEqualTo(50);
	}
}