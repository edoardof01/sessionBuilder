package com.sessionbuilder.core.it;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.sessionbuilder.core.backend.StudySession;
import com.sessionbuilder.core.backend.StudySessionInterface;
import com.sessionbuilder.core.backend.StudySessionRepository;
import com.sessionbuilder.core.backend.StudySessionRepositoryInterface;
import com.sessionbuilder.core.backend.StudySessionService;
import com.sessionbuilder.core.backend.Topic;
import com.sessionbuilder.core.backend.TopicRepository;
import com.sessionbuilder.core.backend.TopicRepositoryInterface;
import com.sessionbuilder.core.backend.TopicService;
import com.sessionbuilder.core.backend.TopicServiceInterface;
import com.sessionbuilder.core.backend.TransactionManager;
import com.sessionbuilder.core.backend.TransactionManagerImpl;
import org.junit.Test;

public class StudySessionServiceIT extends BaseBackendIntegrationTest {
	private StudySessionInterface sessionService;
	private TopicServiceInterface topicService;

	@Override
	protected AbstractModule getTestSpecificModule() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				bind(StudySessionRepositoryInterface.class).to(StudySessionRepository.class).in(Singleton.class);
				bind(TopicRepositoryInterface.class).to(TopicRepository.class).in(Singleton.class);
				bind(TopicServiceInterface.class).to(TopicService.class).in(Singleton.class);
				bind(TransactionManager.class).to(TransactionManagerImpl.class).in(Singleton.class);
				bind(StudySessionInterface.class).to(StudySessionService.class).in(Singleton.class);
			}
		};
	}

	@Override
	protected void onSetup() {
		sessionService = injector.getInstance(StudySessionInterface.class);
		topicService = injector.getInstance(TopicServiceInterface.class);
	}

	@Test
	public void testCreateAndGetSessionIt() {
		Topic topic = new Topic("Math", "Algebra", 3, new ArrayList<>());
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(topic);
			return null;
		});
		StudySession createdSession = sessionService.createSession(LocalDate.now().plusDays(1), 60, "Test session", new ArrayList<>(List.of(topic.getId())));
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
		assertThat(exception.getMessage()).isEqualTo("non esiste una session con tale id");
	}

	@Test
	public void testCompleteSessionIt() {
		Topic topic = new Topic("Physics", "Mechanics", 4, new ArrayList<>());
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(topic);
			return null;
		});
		StudySession session = sessionService.createSession(LocalDate.now().plusDays(1), 90, "Physics session", new ArrayList<>(List.of(topic.getId())));
		long sessionId = session.getId();
		assertThat(session.isComplete()).isFalse();
		sessionService.completeSession(sessionId);
		StudySession completedSession = sessionService.getSessionById(sessionId);
		assertThat(completedSession.isComplete()).isTrue();
	}

	@Test
	public void testAddTopicToSessionIt() {
		Topic topic1 = new Topic("Topic Iniziale", "Desc", 1, new ArrayList<>());
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(topic1);
			return null;
		});
		Topic topic2 = new Topic("Chemistry", "Organic Chemistry", 5, new ArrayList<>());
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(topic2);
			return null;
		});
		long topic2Id = topic2.getId();
		StudySession session = sessionService.createSession(LocalDate.now().plusDays(1), 120, "Chemistry session", new ArrayList<>(List.of(topic1.getId())));
		long sessionId = session.getId();
		sessionService.addTopic(sessionId, topic2Id);
		StudySession updatedSession = sessionService.getSessionById(sessionId);
		assertThat(updatedSession.getTopicList()).hasSize(2);
		assertThat(updatedSession.getTopicList().stream().map(Topic::getName).anyMatch(name -> name.equals("Chemistry"))).isTrue();
	}

	@Test
	public void testRemoveTopicFromSessionIt() {
		Topic topic1 = topicService.createTopic("Topic Uno", "desc 1", 1, new ArrayList<>());
		Topic topic2 = topicService.createTopic("Topic Due", "desc 2", 2, new ArrayList<>());
		long topic1Id = topic1.getId();
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 90, "History session", new ArrayList<>());
		transactionManager.doInMultiRepositoryTransaction(context -> {
			Topic managedTopic1 = context.getTopicRepository().findById(topic1.getId());
			Topic managedTopic2 = context.getTopicRepository().findById(topic2.getId());
			session.addTopic(managedTopic1);
			session.addTopic(managedTopic2);
			context.getSessionRepository().save(session);
			return null;
		});
		long sessionId = session.getId();
		StudySession sessionWithTopics = sessionService.getSessionById(sessionId);
		assertThat(sessionWithTopics.getTopicList()).hasSize(2);
		sessionService.removeTopic(sessionId, topic1Id);
		StudySession sessionWithoutTopic = sessionService.getSessionById(sessionId);
		assertThat(sessionWithoutTopic.getTopicList()).hasSize(1);
		assertThat(sessionWithoutTopic.getTopicList().get(0).getName()).isEqualTo("Topic Due");
	}

	@Test
	public void testDeleteSessionIt() {
		Topic topic = new Topic("Literature", "Shakespeare", 4, new ArrayList<>());
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(topic);
			return null;
		});
		StudySession session = sessionService.createSession(LocalDate.now().plusDays(1), 75, "Literature session", new ArrayList<>(List.of(topic.getId())));
		long sessionId = session.getId();
		StudySession existingSession = sessionService.getSessionById(sessionId);
		assertThat(existingSession).isNotNull();
		sessionService.deleteSession(sessionId);
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
			() -> sessionService.getSessionById(sessionId));
		assertThat(exception.getMessage()).isEqualTo("non esiste una session con tale id");
	}
}