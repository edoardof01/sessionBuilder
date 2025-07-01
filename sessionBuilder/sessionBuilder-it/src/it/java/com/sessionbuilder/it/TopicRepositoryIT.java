package com.sessionbuilder.it;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import com.google.inject.*;
import com.sessionbuilder.core.EmfFactory;
import com.sessionbuilder.core.StudySessionRepository;
import com.sessionbuilder.core.StudySessionRepositoryInterface;
import com.sessionbuilder.core.Topic;
import com.sessionbuilder.core.TopicRepository;
import com.sessionbuilder.core.TopicRepositoryInterface;
import com.sessionbuilder.core.TransactionManager;
import com.sessionbuilder.core.TransactionManagerImpl;

import static org.assertj.core.api.Assertions.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import static org.junit.Assert.assertThrows;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TopicRepositoryIT {
	
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
	public void testFindByIdWithNonExistentIdIt() {
		long nonExistentId = 999999L;
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
			() -> transactionManager.doInTopicTransaction(repo -> repo.findById(nonExistentId)));
		assertThat(exception.getMessage()).isEqualTo("non esiste un topic con tale id");
	}
	
	@Test 
	public void testSaveAndFindTopicIt() {
		Topic topic = new Topic("Scultura", "Vasi di ceramica antica", 4, new ArrayList<>());
		
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(topic);
			return null;
		});
		
		assertThat(topic.getId()).isPositive();
		
		Topic retrievedTopic = transactionManager.doInTopicTransaction(repo -> repo.findById(topic.getId()));
		
		assertThat(retrievedTopic).isNotNull();
		assertThat(retrievedTopic.getName()).isEqualTo("Scultura");
		assertThat(retrievedTopic.getDescription()).isEqualTo("Vasi di ceramica antica");
		assertThat(retrievedTopic.getDifficulty()).isEqualTo(4);
		assertThat(retrievedTopic.getSessionList()).isEmpty();
	}
	
	@Test
	public void testUpdateTopicIt() {
		Topic originalTopic = new Topic("Scultura", "Vasi di ceramica", 4, new ArrayList<>());
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(originalTopic);
			return null;
		});
		
		long topicId = originalTopic.getId();
		
		Topic savedTopic = transactionManager.doInTopicTransaction(repo -> repo.findById(topicId));
		
		assertThat(savedTopic.getName()).isEqualTo("Scultura");
		assertThat(savedTopic.getDescription()).isEqualTo("Vasi di ceramica");
		
		savedTopic.setName("Musei");
		savedTopic.setDescription("Collezioni museali moderne");
		
		transactionManager.doInTopicTransaction(repo -> {
			repo.update(savedTopic);
			return null;
		});
		
		Topic updatedTopic = transactionManager.doInTopicTransaction(repo -> repo.findById(topicId));
		
		assertThat(updatedTopic.getName()).isEqualTo("Musei");
		assertThat(updatedTopic.getDescription()).isEqualTo("Collezioni museali moderne");
		assertThat(updatedTopic.getDifficulty()).isEqualTo(4);
		assertThat(updatedTopic.getId()).isEqualTo(topicId);
	}
	
	@Test
	public void testDeleteTopicIt() {
		Topic topic = new Topic("Arte Contemporanea", "Installazioni moderne", 3, new ArrayList<>());
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(topic);
			return null;
		});
		
		long topicId = topic.getId();
		
		Topic savedTopic = transactionManager.doInTopicTransaction(repo -> repo.findById(topicId));
		assertThat(savedTopic).isNotNull();
		assertThat(savedTopic.getName()).isEqualTo("Arte Contemporanea");
		
		transactionManager.doInTopicTransaction(repo -> {
			repo.delete(topicId);
			return null;
		});
		
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
			() -> transactionManager.doInTopicTransaction(repo -> repo.findById(topicId)));
		assertThat(exception.getMessage()).isEqualTo("non esiste un topic con tale id");
	}

	@Test
	public void testUpdateNonExistentTopicIt() {
		Topic nonPersistedTopic = new Topic("Fake Topic", "Non salvato", 1, new ArrayList<>());
		nonPersistedTopic.setId(999999L);
		
		transactionManager.doInTopicTransaction(repo -> {
			repo.update(nonPersistedTopic);
			return null;
		});
		
		Topic result = transactionManager.doInTopicTransaction(repo -> repo.findByNameDescriptionAndDifficulty("Fake Topic", "Non salvato", 1));
		assertThat(result).isNotNull();
		assertThat(result.getId()).isNotEqualTo(999999L);
	}
	
	@Test
	public void testDeleteNonExistentTopicIt() {
		long nonExistentId = 888888L;
		
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
			() -> transactionManager.doInTopicTransaction(repo -> {
				repo.delete(nonExistentId);
				return null;
				}));
		assertThat(exception.getMessage()).isEqualTo("il topic da rimuovere è null");
	}

}