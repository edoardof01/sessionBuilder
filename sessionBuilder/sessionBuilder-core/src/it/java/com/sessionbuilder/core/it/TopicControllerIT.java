package com.sessionbuilder.core.it;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import com.google.inject.AbstractModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import com.google.inject.Singleton;
import com.sessionbuilder.core.backend.StudySession;
import com.sessionbuilder.core.backend.StudySessionRepository;
import com.sessionbuilder.core.backend.StudySessionRepositoryInterface;
import com.sessionbuilder.core.backend.Topic;
import com.sessionbuilder.core.backend.TopicController;
import com.sessionbuilder.core.backend.TopicRepository;
import com.sessionbuilder.core.backend.TopicRepositoryInterface;
import com.sessionbuilder.core.backend.TopicService;
import com.sessionbuilder.core.backend.TopicServiceInterface;
import com.sessionbuilder.core.backend.TopicViewCallback;
import com.sessionbuilder.core.backend.TransactionManager;
import com.sessionbuilder.core.backend.TransactionManagerImpl;

@RunWith(MockitoJUnitRunner.class)
public class TopicControllerIT extends BaseBackendIntegrationTest {
	private TopicController topicController;
	private StudySessionRepositoryInterface sessionRepository;
	@Mock
	private TopicViewCallback viewCallback;
	private TopicServiceInterface topicService;

