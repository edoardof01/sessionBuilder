package com.sessionBuilder.it;

import static org.mockito.ArgumentMatchers.any;
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
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sessionbuilder.core.AppModule;
import com.sessionbuilder.core.StudySession;
import com.sessionbuilder.core.StudySessionRepositoryInterface;
import com.sessionbuilder.core.Topic;
import com.sessionbuilder.core.TopicController;
import com.sessionbuilder.core.TopicServiceInterface;
import com.sessionbuilder.core.TopicViewCallback;


import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class TopicControllerIT {

	private EntityManagerFactory emf;
	private TopicController topicController;
	private StudySessionRepositoryInterface  sessionRepository;
	private TopicViewCallback viewCallback;
	private TopicServiceInterface topicService;
	
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
		AppModule module = new AppModule("sessionbuilder-test", properties);
		Injector injector = Guice.createInjector(module);
		topicController = injector.getInstance(TopicController.class);
		sessionRepository = injector.getInstance(StudySessionRepositoryInterface.class);
		viewCallback = spy(TopicViewCallback.class);
		topicController.setViewCallback(viewCallback);
		topicService = injector.getInstance(TopicServiceInterface.class);
	}
	
	@After
	public void tearDown() {
		if (emf != null && emf.isOpen()) {
			emf.close();
		}
	}
	
	@Test
	public void handleGetTopicByIdItSuccessIt() {
		Topic topic = topicService.createTopic("chimica", "tavola periodica", 3, new ArrayList<>());
		assertThat(topic.getId()).isPositive();
		assertThat(topicService.getTopicById(topic.getId())).isEqualTo(topic);
		Topic result = topicController.handleGetTopicById(topic.getId());
		assertThat(result).isNotNull().isEqualTo(topic);
	}
	
	@Test
	public void handleCreateTopicSuccessIt() {
		Topic topic = topicController.handleCreateTopic("chimica", "tavola periodica", 3, new ArrayList<>());
		verify(viewCallback).onTopicAdded(topic);
		assertThat(topic).isNotNull();
		assertThat(topic.getId()).isPositive();
		assertThat(topicService.getTopicById(topic.getId())).isEqualTo(topic);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void handleCreateTopicFailureIt() {
		topicController.handleCreateTopic(null, "tavola periodica", 3, new ArrayList<>());
		verify(viewCallback, never()).onTopicAdded(any());
		verify(viewCallback).onTopicError(anyString());
	}
	
	@Test
	public void handleDeleteTopicIt() {
		Topic topic = topicService.createTopic("chimica", "tavola periodica", 3, new ArrayList<>());
		assertThat(topic).isNotNull();
		assertThat(topic.getId()).isPositive();
		assertThat(topicService.getTopicById(topic.getId())).isEqualTo(topic);
		topicController.handleDeleteTopic(topic.getId());
		assertThat(topicService.getTopicById(topic.getId())).isNull();
		verify(viewCallback).onTopicRemoved(topic);
		verify(viewCallback, never()).onTopicError(anyString());
	}
	
	@Test
	public void handleAddSessionToTopicIt() {
		Topic topic = topicService.createTopic("chimica", "tavola periodica", 3, new ArrayList<>());
		Topic topic2 = topicService.createTopic("biologia", "cetacei", 3, new ArrayList<>());
		assertThat(topic).isNotNull();
		assertThat(topic.getId()).isPositive();
		assertThat(topicService.getTopicById(topic.getId())).isEqualTo(topic);
		assertThat(topic2).isNotNull();
		assertThat(topic2.getId()).isPositive();
		assertThat(topicService.getTopicById(topic2.getId())).isEqualTo(topic2);
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>(List.of(topic)));
		sessionRepository.save(session);
		assertThat(sessionRepository.findById(session.getId())).isNotNull();
		assertThat(session.getId()).isPositive();
		topicController.handleAddSessionToTopic(topic2.getId(), session.getId());
		assertThat(topicService.getTopicById(topic2.getId()).getSessionList()).contains(session);
		verify(viewCallback, never()).onTopicError(anyString());
	}
	
	@Test
	public void handleRemoveSessionFromTopicIt() {
		Topic topic = topicService.createTopic("chimica", "tavola periodica", 3, new ArrayList<>());
		assertThat(topic).isNotNull();
		assertThat(topic.getId()).isPositive();
		assertThat(topicService.getTopicById(topic.getId())).isEqualTo(topic);
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>(List.of(topic)));
		sessionRepository.save(session);
		assertThat(sessionRepository.findById(session.getId())).isNotNull();
		assertThat(session.getId()).isPositive();
		topicController.handleRemoveSessionFromTopic(topic.getId(), session.getId());
		assertThat(topicService.getTopicById(topic.getId()).getSessionList()).doesNotContain(session);
		assertThat(topicService.getTopicById(topic.getId()).getSessionList()).isEmpty();
		verify(viewCallback, never()).onTopicError(anyString());
	}
	
	@Test
	public void handleTotalTimeIt() {
		Topic topic = topicService.createTopic("chimica", "tavola periodica", 3, new ArrayList<>());
		assertThat(topic).isNotNull();
		assertThat(topic.getId()).isPositive();
		assertThat(topicService.getTopicById(topic.getId())).isEqualTo(topic);
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>(List.of(topic)));
		sessionRepository.save(session);
		assertThat(sessionRepository.findById(session.getId())).isNotNull();
		assertThat(session.getId()).isPositive();
		Integer time = topicController.handleTotalTime(topic.getId());
		assertThat(time).isNotZero().isEqualTo(60);
		verify(viewCallback).onTotalTimeCalculated(time);
	}
	
	@Test
	public void handlePercentageOfCompletion() {
		Topic topic = topicService.createTopic("chimica", "tavola periodica", 3, new ArrayList<>());
		assertThat(topic).isNotNull();
		assertThat(topic.getId()).isPositive();
		assertThat(topicService.getTopicById(topic.getId())).isEqualTo(topic);
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>(List.of(topic)));
		sessionRepository.save(session);
		assertThat(sessionRepository.findById(session.getId())).isNotNull();
		assertThat(session.getId()).isPositive();
		StudySession session2 = new StudySession(LocalDate.now().plusDays(2), 60, "un'altra nota", new ArrayList<>(List.of(topic)));
		session2.complete();
		sessionRepository.save(session2);
		assertThat(sessionRepository.findById(session.getId())).isNotNull();
		assertThat(session.getId()).isPositive();
		Integer percentage = topicController.handlePercentageOfCompletion(topic.getId());
		assertThat(percentage).isNotZero().isEqualTo(50);
		verify(viewCallback).onPercentageCalculated(percentage);
	}
	
}
