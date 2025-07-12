package com.sessionbuilder.core.it;

import java.time.LocalDate;
import java.util.ArrayList;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.sessionbuilder.core.backend.StudySession;
import com.sessionbuilder.core.backend.StudySessionRepository;
import com.sessionbuilder.core.backend.StudySessionRepositoryInterface;
import com.sessionbuilder.core.backend.Topic;
import com.sessionbuilder.core.backend.TopicRepository;
import com.sessionbuilder.core.backend.TopicRepositoryInterface;
import com.sessionbuilder.core.backend.TopicService;
import com.sessionbuilder.core.backend.TopicServiceInterface;
import com.sessionbuilder.core.backend.TransactionManager;
import com.sessionbuilder.core.backend.TransactionManagerImpl;

public class TopicServiceIT extends BaseBackendIntegrationTest {

	private TopicServiceInterface topicService;

	private String name;
	private String description;
	private int difficulty;

	@Override
	protected AbstractModule getTestSpecificModule() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				bind(StudySessionRepositoryInterface.class).to(StudySessionRepository.class).in(Singleton.class);
				bind(TopicRepositoryInterface.class).to(TopicRepository.class).in(Singleton.class);
				bind(TransactionManager.class).to(TransactionManagerImpl.class).in(Singleton.class);
				bind(TopicServiceInterface.class).to(TopicService.class).in(Singleton.class);
			}
		};
	}

	@Override
	protected void onSetup() {
		name = "Fumetti";
		description = "leggi topolino";
		difficulty = 1;

		topicService = injector.getInstance(TopicServiceInterface.class);
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