	@Override
	protected AbstractModule getTestSpecificModule() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				bind(TopicRepositoryInterface.class).to(TopicRepository.class).in(Singleton.class);
				bind(StudySessionRepositoryInterface.class).to(StudySessionRepository.class).in(Singleton.class);
				bind(TransactionManager.class).to(TransactionManagerImpl.class).in(Singleton.class);
				bind(TopicServiceInterface.class).to(TopicService.class).in(Singleton.class);
				bind(TopicViewCallback.class).toInstance(viewCallback);
			}
		};
	}

	@Override
	protected void onSetup() {
		topicController = injector.getInstance(TopicController.class);
		sessionRepository = injector.getInstance(StudySessionRepositoryInterface.class);
		viewCallback = spy(TopicViewCallback.class);
		topicController.setViewCallback(viewCallback);
		topicService = injector.getInstance(TopicServiceInterface.class);
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

	@Test
	public void handleCreateTopicFailureIt() {
		List<StudySession> list = new ArrayList<>();
		assertThrows(IllegalArgumentException.class, () ->
			topicController.handleCreateTopic(null, "tavola periodica", 3, list));
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
		long topicId = topic.getId();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> topicService.getTopicById(topicId));
		assertThat(e.getMessage()).isEqualTo("non esiste un topic con tale id");
		verify(viewCallback).onTopicRemoved(topic);
		verify(viewCallback, never()).onTopicError(anyString());
	}

	@Test
	public void handleAddSessionToTopicIt() {
		Topic topic = new Topic("chimica", "tavola periodica", 3, new ArrayList<>());
		Topic topic2 = new Topic("biologia", "cetacei", 3, new ArrayList<>());
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(topic);
			repo.save(topic2);
			return null;
		});
		assertThat(topic).isNotNull();
		assertThat(topic.getId()).isPositive();
		assertThat(topicService.getTopicById(topic.getId())).isEqualTo(topic);
		assertThat(topic2).isNotNull();
		assertThat(topic2.getId()).isPositive();
		assertThat(topicService.getTopicById(topic2.getId())).isEqualTo(topic2);
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>(List.of(topic)));
		transactionManager.doInSessionTransaction(repo -> {
			repo.save(session);
			return null;
		});
		assertThat(session.getId()).isPositive();
		topicController.handleAddSessionToTopic(topic2.getId(), session.getId());
		assertThat(topicService.getTopicById(topic2.getId()).getSessionList()).contains(session);
		verify(viewCallback, never()).onTopicError(anyString());
	}

	@Test
	public void handleRemoveSessionFromTopicIt() {
		Topic topic = new Topic("chimica", "tavola periodica", 3, new ArrayList<>());
		Topic topic2 = new Topic("biologia", "cetacei", 3, new ArrayList<>());
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(topic);
			repo.save(topic2);
			return null;
		});
		assertThat(topic).isNotNull();
		assertThat(topic.getId()).isPositive();
		assertThat(topicService.getTopicById(topic.getId())).isEqualTo(topic);
		StudySession managedSession = transactionManager.doInTopicTransaction(topicRepo -> {
			Topic searchedTopic = topicRepo.findById(topic.getId());
			Topic searchedTopic2 = topicRepo.findById(topic2.getId());
			StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>(List.of(searchedTopic, searchedTopic2)));
			sessionRepository.save(session);
			topicRepo.update(searchedTopic);
			topicRepo.update(searchedTopic2);
			return session;
		});
		topicController.handleRemoveSessionFromTopic(topic.getId(), managedSession.getId());
		assertThat(topicService.getTopicById(topic.getId()).getSessionList()).doesNotContain(managedSession);
		assertThat(topicService.getTopicById(topic.getId()).getSessionList()).isEmpty();
		verify(viewCallback, never()).onTopicError(anyString());
	}

	@Test
	public void handleTotalTimeIt() {
		Topic topic = new Topic("chimica", "tavola periodica", 3, new ArrayList<>());
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(topic);
			return null;
		});
		assertThat(topic).isNotNull();
		assertThat(topic.getId()).isPositive();
		StudySession session = transactionManager.doInMultiRepositoryTransaction(context -> {
			Topic managedTopic = context.getTopicRepository().findById(topic.getId());
			StudySession newSession = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>(List.of(managedTopic)));
			context.getSessionRepository().save(newSession);
			context.getTopicRepository().update(managedTopic);
			return newSession;
		});
		assertThat(session.getId()).isPositive();
		Topic topicWithAssociatedSessions = topicService.getTopicById(topic.getId());
		assertThat(topicWithAssociatedSessions.getSessionList()).hasSize(1);
		assertThat(topicWithAssociatedSessions.getSessionList().get(0).getDuration()).isEqualTo(60);
		Integer time = topicController.handleTotalTime(topic.getId());
		assertThat(time).isNotZero().isEqualTo(60);
		verify(viewCallback).onTotalTimeCalculated(time);
	}

	@Test
	public void handlePercentageOfCompletion() {
		Topic topic = new Topic("chimica", "tavola periodica", 3, new ArrayList<>());
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(topic);
			return null;
		});
		assertThat(topic).isNotNull();
		assertThat(topic.getId()).isPositive();
		StudySession session1 = transactionManager.doInMultiRepositoryTransaction(context -> {
			Topic managedTopicForSession1 = context.getTopicRepository().findById(topic.getId());
			StudySession newSession = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>(List.of(managedTopicForSession1)));
			context.getSessionRepository().save(newSession);
			context.getTopicRepository().update(managedTopicForSession1);
			return newSession;
		});
		assertThat(session1.getId()).isPositive();
		StudySession session2 = transactionManager.doInMultiRepositoryTransaction(context -> {
			Topic managedTopicForSession2 = context.getTopicRepository().findById(topic.getId());
			StudySession newSession2 = new StudySession(LocalDate.now().plusDays(2), 60, "un'altra nota", new ArrayList<>(List.of(managedTopicForSession2)));
			newSession2.complete();
			context.getSessionRepository().save(newSession2);
			context.getTopicRepository().update(managedTopicForSession2);
			return newSession2;
		});
		assertThat(session2.getId()).isPositive();
		Topic topicWithAssociatedSessions = topicService.getTopicById(topic.getId());
		assertThat(topicWithAssociatedSessions.getSessionList()).hasSize(2);
		Integer percentage = topicController.handlePercentageOfCompletion(topic.getId());
		assertThat(percentage).isNotZero().isEqualTo(50);
		verify(viewCallback).onPercentageCalculated(percentage);
	}
}