package com.sessionbuilder.core.it;

import org.junit.Test;
import com.google.inject.*;
import com.sessionbuilder.core.backend.StudySessionRepository;
import com.sessionbuilder.core.backend.StudySessionRepositoryInterface;
import com.sessionbuilder.core.backend.Topic;
import com.sessionbuilder.core.backend.TopicRepository;
import com.sessionbuilder.core.backend.TopicRepositoryInterface;
import com.sessionbuilder.core.backend.TransactionManager;
import com.sessionbuilder.core.backend.TransactionManagerImpl;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertThrows;
import java.util.ArrayList;

public class TopicRepositoryIT extends BaseBackendIntegrationTest {

	@Override
	protected AbstractModule getTestSpecificModule() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				bind(StudySessionRepositoryInterface.class).to(StudySessionRepository.class).in(Singleton.class);
				bind(TopicRepositoryInterface.class).to(TopicRepository.class).in(Singleton.class);
				bind(TransactionManager.class).to(TransactionManagerImpl.class).in(Singleton.class);
			}
		};
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