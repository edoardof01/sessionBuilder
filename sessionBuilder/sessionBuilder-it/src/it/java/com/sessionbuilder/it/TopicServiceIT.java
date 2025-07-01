package com.sessionbuilder.it;

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
import com.sessionbuilder.core.EmfFactory;
import com.sessionbuilder.core.StudySession;
import com.sessionbuilder.core.StudySessionRepository;
import com.sessionbuilder.core.StudySessionRepositoryInterface;
import com.sessionbuilder.core.Topic;
import com.sessionbuilder.core.TopicRepository;
import com.sessionbuilder.core.TopicRepositoryInterface;
import com.sessionbuilder.core.TopicService;
import com.sessionbuilder.core.TopicServiceInterface;
import com.sessionbuilder.core.TransactionManager;
import com.sessionbuilder.core.TransactionManagerImpl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

public class TopicServiceIT {
	
	private EntityManagerFactory emf;
	private TopicServiceInterface topicService;
	private TransactionManager transactionManager;
	
	private String name;
	private String description;
	private int difficulty;

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
		
		emf = EmfFactory.createEntityManagerFactory("sessionbuilder-test", properties);
		cleanDatabase();
		AbstractModule module = new AbstractModule() {
			@Override
			protected void configure() {
				bind(EntityManagerFactory.class).toInstance(emf);
				bind(StudySessionRepositoryInterface.class).to(StudySessionRepository.class);
				bind(TopicRepositoryInterface.class).to(TopicRepository.class);
				bind(TransactionManager.class).to(TransactionManagerImpl.class);
				bind(TopicServiceInterface.class).to(TopicService.class);
			}
		};
		Injector injector = Guice.createInjector(module);
		topicService = injector.getInstance(TopicServiceInterface.class);
		injector.getInstance(StudySessionRepositoryInterface.class);
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
		transactionManager.doInSessionTransaction(repo -> {
			repo.save(session);
			return null;
		});
		long sessionId = session.getId();
		
		topicService.addSessionToTopic(topicId, sessionId);
		
		Topic updatedTopic = topicService.getTopicById(topicId);
		assertThat(updatedTopic.getSessionList()).hasSize(1);
		assertThat(updatedTopic.getSessionList().get(0).getNote()).isEqualTo("una nota");
	}
	
	@Test
	public void testRemoveSessionFromTopicIt() {
		Topic topic1 = topicService.createTopic("Topic Uno", "desc 1", 1, new ArrayList<>());
		Topic topic2 = topicService.createTopic("Topic Due", "desc 2", 2, new ArrayList<>());
		long topic1Id = topic1.getId();
		long topic2Id = topic2.getId();
		
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "Sessione condivisa", new ArrayList<>());
		transactionManager.doInSessionTransaction(repo -> {
			repo.save(session);
			return null;
		});
		long sessionId = session.getId();
		
		topicService.addSessionToTopic(topic1Id, sessionId);
		topicService.addSessionToTopic(topic2Id, sessionId);
		
		topicService.removeSessionFromTopic(topic1Id, sessionId);
		
		Topic updatedTopic1 = topicService.getTopicById(topic1Id);
		Topic updatedTopic2 = topicService.getTopicById(topic2Id);
		
		assertThat(updatedTopic1.getSessionList()).isEmpty();
		assertThat(updatedTopic2.getSessionList()).hasSize(1);
		assertThat(updatedTopic2.getSessionList().get(0).getId()).isEqualTo(sessionId);
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
		assertThat(exception.getMessage()).isEqualTo("non esiste un topic con tale id");
	}
	
	@Test
	public void testCalculateTotalTimeIt() {
		Topic topic = topicService.createTopic(name, description, difficulty, new ArrayList<>());
		long topicId = topic.getId();
		
		StudySession session1 = new StudySession(LocalDate.now().plusDays(1), 60, "session 1", new ArrayList<>());
		StudySession session2 = new StudySession(LocalDate.now().plusDays(2), 30, "session 2", new ArrayList<>());
		
		transactionManager.doInSessionTransaction(repo -> {
			repo.save(session1);
			repo.save(session2);
			return null;
		});
		
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
		StudySession completedSession = new StudySession(LocalDate.now().plusDays(2), 60, "un'altra nota", new ArrayList<>());
		
		transactionManager.doInSessionTransaction(repo -> {
			repo.save(uncompletedSession);
			repo.save(completedSession);
			return null;
		});
		
		topicService.addSessionToTopic(topicId, uncompletedSession.getId());
		topicService.addSessionToTopic(topicId, completedSession.getId());
		
		transactionManager.doInSessionTransaction(repo -> {
			StudySession sessionToUpdate = repo.findById(completedSession.getId());
			sessionToUpdate.setIsComplete(true);
			repo.update(sessionToUpdate);
			return null;
		});
		
		Integer percentage = topicService.calculatePercentageOfCompletion(topicId);
		assertThat(percentage).isEqualTo(50);
	}
}