package com.sessionBuilder.it;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;
import com.google.inject.AbstractModule;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testcontainers.containers.PostgreSQLContainer;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sessionbuilder.core.SessionViewCallback;
import com.sessionbuilder.core.StudySession;
import com.sessionbuilder.core.StudySessionController;
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

@RunWith(MockitoJUnitRunner.class)
public class StudySessionControllerIT {

	private EntityManagerFactory emf;
	private StudySessionController sessionController;
	private TopicRepositoryInterface topicRepository;
	@Mock
	private SessionViewCallback viewCallback;
	private StudySessionInterface sessionService;
	
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
				bind(TopicRepositoryInterface.class).to(TopicRepository.class);
				bind(StudySessionRepositoryInterface.class).to(StudySessionRepository.class);
				bind(TransactionManager.class).to(TransactionManagerImpl.class);
				bind(StudySessionInterface.class).to(StudySessionService.class);
				 bind(SessionViewCallback.class).toInstance(viewCallback);
			}
		};
		Injector injector = Guice.createInjector(module);
		sessionController = injector.getInstance(StudySessionController.class);
		topicRepository = injector.getInstance(TopicRepositoryInterface.class);
		viewCallback = spy(SessionViewCallback.class);
		sessionController.setViewCallBack(viewCallback);
		sessionService = injector.getInstance(StudySessionInterface.class);
	}
	
	@After
	public void tearDown() {
		if (emf != null && emf.isOpen()) {
			emf.close();
		}
	}
	
	@Test
	public void handleCreateSessionIt() {
		Topic topic = new Topic("Matematica", "Algebra lineare", 4, new ArrayList<>());
		topicRepository.save(topic);
		ArrayList<Topic> topics = new ArrayList<>(List.of(topic));
		
		StudySession session = sessionController.handleCreateSession(
			LocalDate.now().plusDays(1), 90, "sessione di algebra", topics);
		
		verify(viewCallback).onSessionAdded(session);
		verify(viewCallback, never()).onSessionError(anyString());
		assertThat(session).isNotNull();
		assertThat(session.getId()).isPositive();
		assertThat(session.getDate()).isEqualTo(LocalDate.now().plusDays(1));
		assertThat(session.getDuration()).isEqualTo(90);
		assertThat(session.getNote()).isEqualTo("sessione di algebra");
		assertThat(session.getTopicList()).contains(topic);
		assertThat(sessionService.getSessionById(session.getId())).isEqualTo(session);
	}
	
	@Test
	public void handleGetSessionIt() {
		Topic topic = new Topic("Biologia", "Genetica", 3, new ArrayList<>());
		topicRepository.save(topic);
		ArrayList<Topic> topics = new ArrayList<>(List.of(topic));
		
		StudySession createdSession = sessionService.createSession(
			LocalDate.now().plusDays(1), 75, "studio genetica", topics);
		
		StudySession retrievedSession = sessionController.handleGetSession(createdSession.getId());
		
		assertThat(retrievedSession).isNotNull().isEqualTo(createdSession);
		assertThat(retrievedSession.getNote()).isEqualTo("studio genetica");
		verify(viewCallback, never()).onSessionError(anyString());
	}

	@Test
	public void handleDeleteSessionIt() {
		Topic topic = new Topic("Filosofia", "Etica", 3, new ArrayList<>());
		topicRepository.save(topic);
		ArrayList<Topic> topics = new ArrayList<>(List.of(topic));
		StudySession session = sessionService.createSession(
			LocalDate.now().plusDays(1), 60, "sessione di etica", topics);
		long sessionId = session.getId();
		sessionController.handleDeleteSession(sessionId);
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
			() -> sessionService.getSessionById(sessionId));
		assertThat(exception.getMessage()).contains("non esiste una session");
		verify(viewCallback).onSessionRemoved(session);
		verify(viewCallback, never()).onSessionError(anyString());
	}
	
	@Test
	public void handleAddTopicIt() {
		Topic topic1 = new Topic("Letteratura", "Dante", 3, new ArrayList<>());
		Topic topic2 = new Topic("Arte", "Rinascimento", 4, new ArrayList<>());
		topicRepository.save(topic1);
		topicRepository.save(topic2);
		
		StudySession session = sessionService.createSession(
			LocalDate.now().plusDays(1), 120, "sessione multidisciplinare", new ArrayList<>(List.of(topic1)));
		
		sessionController.handleAddTopic(session.getId(), topic2.getId());
		
		StudySession updatedSession = sessionService.getSessionById(session.getId());
		assertThat(updatedSession.getTopicList()).contains(topic1, topic2);
		verify(viewCallback, never()).onSessionError(anyString());
	}
	
	@Test
	public void handleRemoveTopicIt() {
		Topic topic1 = new Topic("Economia", "Microeconomia", 4, new ArrayList<>());
		Topic topic2 = new Topic("Diritto", "Costituzionale", 3, new ArrayList<>());
		topicRepository.save(topic1);
		topicRepository.save(topic2);
		
		StudySession session = sessionService.createSession(
			LocalDate.now().plusDays(1), 90, "sessione economico-giuridica", new ArrayList<>(List.of(topic1, topic2)));
		
		sessionController.handleRemoveTopic(session.getId(), topic2.getId());
		
		StudySession updatedSession = sessionService.getSessionById(session.getId());
		assertThat(updatedSession.getTopicList()).contains(topic1);
		assertThat(updatedSession.getTopicList()).doesNotContain(topic2);
		verify(viewCallback, never()).onSessionError(anyString());
	}
	
	@Test
	public void handleCompleteSessionSuccessIt() {
		Topic topic = new Topic("Psicologia", "Cognitivismo", 4, new ArrayList<>());
		topicRepository.save(topic);
		ArrayList<Topic> topics = new ArrayList<>(List.of(topic));
		
		StudySession session = sessionService.createSession(
			LocalDate.now().plusDays(1), 120, "sessione di psicologia", topics);
		
		assertThat(session.isComplete()).isFalse();
		
		sessionController.handleCompleteSession(session.getId());
		
		StudySession completedSession = sessionService.getSessionById(session.getId());
		assertThat(completedSession.isComplete()).isTrue();
		verify(viewCallback, never()).onSessionError(anyString());
	}
	
	@Test
	public void handleCompleteAlreadyCompletedSessionFailureIt() {
		Topic topic = new Topic("Sociologia", "Teorie sociali", 3, new ArrayList<>());
		topicRepository.save(topic);
		ArrayList<Topic> topics = new ArrayList<>(List.of(topic));
		StudySession session = sessionService.createSession(
			LocalDate.now().plusDays(1), 90, "sessione di sociologia", topics);
		sessionController.handleCompleteSession(session.getId());
		sessionController.handleCompleteSession(session.getId());
		verify(viewCallback).onSessionError(anyString());
	}
	
	@Test
	public void completeWorkflowIt() {
		Topic topic1 = new Topic("Informatica", "Algoritmi", 5, new ArrayList<>());
		Topic topic2 = new Topic("Matematica", "Calcolo", 4, new ArrayList<>());
		topicRepository.save(topic1);
		topicRepository.save(topic2);
		StudySession session = sessionController.handleCreateSession(
			LocalDate.now().plusDays(1), 150, "sessione algoritmi e calcolo", new ArrayList<>(List.of(topic1)));
		long sessionId = session.getId();
		sessionController.handleAddTopic(sessionId, topic2.getId());
		StudySession sessionWithBothTopics = sessionController.handleGetSession(sessionId);
		assertThat(sessionWithBothTopics.getTopicList()).hasSize(2);
		assertThat(sessionWithBothTopics.getTopicList()).contains(topic1, topic2);
		sessionController.handleCompleteSession(sessionId);
		StudySession completedSession = sessionController.handleGetSession(sessionId);
		assertThat(completedSession.isComplete()).isTrue();
		StudySession sessionWithOneTopic = sessionController.handleGetSession(sessionId);
		assertThat(sessionWithOneTopic.getTopicList()).hasSize(2);
		sessionController.handleDeleteSession(sessionId);
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
			() -> sessionController.handleGetSession(sessionId));
		assertThat(exception.getMessage()).contains("non esiste una session con tale id");
	}

}