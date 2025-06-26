package com.sessionBuilder.it;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import com.google.inject.*;
import com.google.inject.Module;
import com.sessionBuilder.core.Topic;
import com.sessionBuilder.core.TopicRepository;
import com.sessionBuilder.core.TopicRepositoryInterface;
import com.sessionBuilder.core.TransactionManager;
import com.sessionBuilder.core.TransactionManagerImpl;

import static org.assertj.core.api.Assertions.*;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import static org.junit.Assert.assertThrows;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TopicRepositoryIt {
	
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
				bind(TopicRepositoryInterface.class).to(TopicRepository.class);
				bind(TransactionManager.class).to(TransactionManagerImpl.class);
			}
		};
		Injector injector = Guice.createInjector(module);
		topicRepository = injector.getInstance(TopicRepositoryInterface.class);
	}
	
	@After
	public void tearDown() {
		if (emf != null && emf.isOpen()) {
			emf.close();
		}
	}
	
	@Test
	public void testFindByIdWithNonExistentIdIt() {
		long nonExistentId = 999999L;
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
			() -> topicRepository.findById(nonExistentId));
		assertThat(exception.getMessage()).isEqualTo("non esiste un topic con tale id");
	}
	
	@Test 
	public void testSaveAndFindTopicIt() {
		Topic topic = new Topic("Scultura", "Vasi di ceramica antica", 4, new ArrayList<>());
		topicRepository.save(topic);
		assertThat(topic.getId()).isPositive();
		Topic retrievedTopic = topicRepository.findById(topic.getId());
		assertThat(retrievedTopic).isNotNull();
		assertThat(retrievedTopic.getName()).isEqualTo("Scultura");
		assertThat(retrievedTopic.getDescription()).isEqualTo("Vasi di ceramica antica");
		assertThat(retrievedTopic.getDifficulty()).isEqualTo(4);
		assertThat(retrievedTopic.getSessionList()).isEmpty();
	}
	
	@Test
	public void testUpdateTopicIt() {
		Topic originalTopic = new Topic("Scultura", "Vasi di ceramica", 4, new ArrayList<>());
		topicRepository.save(originalTopic);
		long topicId = originalTopic.getId();
		Topic savedTopic = topicRepository.findById(topicId);
		assertThat(savedTopic.getName()).isEqualTo("Scultura");
		assertThat(savedTopic.getDescription()).isEqualTo("Vasi di ceramica");
		savedTopic.setName("Musei");
		savedTopic.setDescription("Collezioni museali moderne");
		topicRepository.update(savedTopic);
		Topic updatedTopic = topicRepository.findById(topicId);
		assertThat(updatedTopic.getName()).isEqualTo("Musei");
		assertThat(updatedTopic.getDescription()).isEqualTo("Collezioni museali moderne");
		assertThat(updatedTopic.getDifficulty()).isEqualTo(4);
		assertThat(updatedTopic.getId()).isEqualTo(topicId);
	}
	
	@Test
	public void testDeleteTopicIt() {
		Topic topic = new Topic("Arte Contemporanea", "Installazioni moderne", 3, new ArrayList<>());
		topicRepository.save(topic);
		long topicId = topic.getId();
		Topic savedTopic = topicRepository.findById(topicId);
		assertThat(savedTopic).isNotNull();
		assertThat(savedTopic.getName()).isEqualTo("Arte Contemporanea");
		topicRepository.delete(topicId);
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
			() -> topicRepository.findById(topicId));
		assertThat(exception.getMessage()).isEqualTo("non esiste un topic con tale id");
	}

	@Test
	public void testUpdateNonExistentTopicIt() {
		Topic nonPersistedTopic = new Topic("Fake Topic", "Non salvato", 1, new ArrayList<>());
		nonPersistedTopic.setId(999999L);
		try {
			topicRepository.update(nonPersistedTopic);
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
				() -> topicRepository.findById(999999L));
			assertThat(exception.getMessage()).isEqualTo("non esiste un topic con tale id");
		} catch (Exception e) {
			assertThat(e).isInstanceOf(RuntimeException.class);
		}
	}
	
	@Test
	public void testDeleteNonExistentTopicIt() {
		long nonExistentId = 888888L;
		try {
			topicRepository.delete(nonExistentId);
		} catch (Exception e) {
			assertThat(e).isInstanceOf(RuntimeException.class);
		}
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
			() -> topicRepository.findById(nonExistentId));
		assertThat(exception.getMessage()).isEqualTo("non esiste un topic con tale id");
	}

}